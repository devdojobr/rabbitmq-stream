package org.example.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

public interface ObjectMapperSupplier<T> extends FunctionExcept<FunctionExcept<ObjectMapper, T>, T> {

}
