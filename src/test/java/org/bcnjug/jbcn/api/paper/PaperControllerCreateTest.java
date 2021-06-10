package org.bcnjug.jbcn.api.paper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bcnjug.jbcn.api.papers.Paper;
import org.bcnjug.jbcn.api.papers.PapersRepository;
import org.bcnjug.jbcn.api.sec.Roles;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
class PaperControllerCreateTest {

    private static final String MONGODB_ID_PATTERN = "[a-f\\d]{24}";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PapersRepository papersRepository;

    @Autowired
    ObjectMapper mapper;



    @Test
    @WithMockUser(username = "test-user", roles = {Roles.HELPER})
    @DisplayName("Given a valid paper should create it")
    void should_create_a_paper() throws JsonProcessingException {

        // Prepare request
        Paper expected = Paper.builder()
                .edition(UUID.randomUUID().toString())
                .title(UUID.randomUUID().toString())
                .createdBy("this field should be ignored")
                .build();

        // Launch rest operation
        byte[] responseByte = webTestClient.post()
                .uri("/papers")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(expected))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                // Check it returns and ID and it is an mongodb id.
                .jsonPath("$.id").value(Matchers.matchesPattern(MONGODB_ID_PATTERN))
                .returnResult().getResponseBody();

        // Recovers json repsonse
        assertNotNull(responseByte);
        String json = new String(responseByte, StandardCharsets.UTF_8);
        assertNotNull(json);

        // Recovers id to get just created paper from database
        Map<String, String> result = this.mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        assertNotNull(result.get("id"));
        String actualId = result.get("id");
        Mono<Paper> actualMono = this.papersRepository.findById(actualId);
        // Check valiues en database
        StepVerifier.create(actualMono)
                .assertNext( actual -> {
                    assertEquals(actualId, actual.getId());
                    assertEquals(expected.getTitle(), actual.getTitle());
                    assertEquals(expected.getEdition(), actual.getEdition());
                    assertEquals("test-user", actual.getCreatedBy());
                    assertNotNull(actual.getCreatedOn());

                    // Check if createdOn field is set just not in UTC-0
                    long createdOnMillis = actual.getCreatedOn().toInstant(ZoneOffset.UTC).toEpochMilli();
                    assert System.currentTimeMillis() - createdOnMillis < 1;
                });

    }

}
