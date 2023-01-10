package org.example.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.stream.MessageHandler;

public abstract class StreamListener<T> extends TypeReference<T> {
    public void onMessage(T payload) {
        throw new UnsupportedOperationException("Method must be override");
    }

    public void onMessage(T payload, MessageHandler.Context context) {
        onMessage(payload);
    }
}
