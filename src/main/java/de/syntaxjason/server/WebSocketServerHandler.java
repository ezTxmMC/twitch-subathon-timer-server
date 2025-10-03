package de.syntaxjason.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.syntaxjason.server.handler.MessageHandler;
import de.syntaxjason.server.model.ServerMessage;
import de.syntaxjason.server.session.SessionManager;
import de.syntaxjason.server.util.LocalDateTimeAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;
    private final Gson gson;
    private static final AttributeKey<String> URI_KEY = AttributeKey.valueOf("URI");
    private String sessionId;
    private String channelName;

    public WebSocketServerHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.messageHandler = new MessageHandler(sessionManager);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("Client verbunden: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (sessionId != null && channelName != null) {
            sessionManager.leaveSession(sessionId, channelName);
        }

        System.out.println("Client getrennt: " + ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String uri = ctx.channel().attr(URI_KEY).get();

        if (uri != null) {
            extractSessionInfo(uri);
            System.out.println("Handler added - Session ID: " + sessionId + ", Channel: " + channelName);
        }

        super.handlerAdded(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
