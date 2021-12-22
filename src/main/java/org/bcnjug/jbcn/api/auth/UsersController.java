package org.bcnjug.jbcn.api.auth;

import lombok.Data;
import org.bcnjug.jbcn.api.common.RequiredParameter;
import org.bcnjug.jbcn.api.common.RestCollection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class UsersController {

    private final MongodbReactiveUserDetailsService userDetailsService;
    private final UsersRepository usersRepository;

    public UsersController(MongodbReactiveUserDetailsService userDetailsService,
                           UsersRepository usersRepository) {
        this.userDetailsService = userDetailsService;
        this.usersRepository = usersRepository;
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
                .onErrorMap(DuplicateKeyException.class, e -> new InvalidUsername())
                .map(user -> user.getId())
                .map(id -> Map.of("id", id));
    }

    @GetMapping(value = "/users/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<User>> getUser(@PathVariable String id) {
        return usersRepository.findById(id)
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .switchIfEmpty(Mono.fromSupplier(() -> ResponseEntity.notFound().build()));
    }

    @GetMapping(value = "/users", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<RestCollection> getAllUsers() {
        // TODO Obtain actual total of repository data.
        // For now this works because we are not filtering/paging results
        return usersRepository.findAll()
                .sort(Comparator.comparing(User::getCreatedOn))
                .map(user -> {
                    user.setPassword(null);
                    return user;
                }).collectList()
                .map(users -> new RestCollection(users, users.size()));
    }

    @Data
    static class CreateUserDto {
        private String username;
        private String email;
        private Set<String> roles;
        private String password;
    }
}
