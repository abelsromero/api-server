package org.bcnjug.jbcn.api.auth;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MongodbReactiveUserDetailsService implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public MongodbReactiveUserDetailsService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return usersRepository.findByUsername(username)
                .map(this::mapUser);
    }

    private UserDetails mapUser(org.bcnjug.jbcn.api.auth.User user) {
        return User.withUsername(user.getUsername())
                .authorities(rolesToAuthorities(user.getRoles()))
                .password(user.getPassword())
                .build();
    }

    private List<SimpleGrantedAuthority> rolesToAuthorities(Set<String> roles) {
        if (roles == null) return List.of();

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
        // TODO Set updatedBy
        return usersRepository.findByUsername(user.getUsername())
                .flatMap(mongodbUser -> {
                    final String encodePassword = passwordEncoder.encode(newPassword);
                    final LocalDateTime now = LocalDateTime.now();

                    org.bcnjug.jbcn.api.auth.User newUser = new org.bcnjug.jbcn.api.auth.User(
                            mongodbUser.getId(),
                            mongodbUser.getCreatedBy(),
                            mongodbUser.getCreatedOn(),
                            mongodbUser.getUpdatedBy(),
                            now,
                            mongodbUser.getUsername(),
                            mongodbUser.getEmail(),
                            mongodbUser.getRoles(),
                            encodePassword);
                    return usersRepository.save(newUser);
                })
                .map(this::mapUser);
    }

    public Mono<org.bcnjug.jbcn.api.auth.User> createUser(String username, String email, Set<String> roles, String password,
                                                          String creator) {
        final String encodedPassword = passwordEncoder.encode(password);
        final LocalDateTime now = LocalDateTime.now();

        org.bcnjug.jbcn.api.auth.User user = new org.bcnjug.jbcn.api.auth.User(
                null,
                creator,
                now,
                creator,
                now,
                username,
                email,
                roles,
                encodedPassword);

        return usersRepository.insert(user);
    }
}
