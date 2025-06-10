package com.wart.wartpicturebackend.service;

import com.wart.wartpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wart.wartpicturebackend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Wart
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-06-10 12:36:56
 */
public interface UserService extends IService<User> {
  /**
   * 用户注册
   *
   * @param userAccount   用户账户
   * @param userPassword  用户密码
   * @param checkPassword 密码校验
   * @return 新用户id
   */
  long userRegister(String userAccount, String userPassword, String checkPassword);
  
  /**
   * 用户登录
   *
   * @param userAccount  用户账号
   * @param userPassword 用户密码
   * @param request      需要保存当前session信息
   * @return 脱敏后的登录信息
   */
  LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);
  
  /**
   *  获取当前登录用户
   *
   * @param request 请求
   * @return 当前登录用户
   */
  User getLoginUser(HttpServletRequest request);
  
  /**
   * 用户登出
   * @param request 请求
   * @return 当前登录用户信息
   */
  boolean userLogout(HttpServletRequest request);
  
  /**
   * 获取加密密码
   *
   * @param userPassword 用户密码
   * @return 加密后的密码
   */
  String getEncryptPassword(String userPassword);
  
  /**
   * 将用户转换成脱敏后的用户信息
   *
   * @param user 用户信息
   * @return 脱敏后的用户信息
   */
  LoginUserVO getLoginUserVO(User user);
}
