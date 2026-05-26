package com.sps.sam.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_SAM = "sam.compras.queue";
    public static final String EXCHANGE_SAM = "sam.exchange";
    public static final String ROUTING_KEY_SAM = "sam.compra.nueva";

    @Bean
    public Queue queueSAM() {
        return new Queue(QUEUE_SAM, true);
    }

    @Bean
    public DirectExchange exchangeSAM() {
        return new DirectExchange(EXCHANGE_SAM, true, false);
    }

    @Bean
    public Binding bindingSAM(Queue queueSAM, DirectExchange exchangeSAM) {
        return BindingBuilder.bind(queueSAM).to(exchangeSAM).with(ROUTING_KEY_SAM);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
