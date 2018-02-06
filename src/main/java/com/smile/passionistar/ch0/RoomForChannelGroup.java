package com.smile.passionistar.ch0;

import java.util.ArrayList;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.concurrent.GlobalEventExecutor;

public class RoomForChannelGroup {
	RedisCluster redisCluster = new RedisCluster();

    public static ArrayList<Room> roomValues = new ArrayList<Room>();//방 목록을 관리하는 static 메서드 
	Channel ch;
	FullHttpRequest req; // 쿼리스트링 얻기 위해 받아옴 
	
	public RoomForChannelGroup(Channel ch, FullHttpRequest req) {
		this.ch = ch;
		this.req =req;
	}
	
	public Channel AddChannelGroup() {
		Room rtemp = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), "false");
		
		if(roomValues.isEmpty()) {
			Room room = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), req.getUri()); // 쿼리스트링 값에 해당하는 룸객체를 생성 
			room.cg.add(ch); //룸에 새로운 채널그룹을 생
			roomValues.add(room);
			rtemp = room;
			redisCluster.redisClusterLancher(req.getUri(), room.cg);
			return room.cg.find(ch.id());
		}
		
		for(Room r : roomValues) {// 모든 룸객체에 찾아가서 
			if(r.qs.equals(req.getUri())) { // 쿼리스트링이 일치하면 
				r.cg.add(ch); //채널 그룹객체에 채널을 추가하
				rtemp = r;
			}
			
		}

		System.out.println(roomValues.toString());
		if(!rtemp.qs.equals(req.getUri())) { // 만약 room 객체에 아무런 qs 일치값이 없을 경우 
			Room room = new Room(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE), req.getUri()); // 쿼리스트링 값에 해당하는 룸객체를 생성 
			room.cg.add(ch); //룸에 새로운 채널그룹을 생
			roomValues.add(room); // 생성된 새로운 룸을 룸 목록을 관리하는배열에 넣어서 관리한다.
			rtemp = room;// 룸객체가 실제로 생성되었다면, 리턴하기 위햇 사용해
			redisCluster.redisClusterLancher(req.getUri(), room.cg);//redis cluster에 새로운 채널그룹을 넣고 이를 등록해서 다음번에 문자를 보낼 시에 사용할 수 있게 셋팅함
		}
		return rtemp.cg.find(ch.id());
	}
	
	public ChannelGroup findByChannelId(Channel c) {
		if(roomValues.isEmpty()) {
			return null;
		}
		
		for(Room r : roomValues) {
			if(r.cg.find(c.id()) != null) {
				return r.cg;
			}
		}
		
		return null;
	}
	
	public ChannelGroup findByQueryString(String qs) {
		if(roomValues.isEmpty()) {
			return null;
		}
		
		for(Room r : roomValues) {
			if(r.qs.equals(qs) == true) {
				return r.cg;
			}
		}
		
		return null;
	}
	
	public String findByChannelIdReturnQs(Channel c) {
		if(roomValues.isEmpty()) {
			return null;
		}
		
		for(Room r : roomValues) {
			if(r.cg.find(c.id()) != null) {
				return r.qs;
			}
		}
		
		return null;
	}
	
	

}
