package com.wart.wartpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户新增请求
 */
@Data
public class UserAddRequest implements Serializable {
  
  private static final long serialVersionUID = 6298561265474795081L;
  /**
   * 用户昵称
   */
  private String userName;
  
  /**
   * 账号
   */
  private String userAccount;
  
  /**
   * 用户头像
   */
  private String userAvatar;
  
  /**
   * 用户简介
   */
  private String userProfile;
  
  /**
   * 用户角色: user, admin
   */
  private String userRole;
  
}

