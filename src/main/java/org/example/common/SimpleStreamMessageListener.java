package org.example.common;

import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.MessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.rabbit.stream.listener.StreamMessageListener;

@RequiredArgsConstructor
public class SimpleStreamMessageListener<T> implements StreamMessageListener {
    private final StreamListener<T> streamListener;
    private final ObjectMapperSupplier<T> objectMapperSupplier;

    @Override
    public void onStreamMessage(Message message, MessageHandler.Context context) {
        streamListener.onMessage(
                message,
                objectMapperSupplier.sneakyThrows(objectMapper -> objectMapper.readValue(message.getBodyAsBinary(), streamListener)),
                context
        );
    }

    @Override
    public void onMessage(org.springframework.amqp.core.Message message) {
        throw new UnsupportedOperationException();
    }
}
