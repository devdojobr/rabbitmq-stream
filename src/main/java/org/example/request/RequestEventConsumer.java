package org.example.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
class RequestEventConsumer {
    private final ObjectMapper objectMapper;
    private final RabbitAdmin rabbitAdmin;
    private final Queue streamPartition;

    @PostConstruct
    void init() {
        rabbitAdmin.declareQueue(streamPartition);
    }

    @Retryable(interceptor = "streamRetryOperationsInterceptorFactoryBean")
    @RabbitListener(
            queues = "#{streamPartition.name}",
            containerFactory = "streamContainerFactory"
    )
    void onConsumer(Message in, MessageHandler.Context context) throws IOException {
        log.info("Stream partition message offset: {}", context.offset());
        var request = objectMapper.readValue(in.getBodyAsBinary(), Request.class);
        log.info("Consumer message: {}", request);
    }
}
