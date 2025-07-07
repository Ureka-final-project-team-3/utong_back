package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class InvalidFileExtensionException extends BusinessException {
  public InvalidFileExtensionException() {
    super(ErrorCode.INVALID_FILE_EXTENSION);
  }

  public InvalidFileExtensionException(String message) {
    super(ErrorCode.INVALID_FILE_EXTENSION, message);
  }
}