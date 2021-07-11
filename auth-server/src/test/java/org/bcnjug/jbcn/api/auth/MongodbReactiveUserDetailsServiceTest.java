package org.bcnjug.jbcn.api.auth;

import org.junit.jupiter.api.Test;
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
        MongodbReactiveUserDetailsService.class
})
public class MongodbReactiveUserDetailsServiceTest {

    @Autowired
    UsersRepository usersRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    MongodbReactiveUserDetailsService userDetailsService;


    @Test
    void should_create_a_user() {
        LocalDateTime now = LocalDateTime.now();
        String username = randomUsername();
        String password = "my-password";

        StepVerifier.create(userDetailsService.createUser(username, null, null, password, null))
                .assertNext(createdUser -> {
                    assertThat(createdUser.getId()).isNotEmpty();
                    assertThat(createdUser.getCreatedOn()).isAfter(now);
                    assertThat(createdUser.getUpdatedOn()).isAfter(now);

                    assertThat(createdUser.getSalt()).isNotEmpty();
                    assertThat(createdUser.getPassword()).isNotEqualTo(password);

                    String rawPassword = password + stringify(createdUser.getSalt());
                    assertThat(passwordEncoder.matches(rawPassword, createdUser.getPassword())).isTrue();
                });
    }

    @Test
    void should_update_password() {
        String username = randomUsername();
        String password = "my-password";
        String newPassword = "my-new-password";

        UserDetails testUsername = userDetailsService.createUser(username, null, null, password, null)
                .flatMap(user -> userDetailsService.findByUsername(user.getUsername()))
                .block();

        StepVerifier.create(userDetailsService.updatePassword(testUsername, newPassword))
                .expectComplete();

        StepVerifier.create(usersRepository.findByUsername(username))
                .assertNext(updatedUser -> {
                    String rawPassword = newPassword + stringify(updatedUser.getSalt());
                    assertThat(passwordEncoder.matches(rawPassword, updatedUser.getPassword())).isTrue();
                });
    }

    private String randomUsername() {
        return "username-" + UUID.randomUUID();
    }

    private String stringify(byte[] salt) {
        return new String(salt);
    }
}
