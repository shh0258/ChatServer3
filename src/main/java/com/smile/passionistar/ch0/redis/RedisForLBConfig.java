package com.smile.passionistar.ch0.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ComponentScan("com.smile.passionistar.ch0.redis")
public class RedisForLBConfig {
	
	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		JedisConnectionFactory redisConnectionFactory =new JedisConnectionFactory();
		redisConnectionFactory.setHostName("52.79.83.113");//여기에로드벨런싱용 레디스 주소를 넣는다 
		//redisConnectionFactory.setHostName("127.0.0.1");
		redisConnectionFactory.setPort(6379);
		redisConnectionFactory.setUsePool(true);
		return redisConnectionFactory;
	}
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		redisTemplate.setDefaultSerializer(stringSerializer());
		return redisTemplate;
	}
	
	@Bean
	public StringRedisSerializer stringSerializer() {
		return new StringRedisSerializer();
	}
	
	

}
