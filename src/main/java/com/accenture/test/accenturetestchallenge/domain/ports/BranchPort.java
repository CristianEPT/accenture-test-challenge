package com.accenture.test.accenturetestchallenge.domain.ports;

import com.accenture.test.accenturetestchallenge.domain.model.Branch;
import reactor.core.publisher.Mono;

public interface BranchPort {

  Mono<Branch> createBranch(String franchiseId, String branchName);

  Mono<Boolean> existsBranch(String franchiseId, String branchId);
}
