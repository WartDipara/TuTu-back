package com.wart.wartpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.repository.AbstractRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wart.wartpicturebackend.constant.UserConstant;
import com.wart.wartpicturebackend.exception.BusinessException;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.exception.ThrowUtils;
import com.wart.wartpicturebackend.model.dto.UserAddRequest;
import com.wart.wartpicturebackend.model.dto.UserQueryRequest;
import com.wart.wartpicturebackend.model.entity.User;
import com.wart.wartpicturebackend.model.enums.UserRoleEnum;
import com.wart.wartpicturebackend.model.vo.LoginUserVO;
import com.wart.wartpicturebackend.model.vo.UserVO;
import com.wart.wartpicturebackend.service.UserService;
import com.wart.wartpicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Wart
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-06-10 12:36:56
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
  
  /**
   * 注册
   * 数据库设置了唯一索引，所以不加事务也不会发生数据冲突，但是可能会导致报错信息不好看
   *
   * @param userAccount   用户账户
   * @param userPassword  用户密码
   * @param checkPassword 密码校验
   * @return 用户ID
   */
  @Override
  public long userRegister(String userAccount, String userPassword, String checkPassword) {
    //1.校验 也可以用ThrowUtils工具类，之后都会使用ThrowUtils工具类来抛
    if (StrUtil.hasBlank(userAccount, userPassword, checkPassword))
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "empty params");
    if (userAccount.length() < 4)
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "user account is too short");
    if (userPassword.length() < 8)
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "user password is too short");
    if (!userPassword.equals(checkPassword))
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "two entered passwords do not match");
    //2.检查重复
    Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserAccount, userAccount));
    if (count > 0)
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "duplicate account");
    //3.加密
    String encryptPassword = getEncryptPassword(userPassword);
    //4，插入数据
    User user = new User();
    user.setUserAccount(userAccount);
    user.setUserPassword(encryptPassword);
    user.setUserName(userAccount);
    user.setUserRole(UserRoleEnum.USER.getValue());
    boolean saveResult = this.save(user);
    if (!saveResult)
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "registration failed, a database error occurred");
    return user.getId();
  }
  
  /**
   * 登录
   *
   * @param userAccount  用户账号
   * @param userPassword 用户密码
   * @param request      需要保存当前session信息
   * @return 登录用户信息
   */
  @Override
  public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
    //1.校验 这里使用自己写的工具类来抛（更简短且优雅） 更直接的参考注册部分
    ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "empty params");
    ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "user account error");
    ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "user password error");
    //2.对用户密码进行加密
    String encryptPassword = getEncryptPassword(userPassword);
    //3.匹配加密后的密码与数据库是否存在
    User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>()
        .eq(User::getUserAccount, userAccount)
        .eq(User::getUserPassword, encryptPassword)
    );
    //抛出异常
    if (user == null) {
      log.info("user login failed, userAccount cannot match userPassword");
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "user does not exist or password is incorrect");
    }
    //4.保存用户登录态
    request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
    return this.getLoginUserVO(user);
  }
  
  /**
   * 获取当前登录用户
   *
   * @param request 请求
   * @return 用户
   */
  @Override
  public User getLoginUser(HttpServletRequest request) {
    // 判断是否登录
    Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    User curUser = (User) attribute;
    ThrowUtils.throwIf((curUser == null || curUser.getId() == null), ErrorCode.NOT_LOGIN_ERROR);
    // 考虑到数据库数据可能有变动,比如改名了（从数据库再获取一次，用性能换取稳定性）
    curUser = this.getById(curUser.getId());
    // 如果用户被删除了或者找不到了，抛出异常
    ThrowUtils.throwIf((curUser == null || curUser.getIsDelete() == 1), ErrorCode.NOT_LOGIN_ERROR);
    return curUser;
  }
  
  @Override
  public boolean userLogout(HttpServletRequest request) {
    // 判断是否登录
    Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    User curUser = (User) attribute;
    ThrowUtils.throwIf(curUser == null, ErrorCode.OPERATION_ERROR, "not sign in");
    // 移除登录
    request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
    return true;
  }
  
  /**
   * 加密方法
   *
   * @param userPassword 用户密码
   * @return 加密后的密码
   */
  @Override
  public String getEncryptPassword(String userPassword) {
    // 加盐操作 混淆密码
    final String SALT = "MirrorKafka";
    return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
  }
  
  /**
   * 用户转换逻辑
   *
   * @param user 用户信息
   * @return 脱敏后的用户信息
   */
  @Override
  public LoginUserVO getLoginUserVO(User user) {
    if (user == null) {
      return null;
    }
    LoginUserVO loginUserVO = new LoginUserVO();
    BeanUtil.copyProperties(user, loginUserVO);
    return loginUserVO;
  }
  
  /**
   * 用户转换逻辑
   *
   * @param user 用户信息
   * @return
   */
  @Override
  public UserVO getUserVO(User user) {
    if (user == null) {
      return null;
    }
    UserVO UserVO = new UserVO();
    BeanUtil.copyProperties(user, UserVO);
    return UserVO;
  }
  
  /**
   * 获取脱敏用户列表
   *
   * @param userList 用户信息
   * @return 脱敏用户列表
   */
  @Override
  public List<UserVO> getUserVOList(List<User> userList) {
    if (CollUtil.isEmpty(userList))
      return new ArrayList<>();
    return userList.stream().map(this::getUserVO).collect(Collectors.toList());
  }
  
  /**
   * 获取查询条件
   *
   * @param userQueryRequest 用户查询条件
   * @return
   */
  @Override
  public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
    //校验
    ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "request params are empty");
    //获取对象
    Long id = userQueryRequest.getId();
    String userName = userQueryRequest.getUserName();
    String userAccount = userQueryRequest.getUserAccount();
    String userProfile = userQueryRequest.getUserProfile();
    String userRole = userQueryRequest.getUserRole();
    int current = userQueryRequest.getCurrent();
    int pageSize = userQueryRequest.getPageSize();
    String sortField = userQueryRequest.getSortField();
    String sortOrder = userQueryRequest.getSortOrder();
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq(id != null && id > 0, "id", id);
    queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
    queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
    queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
    queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
    queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),  sortOrder.equals("ascend"), sortField);
    return queryWrapper;
  }
}




