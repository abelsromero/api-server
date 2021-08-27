package org.bcnjug.jbcn.api;

import lombok.extern.slf4j.Slf4j;
import org.bcnjug.jbcn.api.auth.MongodbReactiveUserDetailsService;
import org.bcnjug.jbcn.api.auth.PasswordPolicy;
import org.bcnjug.jbcn.api.auth.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import static org.bcnjug.jbcn.api.DefaultUsersConfiguration.DefaultUserPasswordGenerator.generateDefaultPassword;

@Configuration
@ConditionalOnProperty(value = "api-server.defaults.admin.create", havingValue = "true")
@Slf4j
public class DefaultUsersConfiguration {

    @Bean
    DefaultUsersInitializer defaultUsersInitializer(UsersRepository usersRepository,
                                                    MongodbReactiveUserDetailsService userDetailsService,
                                                    @Value("${api-server.defaults.admin.password:#{null}}") String adminPassword) {

        final String sanitizedPassword = generateDefaultPassword(adminPassword);
        return new DefaultUsersInitializer(usersRepository, userDetailsService, sanitizedPassword);
    }


    @Bean
    ApplicationRunner defaultUserHandler(DefaultUsersInitializer initializer) {
        return args -> initializer.setupDefaultUsers()
                .subscribe(user -> logger.info("Default user(s) created"));
    }

    public final static class DefaultUserPasswordGenerator {

        public static String generateDefaultPassword(String basePassword) {
            if (!StringUtils.hasText(basePassword)) {
                String generatedPassword = PasswordPolicy.PasswordGenerator.generateDefaultPassword();
                logger.info("Using generated security password: " + generatedPassword);
                return generatedPassword;
            }
            return basePassword;
        }
    }
}
