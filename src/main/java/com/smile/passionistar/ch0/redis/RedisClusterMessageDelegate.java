package com.smile.passionistar.ch0.redis;

import java.io.Serializable;
import java.util.Map;

public class RedisClusterMessageDelegate implements IMessageDelegate{

	@Override
	public void handleMessage(String message) {
		System.out.println("Message Received: " + message);
		
	}

	@Override
	public void handleMessage(Map message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(byte[] message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(Serializable message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(Serializable message, String channel) {
		System.out.println("Message Received at Delegate: " + message.toString() + " from Channel [" + channel +"]");
		
	}

}
