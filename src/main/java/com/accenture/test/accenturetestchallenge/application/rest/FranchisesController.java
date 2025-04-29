package com.accenture.test.accenturetestchallenge.application.rest;

import com.accenture.test.accenturetestchallenge.application.BranchResponse;
import com.accenture.test.accenturetestchallenge.application.FranchiseRequest;
import com.accenture.test.accenturetestchallenge.application.FranchiseResponse;
import com.accenture.test.accenturetestchallenge.application.api.FranchiseApi;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class FranchisesController implements FranchiseApi {

  @Override
  public Mono<ResponseEntity<FranchiseResponse>> createFranchise(
      Mono<FranchiseRequest> franchiseRequestMono, ServerWebExchange exchange) {

    return franchiseRequestMono.map(
        request -> {
          FranchiseResponse response = new FranchiseResponse();
          response.setId(UUID.randomUUID().toString());
          response.setName(request.getName());
          BranchResponse branch = new BranchResponse();
          branch.setName("branch - " + request.getName());
          branch.setProducts(new ArrayList<>());
          response.setBranches(List.of(branch));
          return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
  }
}
