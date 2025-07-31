package com.izza.search.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BeopjungDongType {
    SIDO(2),
    SIG(5),
    EMD(8),
    RI(10);

    private final int codeLength;
}