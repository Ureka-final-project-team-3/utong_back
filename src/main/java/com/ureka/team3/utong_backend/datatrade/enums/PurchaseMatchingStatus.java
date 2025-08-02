package com.ureka.team3.utong_backend.datatrade.enums;

public enum PurchaseMatchingStatus implements MatchingStatus {
    UNDER_MINIMUM_SALE_PRICE,
    ALL_MATCHED,
    PART_MATCHED;

    public boolean isAllMatched() {
        return this == ALL_MATCHED;
    }

    public boolean isPartiallyMatched() {
        return this == PART_MATCHED;
    }

    public boolean isWaitingOnly() {
        return this == UNDER_MINIMUM_SALE_PRICE;
    }
}
