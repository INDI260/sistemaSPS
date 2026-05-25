package com.sps.shc.config;

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

    public static final String QUEUE_SHC = "shc.compras.queue";
    public static final String EXCHANGE_SHC = "shc.exchange";
    public static final String ROUTING_KEY_SHC = "shc.compra.nueva";

    @Bean
    public Queue queueSHC() {
        return new Queue(QUEUE_SHC, true);
    }

    @Bean
    public DirectExchange exchangeSHC() {
        return new DirectExchange(EXCHANGE_SHC, true, false);
    }

    @Bean
    public Binding bindingSHC(Queue queueSHC, DirectExchange exchangeSHC) {
        return BindingBuilder.bind(queueSHC).to(exchangeSHC).with(ROUTING_KEY_SHC);
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
