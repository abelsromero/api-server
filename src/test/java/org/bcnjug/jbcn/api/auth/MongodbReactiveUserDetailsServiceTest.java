package org.bcnjug.jbcn.api.auth;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import({
        BCryptPasswordEncoder.class,
        PasswordPolicy.Validator.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MongodbReactiveUserDetailsServiceTest {

    @Autowired
    UsersRepository usersRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    MongodbReactiveUserDetailsService userDetailsService;

    @BeforeAll
    void setup() {
        userDetailsService = new MongodbReactiveUserDetailsService(usersRepository, passwordEncoder, password -> true);
    }

    @Test
    void should_create_a_user() {
        LocalDateTime now = LocalDateTime.now();
        String username = randomUsername();
        String plainPassword = "my-plainPassword";

        StepVerifier.create(userDetailsService.createUser(username, null, null, plainPassword, null))
                .assertNext(createdUser -> {
                    assertThat(createdUser.getId()).isNotEmpty();
                    assertThat(createdUser.getCreatedOn()).isAfter(now);
                    assertThat(createdUser.getUpdatedOn()).isAfter(now);
                    assertThat(createdUser.getPassword()).isNotEqualTo(plainPassword);

                    assertThat(passwordEncoder.matches(plainPassword, createdUser.getPassword())).isTrue();
                });
    }

    @Test
    void should_fail_creating_user_if_password_does_not_match_policy() {
        LocalDateTime now = LocalDateTime.now();
        String username = randomUsername();
        String plainPassword = "easypassword";

        StepVerifier.create(userDetailsService.createUser(username, null, null, plainPassword, null))
                .assertNext(createdUser -> {
                    assertThat(createdUser.getId()).isNotEmpty();
                    assertThat(createdUser.getCreatedOn()).isAfter(now);
                    assertThat(createdUser.getUpdatedOn()).isAfter(now);
                    assertThat(createdUser.getPassword()).isNotEqualTo(plainPassword);

                    assertThat(passwordEncoder.matches(plainPassword, createdUser.getPassword())).isTrue();
                });
    }

    @Test
    void should_update_password() {
        String username = randomUsername();
        String password = "my-0LDS-password";
        String newPassword = "my-N3W-password";

        UserDetails testUsername = userDetailsService.createUser(username, null, null, password, null)
                .flatMap(user -> userDetailsService.findByUsername(user.getUsername()))
                .block();

        StepVerifier.create(userDetailsService.updatePassword(testUsername, newPassword))
                .expectComplete();

        StepVerifier.create(usersRepository.findByUsername(username))
                .assertNext(updatedUser -> assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue());
    }

    private String randomUsername() {
        return "username-" + UUID.randomUUID();
    }
}
