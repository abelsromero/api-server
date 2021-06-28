package org.bcnjug.jbcn.api;

import org.bcnjug.jbcn.api.DefaultUsersHandler.DefaultUsersInitializer;
import org.bcnjug.jbcn.api.auth.MongodbReactiveUserDetailsService;
import org.bcnjug.jbcn.api.auth.Role;
import org.bcnjug.jbcn.api.auth.User;
import org.bcnjug.jbcn.api.auth.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import({
        BCryptPasswordEncoder.class,
        MongodbReactiveUserDetailsService.class,
        DefaultUsersInitializer.class,
})
public class DefaultUsersInitializerTest {

    static final String ADMIN_ROLE = Role.ADMIN.toString();

    @Autowired
    DefaultUsersInitializer defaultUsersInitializer;

    @Autowired
    UsersRepository usersRepository;

    @BeforeEach
    void setup() {
        StepVerifier.create(usersRepository.deleteAll())
                .verifyComplete();
    }

    @Test
    void should_create_default_admin_user_when_no_admins_exist() {

        final LocalDateTime now = LocalDateTime.now();

        StepVerifier.create(usersRepository.findByRolesContains(ADMIN_ROLE))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(defaultUsersInitializer.setupDefaultUsers())
                .assertNext(user -> {
                    assertAdminUsernameAndEmail(user, "admin", "admin@no-reply.com");
                    assertThat(user.getCreatedBy())
                            .isEqualTo("admin");
                    assertThat(user.getCreatedOn())
                            .isEqualTo(user.getUpdatedOn())
                            .isAfter(now);
                })
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void should_not_create_default_admin_user_when_one_already_exists() {

        usersRepository.save(User.builder()
                .username("any-name")
                .email("any-email@test.com")
                .roles(Set.of(Role.ADMIN.toString()))
                .build())
                .block();

        StepVerifier.create(usersRepository.findByRolesContains(ADMIN_ROLE))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(defaultUsersInitializer.setupDefaultUsers())
                .assertNext(user -> assertAdminUsernameAndEmail(user, "any-name", "any-email@test.com"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void should_not_create_default_admin_user_when_many_already_exist() {

        usersRepository.saveAll(Flux.just(
                User.builder()
                        .username("any-name")
                        .email("any-email@test.com")
                        .roles(Set.of(Role.ADMIN.toString()))
                        .build(),
                User.builder()
                        .username("another-name")
                        .email("another-email@test.com")
                        .roles(Set.of(Role.ADMIN.toString()))
                        .build()))
                .collectList()
                .block();

        StepVerifier.create(usersRepository.findByRolesContains(ADMIN_ROLE))
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(defaultUsersInitializer.setupDefaultUsers())
                .assertNext(user -> assertAdminUsernameAndEmail(user, "any-name", "any-email@test.com"))
                .assertNext(user -> assertAdminUsernameAndEmail(user, "another-name", "another-email@test.com"))
                .expectNextCount(0)
                .verifyComplete();
    }

    private void assertAdminUsernameAndEmail(User user, String admin, String s) {
        assertThat(user.getUsername()).isEqualTo(admin);
        assertThat(user.getEmail()).isEqualTo(s);
        assertThat(user.getRoles()).containsExactly(ADMIN_ROLE);
    }
}
