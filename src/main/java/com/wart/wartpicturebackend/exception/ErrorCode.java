package com.wart.wartpicturebackend.exception;

import lombok.Getter;

/**
 * @author Yips
 */
@Getter
public enum ErrorCode {
  SUCCESS(0, "ok"),
  PARAMS_ERROR(40000, "Request Params are Incorrect"),
  NOT_LOGIN_ERROR(40100, "Not Sign In"),
  NO_AUTH_ERROR(40101, "No Authorization"),
  NOT_FOUND_ERROR(40400, "Request Data Does Not Exist"),
  FORBIDDEN_ERROR(40300, "Access Denied"),
  SYSTEM_ERROR(50000, "Internal System Error"),
  OPERATION_ERROR(50001, "Operation Failed");
  
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
