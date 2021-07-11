package org.bcnjug.jbcn.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid username")
public class InvalidUsername extends RuntimeException {
    
}
