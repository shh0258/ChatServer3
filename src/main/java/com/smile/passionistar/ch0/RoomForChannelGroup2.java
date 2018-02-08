package com.smile.passionistar.ch0;

import java.util.HashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.concurrent.GlobalEventExecutor;

public class RoomForChannelGroup2 {// 해시형태 룸관리 
	RedisCluster redisCluster = new RedisCluster();

    public static HashMap<ChannelId, Room> roomMap = new HashMap<ChannelId, Room>();
    
	Channel ch;
	FullHttpRequest req; // 쿼리스트링 얻기 위해 받아옴 
	
	public RoomForChannelGroup2(Channel ch, FullHttpRequest req) {
		this.ch = ch;
		this.req =req;
	}
	
	
	public Channel AddChannelGroup() {
		Room rtemp = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), "false");
		
		if(roomMap.isEmpty()) {
			Room room = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), req.getUri()); // 쿼리스트링 값에 해당하는 룸객체를 생성 
			room.cg.add(ch); //룸에 새로운 채널그룹을 생
			roomMap.put(ch.id(), room);
			rtemp = room;
			redisCluster.redisClusterLancher(req.getUri(), room.cg);// 레디스 채널에 등록 
			return room.cg.find(ch.id());
		}
		
		if(roomMap.containsKey(ch.id())) {
			roomMap.get(ch.id()).cg.add(ch);
			rtemp=roomMap.get(ch.id());
		}
		
		if(!roomMap.containsKey(ch.id())) { // 만약 room 객체에 아무런 qs 일치값이 없을 경우 
			Room room = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), req.getUri()); // 쿼리스트링 값에 해당하는 룸객체를 생성 
			room.cg.add(ch); //룸에 새로운 채널그룹을 생
			roomMap.put(ch.id(), room);
			rtemp = room;// 룸객체가 실제로 생성되었다면, 리턴하기 위햇 사용해
			redisCluster.redisClusterLancher(req.getUri(), room.cg);//redis cluster에 새로운 채널그룹을 넣고 이를 등록해서 다음번에 문자를 보낼 시에 사용할 수 있게 셋팅함
		}
		return rtemp.cg.find(ch.id());
	}
	
	public ChannelGroup findByChannelId(Channel c) {
		if(roomMap.isEmpty()) {
			return null;
		}
		
		if(roomMap.containsKey(c.id())) {
			return roomMap.get(ch.id()).cg;
		}
		
		return null;
	}
	
	public String findByChannelIdReturnQs(Channel c) {
		if(roomMap.isEmpty()) {
			return null;
		}
		
		if(roomMap.containsKey(c.id())) {
			return roomMap.get(ch.id()).qs;
		}
		
		return null;
	}
	
	

}
