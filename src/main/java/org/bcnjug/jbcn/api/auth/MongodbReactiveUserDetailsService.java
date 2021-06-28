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

import static org.bcnjug.jbcn.api.auth.MongodbReactiveUserDetailsService.SaltGenerator.generateSalt;
import static org.bcnjug.jbcn.api.auth.MongodbReactiveUserDetailsService.SaltGenerator.stringify;

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
                .flatMap(mongodbUser -> {
                    final byte[] salt = generateSalt();
                    final String encodePassword = passwordEncoder.encode(newPassword + stringify(salt));
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
                            encodePassword,
                            salt);
                    return usersRepository.save(newUser);
                })
                .map(this::mapUser);
    }

    public Mono<org.bcnjug.jbcn.api.auth.User> createUser(String username, String email, Set<String> roles, String password,
                                                          String creator) {
        // NOTE: not sure we really need salting. encoder generated different chain every time
        final byte[] salt = generateSalt();
        final String encodedPassword = passwordEncoder.encode(password + stringify(salt));
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

        return usersRepository.insert(user);
    }

    static class SaltGenerator {

        private static final Random random = new Random();

        static byte[] generateSalt() {
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            return salt;
        }

        static String stringify(byte[] salt) {
            return new String(salt);
        }
    }
}
