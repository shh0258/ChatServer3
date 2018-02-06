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
package com.smile.passionistar.ch0;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

/**
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;// ssl 객체 생성 

    public WebSocketServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception { // 클라이언트 채널에 파이프라인 설정을 하기위에 자동으로 호출되는 메서드 
        ChannelPipeline pipeline = ch.pipeline(); // 각 채널에 파이프라인 할당 
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));// ByteBufferAlocater 이용해서 버퍼생성하고 sslCtx를 파이프라인에 핸들러로 등록해서 사용 
        }
        pipeline.addLast(new HttpServerCodec()); // httprequest, httpcontent로 인코딩하거나, httpresponse 를 byte 형으로 디코딩해서 다음 핸들러로 전
        pipeline.addLast(new HttpObjectAggregator(1024*64)); // 하나의 fullhttp객체로 만들어주고, 사용할 수 있게 한다. 만약 이걸 사용하지 않는다면 웹소켓서버핸들러에서 if 문으로 처리해서 사용한다., 64kb정도의 데이터를 최대 받을 수 있다
        pipeline.addLast(new WebSocketServerHandler()); //웹소켓 서버 핸들러에서 주요 로직 처리 
    }
}
