package com.accenture.test.accenturetestchallenge.domain.repositories;

import com.accenture.test.accenturetestchallenge.domain.entities.FranchiseEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FranchiseRepository extends ReactiveMongoRepository<FranchiseEntity, String> {}
