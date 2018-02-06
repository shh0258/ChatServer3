package com.smile.passionistar.ch0.spring;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan("com.smile.passionistar.ch0.spring")
@PropertySource("classpath:telnet-server.properties")
public class WebSocketServerConfig {
	@Value("${boss.thread.count}")
	private int bossCount;
	
	@Value("${worker.thread.count}")
	private int workerCount;
	
	@Value("${tcp.port}")
	private int tcpPort;
	
	public int getBossCount() {
		return bossCount;
	}

	public int getWorkerCount() {
		return workerCount;
	}
	
	public int getTcpPort() {
		return tcpPort;
	}
	
	@Bean(name = "tcpSocketAddress")
	public InetSocketAddress tcpPort() {
		return new InetSocketAddress(tcpPort);
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
