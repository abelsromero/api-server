package org.bcnjug.jbcn.api;

import lombok.extern.slf4j.Slf4j;
import org.bcnjug.jbcn.api.auth.MongodbReactiveUserDetailsService;
import org.bcnjug.jbcn.api.auth.Role;
import org.bcnjug.jbcn.api.auth.User;
import org.bcnjug.jbcn.api.auth.UsersRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
public class DefaultUsersInitializer {

    private final UsersRepository usersRepository;
    private final MongodbReactiveUserDetailsService userDetailsService;
    private final String adminPassword;

    public DefaultUsersInitializer(UsersRepository usersRepository,
                                   MongodbReactiveUserDetailsService userDetailsService,
                                   String adminPassword) {
        this.adminPassword = adminPassword;
        this.usersRepository = usersRepository;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Returns admin users.
     * Additionally, if none is found, it will create 1 with default parameters.
     */
    public Flux<User> setupDefaultUsers() {
        return usersRepository.findByRolesContains(Role.ADMIN.toString())
                .switchIfEmpty(Mono.defer(() -> {
                    final String adminUsername = "dadmin";
                    return userDetailsService.saveUser(
                            adminUsername,
                            "admin@no-reply.com",
                            Set.of(Role.ADMIN.toString()),
                            adminPassword,
                            adminUsername);
                }));
    }

}
