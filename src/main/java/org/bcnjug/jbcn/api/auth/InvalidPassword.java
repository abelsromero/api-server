package org.bcnjug.jbcn.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid password: must be 12 chars min, contain characters & numbers")
public class InvalidPassword extends RuntimeException {
    
}
