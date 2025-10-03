package de.syntaxjason.server;

import de.syntaxjason.server.session.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SubathonServer {
    private static final int PORT = 8080;
    private final SessionManager sessionManager;

    public SubathonServer() {
        this.sessionManager = new SessionManager();
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketServerInitializer(sessionManager))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel channel = bootstrap.bind(PORT).sync().channel();

            System.out.println("╔════════════════════════════════════════════════╗");
            System.out.println("║   Subathon Timer Server gestartet             ║");
            System.out.println("║   WebSocket: ws://localhost:" + PORT + "           ║");
            System.out.println("║   OBS Overlay: http://localhost:" + PORT + "/overlay ║");
            System.out.println("╚════════════════════════════════════════════════╝");

            channel.closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        SubathonServer server = new SubathonServer();
        server.start();
    }
}
