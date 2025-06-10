package com.wart.wartpicturebackend.exception;

import lombok.Getter;

/**
 * @author Yips
 */

/**
 * 自定义异常
 */
@Getter
public class BusinessException extends RuntimeException{
  /**
   * 错误码
   */
  private final int code;
  
  /**
   * 错误信息
   * @param code
   * @param message
   */
  public BusinessException(int code,String message){
    super(message);
    this.code = code;
  }
  public BusinessException(ErrorCode errorCode){
    super(errorCode.getMessage());
    this.code = errorCode.getCode();
  }
  public BusinessException(ErrorCode errorCode,String message){
    super(message);
    this.code = errorCode.getCode();
  }
}
