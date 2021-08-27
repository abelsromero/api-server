package org.bcnjug.jbcn.api.auth;

import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.util.StringUtils;

public final class PasswordPolicy {

    public static final class Validator {

        public boolean isValid(String password) {
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

    public static final class PasswordGenerator {

        public static String generateDefaultPassword() {
            return addUpperCharacter(KeyGenerators.string().generateKey());
        }

        private static String addUpperCharacter(String generateKey) {
            char[] chars = generateKey.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (Character.isLetter(chars[i])) {
                    char current = chars[i];
                    chars[i] = Character.toUpperCase(current);
                    return new String(chars);
                }
            }
            return generateKey;
        }
    }
}
