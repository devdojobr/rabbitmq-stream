package org.example.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class DevDojoSuperConsumer implements MessageListener {
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    @Retryable(interceptor = "streamRetryOperationsInterceptorFactoryBean")
    public void onMessage(Message message) {
        var request = objectMapper.readValue(message.getBody(), Request.class);
        log.info("Consumer message: {}", request);
    }
}
