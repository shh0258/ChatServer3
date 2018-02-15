package com.smile.passionistar.ch0.util;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisForLB {
	String count;
	private HashOperations<String, String, String> hashOps;
	
	public RedisForLB(int count) {
		this.count= Integer.toString(count);
	}
	
	public void sendCount() {
		@SuppressWarnings("unchecked")
		RedisTemplate<String, Object> redisTemplate = RedisCluster.ctx.getBean("redisTemplate", RedisTemplate.class);
		hashOps = redisTemplate.opsForHash();
		hashOps.put("ServerClientCnt", "192.168.0.56:8080", count);// 서버 포트번호와ip주소를키값으로 보내준다 
	}
}
