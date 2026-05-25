package com.sps.compra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SHC_EXCHANGE = "shc.exchange";
    public static final String SHC_QUEUE = "shc.compras.queue";
    public static final String SHC_ROUTING_KEY = "shc.compra.nueva";

    public static final String SAM_EXCHANGE = "sam.exchange";
    public static final String SAM_QUEUE = "sam.compras.queue";
    public static final String SAM_ROUTING_KEY = "sam.compra.nueva";

    @Bean
    public DirectExchange shcExchange() {
        return new DirectExchange(SHC_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange samExchange() {
        return new DirectExchange(SAM_EXCHANGE, true, false);
    }

    @Bean
    public Queue shcQueue() {
        return new Queue(SHC_QUEUE, true);
    }

    @Bean
    public Queue samQueue() {
        return new Queue(SAM_QUEUE, true);
    }

    @Bean
    public Binding shcBinding(Queue shcQueue, DirectExchange shcExchange) {
        return BindingBuilder.bind(shcQueue).to(shcExchange).with(SHC_ROUTING_KEY);
    }

    @Bean
    public Binding samBinding(Queue samQueue, DirectExchange samExchange) {
        return BindingBuilder.bind(samQueue).to(samExchange).with(SAM_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
