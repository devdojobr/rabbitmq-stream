package org.example.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
class RequestEventConsumer {
    private final ObjectMapper objectMapper;

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
