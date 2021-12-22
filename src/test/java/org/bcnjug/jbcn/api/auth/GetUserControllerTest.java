package org.bcnjug.jbcn.api.auth;

import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    @BeforeEach
    public void setup() {
        usersRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN"})
    void should_get_a_user() {
        final User testUser = createRandomUser();

        webTestClient.get()
                .uri("/users/{id}", testUser.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("id").isEqualTo(testUser.getId())
                .jsonPath("email").isEqualTo(testUser.getEmail())
                .jsonPath("createdBy").isEqualTo(testUser.getCreatedBy())
                .jsonPath("updatedBy").isEqualTo(testUser.getUpdatedBy())
                .jsonPath("createdOn").isNotEmpty()
                .jsonPath("updatedOn").isNotEmpty()
                .jsonPath("roles").isArray()
                .jsonPath("roles").value((Consumer<JSONArray>) o -> assertThat(o).containsExactlyInAnyOrder("SPEAKER", "HELPER"))
                .jsonPath("password").doesNotHaveJsonPath();
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN"})
    void should_get_all_users() {

        final User testUser1 = createTestUser("test-user-1");
        final User testUser2 = createTestUser("test-user-2");
        final User testUser3 = createTestUser("test-user-3");

        webTestClient.get()
                .uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("total").isEqualTo(3)
                .jsonPath("items[0]").value((Consumer<Map<String, Object>>) item -> {
                    assertThat(item).containsOnlyKeys("id", "createdBy", "createdOn", "updatedBy", "updatedOn", "username", "email", "roles");
                    assertThat(item).doesNotContainKey("password");

                    assertSystemProperties(item);
                    assertThat((String) item.get("username")).isNotBlank();
                    assertThat((String) item.get("email")).isNotBlank();
                    assertThat((List<String>) item.get("roles")).contains("HELPER", "SPEAKER");
                });
    }

    private void assertSystemProperties(Map<String, Object> item) {
        assertThat((String) item.get("id")).isNotBlank();
        assertThat((String) item.get("createdBy")).isNotBlank();
        assertThat((String) item.get("createdOn")).isNotBlank();
        assertThat((String) item.get("updatedBy")).isNotBlank();
        assertThat((String) item.get("updatedOn")).isNotBlank();
    }

    private User createRandomUser() {
        return createTestUser(UUID.randomUUID().toString());
    }

    private User createTestUser(String username) {
        final String email = username + "@email.com";
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
        return usersRepository.save(testUser).block();
    }
}

