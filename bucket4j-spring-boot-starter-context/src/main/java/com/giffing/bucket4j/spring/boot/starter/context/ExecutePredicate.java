package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.function.Predicate;

public interface ExecutePredicate<T> extends Predicate<T> {

	ExecutePredicate<T> newInstance();
	
	String name();
	
	ExecutePredicate<T> setValue(String value);
	
}
