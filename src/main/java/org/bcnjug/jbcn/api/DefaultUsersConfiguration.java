package org.bcnjug.jbcn.api;

import lombok.extern.slf4j.Slf4j;
import org.bcnjug.jbcn.api.auth.MongodbReactiveUserDetailsService;
import org.bcnjug.jbcn.api.auth.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "api-server.defaults.admin.create", havingValue = "true")
@Slf4j
public class DefaultUsersConfiguration {

    @Bean
    DefaultUsersInitializer defaultUsersInitializer(UsersRepository usersRepository,
                                                    MongodbReactiveUserDetailsService userDetailsService,
                                                    @Value("${api-server.defaults.admin.password}") String adminPassword) {
        return new DefaultUsersInitializer(usersRepository, userDetailsService, adminPassword);
    }

    @Bean
    ApplicationRunner defaultUserHandler(DefaultUsersInitializer initializer) {
        return args -> initializer.setupDefaultUsers()
                .subscribe(user -> logger.info("Default user(s) created"));
    }
}
