package com.smile.passionistar.ch0.util;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.smile.passionistar.ch0.redis.RedisClusterConfig;
import com.smile.passionistar.ch0.redis.RedisClusterMessageListener;

import io.netty.channel.group.ChannelGroup;

public class RedisCluster {
	String queryString ="";
	static AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(RedisClusterConfig.class);
	
	public RedisTemplate<String, Object> redisClusterLancher(ChannelGroup cg) {//빌더패턴
		return redisClusterLancher(null, cg);
	}
	
	public RedisTemplate<String, Object> redisClusterLancher(String queryString, ChannelGroup cg){
		RedisClusterMessageListener redisClusterMessageListener = new RedisClusterMessageListener();
		redisClusterMessageListener.setChannelGroup(cg);
				
		@SuppressWarnings("unchecked")
		RedisTemplate<String, Object> redisTemplate = ctx.getBean("redisTemplate", RedisTemplate.class);
		if(queryString != null) {
			redisTemplate.convertAndSend("testset", "ms");//이 코드는 빈 파일이 업데이트 되지않는 비정상적인 동작 방식 때문에 넣었음 
			RedisMessageListenerContainer redisContainer = ctx.getBean("redisContainer", RedisMessageListenerContainer.class);
			MessageListenerAdapter messageListener = ctx.getBean("messageListener", MessageListenerAdapter.class);
			redisContainer.addMessageListener(messageListener, new PatternTopic("c."+queryString));
		}
		
		return redisTemplate;
	}
}
