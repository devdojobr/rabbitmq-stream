package org.example.common;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.impl.StreamEnvironmentBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.retry.StreamRetryOperationsInterceptorFactoryBean;
import org.springframework.retry.support.RetryTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
class StreamCommonConfig {
    @Bean
    Environment env(RabbitProperties rabbitProperties) {
        return new StreamEnvironmentBuilder()
                .host(rabbitProperties.getHost())
                .port(rabbitProperties.getStream().getPort())
                .username(rabbitProperties.getUsername())
                .password(rabbitProperties.getPassword())
                .virtualHost(rabbitProperties.getVirtualHost())
                .build();
    }

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    <T> StreamListenerMessageConverter<T> streamListenerMessageConverter(ObjectMapperSupplier<T> objectMapperSupplier) {
        return streamListener -> message -> streamListener.onMessage(
                objectMapperSupplier.sneakyThrows(
                        objectMapper -> objectMapper.readValue(message.getBody(), streamListener)
                )
        );
    }

    @Bean
    RetryTemplate basicStreamRetryTemplate() {
        return RetryTemplate.builder()
                .infiniteRetry()
                .exponentialBackoff(
                        TimeUnit.SECONDS.toMillis(10),
                        1.5,
                        TimeUnit.MINUTES.toMillis(5)
                ).build();
    }

    @Bean
    StreamRetryOperationsInterceptorFactoryBean streamRetryOperationsInterceptorFactoryBean(RetryTemplate basicStreamRetryTemplate) {
        var factoryBean = new StreamRetryOperationsInterceptorFactoryBean();
        factoryBean.setRetryOperations(basicStreamRetryTemplate);
        return factoryBean;
    }
}
