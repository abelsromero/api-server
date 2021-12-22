package org.bcnjug.jbcn.api.common;

import lombok.Getter;

import java.util.List;

@Getter
public class RestCollection<T> {

    final List<T> items;
    // Total number of elements (regardless of limits)
    final int total;

    public RestCollection(List<T> items, int total) {
        this.items = items;
        this.total = total;
    }
}
