package org.bcnjug.jbcn.api.auth;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MongodbReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final UsersRepository usersRepository;

    public MongodbReactiveUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return usersRepository.findByUsername(username)
                .map(user -> new User(user.getUsername(), null, rolesToAuthorities(user.getRoles())));
    }

    @NotNull
    private List<SimpleGrantedAuthority> rolesToAuthorities(Set<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
