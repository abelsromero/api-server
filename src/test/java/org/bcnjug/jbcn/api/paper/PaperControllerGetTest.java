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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
class PaperControllerGetTest {

    private static final String MONGODB_ID_PATTERN = "[a-f\\d]{24}";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PapersRepository papersRepository;

    @Autowired
    ObjectMapper mapper;



    @Test
    @WithMockUser(username = "test-user", roles = {Roles.HELPER})
    @DisplayName("Given a not existing paper Id it should return an http 404 error")
    void non_exising_id_should_return_404() throws JsonProcessingException {
        // Launch rest operation
        webTestClient.get()
                .uri("/papers/"+Integer.MAX_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    @WithMockUser(username = "test-user", roles = {Roles.HELPER})
    @DisplayName("Given an existing paper it should return this paper")
    void non_exising_id_should_valid_paper() throws JsonProcessingException {

        // Prepare request
        Paper expected = Paper.builder()
                .edition(UUID.randomUUID().toString())
                .title(UUID.randomUUID().toString())
                .createdBy("this field should be ignored")
                .build();

        Paper entity = papersRepository.save(expected).block();
        assertNotNull(entity);
        assertNotNull(entity.getId());

        // Launch rest operation
        byte[] responseByte =  webTestClient.get()
                .uri("/papers/"+entity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody().returnResult().getResponseBody();


        assertNotNull(responseByte);
        String json = new String(responseByte, StandardCharsets.UTF_8);
        assertNotNull(json);

        // Recovers id to get just created paper from database
        Paper actual = this.mapper.readValue(json, Paper.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(expected.getEdition(), actual.getEdition());
        assertEquals(expected.getTitle(), actual.getTitle());

    }



}
