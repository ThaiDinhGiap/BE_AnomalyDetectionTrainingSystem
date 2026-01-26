package com.sep490.anomaly_training_backend.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMQConfig {

    // Queue names
    public static final String NOTIFICATION_QUEUE = "email.notification";
    public static final String RETRY_QUEUE = "email.retry";
    public static final String DEAD_LETTER_QUEUE = "email.dead-letter";

    // Exchange names
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String RETRY_EXCHANGE = "retry.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    // Routing keys
    public static final String NOTIFICATION_ROUTING_KEY = "notification.email";
    public static final String RETRY_ROUTING_KEY = "retry.email";
    public static final String DLX_ROUTING_KEY = "dlx.email";

    // Main notification queue with priority support
    @Bean
    public Queue notificationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-max-priority", 10); // Priority từ 0-10
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLX_ROUTING_KEY);

        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .withArguments(args)
                .build();
    }

    // Retry queue với TTL (Time To Live)
    @Bean
    public Queue retryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 300000); // 5 phút
        args.put("x-dead-letter-exchange", NOTIFICATION_EXCHANGE);
        args.put("x-dead-letter-routing-key", NOTIFICATION_ROUTING_KEY);

        return QueueBuilder
                .durable(RETRY_QUEUE)
                .withArguments(args)
                .build();
    }

    // Dead letter queue để lưu các message fail vĩnh viễn
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(DEAD_LETTER_QUEUE)
                .build();
    }

    // Exchanges
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // Bindings
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder
                .bind(retryQueue())
                .to(retryExchange())
                .with(RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(dlxExchange())
                .with(DLX_ROUTING_KEY);
    }

    // Message converter để serialize/deserialize
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
