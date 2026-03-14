package com.giffing.bucket4j.spring.boot.starter.examples.postgresql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Only for debug purposes
 */
@Entity(name = "bucket")
@ToString
@Getter
@Setter
public class BucketEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private String key;

    @Column(name = "state")
    private String state;

    @Column(name = "expires_at")
    private Long expiresAt;

    @Column(name = "explicit_lock")
    private Long explicitLock;

}
