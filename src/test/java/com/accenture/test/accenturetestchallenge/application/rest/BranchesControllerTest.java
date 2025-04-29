package com.accenture.test.accenturetestchallenge.application.rest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;

import com.accenture.test.accenturetestchallenge.application.BranchRequest;
import com.accenture.test.accenturetestchallenge.application.BranchResponse;
import com.accenture.test.accenturetestchallenge.domain.model.Branch;
import com.accenture.test.accenturetestchallenge.domain.ports.BranchPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = BranchesController.class)
class BranchesControllerTest {
  @Autowired private WebTestClient webTestClient;

  @MockitoBean private BranchPort branchPort;

  private static final String FRANCHISE_ID = "1";

  @Test
  void createBranch_whenAllSuccess() {
    BranchRequest request = new BranchRequest();
    request.setName("North Pizzeria");

    Branch branch = new Branch(FRANCHISE_ID, "123", "North Pizzeria");

    Mockito.when(branchPort.createBranch(any(), any())).thenReturn(Mono.just(branch));

    webTestClient
        .post()
        .uri("/franchises/" + FRANCHISE_ID + "/branches")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(BranchResponse.class)
        .value(
            response -> {
              assertThat(response.getFranchiseId()).isEqualTo(FRANCHISE_ID);
              assertThat(response.getId()).isEqualTo("123");
              assertThat(response.getName()).isEqualTo("North Pizzeria");
            });
  }

  @Test
  void createBranch_whenNameIsNull_returnsError() {
    BranchRequest request = new BranchRequest();

    webTestClient
        .post()
        .uri("/franchises/" + FRANCHISE_ID + "/branches")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody()
        .consumeWith(result -> assertThat(result.getResponseBody()).isNotNull());
  }

  @Test
  void createFranchise_whenFailed_returnsError() {

    BranchRequest request = new BranchRequest();
    request.setName("North Pizzeria");

    Mockito.when(branchPort.createBranch(any(), any()))
        .thenThrow(new RuntimeException("error saving franchise"));

    webTestClient
        .post()
        .uri("/franchises/" + FRANCHISE_ID + "/branches")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody()
        .consumeWith(result -> assertThat(result.getResponseBody()).isNotNull());
  }
}
