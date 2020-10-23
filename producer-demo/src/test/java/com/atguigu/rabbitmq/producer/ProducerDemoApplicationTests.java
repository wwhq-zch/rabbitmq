package com.atguigu.rabbitmq.producer;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProducerDemoApplicationTests {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Test
	void contextLoads() {
		this.rabbitTemplate.convertAndSend("SPRING_DELAY_EXCHANGE", "ab.delay", "hello spring rabbit!");
	}

}
