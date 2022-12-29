package org.example.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.StreamListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class DevDojoSuperStreamConsumer extends StreamListener<Request> {

    @Override
    @Retryable(interceptor = "streamRetryOperationsInterceptorFactoryBean")
    public void onMessage(Request payload) {
        log.info("Consumer message: {}", payload);
    }
}
