package com.smile.passionistar.ch0.util;

import io.netty.channel.group.ChannelGroup;

public class Room {
	public ChannelGroup cg;//  채널그
	public String qs; // 쿼리스트링, 이걸 기준으로 방이 만들어지고 채널 그룹이 할당되어 관리됨 
	public int count=0; // 0이 될때 삭제하기 위한 기준값 
	
	public Room(ChannelGroup cg, String qs) {
		this.cg = cg;
		this.qs = qs;
	}
}
