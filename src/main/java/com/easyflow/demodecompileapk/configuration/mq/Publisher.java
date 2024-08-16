package com.easyflow.demodecompileapk.configuration.mq;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@EnableRabbit
public class Publisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Qualifier("quequeGeneral")
    @Autowired
    private org.springframework.amqp.core.Queue queueGeneral;
    @Qualifier("quequeProcess")
    @Autowired
    private Queue queueprocess;
    @Value(value = "${easyflow.rabbitmq.routingKey-notificationsInfo}")
    private String routingKeyNotificationsInfo;
    @Value(value = "${easyflow.rabbitmq.routingKey-notificationsError}")
    private String routingKeyNotificationsError;
    @Value(value = "${easyflow.rabbitmq.routingKey-processInfo}")
    private String routingKeyProcessInfo;
    @Value(value = "${easyflow.rabbitmq.routingKey-processError}")
    private String routingKeyProcessError;

    public void sendMessage(String message) {
        //rabbitTemplate.convertAndSend(queueGeneral.getName(),message);
        MessageProperties messageProperties = new MessageProperties();
        MessageConverter messageConverter = new SimpleMessageConverter();
        org.springframework.amqp.core.Message out = messageConverter.toMessage(message, messageProperties);
        rabbitTemplate.send(queueGeneral.getName(), routingKeyNotificationsInfo, out);
    }

    public void sendMessageInfo(Message message) {
        rabbitTemplate.convertAndSend(queueGeneral.getName(), routingKeyNotificationsInfo, message.toString());
    }

    public void sendMessageError(Message message) {
        rabbitTemplate.convertAndSend(queueGeneral.getName(), routingKeyNotificationsError, message.toString());
    }

    public void sendMessageUserInfo(String message, String username) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("username", username);
        MessageConverter messageConverter = new SimpleMessageConverter();
        org.springframework.amqp.core.Message out = messageConverter.toMessage(message, messageProperties);
        rabbitTemplate.send(queueGeneral.getName(), routingKeyNotificationsError, out);
    }

    public void sendMessageUserError(String message, String username) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("username", username);
        MessageConverter messageConverter = new SimpleMessageConverter();
        org.springframework.amqp.core.Message out = messageConverter.toMessage(message, messageProperties);
        rabbitTemplate.send(queueGeneral.getName(), routingKeyNotificationsError, out);
    }

    public void sendProcessInfo(String message, String username) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("username", username);
        MessageConverter messageConverter = new SimpleMessageConverter();
        org.springframework.amqp.core.Message out = messageConverter.toMessage(message, messageProperties);
        rabbitTemplate.send(queueprocess.getName(), routingKeyProcessInfo, out);
    }

    public void sendProcessError(String message, String username) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("username", username);
        MessageConverter messageConverter = new SimpleMessageConverter();
        org.springframework.amqp.core.Message out = messageConverter.toMessage(message, messageProperties);
        rabbitTemplate.send(queueprocess.getName(), routingKeyProcessError, out);
    }

}
