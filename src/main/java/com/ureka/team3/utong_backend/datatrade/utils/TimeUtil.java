package com.ureka.team3.utong_backend.datatrade.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtil {

    private TimeUtil() {} // 인스턴스 생성 방지

    public static long toEpochMillis(LocalDateTime time) {
        if (time == null) throw new IllegalArgumentException();
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
