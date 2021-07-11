package org.bcnjug.jbcn.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class RequiredParameter extends RuntimeException {

    public RequiredParameter(String name) {
        super("Missing required parameter: " + name);
    }
}
