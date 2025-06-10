package com.wart.wartpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import lombok.Data;

/**
 * 用户
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User {
  /**
   * id
   */
  @TableId(type = IdType.ASSIGN_ID) // long id
  private Long id;
  
  /**
   * 账号
   */
  private String userAccount;
  
  /**
   * 密码
   */
  private String userPassword;
  
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
   * 编辑时间，由业务决定
   */
  private Date editTime;
  
  /**
   * 创建时间
   */
  private Date createTime;
  
  /**
   * 更新时间，由数据库决定
   */
  private Date updateTime;
  
  /**
   * 是否删除
   */
  @TableLogic
  private Integer isDelete;
  
  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}