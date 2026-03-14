package com.giffing.bucket4j.spring.boot.starter.examples.postgresql;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BucketRepository extends ListCrudRepository<BucketEntity, String> {
}
