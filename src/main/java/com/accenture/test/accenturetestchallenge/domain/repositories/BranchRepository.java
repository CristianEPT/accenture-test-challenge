package com.accenture.test.accenturetestchallenge.domain.repositories;

import com.accenture.test.accenturetestchallenge.domain.entities.BranchEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends ReactiveMongoRepository<BranchEntity, String> {}
