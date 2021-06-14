package org.bcnjug.jbcn.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MongodbReactiveUserDetailsServiceTest {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    MongodbReactiveUserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;


    @Test
    void should_create_a_user() {
        LocalDateTime now = LocalDateTime.now();
        String username = randomUsername();
        String password = "my-password";

        User createdUser = userDetailsService.saveUser(username, null, null, password)
                .block();

        assertThat(createdUser.getId()).isNotEmpty();
        assertThat(createdUser.getCreatedOn()).isAfter(now);
        assertThat(createdUser.getUpdatedOn()).isAfter(now);

        assertThat(createdUser.getSalt()).isNotEmpty();
        assertThat(createdUser.getPassword()).isNotEqualTo(password);

        String rawPassword = password + createdUser.getSalt();
        assertThat(passwordEncoder.matches(rawPassword, createdUser.getPassword())).isTrue();

        System.out.println(usersRepository.count().block());
    }

    @Test
    void should_update_password() {
        String username = randomUsername();
        String password = "my-password";
        String newPassword = "my-new-password";

        userDetailsService.saveUser(username, null, null, password)
                .flatMap(user -> userDetailsService.findByUsername(user.getUsername()))
                .flatMap(userDetails -> userDetailsService.updatePassword(userDetails, newPassword))
                .block();

        User updatedUser = usersRepository.findByUsername(username)
                .block();

        String rawPassword = newPassword + updatedUser.getSalt();
        assertThat(passwordEncoder.matches(rawPassword, updatedUser.getPassword())).isTrue();

        System.out.println(usersRepository.count().block());
    }

    private String randomUsername() {
        return "username-" + UUID.randomUUID();
    }
}