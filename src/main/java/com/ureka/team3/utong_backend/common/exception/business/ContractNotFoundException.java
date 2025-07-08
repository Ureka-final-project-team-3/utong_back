package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class ContractNotFoundException extends BusinessException {
  public ContractNotFoundException() {
    super(ErrorCode.CONTRACT_NOT_FOUND);
  }

  public ContractNotFoundException(String message) {
    super(ErrorCode.CONTRACT_NOT_FOUND, message);
  }
}