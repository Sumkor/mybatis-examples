package com.sumkor.plugin.page;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Page {

    private final int offset;

    private final int limit;

}