package com.giffing.bucket4j.spring.boot.starter.user;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class User implements Serializable {

	private String firstname;
	private String lastname;
	
}
