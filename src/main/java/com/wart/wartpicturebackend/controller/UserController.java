package com.wart.wartpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wart.wartpicturebackend.annotation.AuthCheck;
import com.wart.wartpicturebackend.common.BaseResponse;
import com.wart.wartpicturebackend.common.DeleteRequest;
import com.wart.wartpicturebackend.common.ResultUtils;
import com.wart.wartpicturebackend.constant.UserConstant;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.exception.ThrowUtils;
import com.wart.wartpicturebackend.model.dto.*;
import com.wart.wartpicturebackend.model.entity.User;
import com.wart.wartpicturebackend.model.vo.LoginUserVO;
import com.wart.wartpicturebackend.model.vo.UserVO;
import com.wart.wartpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
  
  /**
   * 新增用户
   * @param userAddRequest 新增用户信息(管理员）
   * @return 用户ID
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
    ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
    User user = new User();
    BeanUtil.copyProperties(userAddRequest, user);
    // 默认密码
    final String DEFAULT_PASSWORD = "12345678";
    user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));
    //插入数据库
    boolean saveResult = userService.save(user);
    ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR,  "注册用户失败");
    return ResultUtils.success(user.getId());
  }
  
  /**
   * 根据id获取用户（仅管理员）
   * @param id
   * @return
   */
  @GetMapping("/get")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<User> getUserById(long id){
    ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
    User user = userService.getById(id);
    ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
    return ResultUtils.success(user);
  }
  
  /**
   * 根据id获取包装类
   * @param id
   * @return
   */
  @GetMapping("/get/vo")
  public BaseResponse<UserVO> getUserVOById(long id){
    BaseResponse<User> response = getUserById(id);
    User user = response.getData();
    return ResultUtils.success(userService.getUserVO(user));
  }
  
  /**
   * 删除用户 (仅限管理员）
   * @param deleteRequest 删除用户请求体
   * @return 删除结果
   */
  @PostMapping("/delete")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
    ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
    User user = new User();
    boolean b = userService.removeById(deleteRequest.getId());
    return ResultUtils.success(b);
  }
  
  /**
   * 更新用户信息（仅限管理员）
   * @param userUpdateRequest 更新用户信息
   * @return 更新结果
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
    ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
    User user = new User();
    BeanUtil.copyProperties(userUpdateRequest, user);
    boolean b = userService.updateById(user);
    ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }
  
  /**
   * 分页获取请求封装列表（仅管理员）
   * @param userQueryRequest 查询条件
   * @return 请求封装列表
   */
  @PostMapping("/list/page/vo")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
    ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
    long current = userQueryRequest.getCurrent();
    long pageSize = userQueryRequest.getPageSize();
    Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
    //脱敏操作
    Page<UserVO> userVOPage = new Page<>(current,pageSize,  userPage.getTotal());
    List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
    return ResultUtils.success(userVOPage.setRecords(userVOList));
  }
  
}
