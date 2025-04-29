package com.accenture.test.accenturetestchallenge.domain.ports;

import com.accenture.test.accenturetestchallenge.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchisePort {

  Mono<Franchise> createFranchise(String franchiseName);

  Mono<Boolean> existsFranchise(String franchiseId);
}
