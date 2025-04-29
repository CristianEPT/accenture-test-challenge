package com.accenture.test.accenturetestchallenge.application.rest;

import com.accenture.test.accenturetestchallenge.application.BranchRequest;
import com.accenture.test.accenturetestchallenge.application.BranchResponse;
import com.accenture.test.accenturetestchallenge.application.api.BranchApi;
import com.accenture.test.accenturetestchallenge.domain.model.Branch;
import com.accenture.test.accenturetestchallenge.domain.ports.BranchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BranchesController implements BranchApi {

  private final BranchPort branchPort;

  @Override
  public Mono<ResponseEntity<BranchResponse>> addBranch(
      String franchiseId, Mono<BranchRequest> branchRequest, ServerWebExchange exchange) {

    return branchRequest
        .map(BranchRequest::getName)
        .flatMap(branch -> branchPort.createBranch(franchiseId, branch))
        .map(this::mapDomainToResponse)
        .map(branchResponse -> ResponseEntity.status(HttpStatus.CREATED).body(branchResponse));
  }

  private BranchResponse mapDomainToResponse(Branch branch) {
    BranchResponse branchResponse = new BranchResponse();
    branchResponse.setName(branch.getName());
    branchResponse.setId(branch.getId());
    branchResponse.setFranchiseId(branch.getFranchiseId());
    return branchResponse;
  }
}
