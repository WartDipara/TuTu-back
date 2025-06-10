package com.wart.wartpicturebackend.aop;

import com.wart.wartpicturebackend.annotation.AuthCheck;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.exception.ThrowUtils;
import com.wart.wartpicturebackend.model.entity.User;
import com.wart.wartpicturebackend.model.enums.UserRoleEnum;
import com.wart.wartpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yips
 */
@Aspect
@Component
public class AuthInterceptor {
  @Resource
  private UserService userService;
  
  /**
   * 执行拦截
   * @param joinPoint 切入点
   * @param authCheck 权限注解
   * @return
   */
  @Around("@annotation(authCheck)")
  public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable{
    String mustRole = authCheck.mustRole();
    RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
    // 当前登录用户
    User loginUser = userService.getLoginUser(request);
    UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
    // 不需要权限 直接放行
    if(mustRoleEnum == null){
      return joinPoint.proceed();
    }
    // 下面代码必须有权限才可以通过
    UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
    ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.NO_AUTH_ERROR);
    // 要求管理员权限，但用户没有管理员权限，拒绝
    ThrowUtils.throwIf((UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)), ErrorCode.NO_AUTH_ERROR);
    return joinPoint.proceed();
  }
}
