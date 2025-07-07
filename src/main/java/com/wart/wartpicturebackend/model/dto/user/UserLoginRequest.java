package com.wart.wartpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yips
 */

/**
 * 用户登录请求
 */
@Data
public class UserLoginRequest implements Serializable {
  private static final long serialVersionUID = 7750903412535001178L;
  private String userAccount;
  private String userPassword;
}
