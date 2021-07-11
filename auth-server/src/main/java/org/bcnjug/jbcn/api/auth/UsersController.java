package org.bcnjug.jbcn.api.auth;

import lombok.Data;
import org.bcnjug.jbcn.api.common.RequiredParameter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@RestController
public class UsersController {

    private final MongodbReactiveUserDetailsService userDetailsService;

    public UsersController(MongodbReactiveUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, String>> create(@RequestBody CreateUserDto userDto, Principal principal) {
        // NOTE: Bean validation generated messages leaking internal and required too much complexity to customize them
        if (!StringUtils.hasText(userDto.getUsername()))
            throw new RequiredParameter("username");

        if (!StringUtils.hasText(userDto.getEmail()))
            throw new RequiredParameter("email");

        if (!StringUtils.hasText(userDto.getPassword()))
            throw new RequiredParameter("password");

        return userDetailsService.createUser(userDto.getUsername(), userDto.getEmail(), userDto.getRoles(), userDto.getPassword(), principal.getName())
                .onErrorMap(DuplicateKeyException.class, e -> {
                    System.out.println("ssss");
                    return new InvalidUsername();
                })
                .map(user -> user.getId())
                .map(id -> Map.of("id", id));
    }

    @Data
    static class CreateUserDto {
        private String username;
        private String email;
        private Set<String> roles;
        private String password;
    }
}
