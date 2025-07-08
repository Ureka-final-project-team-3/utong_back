package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class FileProcessingException extends BusinessException {
  public FileProcessingException() {
    super(ErrorCode.FILE_PROCESSING_ERROR);
  }

  public FileProcessingException(String message) {
    super(ErrorCode.FILE_PROCESSING_ERROR, message);
  }
}