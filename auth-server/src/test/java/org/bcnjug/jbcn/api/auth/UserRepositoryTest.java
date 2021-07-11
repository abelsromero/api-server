package org.bcnjug.jbcn.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class UserRepositoryTest {

    @Autowired
    UsersRepository usersRepository;


    @BeforeEach
    void setup() {
        StepVerifier.create(usersRepository.deleteAll())
                .verifyComplete();
    }

    @Test
    void should_find_user_by_username() {
        final String testUsername = "test_username";
        final User testUserData = User.builder()
                .username(testUsername)
                .roles(Set.of("user", "test"))
                .build();
        User testUser = usersRepository.save(testUserData).block();

        StepVerifier.create(usersRepository.findByUsername(testUsername))
                .assertNext(user -> {
                    assertThat(user.getId()).isEqualTo(testUser.getId());
                    assertThat(user.getUsername()).isEqualTo(testUsername);
                    assertThat(user.getRoles()).containsExactlyInAnyOrder("user", "test");
                });
    }

    @Test
    void should_find_single_user_by_role() {
        final String roleName = "test";
        final User testUserData = User.builder()
                .username("test_username")
                .roles(Set.of("user", roleName))
                .build();
        User testUser = usersRepository.save(testUserData).block();

        StepVerifier.create(usersRepository.findByRolesContains(roleName))
                .expectNextCount(1)
                .assertNext(user -> {
                    assertThat(user.getId()).isEqualTo(testUser.getId());
                    assertThat(user.getUsername()).isEqualTo("test_username");
                    assertThat(user.getRoles()).containsExactlyInAnyOrder("user", "test");
                });
    }

    @Test
    void should_find_multiple_users_by_role() {
        List<String> userIds = usersRepository.saveAll(Flux.just(randomUsernameUser(), randomUsernameUser(), randomUsernameUser()))
                .map(User::getId)
                .collectList()
                .block();

        StepVerifier.create(usersRepository.findByRolesContains("random_username"))
                .expectNextCount(3)
                .assertNext(user -> {
                    assertThat(user.getId()).isIn(userIds);
                    assertThat(user.getUsername()).isEqualTo("test_username");
                    assertThat(user.getRoles()).containsExactlyInAnyOrder("user", "random_username");
                });
    }

    private User randomUsernameUser() {
        final String username = "user_" + UUID.randomUUID();
        return User.builder()
                .username(username)
                .roles(Set.of("user", "random_username"))
                .build();
    }

}
