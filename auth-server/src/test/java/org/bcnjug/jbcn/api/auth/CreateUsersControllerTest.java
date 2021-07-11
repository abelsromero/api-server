package org.bcnjug.jbcn.api.auth;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bcnjug.jbcn.api.common.TestData.MONGODB_ID_PATTERN;

@SpringBootTest
@AutoConfigureWebTestClient
public class CreateUsersControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    UsersRepository usersRepository;

    @Test
    @WithMockUser(username = "test_user", roles = {})
    void should_fail_create_user_when_not_admin() {
        webTestClient.post()
                .uri("/users")
                .body(BodyInserters.fromValue(Map.of()))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().isEmpty();
    }

    @Test
    @WithMockUser(username = "test_user", roles = {"ADMIN"})
    void should_fail_when_missing_username() {
        webTestClient.post()
                .uri("/users")
                .body(BodyInserters.fromValue(Map.of()))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Missing required parameter: username");
    }

    @Test
    @WithMockUser(username = "test_user", roles = {"ADMIN"})
    void should_fail_when_missing_email() {
        webTestClient.post()
                .uri("/users")
                .body(BodyInserters.fromValue(Map.of("username", "user")))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Missing required parameter: email");
    }

    @Test
    @WithMockUser(username = "test_user", roles = {"ADMIN"})
    void should_fail_when_missing_password() {
        webTestClient.post()
                .uri("/users")
                .body(BodyInserters.fromValue(Map.of("username", "user", "email", "pass")))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Missing required parameter: password");
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void should_create_user() {
        final LocalDateTime now = LocalDateTime.now();
        final Map<String, Object> userRequest = Map.of(
                "username", "user",
                "email", "user@mail.com",
                "password", "12345678",
                "roles", List.of("USER", "COOL")
        );
        webTestClient.post()
                .uri("/users")
                .body(BodyInserters.fromValue(userRequest))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(Matchers.matchesPattern(MONGODB_ID_PATTERN));

        User testUser = usersRepository.findByUsername("user").block();
        assertThat(testUser.getUsername()).isEqualTo("user");
        assertThat(testUser.getEmail()).isEqualTo("user@mail.com");
        assertThat(testUser.getRoles()).containsExactly("COOL", "USER");
        assertThat(testUser.getCreatedBy()).isEqualTo(testUser.getUpdatedBy()).isEqualTo("admin_user");
        assertThat(testUser.getCreatedOn()).isEqualTo(testUser.getUpdatedOn()).isAfter(now);
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void should_not_allow_users_with_same_name() {
        String username = "user-" + UUID.randomUUID();
        final Map<String, Object> userRequest = Map.of(
                "username", username,
                "email", "user@mail.com",
                "password", "12345678",
                "roles", List.of("USER", "COOL")
        );
        webTestClient.post()
                .uri("/users")
                .body(BodyInserters.fromValue(userRequest))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(Matchers.matchesPattern(MONGODB_ID_PATTERN));

        webTestClient.post()
                .uri("/users")
                .body(BodyInserters.fromValue(userRequest))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
