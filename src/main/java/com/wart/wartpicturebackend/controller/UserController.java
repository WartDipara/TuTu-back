package com.wart.wartpicturebackend.controller;

import com.wart.wartpicturebackend.common.BaseResponse;
import com.wart.wartpicturebackend.common.ResultUtils;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.exception.ThrowUtils;
import com.wart.wartpicturebackend.model.dto.UserLoginRequest;
import com.wart.wartpicturebackend.model.dto.UserRegisterRequest;
import com.wart.wartpicturebackend.model.entity.User;
import com.wart.wartpicturebackend.model.vo.LoginUserVO;
import com.wart.wartpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yips
 */
@RestController
@RequestMapping("/user")
public class UserController {
  @Resource
  private UserService userService;
  
  /**
   * 用户注册接口
   *
   * @param userRegisterRequest 用户注册信息
   * @return 用户id
   */
  @PostMapping("/register")
  public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
    String userAccount = userRegisterRequest.getUserAccount();
    String userPassword = userRegisterRequest.getUserPassword();
    String checkPassword = userRegisterRequest.getCheckPassword();
    long result = userService.userRegister(userAccount, userPassword, checkPassword);
    return ResultUtils.success(result);
  }
  
  /**
   * 用户登录接口
   *
   * @param userLoginRequest 用户登录信息
   * @return 登录用户信息
   */
  @PostMapping("/login")
  public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
    String userAccount = userLoginRequest.getUserAccount();
    String userPassword = userLoginRequest.getUserPassword();
    LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
    return ResultUtils.success(loginUserVO);
  }
  
  /**
   * 获取当前登录用户信息
   *
   * @return 登录用户信息
   */
  @GetMapping("/get/login")
  public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    return ResultUtils.success(userService.getLoginUserVO(loginUser));
  }
  
  /**
   * 用户登出
   *
   * @return boolean
   */
  @PostMapping("/logout")
  public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
    ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
    return ResultUtils.success(userService.userLogout(request));
  }
}
