package org.bcnjug.jbcn.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;

@SpringBootTest
@AutoConfigureWebTestClient
public class OAuthControllerIntTest {

    static final String TEST_USERNAME = "test_username";
    static final String TEST_PASSWORD = "T3s_P4ssw0rd";
    static final String TEST_EMAIL = "user@supermail.com";

    @Value("${api-server.auth.client-id}")
    String clientId;
    @Value("${api-server.jwt.ttl-millis}")
    Long tokenTtlMillis;

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MongodbReactiveUserDetailsService userDetailsService;


    @Test
    void should_get_token() {

        Set<String> roles = Set.of("USER", "READER", "WRITER");
        userDetailsService.createUser(TEST_USERNAME, TEST_EMAIL, roles, TEST_PASSWORD, null).block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/oauth/token")
                        .queryParam("grant_type", AuthorizationGrantType.PASSWORD.getValue())
                        .queryParam("client_id", clientId)
                        .queryParam("username", TEST_USERNAME)
                        .queryParam("password", TEST_PASSWORD)
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.access_token").isNotEmpty()
                .jsonPath("$.token_type").isEqualTo("bearer")
                .jsonPath("$.expires_in").isEqualTo(tokenTtlMillis);
    }
}
