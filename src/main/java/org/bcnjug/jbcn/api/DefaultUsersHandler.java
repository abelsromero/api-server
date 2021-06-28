package org.bcnjug.jbcn.api;

import lombok.extern.slf4j.Slf4j;
import org.bcnjug.jbcn.api.auth.MongodbReactiveUserDetailsService;
import org.bcnjug.jbcn.api.auth.Role;
import org.bcnjug.jbcn.api.auth.User;
import org.bcnjug.jbcn.api.auth.UsersRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Component
@ConditionalOnProperty(value = "defaults.admin.create", havingValue = "true")
public class DefaultUsersHandler implements ApplicationRunner {

    private final DefaultUsersInitializer usersHandler;

    public DefaultUsersHandler(DefaultUsersInitializer usersHandler) {
        this.usersHandler = usersHandler;
    }


    @Override
    public void run(ApplicationArguments args) {

        usersHandler.setupDefaultUsers()
                .subscribe(user -> logger.info("Default user(s) created"));
    }


    @Component
    public static final class DefaultUsersInitializer {

        final UsersRepository usersRepository;
        private final MongodbReactiveUserDetailsService userDetailsService;

        public DefaultUsersInitializer(UsersRepository usersRepository, MongodbReactiveUserDetailsService userDetailsService) {
            this.usersRepository = usersRepository;
            this.userDetailsService = userDetailsService;
        }

        /**
         * Returns admin users.
         * Additionally, if none is found, it will create 1 with default parameters.
         */
        // TODO obtain password from injected properties
        public Flux<User> setupDefaultUsers() {
            return usersRepository.findByRolesContains(Role.ADMIN.toString())
                    .switchIfEmpty(Mono.defer(() -> {
                        final String adminUsername = "admin";
                        return userDetailsService.saveUser(
                                adminUsername,
                                "admin@no-reply.com",
                                Set.of(Role.ADMIN.toString()),
                                "12345678",
                                adminUsername);
                    }));
        }
    }
}
