package org.example.common;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import com.rabbitmq.stream.impl.StreamEnvironmentBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.rabbit.stream.retry.StreamRetryOperationsInterceptorFactoryBean;
import org.springframework.retry.support.RetryTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.delayedExecutor;

@Configuration
class StreamCommonConfig {
    @Value("${spring.application.name}")
    private String applicationName;

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
        return streamListener -> new SimpleStreamMessageListener<>(streamListener, objectMapperSupplier);
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

    @Bean
    RabbitStreamTemplateSimpleFactory rabbitStreamTemplateSimpleFactory(Environment env,
                                                                        Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        return stream -> {
            var template = new RabbitStreamTemplate(env, stream);
            template.setMessageConverter(jackson2JsonMessageConverter);
            template.setSuperStreamRouting(message -> UUID.randomUUID().toString());
            return template;
        };
    }

    @Bean
    <T> StreamListenerContainerSimpleFactory<T> streamListenerContainerSimpleFactory(Environment env,
                                                                                     StreamListenerMessageConverter<T> streamListenerMessageConverter) {
        return (stream, streamListener) -> {
            var container = new StreamListenerContainer(env);
            container.setAutoStartup(false);
            container.superStream(stream, applicationName);
            container.setupMessageListener(streamListenerMessageConverter.apply(streamListener));
            container.setConsumerCustomizer(
                    (id, builder) -> builder.offset(OffsetSpecification.first())
                            .manualTrackingStrategy()
            );
            delayedExecutor(5, TimeUnit.SECONDS).execute(container::start);
            return container;
        };
    }
}
