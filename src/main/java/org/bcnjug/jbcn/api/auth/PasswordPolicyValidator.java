package org.bcnjug.jbcn.api.auth;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PasswordPolicyValidator {

    boolean isValid(String password) {
        if (!StringUtils.hasText(password))
            return false;

        if (password.length() < 12)
            return false;

        if (!password.matches(".*\\d.*"))
            return false;

        if (!password.matches(".*\\p{Upper}.*"))
            return false;

        if (!password.matches(".*\\p{Lower}.*"))
            return false;

        return true;
    }
}
