package com.smile.passionistar.ch0;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public final class WebSocketServer { // 스프링으로 부팅할 땐 쓰지 않는다.

    static final boolean SSL = System.getProperty("ssl") != null; //ssl 정보 입력 
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080")); //포트번호 지정  ssl 아니면 8443

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)//리눅스에 배포시 epoll로 설정가능, 추상화 수준과 빌더패턴 때문에 인자만 바꾸어서 사용 가능 
             .handler(new LoggingHandler(LogLevel.INFO)) // 서버 소켓 측에 로그핸들러 등록, 이는 클라이언트 소켓채널에 등록하지 않았음으로, log핸들러가 io바운드 핸들러를 상속했음에도 서버측 로그만 나옴 
             .childHandler(new WebSocketServerInitializer(sslCtx)); //클라이언트 소켓 채널 측에 ssl 에 관련된 핸들러를 등록한다.

            Channel ch = b.bind(PORT).sync().channel();
            System.out.println("Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');// 접속위치 콘솔에 알리기 테스트용
            

            ch.closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
