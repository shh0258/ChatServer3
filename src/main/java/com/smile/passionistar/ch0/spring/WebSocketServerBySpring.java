package com.smile.passionistar.ch0.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class WebSocketServerBySpring {
	public static void main(String[] args) throws Exception {
		AbstractApplicationContext springContext = null;
		try {
			springContext =new AnnotationConfigApplicationContext(WebSocketServerConfig.class);
			springContext.registerShutdownHook(); // 싱글톤의 안정적인 종료를 위해 사용하는 메서드 
			
			WebSocketServerV2 server = springContext.getBean(WebSocketServerV2.class);
			server.start();
		}
		finally {
			springContext.close();
		}
	}
}
