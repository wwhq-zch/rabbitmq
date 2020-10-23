package com.atguigu.rabbitmq.producer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 配置发送方确认
 */
@Configuration
@Slf4j
public class RabbitmqConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){

        // 确认消息是否到达交换机
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if(!ack){
                log.warn("消息没有到达交换机：" + cause);
            }
        });

        // 确认消息是否到达队列，到达队列该方法不执行
        this.rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            // {} : 占位符
            log.warn("消息没有到达队列，来自于交换机：{}，路由键：{}，消息内容：{}", exchange, routingKey, new String(message.getBody()));
        });

    }

    @Bean
    public TopicExchange delayExchange(){
        return ExchangeBuilder.topicExchange("SPRING_DELAY_EXCHANGE").build();
    }

    @Bean
    public Queue delayQueue(){
        return QueueBuilder.durable("SPRING_DELAY_QUEUE")
                .withArgument("x-message-ttl",30000) // 单位：ms
                .withArgument("x-dead-letter-exchange", "SPRING_DEAD_EXCHANGE")
                .withArgument( "x-dead-letter-routing-key", "ab.dead")
                .build();
    }

    @Bean
    public Binding delayBinding(TopicExchange delayExchange, Queue delayQueue){
        return BindingBuilder.bind(delayQueue).to(delayExchange).with("ab.delay");
    }

    @Bean
    public TopicExchange deadExchange(){
        return ExchangeBuilder.topicExchange("SPRING_DEAD_EXCHANGE").build();
    }

    @Bean
    public Queue deadQueue(){
        return QueueBuilder.durable("SPRING_DEAD_QUEUE").build();
    }

    @Bean
    public Binding deadBinding(TopicExchange deadExchange, Queue deadQueue){
        return BindingBuilder.bind(deadQueue).to(deadExchange).with("ab.dead");
    }




}
