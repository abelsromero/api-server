package org.bcnjug.jbcn.api.auth;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration")
// Not working, use @TestPropertySource
// @EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
// Does not work: all calls return 404
//@ExtendWith(SpringExtension.class)
//@WebFluxTest(controllers = OAuthController.class)
//@ContextConfiguration(classes = {SecurityConfiguration.class})
public class OAuthControllerTest {

    @Value("${auth.client-id}")
    String clientId;
    @Value("${jwt.ttl-millis}")
    Long tokenTtlMillis;

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    UsersRepository usersRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Test
    void should_fail_when_username_is_missing() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/oauth/token")
                        .queryParam("grant_type", "password")
                        .queryParam("client_id", clientId)
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Required String parameter 'username' is not present");
    }

    @Test
    void should_fail_when_password_is_missing() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/oauth/token")
                        .queryParam("grant_type", "password")
                        .queryParam("client_id", clientId)
                        .queryParam("username", "test_user")
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Required String parameter 'password' is not present");
    }

    @Test
    void should_fail_when_username_does_not_exist() {
        final String testUsername = "test_user";

        when(usersRepository.findByUsername(testUsername))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/oauth/token")
                        .queryParam("grant_type", "password")
                        .queryParam("client_id", clientId)
                        .queryParam("username", testUsername)
                        .queryParam("password", "test_password")
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid username");
    }

    @Test
    void should_fail_when_password_does_not_match() {
        final String testUsername = "test_user";
        final String testPassword = "test_password";

        when(usersRepository.findByUsername(testUsername))
                .thenReturn(Mono.just(testUser(testUsername)));
        when(passwordEncoder.matches(Mockito.any(), Mockito.any()))
                .thenReturn(Boolean.FALSE);

        webTestClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/oauth/token")
                            .queryParam("grant_type", "password")
                            .queryParam("client_id", clientId)
                            .queryParam("username", testUsername)
                            .queryParam("password", testPassword)
                            .build();
                })
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid username");
    }

    @Test
    void should_get_token() {
        final String testUsername = "test_user";

        when(usersRepository.findByUsername(testUsername))
                .thenReturn(Mono.just(testUser(testUsername)));
        when(passwordEncoder.matches(Mockito.any(), Mockito.any()))
                .thenReturn(Boolean.TRUE);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/oauth/token")
                        .queryParam("grant_type", "password")
                        .queryParam("client_id", clientId)
                        .queryParam("username", testUsername)
                        .queryParam("password", "test_password")
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.access_token").isNotEmpty()
                .jsonPath("$.token_type").isEqualTo("bearer")
                .jsonPath("$.expires_in").isEqualTo(tokenTtlMillis);
    }

    private User testUser(String testUsername) {
        return new User("", "", LocalDateTime.now(), "", LocalDateTime.now(), testUsername, "mail", Set.of("USER"), "", "");
    }
}
