package org.bcnjug.jbcn.api;

import org.bcnjug.jbcn.api.auth.PasswordPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bcnjug.jbcn.api.DefaultUsersConfiguration.DefaultUserPasswordGenerator.generateDefaultPassword;


public class DefaultUsersConfigurationTest {

    @Test
    void should_generate_random_admin_password_when_not_set() {
        String generatedPassword = generateDefaultPassword(null);

        final var passwordValidator = new PasswordPolicy.Validator();
        assertThat(passwordValidator.isValid(generatedPassword))
                .isTrue();
    }

    @Test
    void should_maintain_password_when_set() {
        String validPassword = "1234567890";
        String generatedPassword = generateDefaultPassword(validPassword);

        assertThat(generatedPassword)
                .isEqualTo(validPassword);
    }
}
