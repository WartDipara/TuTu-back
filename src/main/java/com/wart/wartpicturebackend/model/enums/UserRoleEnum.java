package com.wart.wartpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @author Yips
 */
@Getter
public enum UserRoleEnum {
  USER("用户", "user"),
  ADMIN("管理员", "admin");
  
  private final String text;
  private final String value;
  
  UserRoleEnum(String text, String value) {
    this.text = text;
    this.value = value;
  }
  
  public static UserRoleEnum getEnumByValue(String value) {
    if (ObjectUtil.isEmpty(value)) {
      return null;
    }
    // 根据值获取枚举
    for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
      if (userRoleEnum.value.equals(value)) {
        return userRoleEnum;
      }
    }
    return null;
  }
}
