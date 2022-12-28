package org.example.request;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.StreamListener;
import org.example.common.StreamListenerMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.config.SuperStream;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.delayedExecutor;

@Slf4j
@Configuration
@RequiredArgsConstructor
class DevDojoSuperStreamConfig {
    private static final String SUPER_STREAM = "devdojo.super";
    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    SuperStream devDojoSuperStream() {
        return new SuperStream(SUPER_STREAM, 3);
    }

    @Bean
    RabbitStreamTemplate devDojoRabbitStreamTemplate(Environment env,
                                                     Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        var template = new RabbitStreamTemplate(env, SUPER_STREAM);
        template.setMessageConverter(jackson2JsonMessageConverter);
        template.setSuperStreamRouting(message -> UUID.randomUUID().toString());
        return template;
    }

    @Bean
    <T> StreamListenerContainer devDojoContainer(Environment env,
                                                 StreamListener<T> devDojoSuperConsumer,
                                                 StreamListenerMessageConverter<T> streamListenerMessageConverter) {
        var container = new StreamListenerContainer(env);
        container.setAutoStartup(false);
        container.superStream(SUPER_STREAM, applicationName);
        container.setupMessageListener(streamListenerMessageConverter.apply(devDojoSuperConsumer));
        container.setConsumerCustomizer(
                (id, builder) -> builder.offset(OffsetSpecification.first())
                        .manualTrackingStrategy()
        );
        delayedExecutor(5, TimeUnit.SECONDS).execute(container::start);
        return container;
    }
}
