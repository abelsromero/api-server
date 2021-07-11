package org.bcnjug.jbcn.api.papers;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PaperState {

    PAPER_STATE_SENT("sent"), // default
    PAPER_STATE_VOTING("voting"),
    PAPER_STATE_ACCEPTED("accepted"),
    PAPER_STATE_REJECTED("rejected"),
    PAPER_STATE_CANCELED("canceled");

    @JsonValue
    private final String state;

    PaperState(String state) {
        this.state = state;
    }

    public static PaperState toPaperState(String state) {
        return Arrays.stream(values())
                .filter(it -> it.getState().equals(state))
                .findFirst()
                .orElse(PAPER_STATE_SENT);
    }

    public static boolean isValid(String state) {
        return Arrays.stream(values())
                .filter(it -> it.getState().equals(state))
                .findFirst()
                .isPresent();
    }

}
