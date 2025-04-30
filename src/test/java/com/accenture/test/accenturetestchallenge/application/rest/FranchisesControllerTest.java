package com.accenture.test.accenturetestchallenge.application.rest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.accenture.test.accenturetestchallenge.application.FranchiseRequest;
import com.accenture.test.accenturetestchallenge.application.FranchiseResponse;
import com.accenture.test.accenturetestchallenge.domain.model.Franchise;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = FranchisesController.class)
class FranchisesControllerTest {

  @Autowired private WebTestClient webTestClient;

  @MockitoBean private FranchisePort franchisePort;

  @Test
  void createFranchise_whenAllSuccess() {
    FranchiseRequest request = new FranchiseRequest();
    request.setName("Pizzeria");

    Franchise franchise = new Franchise("123", "Pizzeria");

    Mockito.when(franchisePort.createFranchise("Pizzeria")).thenReturn(Mono.just(franchise));

    webTestClient
        .post()
        .uri("/franchise")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(FranchiseResponse.class)
        .value(
            response -> {
              assertThat(response.getId()).isEqualTo("123");
              assertThat(response.getName()).isEqualTo("Pizzeria");
            });
  }

  @Test
  void createFranchise_whenNameIsNull_returnsError() {
    FranchiseRequest request = new FranchiseRequest();

    webTestClient
        .post()
        .uri("/franchise")
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

    FranchiseRequest request = new FranchiseRequest();
    request.setName("Pizzeria");

    Mockito.when(franchisePort.createFranchise("Pizzeria"))
        .thenThrow(new RuntimeException("error saving franchise"));

    webTestClient
        .post()
        .uri("/franchise")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody()
        .consumeWith(result -> assertThat(result.getResponseBody()).isNotNull());
  }

  @Test
  void updateFranchise_whenAllSuccess() {
    String franchiseId = "123";
    String updatedName = "Updated Franchise";

    FranchiseRequest request = new FranchiseRequest();
    request.setName(updatedName);

    Franchise updatedFranchise = new Franchise(franchiseId, updatedName);

    Mockito.when(franchisePort.updateFranchiseName(franchiseId, updatedName))
        .thenReturn(Mono.just(updatedFranchise));

    webTestClient
        .put()
        .uri("/franchise/{id}", franchiseId)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FranchiseResponse.class)
        .value(
            response -> {
              assertThat(response.getId()).isEqualTo(franchiseId);
              assertThat(response.getName()).isEqualTo(updatedName);
            });
  }
}
