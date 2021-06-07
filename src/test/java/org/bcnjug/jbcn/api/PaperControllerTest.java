package org.bcnjug.jbcn.api;

import org.bcnjug.jbcn.api.papers.Paper;
import org.bcnjug.jbcn.api.sec.Roles;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest
@AutoConfigureWebTestClient
class PaperControllerTest {

    private static final String MONGODB_ID_PATTERN = "[a-f\\d]{24}";

    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser(username = "test-user", roles = {Roles.HELPER})
    void should_create_a_paper() {
        Paper paper = Paper.builder()
                .edition("2021")
                .title("Amazing paper")
                .build();

        webTestClient.post()
                .uri("/papers")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paper))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(Matchers.matchesPattern(MONGODB_ID_PATTERN));
    }

}
