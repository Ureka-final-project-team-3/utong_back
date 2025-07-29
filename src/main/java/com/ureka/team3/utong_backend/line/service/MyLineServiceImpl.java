package com.ureka.team3.utong_backend.line.service;


import com.ureka.team3.utong_backend.user.entity.User;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.line.repository.LineRepository;
import com.ureka.team3.utong_backend.user.repository.UserRepository;
import com.ureka.team3.utong_backend.common.exception.business.LineNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.dto.MyLineResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyLineServiceImpl implements MyLineService {

    private final LineRepository lineRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public List<MyLineResponseDto> getMyLines(String accountId, String defaultLineId) {
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(UserNotFoundException::new);

        List<Line> lines = lineRepository.findAllByUserId(user.getId());

        return lines.stream()
                .map(line -> MyLineResponseDto.builder()
                        .lineId(line.getId())
                        .phoneNumber(line.getPhoneNumber())
                        .isDefault(line.getId().equals(defaultLineId))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<MyLineResponseDto> setDefaultLine(String accountId, String lineId) {
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(UserNotFoundException::new);

        boolean owned = lineRepository.existsByIdAndUserId(lineId, user.getId());
        if (!owned) throw new LineNotFoundException();

        accountRepository.updateDefaultLine(accountId, lineId);

        return getMyLines(accountId, lineId);
    }
}