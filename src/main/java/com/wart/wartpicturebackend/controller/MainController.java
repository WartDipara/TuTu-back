package com.wart.wartpicturebackend.controller;

import com.wart.wartpicturebackend.common.BaseResponse;
import com.wart.wartpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yips
 */
@RestController
@RequestMapping("/")
public class MainController {
  /**
   * 测试接口
   * @return 返回ok
   */
  @GetMapping("/test")
  public BaseResponse<String> test(){
    return ResultUtils.success("ok");
  }
}
