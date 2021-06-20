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
import java.util.Random;
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

    private User mapUser(org.bcnjug.jbcn.api.auth.User user) {
        return new User(user.getUsername(), user.getPassword(), rolesToAuthorities(user.getRoles()));
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
                .flatMap(mongodbUse -> {
                    final LocalDateTime now = LocalDateTime.now();
                    final var salt = generateSalt();
                    org.bcnjug.jbcn.api.auth.User newUser = new org.bcnjug.jbcn.api.auth.User(
                            mongodbUse.getId(),
                            mongodbUse.getCreatedBy(),
                            mongodbUse.getCreatedOn(),
                            mongodbUse.getUpdatedBy(),
                            now,
                            mongodbUse.getUsername(),
                            mongodbUse.getEmail(),
                            mongodbUse.getRoles(),
                            passwordEncoder.encode(newPassword + salt),
                            salt);
                    return usersRepository.save(newUser);
                })
                .map(this::mapUser);
    }

    public Mono<org.bcnjug.jbcn.api.auth.User> saveUser(String username, String email, Set<String> roles, String password,
                                                        String creator) {
        // TODO Set createdBy & updatedBy
        // NOTE: not sure we really need salting. encoder generated different chain every time
        final var salt = generateSalt();
        String encodedPassword = passwordEncoder.encode(password + salt);

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
                encodedPassword,
                salt);

        return usersRepository.save(user);
    }

    private static final Random random = new Random();

    private String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return new String(salt);
    }
}
