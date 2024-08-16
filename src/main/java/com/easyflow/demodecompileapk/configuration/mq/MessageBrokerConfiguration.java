package com.easyflow.demodecompileapk.configuration.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageBrokerConfiguration {

    @Value("${easyflow.rabbitmq.queueNotification}")
    private String generalMessage;

    @Value("${easyflow.rabbitmq.queueProcess}")
    private String processMessage;

    @Bean
    public org.springframework.amqp.core.Queue quequeGeneral() {
        return new org.springframework.amqp.core.Queue(generalMessage, true);
    }

    @Bean
    public org.springframework.amqp.core.Queue quequeProcess() {
        return new Queue(processMessage, true);
    }

}
