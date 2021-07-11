package org.bcnjug.jbcn.api.model;

import java.time.LocalDateTime;

public interface Tracked {

    String getCreatedBy();

    String getUpdatedBy();

    LocalDateTime getCreatedOn();

    LocalDateTime getUpdatedOn();
}
