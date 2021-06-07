package org.bcnjug.jbcn.api;

import org.bcnjug.jbcn.api.sec.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    ReactiveUserDetailsService reactiveUserDetailsService(PasswordEncoder passwordEncoder) {
        return new MapReactiveUserDetailsService(
                testUser("alice", Roles.SPEAKER, passwordEncoder),
                testUser("bob", Roles.HELPER, passwordEncoder),
                testUser("echo", Roles.VOTER, passwordEncoder),
                testUser("admin", Roles.ADMIN, passwordEncoder)
        );
    }
    
    private UserDetails testUser(String username, String role, PasswordEncoder passwordEncoder) {
        return User.withUsername(username)
                .password(passwordEncoder.encode("test"))
                .roles(role)
                .build();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {

        return httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .authorizeExchange()
                // .pathMatchers("/whoami").authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }
}
