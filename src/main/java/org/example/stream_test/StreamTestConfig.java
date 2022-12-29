package org.example.stream_test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.RabbitStreamTemplateSimpleFactory;
import org.example.common.StreamListener;
import org.example.common.StreamListenerContainerSimpleFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.config.SuperStream;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;

@Configuration
@RequiredArgsConstructor
class StreamTestConfig {
    private static final String SUPER_STREAM = "stream.test";

    @Bean
    SuperStream streamTest() {
        return new SuperStream(SUPER_STREAM, 3);
    }

    @Bean
    RabbitStreamTemplate streamTestTemplate(RabbitStreamTemplateSimpleFactory factory) {
        return factory.apply(SUPER_STREAM);
    }

    @Bean
    <T> StreamListenerContainer streamTestContainer(StreamListenerContainerSimpleFactory<T> factory,
                                                    StreamListener<T> streamTestConsumer) {
        return factory.apply(SUPER_STREAM, streamTestConsumer);
    }
}
