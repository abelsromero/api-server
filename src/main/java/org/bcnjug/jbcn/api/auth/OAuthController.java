package org.bcnjug.jbcn.api.auth;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthController {

    private final ReactiveUserDetailsService userDetailsService;

    public OAuthController(ReactiveUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/oauth/token")
    String getToken(
            @RequestParam(name = "grant_type") String grantType,
            @RequestParam(name = "clientId") String clientId,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password
    ) {

        // TODO authenticate user
        return JwtUtils.createJWS(username, 30 * 60 * 1000);
    }

}
