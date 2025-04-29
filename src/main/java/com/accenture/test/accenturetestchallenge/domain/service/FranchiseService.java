package com.accenture.test.accenturetestchallenge.domain.service;

import com.accenture.test.accenturetestchallenge.domain.model.Franchise;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FranchiseService implements FranchisePort {
  @Override
  public Mono<Franchise> createFranchise(String franchiseName) {
    return Mono.empty();
  }
}
