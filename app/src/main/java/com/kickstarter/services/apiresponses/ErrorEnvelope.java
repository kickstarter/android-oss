package com.kickstarter.services.ApiResponses;

import com.google.common.base.Enums;

import java.util.List;

public class ErrorEnvelope {
  private final List<String> errorMessages;
  private final Integer httpCode;
  private final String ksrCode;

  public enum ErrorCode {
    INVALID_XAUTH_LOGIN,
    TFA_FAILED,
    TFA_REQUIRED,
    UNKNOWN
  }

  private ErrorEnvelope(final List<String> errorMessages, final Integer httpCode, final String ksrCode) {
    this.errorMessages = errorMessages;
    this.httpCode = httpCode;
    this.ksrCode = ksrCode;
  }

  public List<String> errorMessages() {
    return errorMessages;
  }

  public Integer httpCode() {
    return httpCode;
  }

  public ErrorCode ksrCode() {
    return Enums.getIfPresent(ErrorCode.class, ksrCode.toUpperCase()).or(ErrorCode.UNKNOWN);
  }
}
