package com.accenture.test.accenturetestchallenge.application.rest;

import com.accenture.test.accenturetestchallenge.application.FranchiseRequest;
import com.accenture.test.accenturetestchallenge.application.FranchiseResponse;
import com.accenture.test.accenturetestchallenge.application.api.FranchiseApi;
import com.accenture.test.accenturetestchallenge.domain.model.Franchise;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
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
public class FranchisesController implements FranchiseApi {

  private final FranchisePort franchisePort;

  @Override
  public Mono<ResponseEntity<FranchiseResponse>> createFranchise(
      Mono<FranchiseRequest> franchiseRequestMono, ServerWebExchange exchange) {

    return franchiseRequestMono
        .map(FranchiseRequest::getName)
        .flatMap(franchisePort::createFranchise)
        .map(this::mapDomainToResponse)
        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
  }

  private FranchiseResponse mapDomainToResponse(Franchise franchise) {
    FranchiseResponse franchiseResponse = new FranchiseResponse();
    franchiseResponse.setName(franchise.getName());
    franchiseResponse.setId(franchise.getId());
    return franchiseResponse;
  }
}
