package com.smile.passionistar.ch0;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import com.smile.passionistar.ch0.util.RandomNickname;
import com.smile.passionistar.ch0.util.RedisCluster;
import com.smile.passionistar.ch0.util.RoomForChannelGroup;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";
    static final AttributeKey<String> nickAttr = AttributeKey.newInstance("nickname");
    private WebSocketServerHandshaker handshaker;
    RoomForChannelGroup roomForChannelGroup;
    RedisCluster redisCluster;
    
    private void hello(Channel ch, FullHttpRequest req){
    		if(nickName(ch)!= null) return;
    		String nick = new RandomNickname().MakeNickName();
    		bindNickname(ch, nick);
    }

    private String nickName(Channel ch) {
    	
		return ch.attr(nickAttr).get();
	}
    private void bindNickname(Channel ch, String nickname) {
        // 채널에는 AttributeKey라는 것을 설정할 수 잇는데 각 채널마다 유니크한 값을 가질 수 있다. 채널마다 지역변수를 하나씩 들고 있다고 생각하면 좋음
        // set(nickname)은 AttributeKey를 설정한 키의 값(벨류)를 지정한다.
        // nickname은 개별 사용자의 닉네임
        ch.attr(nickAttr).set(nickname);
    }

	@Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {// **1 최초실행 $$1
        if (msg instanceof FullHttpRequest) {
        		hello(ctx.channel(), (FullHttpRequest) msg); //ctx.channel 과 roomBalancing.addchannelgroup()에서나온 채널은 같은 값으로 확인   
        		handleHttpRequest(ctx, (FullHttpRequest) msg); //http 요청일 때,**2 
        } else if (msg instanceof WebSocketFrame) {//$$2
            handleWebSocketFrame(ctx, (WebSocketFrame) msg); // websocketframe 요청일 때  
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (!req.getDecoderResult().isSuccess()) { // httprequest 를 디코딩한 결과가 없을때 
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if (req.getMethod() != GET) { // get 형식이 아닐 때 
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }
        
        if ("/favicon.ico".equals(req.getUri())) { // 
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }
      	
        try {
			if (Pattern.matches("^/[\\w|가-힣]+", URLDecoder.decode(req.getUri(), "UTF-8"))) { // 이 요청일 때, http 뷰페이지 리턴해주기, 서버와 연결 시 필요없는 부분  **3
			    ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
			    FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
			    res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8"); // res 의 헤더값 설정 
			    HttpHeaders.setContentLength(res, content.readableBytes()); // content의 길이 설정 
			    sendHttpResponse(ctx, req, res);//http 요청시에 최초의 뷰 정보를 담아서 ws 웹소켓등급업까지 해주는 http페이지와 정보를 담은 객체를 메서드로 전달 
			    return;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        try {
			if(Pattern.matches("^/[\\w|가-힣]+/websocket", URLDecoder.decode(req.getUri(), "UTF-8"))) {
			// Handshake
					WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
							getWebSocketLocation(req), null, true); // 웹소켓 팩토리에 req를 저장함 
					handshaker = wsFactory.newHandshaker(req); // 팩토리에 새로운 핸드쉐이크를 지정해서 객체에 저장함 
					roomForChannelGroup = new RoomForChannelGroup(ctx.channel(), req); // 룸 채널 그룹 객체에 존재하는roomValues 객체에 room 을 생성, 찾아간 후 채널 그룹에 채널을 등록 
					Channel c = roomForChannelGroup.AddChannelGroup(); // 채널그룹에 등록한 후 리턴되는 채널의 값을 통해 핸드쉐이크를 하고 이 채널에는 핸드쉐이크가 정의된다. 그 상태로 룸객체에 들어가서 태그단위의 관리를 받는다 
					if (handshaker == null) {
						WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(c);
					} else {
						handshaker.handshake(c, req);// 값이 있다면 채널에 핸드쉐이크를 추가한다. 식별할 req와 함께 
					}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {//$$2
    	
        // 클로즈 프레임형태의 이벤트일때 캐치 
        if (frame instanceof CloseWebSocketFrame) { // 소켓을 종료했을 때 날아오는 프레임을 체크 
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            System.out.println("CLosesocket");// 이 단에서 lb를 위한 redis 데이터 업데이트가 이루어 져야함 
            if(--roomForChannelGroup.findByChannelIdReturnRoom(ctx.channel()).count == 0) {// room의 갯수를 주기적으로 줄이기 위해 카운트를 세어줌, 만약 카운트가 0이라면 hash에서 바로 지워버림, 이는 내가 만든 가비지 컬렉터가 없을 떄 반드시 필요함 
        			roomForChannelGroup.deleteByChannelId(ctx.channel());
            }
            RoomForChannelGroup.userCount--;
            return;
        }
        if (frame instanceof PingWebSocketFrame) { // ping 형식이 왔을 때 pong 쏘기 예제 
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            System.out.println("pingwebsocket");
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {//text웹소켓이 날아오지 않을 때 알림 
        		System.out.println("textwebsocket");
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        String request = ((TextWebSocketFrame) frame).text();//콘솔에 전달된 값을 띄워줌 PooledUnsafeDirectByteBuf사용함 
//        JsonParser jsonps = new JsonParser(); // json 데이터형을 사용할 일이 있을 때 사용할 계획 
//        request =jsonps.jsonParsingForText(request);
//        roomForChannelGroup.findByChannelId(ctx.channel()).writeAndFlush(new TextWebSocketFrame(ctx.channel().attr(nickAttr).get()+": "+request));
//        redisCluster.redisClusterLancher(roomForChannelGroup.findByChannelIdReturnQs(ctx.channel())).convertAndSend("c."+roomForChannelGroup.findByChannelIdReturnQs(ctx.channel()), "testset123123");
        RedisCluster redisCluster =new RedisCluster();
        redisCluster.redisClusterLancher(roomForChannelGroup.findByChannelId(ctx.channel()))
        .convertAndSend("c."+roomForChannelGroup.findByChannelIdReturnQs(ctx.channel()), ctx.channel().attr(nickAttr).get()+": "+request);
    }

    private static void sendHttpResponse(//**4
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // 200이 아니면 에러페이지 제너레이트함
        if (res.getStatus().code() != 200) { //200이 아니라면 에러를 위한 새로운 버퍼와 내용 전달 
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);//풀에 존재하지 않는 버퍼에 상태내용과 설정을 담고 
            res.content().writeBytes(buf);//그 내용을 httpcontent 에 담는다.
            buf.release();// buf 객체의 count 를 하나 늘리
            HttpHeaders.setContentLength(res, res.content().readableBytes()); //res의content내용의 길이를 위에 넣어준 대로 헤더에 표시설정 
        }

        // 응답하고 필요하면 연결을 닫음 
        ChannelFuture f = ctx.channel().writeAndFlush(res); // 200이면 받은 res를 채널에 wf함 
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE); // req가 없거나, 200이 아닐 때 채널을 닫는다.
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) { // chat manage 서버와 의견조율 시에 없어질 수 있는 메서드
        String location =  req.headers().get(HOST)+ req.getUri() + WEBSOCKET_PATH;
        if (WebSocketServer.SSL) { //ssl인증키방식 일때 
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }
}
