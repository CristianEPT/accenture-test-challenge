package com.accenture.test.accenturetestchallenge.domain.service;

import com.accenture.test.accenturetestchallenge.domain.model.Branch;
import com.accenture.test.accenturetestchallenge.domain.ports.BranchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchService implements BranchPort {

  @Override
  public Mono<Branch> createBranch(String franchiseId, String branchName) {
    return Mono.empty();
  }
}
