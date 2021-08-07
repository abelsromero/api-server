package org.bcnjug.jbcn.api.papers;

import lombok.Data;
import org.assertj.core.api.Assertions;
import org.bcnjug.jbcn.api.auth.JwtGenerator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Set;
import java.util.UUID;

import static org.bcnjug.jbcn.api.common.TestData.MONGODB_ID_PATTERN;

@SpringBootTest
@AutoConfigureWebTestClient
class PaperControllerTest {

    @Autowired
    JwtGenerator jwtGenerator;

    @Autowired
    WebTestClient webTestClient;

    @Test
        // We can use this to avoid sending token header in tests, but note the Authentication instance is
        // UsernamePasswordAuthenticationToken instead of JWTAuthenticationToken 
        // @WithMockUser(username = "test-user", roles = {"HELPER"})
    void should_create_a_paper() {

        Paper paper = Paper.builder()
                .edition("2021")
                .title("Amazing paper")
                .build();

        String token = jwtGenerator.createJWS("2221", Set.of(""), 60 * 60 * 1000);
        webTestClient.post()
                .uri("/papers")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(BodyInserters.fromValue(paper))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").value(Matchers.matchesPattern(MONGODB_ID_PATTERN));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"HELPER"})
    void should_get_a_paper() {

        Paper paper = Paper.builder()
                .edition("2021")
                .title("Amazing paper")
                .build();

        IdResponse actualId = webTestClient.post()
                .uri("/papers")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paper))
                .exchange()
                .expectBody(IdResponse.class).returnResult()
                .getResponseBody();

        webTestClient.get()
                .uri("/papers/{id}", actualId.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("id").isEqualTo(actualId.getId())
                .jsonPath("title").isEqualTo(paper.getTitle());
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"HELPER"})
    void should_return_404_when_not_found() {

        webTestClient.get()
                .uri("/papers/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    @Data
    static class IdResponse {
        String id;
    }
}
