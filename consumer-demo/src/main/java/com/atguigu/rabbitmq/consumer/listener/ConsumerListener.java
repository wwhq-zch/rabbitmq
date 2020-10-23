package com.atguigu.rabbitmq.consumer.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConsumerListener {

    /*
    @RabbitListener：方法上的注解，声明这个方法是一个消费者方法，需要指定下面的属性：

    - `bindings`：指定绑定关系，可以有多个。值是`@QueueBinding`的数组。`@QueueBinding`包含下面属性：
    - `value`：这个消费者关联的队列。值是`@Queue`，代表一个队列
    - `exchange`：队列所绑定的交换机，值是`@Exchange`类型
    - `key`：队列和交换机绑定的`RoutingKey`
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "SPRING_RABBIT_QUEUE", durable = "true"), // 注意durable为String类型
                    exchange = @Exchange(value = "SPRING_RABBIT_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
                    key = {"a.b"}
            )
    )
    public void listener(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println(msg);
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            // 是否已经重试过
            if (message.getMessageProperties().getRedelivered()){
                // 已重试过直接拒绝
                // 日志输出，记录数据库。
                // 如果requeue为true情况下重新入队，如果为false消息会丢失。
                // 如果该队列绑定了死信队列，该消息会进入死信队列
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } else {
                // 未重试过，重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }

    @RabbitListener(queues = "SPRING_DEAD_QUEUE")
    public void listener2(String msg, Channel channel, Message message) throws IOException {
        try {
//            int i = 1 / 0;
            System.out.println(msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            if (message.getMessageProperties().getRedelivered()){
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } else {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }


}
