package org.example.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperSupplier<T> extends FunctionExcept<FunctionExcept<ObjectMapper, T>, T> {

}
