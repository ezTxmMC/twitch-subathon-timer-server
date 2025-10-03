package de.syntaxjason.server.handler;

import de.syntaxjason.server.model.SessionState;
import de.syntaxjason.server.session.SessionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.io.InputStream;

public class OBSOverlayHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final SessionManager sessionManager;
    private static final AttributeKey<String> URI_KEY = AttributeKey.valueOf("URI");

    public OBSOverlayHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();

        ctx.channel().attr(URI_KEY).set(uri);

        if (isWebSocketUpgrade(request)) {

            ctx.fireChannelRead(request.retain());
            return;
        }

        if (uri.startsWith("/overlay/")) {
            handleOverlayRequest(ctx, request, uri);
            return;
        }

        sendNotFound(ctx);
    }

    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        return headers.contains(HttpHeaderNames.UPGRADE) &&
                headers.get(HttpHeaderNames.UPGRADE).equalsIgnoreCase("websocket");
    }

    private void handleOverlayRequest(ChannelHandlerContext ctx, FullHttpRequest request, String uri) {
        String sessionId = extractSessionId(uri);

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

    private String extractSessionId(String uri) {
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
                        body {
                            font-family: 'Segoe UI', Arial, sans-serif;
                            background: transparent;
                            overflow: hidden;
                        }
                        .timer-container {
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            height: 200px;
                            background: linear-gradient(135deg, rgba(100, 65, 165, 0.9), rgba(80, 50, 140, 0.9));
                            border-radius: 20px;
                            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
                        }
                        .timer {
                            font-size: 120px;
                            font-weight: bold;
                            color: white;
                            text-shadow: 0 5px 20px rgba(0, 0, 0, 0.5);
                            letter-spacing: 10px;
                        }
                        .paused {
                            animation: pulse 1.5s ease-in-out infinite;
                        }
                        @keyframes pulse {
                            0%, 100% { opacity: 1; }
                            50% { opacity: 0.5; }
                        }
                    </style>
                </head>
                <body>
                    <div class="timer-container">
                        <div class="timer" id="timer">00:00:00</div>
                    </div>
                    <script>
                        const sessionId = '""" + sessionId + """
                        ';
                        const ws = new WebSocket('ws://localhost:8080/session/' + sessionId + '?channel=obs_overlay');
                        const timerElement = document.getElementById('timer');

                        ws.onmessage = function(event) {
                            const message = JSON.parse(event.data);

                            if (message.type === 'TIMER_UPDATE') {
                                const seconds = message.data.remainingSeconds;
                                const hours = Math.floor(seconds / 3600);
                                const minutes = Math.floor((seconds % 3600) / 60);
                                const secs = seconds % 60;

                                timerElement.textContent = 
                                    String(hours).padStart(2, '0') + ':' +
                                    String(minutes).padStart(2, '0') + ':' +
                                    String(secs).padStart(2, '0');

                                if (message.data.isPaused) {
                                    timerElement.classList.add('paused');
                                } else {
                                    timerElement.classList.remove('paused');
                                }
                            }

                            if (message.type === 'FULL_SYNC') {
                                const minutes = message.data.remainingMinutes;
                                const totalSeconds = minutes * 60;
                                const hours = Math.floor(totalSeconds / 3600);
                                const mins = Math.floor((totalSeconds % 3600) / 60);
                                const secs = totalSeconds % 60;

                                timerElement.textContent = 
                                    String(hours).padStart(2, '0') + ':' +
                                    String(mins).padStart(2, '0') + ':' +
                                    String(secs).padStart(2, '0');
                            }
                        };

                        ws.onerror = function(error) {
                            console.error('WebSocket error:', error);
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

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}