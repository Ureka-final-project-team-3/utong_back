package com.ureka.team3.utong_backend.datatrade.dto;

import java.util.List;

public interface MatchingResult {
    Long getRequest();
    Long getUsed();
    Long getRemain();
    List<TradeMatch> getMatchList();
}
