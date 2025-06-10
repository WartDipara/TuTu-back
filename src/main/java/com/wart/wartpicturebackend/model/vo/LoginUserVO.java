package com.wart.wartpicturebackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Yips
 */

/**
 * 已登录用户视图 脱敏
 */
@Data
public class LoginUserVO implements Serializable {
  
  private static final long serialVersionUID = -6252804963341941737L;
  
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
  
}
