package org.bcnjug.jbcn.api.sec;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String VOTER = "VOTER";
    public static final String HELPER = "HELPER";
    public static final String SPEAKER = "SPEAKER";
    public static final String SPONSOR = "SPONSOR";

}
