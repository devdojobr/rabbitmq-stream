package org.example.stream_test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.StreamListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class StreamTestConsumer extends StreamListener<PayloadTest> {
    @Override
    @Retryable(interceptor = "streamRetryOperationsInterceptorFactoryBean")
    public void onMessage(PayloadTest payload) {
        log.info("Consumer message: {}", payload);
    }
}