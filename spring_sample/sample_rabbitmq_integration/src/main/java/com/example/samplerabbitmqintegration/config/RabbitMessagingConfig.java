package com.example.samplerabbitmqintegration.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitMessagingConfig {

    @Bean
    RabbitMessagingProperties rabbitMessagingProperties() {
        return new RabbitMessagingProperties(
                "settlement.exchange",
                "settlement.queue",
                "settlement.dlx",
                "settlement.dlq",
                "settlement.process",
                "settlement.failed"
        );
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    DirectExchange settlementExchange(RabbitMessagingProperties properties) {
        return new DirectExchange(properties.exchange(), true, false);
    }

    @Bean
    DirectExchange settlementDeadLetterExchange(RabbitMessagingProperties properties) {
        return new DirectExchange(properties.dlx(), true, false);
    }

    @Bean
    Queue settlementQueue(RabbitMessagingProperties properties) {
        return new Queue(properties.queue(), true, false, false, java.util.Map.of(
                "x-dead-letter-exchange", properties.dlx(),
                "x-dead-letter-routing-key", properties.dlqRoutingKey()
        ));
    }

    @Bean
    Queue settlementDlq(RabbitMessagingProperties properties) {
        return new Queue(properties.dlq(), true);
    }

    @Bean
    Binding settlementBinding(Queue settlementQueue, DirectExchange settlementExchange, RabbitMessagingProperties properties) {
        return BindingBuilder.bind(settlementQueue).to(settlementExchange).with(properties.routingKey());
    }

    @Bean
    Binding settlementDlqBinding(Queue settlementDlq, DirectExchange settlementDeadLetterExchange, RabbitMessagingProperties properties) {
        return BindingBuilder.bind(settlementDlq).to(settlementDeadLetterExchange).with(properties.dlqRoutingKey());
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(4);
        return factory;
    }
}
