package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class FileTooLargeException extends BusinessException {
  public FileTooLargeException() {
    super(ErrorCode.FILE_TOO_LARGE);
  }

  public FileTooLargeException(String message) {
    super(ErrorCode.FILE_TOO_LARGE, message);
  }
}