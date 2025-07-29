package com.ureka.team3.utong_backend.point.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.point.dto.PointChargeRequestDto;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;

public interface PointService {
    PointChargeResponseDto chargePoints(Account account, PointChargeRequestDto requestDto);

    void usePoint(Account account, Long coast);

    void givePoint(Account account, Long point);
}
