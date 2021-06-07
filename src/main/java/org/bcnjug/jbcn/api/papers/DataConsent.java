package org.bcnjug.jbcn.api.papers;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataConsent {

    // e.g. name, surname, id card…
    private boolean identification;
    // e.g. email address…
    private boolean contact;
    // stored in Alf.io
    private boolean financial;

    public static DataConsent denyAll() {
        return new DataConsent(false, false, false);
    }

    public static DataConsent consentAll() {
        return new DataConsent(true, true, true);
    }

}
