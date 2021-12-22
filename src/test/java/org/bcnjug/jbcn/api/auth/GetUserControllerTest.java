package org.bcnjug.jbcn.api.auth;

import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bcnjug.jbcn.api.auth.Role.HELPER;
import static org.bcnjug.jbcn.api.auth.Role.SPEAKER;

@SpringBootTest
@AutoConfigureWebTestClient
public class GetUserControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    UsersRepository usersRepository;

    @Test
    @WithMockUser(username = "test-user", roles = {"HELPER"})
    void should_get_a_user() {

        final String username = "test-user";
        final String email = "user@email.com";
        final Set<String> roles = Set.of(HELPER.toString(), SPEAKER.toString());

        final LocalDateTime now = LocalDateTime.now();
        final String creator = "test";
        final User testUser = User.builder()
                .username(username)
                .email(email)
                .roles(roles)
                .createdBy(creator)
                .createdOn(now)
                .updatedBy(creator)
                .updatedOn(now)
                .build();
        final User actualUser = usersRepository.save(testUser).block();

        webTestClient.get()
                .uri("/users/{id}", actualUser.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("id").isEqualTo(actualUser.getId())
                .jsonPath("email").isEqualTo(email)
                .jsonPath("createdBy").isEqualTo(creator)
                .jsonPath("updatedBy").isEqualTo(creator)
                .jsonPath("createdOn").isNotEmpty()
                .jsonPath("updatedOn").isNotEmpty()
                .jsonPath("roles").isArray()
                .jsonPath("roles").value((Consumer<JSONArray>) o -> assertThat(o).containsExactlyInAnyOrder("SPEAKER", "HELPER"))
                .jsonPath("password").doesNotHaveJsonPath();
    }
}

