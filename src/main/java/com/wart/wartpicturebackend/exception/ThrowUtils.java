package com.wart.wartpicturebackend.exception;

/**
 * @author Yips
 */

/**
 * 异常处理工具类
 */
public class ThrowUtils {
  /**
   * 断言类异常处理方法
   * @param condition 条件
   * @param ex        异常
   */
  public static void throwIf(boolean condition,RuntimeException ex){
    if(condition)
      throw ex;
  }
  
  /**
   * 断言类异常处理方法
   * @param condition 条件
   * @param errorCode 错误码
   */
  public static void throwIf(boolean condition,ErrorCode errorCode){
    throwIf(condition,new BusinessException(errorCode));
  }
  
  /**
   * 断言类异常处理方法
   * @param condition 条件
   * @param errorCode 错误码
   * @param message 错误信息
   */
  public static void throwIf(boolean condition,ErrorCode errorCode,String message){
    throwIf(condition,new BusinessException(errorCode,message));
  }
}
