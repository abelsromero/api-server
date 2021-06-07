package org.bcnjug.jbcn.api;

import lombok.SneakyThrows;
import org.bcnjug.jbcn.api.auth.MongodbReactiveUserDetailsService;
import org.bcnjug.jbcn.api.auth.UsersRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private static final String RESOURCES = System.getenv("HOME") + "/jbcnconf/api-server/src/main/resources/";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    ReactiveUserDetailsService reactiveUserDetailsService(UsersRepository usersRepository) {
        return new MongodbReactiveUserDetailsService(usersRepository);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {

        return httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .oauth2ResourceServer(resourceServer -> {
                    resourceServer.jwt(jwtSpec -> {
                        jwtSpec
                                .publicKey(readPublicKey())
                                .jwtAuthenticationConverter(jwt -> {
                                    List<String> claimRoles = jwt.getClaimAsStringList("roles");
                                    if (claimRoles != null) {
                                        List<SimpleGrantedAuthority> authorities = claimRoles
                                                .stream()
                                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                                .collect(Collectors.toList());

                                        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
                                    } else {
                                        return Mono.just(new JwtAuthenticationToken(jwt, List.of()));
                                    }
                                });
                    });
                })
                .authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/papers/**").authenticated()
                .pathMatchers(HttpMethod.POST, "/oauth/token").permitAll()
                .anyExchange().authenticated()
                .and()
                .build();
    }


    @SneakyThrows
    public static RSAPublicKey readPublicKey() {
        String file = RESOURCES + "public_key.pem";

        String key = new String(Files.readAllBytes(Path.of(file)), Charset.defaultCharset());

        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] decode = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decode);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @SneakyThrows
    public static RSAPrivateKey readPrivateKey() {
        String file = RESOURCES + "private_key.pem";

        String key = new String(Files.readAllBytes(Path.of(file)), Charset.defaultCharset());

        String publicKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] decode = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decode);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

}
