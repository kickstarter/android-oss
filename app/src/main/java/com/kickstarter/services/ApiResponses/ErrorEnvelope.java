package com.kickstarter.services.ApiResponses;

import com.google.common.base.Enums;

import java.util.List;

public class ErrorEnvelope {
  private final List<String> error_messages;
  private final Integer http_code;
  private final String ksr_code;

  public enum ErrorCode {
    INVALID_XAUTH_LOGIN,
    TFA_FAILED,
    TFA_REQUIRED,
    UNKNOWN
  }

  private ErrorEnvelope(final List<String> error_messages, final Integer http_code, final String ksr_code) {
    this.error_messages = error_messages;
    this.http_code = http_code;
    this.ksr_code = ksr_code;
  }

  public List<String> errorMessages() {
    return error_messages;
  }

  public Integer httpCode() {
    return http_code;
  }

  public ErrorCode ksrCode() {
    return Enums.getIfPresent(ErrorCode.class, ksr_code.toUpperCase()).or(ErrorCode.UNKNOWN);
  }
}
