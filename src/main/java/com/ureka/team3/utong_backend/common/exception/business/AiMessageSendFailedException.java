package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class AiMessageSendFailedException extends BusinessException {
  public AiMessageSendFailedException() {
    super(ErrorCode.AI_MESSAGE_SEND_FAILED);
  }

  public AiMessageSendFailedException(String customMessage) {
    super(ErrorCode.AI_MESSAGE_SEND_FAILED, customMessage);
  }

  public AiMessageSendFailedException(Throwable cause) {
    super(ErrorCode.AI_MESSAGE_SEND_FAILED, cause.getMessage());
  }
}