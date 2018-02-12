/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.smile.passionistar.ch0.spring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.smile.passionistar.ch0.WebSocketServerInitializer;
import com.smile.passionistar.ch0.util.RedisForLB;
import com.smile.passionistar.ch0.util.RoomForChannelGroup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

@Component
public final class WebSocketServerV2 {

    static final boolean SSL = System.getProperty("ssl") != null; //ssl 정보 입력 
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080")); //포트번호 지정  ssl 아니면 8443
    
    public void start() throws Exception {
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
//             .handler(new LoggingHandler(LogLevel.INFO)) // 서버 소켓 측에 로그핸들러 등록, 이는 클라이언트 소켓채널에 등록하지 않았음으로, log핸들러가 io바운드 핸들러를 상속했음에도 서버측 로그만 나옴 
             .childHandler(new WebSocketServerInitializer(sslCtx)); //클라이언트 소켓 채널 측에 ssl 에 관련된 핸들러를 등록한다.

            Channel ch = b.bind(PORT).sync().channel();

            System.out.println("Boot bt Spring, Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');// 접속위치 콘솔에 알리기 테스트용    

            Runnable runnable = new Runnable() {
            	
				@Override
				public void run() {
//					RoomForChannelGroup.gabageCollectForRoomMap(); //이는 room 객체에 채팅내용을 저장할 경우가생길 때, 잠시동안은 채팅방을 유지하기 위해서 필요한 내용이다.
	        		RedisForLB rflc = new RedisForLB(RoomForChannelGroup.userCount);
	    		    rflc.sendCount();//redis 서버에 lb를 위한 chatserver의 접속자수를 업데이트 , 이 스케줄링은 서버에 대한 헬스체크가 올때만 실행된다. 헬스체크는 http 형식으로 1초마다 보내기로 chat manage server와 약속되어있다.
				}
            };
            ScheduledExecutorService service = Executors
            		.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(runnable, 10, 5, TimeUnit.SECONDS); //10초 뒤에 5초 간격으로 실행됨 
            //타이머메서드
            
            ch.closeFuture().sync();
            
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
