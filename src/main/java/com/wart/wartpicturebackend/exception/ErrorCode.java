package com.wart.wartpicturebackend.exception;

import lombok.Getter;

/**
 * @author Yips
 */
@Getter
public enum ErrorCode {
  SUCCESS(0, "ok"),
  PARAMS_ERROR(40000, "request params are incorrect"),
  NOT_LOGIN_ERROR(40100, "not sign in"),
  NO_AUTH_ERROR(40101, "no authority"),
  NOT_FOUND_ERROR(40400, "request data does not exist"),
  FORBIDDEN_ERROR(40300, "access denied"),
  SYSTEM_ERROR(50000, "internal system error"),
  OPERATION_ERROR(50001, "operation failed");
  
  /**
   * 状态码
   */
  private final int code;
  
  /**
   * 信息
   */
  private final String message;
  
  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }
  
}
