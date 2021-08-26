package org.bcnjug.jbcn.api.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.PASSWORD;

@RestController
public class OAuthController {

    private static final String ROLE_PREFIX = "ROLE_";

    private final UsersRepository usersRepository;
    private final ReactiveUserDetailsService userDetailsService;
    private final JwtGenerator jwtGenerator;
    private final PasswordEncoder passwordEncoder;

    @Value("${api-server.auth.client-id}")
    private String clientId;
    @Value("${api-server.jwt.ttl-millis}")
    private Integer tokenTtlMillis;

    public OAuthController(UsersRepository usersRepository,
                           ReactiveUserDetailsService userDetailsService,
                           JwtGenerator jwtGenerator,
                           PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.userDetailsService = userDetailsService;
        this.jwtGenerator = jwtGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/oauth/token")
    public Mono<AuthorizationToken> getToken(
            @RequestParam(name = "grant_type") String grantType,
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password
    ) {

        if (!StringUtils.hasText(grantType) || !PASSWORD.getValue().equals(grantType)) {
            throw new RuntimeException("Unsupported grant_type");
        }
        if (!StringUtils.hasText(clientId) || !this.clientId.equals(clientId)) {
            throw new RuntimeException("Unsupported client_id");
        }

        return usersRepository.findByUsername(username)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFound())))
                .flatMap(user -> isCorrectPassword(password, user) ? Mono.just(user) : Mono.empty())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFound())))
                .flatMap(user -> userDetailsService.findByUsername(user.getUsername()))
                .map(userDetails -> userDetails.getAuthorities()
                        .stream()
                        .filter(grantedAuthority -> grantedAuthority.getAuthority().startsWith(ROLE_PREFIX))
                        .map(grantedAuthority -> removePrefix(grantedAuthority))
                        .collect(Collectors.toSet()))
                .map(roles -> {
                    String token = jwtGenerator.createJWS(username, roles, tokenTtlMillis);
                    return new AuthorizationToken(token, "bearer", tokenTtlMillis);
                });
    }

    private boolean isCorrectPassword(String password, User user) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    private String removePrefix(GrantedAuthority grantedAuthority) {
        String authority = grantedAuthority.getAuthority();
        return authority.substring(authority.indexOf("_") + 1);
    }

    @lombok.Value
    class AuthorizationToken {
        String access_token;
        String token_type;
        Integer expires_in;
    }
}
