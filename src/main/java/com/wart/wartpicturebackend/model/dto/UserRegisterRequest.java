package com.wart.wartpicturebackend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yips
 */
@Data
public class UserRegisterRequest implements Serializable {
  private static final long serialVersionUID = 7148403667236162246L;
  //  用户账号
  private String userAccount;
  //  用户密码
  private String userPassword;
  //  用户校验密码
  private String checkPassword;
  
  
}
