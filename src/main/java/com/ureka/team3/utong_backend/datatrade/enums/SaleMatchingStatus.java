package com.ureka.team3.utong_backend.datatrade.enums;

public enum SaleMatchingStatus {
    OVER_MAX_PURCHASE_PRICE,
    ALL_MATCHED,
    PART_MATCHED;

    public boolean isAllMatched() {
        return this == ALL_MATCHED;
    }

    public boolean isPartiallyMatched() {
        return this == PART_MATCHED;
    }

    public boolean isWaitingOnly() {
        return this == OVER_MAX_PURCHASE_PRICE;
    }
}
