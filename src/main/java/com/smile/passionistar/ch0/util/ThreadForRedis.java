package com.smile.passionistar.ch0.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadForRedis {
	public static void threadRunForRedis() {
		Runnable runnable = new Runnable() {
        	
			@Override
			public void run() {
        			RedisForLB rflc = new RedisForLB(RoomForChannelGroup.userCount);
        			rflc.sendCount();//redis 서버에 lb를 위한 chatserver의 접속자수를 업데이트 , 이 스케줄링은 서버에 대한 헬스체크가 올때만 실행된다. 헬스체크는 http 형식으로 1초마다 보내기로 chat manage server와 약속되어있다.
			}
        };
        ScheduledExecutorService service = Executors
        		.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 10, 5, TimeUnit.SECONDS); //10초 뒤에 5초 간격으로 실행됨 
        //타이머메서드
        
        Runnable runnable2 = new Runnable() {
        	
			@Override
			public void run() {
//				  RoomForChannelGroup.gabageCollectForRoomMap(); //이는 room 객체에 채팅내용을 저장할 경우가생길 때, 잠시동안은 채팅방을 유지하기 위해서 필요한 내용이다.
				for(String s : RoomForChannelGroup.channelQs.values()) {
        			RedisForPopularChat rfpc = new RedisForPopularChat(RoomForChannelGroup.roomMap.get(s).count, RoomForChannelGroup.roomMap.get(s).qs);
        			rfpc.sendCountForPopular();//redis 서버에 lb를 위한 chatserver의 접속자수를 업데이트 , 이 스케줄링은 서버에 대한 헬스체크가 올때만 실행된다. 헬스체크는 http 형식으로 1초마다 보내기로 chat manage server와 약속되어있다.
				}
			}
        };
        ScheduledExecutorService service2 = Executors
        		.newSingleThreadScheduledExecutor();
        service2.scheduleAtFixedRate(runnable2, 10, 3600, TimeUnit.SECONDS); //10초 뒤에 5초 간격으로 실행됨 
        
	}
}
