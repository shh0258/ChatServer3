package com.smile.passionistar.ch0.util;

import java.util.HashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.concurrent.GlobalEventExecutor;

public class RoomForChannelGroup {// 해시형태 룸관리 
	RedisCluster redisCluster = new RedisCluster();

    public static HashMap<String, Room> roomMap = new HashMap<String, Room>();//쿼리스트링과 룸을 받음 
    public static HashMap<ChannelId, String> channelQs = new HashMap<ChannelId, String>();// 채널아이디와 쿼리스트링을 받음 
    public static int userCount=0;
    
    
	public Channel ch;
	public FullHttpRequest req; // 쿼리스트링 얻기 위해 받아옴 
	
	public RoomForChannelGroup() {
		
	}
	
	public RoomForChannelGroup(Channel ch, FullHttpRequest req) {
		this.ch = ch;
		this.req =req;
	}
	
	
	public Channel AddChannelGroup() {
		Room rtemp = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), "false");
		
		if(roomMap.isEmpty()) {
			Room room = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), req.getUri()); // 쿼리스트링 값에 해당하는 룸객체를 생성 
			room.cg.add(ch); //룸에 새로운 채널그룹을 생
			room.count++;
			userCount++;
			roomMap.put(req.getUri(), room);
			rtemp = room;
			redisCluster.redisClusterLancher(req.getUri(), room.cg);// 레디스 채널에 등록 
			channelQs.put(ch.id(), req.getUri());
			return room.cg.find(ch.id());
		}
		
		if(roomMap.containsKey(req.getUri())) {//이미 존재하는 room 인 경우 
			roomMap.get(req.getUri()).cg.add(ch);
			roomMap.get(req.getUri()).count++;
			userCount++;
			rtemp=roomMap.get(req.getUri());
			if(!channelQs.containsKey(ch.id())) {
				channelQs.put(ch.id(), req.getUri());
			}
		}
		
		if(!roomMap.containsKey(req.getUri())) { // 만약 room 객체에 아무런 qs 일치값이 없을 경우 
			Room room = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), req.getUri()); // 쿼리스트링 값에 해당하는 룸객체를 생성 
			room.cg.add(ch); //룸에 새로운 채널그룹을 생
			room.count++;
			userCount++;
			roomMap.put(req.getUri(), room);
			rtemp = room;// 룸객체가 실제로 생성되었다면, 리턴하기 위햇 사용해
			redisCluster.redisClusterLancher(req.getUri(), room.cg);//redis cluster에 새로운 채널그룹을 넣고 이를 등록해서 다음번에 문자를 보낼 시에 사용할 수 있게 셋팅함
			channelQs.put(ch.id(), req.getUri());
		}
		
		return rtemp.cg.find(ch.id());
	}
	
	public ChannelGroup findByChannelId(Channel c) {
		if(roomMap.isEmpty()) {
			return null;
		}
		
		if(roomMap.containsKey(channelQs.get(c.id()))) {
			return roomMap.get(channelQs.get(c.id())).cg;
		}
			
		return null;
	}
	
	public Room findByChannelIdReturnRoom(Channel c) {
		if(roomMap.isEmpty()) {
			return null;
		}
		
		if(roomMap.containsKey(channelQs.get(c.id()))) {
			return roomMap.get(channelQs.get(c.id()));
		}
			
		return null;
	}
	
	public void deleteByChannelId(Channel c) {
		if(roomMap.isEmpty()) {
			return;
		}
		
		if(roomMap.containsKey(channelQs.get(c.id()))) {
			channelQs.remove(c.id());
			roomMap.remove(channelQs.get(c.id()));
		}
	}
	
	public String findByChannelIdReturnQs(Channel c) {
		if(roomMap.isEmpty()) {
			return null;
		}
		
		return channelQs.get(c.id());
	}
	
	public static void gabageCollectForRoomMap() {//hash 카운트가 0일 경우 쓰이지 않는 해쉬테이블 이므로 삭제한다. 현재 비즈니스로직에 이미처리되어 있는 기능이지만, 이 메서드의 존재목적은 혹시나 유저가 하나도 없는 채팅방이더라도 생명주기가 어느정도 유지되기를 바랄 떄 사용한다 
		if(!roomMap.isEmpty()) {
			for(Room r :roomMap.values()) {
				if(r.count == 0) {
					roomMap.remove(r.qs);
				}
			}
		}
	}
}
