package de.syntaxjason.server.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.syntaxjason.server.model.ServerMessage;
import de.syntaxjason.server.model.SessionState;
import de.syntaxjason.server.session.SessionManager;
import de.syntaxjason.server.util.LocalDateTimeAdapter;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;
    private final Gson gson;
    private WebSocketServerHandshaker handshaker;
    private String sessionId;
    private String channelName;
    private boolean websocketEstablished = false;

    public HttpRequestHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.messageHandler = new MessageHandler(sessionManager);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();

        if (uri.startsWith("/session/") && isWebSocketUpgrade(request)) {
            handleWebSocketHandshake(ctx, request);
            return;
        }

        if (uri.startsWith("/overlay/")) {
            handleOverlayRequest(ctx, request);
            return;
        }

        sendNotFound(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            return;
        }

        super.channelRead(ctx, msg);
    }

    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        return headers.contains(HttpHeaderNames.UPGRADE) &&
                headers.get(HttpHeaderNames.UPGRADE).equalsIgnoreCase("websocket");
    }

    private void handleWebSocketHandshake(ChannelHandlerContext ctx, FullHttpRequest request) {
        extractSessionInfo(request.uri());

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(request), null, true, 65536);

        handshaker = wsFactory.newHandshaker(request);

        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        }

        handshaker.handshake(ctx.channel(), request).addListener(future -> {
            if (future.isSuccess()) {
                websocketEstablished = true;
                System.out.println("WebSocket Handshake erfolgreich - Session: " + sessionId + ", Channel: " + channelName);
            }
        });
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        if (frame instanceof CloseWebSocketFrame) {
            System.out.println("Close WebSocket Frame empfangen von: " + channelName);
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());

            if (sessionId != null && channelName != null) {
                sessionManager.leaveSession(sessionId, channelName);
            }

            websocketEstablished = false;
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof TextWebSocketFrame) {
            handleTextFrame(ctx, (TextWebSocketFrame) frame);
            return;
        }

        System.err.println("Unsupported frame type: " + frame.getClass().getName());
    }

    private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();

        try {
            ServerMessage message = gson.fromJson(text, ServerMessage.class);

            if (channelName == null) {
                channelName = message.getChannelName();
            }

            messageHandler.handleMessage(sessionId, channelName, message, ctx.channel());

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void extractSessionInfo(String uri) {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        Map<String, List<String>> params = decoder.parameters();

        String path = decoder.path();
        String[] parts = path.split("/");

        if (parts.length >= 3) {
            this.sessionId = parts[2];
        }

        List<String> channelParams = params.get("channel");
        if (channelParams != null && !channelParams.isEmpty()) {
            this.channelName = channelParams.get(0);
        }
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }

    private void handleOverlayRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        String sessionId = extractSessionIdFromUri(uri);

        if (sessionId == null) {
            sendNotFound(ctx);
            return;
        }

        SessionState session = sessionManager.getSession(sessionId);

        if (session == null) {
            sendNotFound(ctx);
            return;
        }

        String html = loadOverlayHTML(sessionId);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(html, CharsetUtil.UTF_8)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private String extractSessionIdFromUri(String uri) {
        String[] parts = uri.split("/");

        if (parts.length >= 3) {
            String sessionPart = parts[2];
            int queryIndex = sessionPart.indexOf('?');
            if (queryIndex > 0) {
                return sessionPart.substring(0, queryIndex);
            }
            return sessionPart;
        }

        return null;
    }

    private String loadOverlayHTML(String sessionId) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("overlay/index.html");

            if (is == null) {
                return getDefaultHTML(sessionId);
            }

            String html = new String(is.readAllBytes(), CharsetUtil.UTF_8);
            return html.replace("{{SESSION_ID}}", sessionId);

        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultHTML(sessionId);
        }
    }

    private String getDefaultHTML(String sessionId) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Sabathon Timer Overlay</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { font-family: 'Segoe UI', Arial, sans-serif; background: transparent; overflow: hidden; }
                        .timer-container { display: flex; align-items: center; justify-content: center; height: 200px;
                            background: linear-gradient(135deg, rgba(100, 65, 165, 0.9), rgba(80, 50, 140, 0.9));
                            border-radius: 20px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5); }
                        .timer { font-size: 120px; font-weight: bold; color: white;
                            text-shadow: 0 5px 20px rgba(0, 0, 0, 0.5); letter-spacing: 10px; }
                        .paused { animation: pulse 1.5s ease-in-out infinite; }
                        @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
                    </style>
                </head>
                <body>
                    <div class="timer-container"><div class="timer" id="timer">00:00:00</div></div>
                    <script>
                        const ws = new WebSocket('ws://localhost:8080/session/""" + sessionId + """
                        ?channel=obs_overlay');
                        const timerElement = document.getElementById('timer');
                        ws.onmessage = function(event) {
                            const message = JSON.parse(event.data);
                            if (message.type === 'TIMER_UPDATE') {
                                const seconds = message.data.remainingSeconds;
                                const hours = Math.floor(seconds / 3600);
                                const minutes = Math.floor((seconds % 3600) / 60);
                                const secs = seconds % 60;
                                timerElement.textContent = String(hours).padStart(2, '0') + ':' +
                                    String(minutes).padStart(2, '0') + ':' + String(secs).padStart(2, '0');
                                if (message.data.isPaused) timerElement.classList.add('paused');
                                else timerElement.classList.remove('paused');
                            }
                            if (message.type === 'FULL_SYNC') {
                                const totalSeconds = message.data.remainingMinutes * 60;
                                const hours = Math.floor(totalSeconds / 3600);
                                const mins = Math.floor((totalSeconds % 3600) / 60);
                                const secs = totalSeconds % 60;
                                timerElement.textContent = String(hours).padStart(2, '0') + ':' +
                                    String(mins).padStart(2, '0') + ':' + String(secs).padStart(2, '0');
                            }
                        };
                        ws.onerror = function(error) {
                            console.error('WebSocket error:', error);
                        };
                        ws.onclose = function() {
                            console.log('WebSocket closed - reconnecting...');
                            setTimeout(function() { location.reload(); }, 5000);
                        };
                    </script>
                </body>
                </html>
                """;
    }

    private void sendNotFound(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer("404 Not Found", CharsetUtil.UTF_8)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if (websocketEstablished && sessionId != null && channelName != null) {
            System.out.println("Client wirklich disconnected: " + channelName);
            sessionManager.leaveSession(sessionId, channelName);
            websocketEstablished = false;
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}