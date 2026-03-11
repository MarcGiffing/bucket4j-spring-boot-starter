package com.giffing.bucket4j.spring.boot.starter.context.converter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@com.fasterxml.jackson.annotation.JacksonAnnotation
@JsonSerialize(using = JacksonEnumSerializer.class)
@JsonDeserialize(using = JacksonEnumDeserializer.class)
@Inherited
public @interface EnumValue {
}
