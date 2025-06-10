package com.wart.wartpicturebackend.model.vo;

/**
 * @author Yips
 */

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图 脱敏
 */
@Data
public class UserVO implements Serializable {
  private static final long serialVersionUID = -2568734594102642663L;
  /**
   * id
   */
  private Long id;
  
  /**
   * 账号
   */
  private String userAccount;
  
  /**
   * 用户昵称
   */
  private String userName;
  
  /**
   * 用户头像地址(并非直接存文件)
   */
  private String userAvatar;
  
  /**
   * 用户简介
   */
  private String userProfile;
  
  /**
   * 用户角色：user/admin
   */
  private String userRole;
  
  
  /**
   * 创建时间
   */
  private Date createTime;
  
}
