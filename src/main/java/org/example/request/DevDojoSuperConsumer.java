package org.example.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class DevDojoSuperConsumer implements MessageListener, CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final RabbitStreamTemplate devDojoRabbitStreamTemplate;

    @Override
    public void run(String... args) {
        /*for (int i = 0; i < 100; i++) {
            devDojoRabbitStreamTemplate.convertAndSend(
                    Request.builder()
                            .password("password " + i)
                            .username("username " + i)
                            .build()
            );
        }*/
    }

    @Override
    @SneakyThrows
    @Retryable(interceptor = "streamRetryOperationsInterceptorFactoryBean")
    public void onMessage(Message message) {
        var request = objectMapper.readValue(message.getBody(), Request.class);
        log.info("Consumer message: {}", request);
    }
}
