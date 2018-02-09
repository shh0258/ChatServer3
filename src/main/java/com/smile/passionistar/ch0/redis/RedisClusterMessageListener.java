package com.smile.passionistar.ch0.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class RedisClusterMessageListener implements MessageListener {
	
	static ChannelGroup cg;
	public void setChannelGroup(ChannelGroup cg) {
		this.cg=cg;
	}
	
	@Override
	public void onMessage(Message msg, byte[] channel) {
		System.err.println("Message Received at Listener: " + msg.toString() + " from Channel [" + new String(channel) +"]");
		cg.writeAndFlush(new TextWebSocketFrame(msg.toString()));
	}	

}
