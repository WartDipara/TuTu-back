package com.wart.wartpicturebackend.model.dto.user;

import com.wart.wartpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 */
@EqualsAndHashCode(callSuper = true) // 继承父类属性
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
  
  private static final long serialVersionUID = -7393640528321218756L;
  /**
   * id
   */
  private Long id;
  
  /**
   * 用户昵称
   */
  private String userName;
  
  /**
   * 账号
   */
  private String userAccount;
  
  /**
   * 简介
   */
  private String userProfile;
  
  /**
   * 用户角色：user/admin/ban
   */
  private String userRole;
  
}

