package org.bcnjug.jbcn.api.auth;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordPolicyTest {

    @ParameterizedTest
    @MethodSource("invalidPasswords")
    void should_not_validate_password(String password) {
        PasswordPolicy.Validator passwordPolicy = new PasswordPolicy.Validator();

        boolean isValid = passwordPolicy.isValid(password);

        assertThat(isValid).isFalse();
    }

    static Stream<String> invalidPasswords() {
        return Stream.of(
                null, "", "   ",
                "t00-sh0rt",
                "no-numbers-no-numbers",
                "all-lowerc4s3-chars", "ALL-UPPERC4S3-CHARSS");
    }

    @ParameterizedTest
    @MethodSource("validPasswords")
    void should_validate_password(String password) {
        PasswordPolicy.Validator passwordPolicy = new PasswordPolicy.Validator();

        boolean isValid = passwordPolicy.isValid(password);

        assertThat(isValid).isTrue();
    }

    static Stream<String> validPasswords() {
        return Stream.of("s0me-StronG-P4ss", "st1llAccepT4bl3");
    }
}
