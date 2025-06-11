package com.wart.wartpicturebackend.controller;

import com.wart.wartpicturebackend.annotation.AuthCheck;
import com.wart.wartpicturebackend.common.BaseResponse;
import com.wart.wartpicturebackend.common.ResultUtils;
import com.wart.wartpicturebackend.constant.UserConstant;
import com.wart.wartpicturebackend.exception.BusinessException;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @author Yips
 */

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
  @Resource
  private CosManager cosManager;
  
  /**
   * 文件上传 测试接口
   *
   * @return 可访问路径
   */
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @PostMapping("/test/upload")
  public BaseResponse<String> testUploadFile(@RequestParam("file") MultipartFile multipartFile) {
    // 文件目录
    String filename = multipartFile.getOriginalFilename();
    String filepath = String.format("/test/%s", filename);
    File tempFile = null;
    try {
      tempFile = File.createTempFile(filepath, null);
      //转到本地 生成临时文件
      multipartFile.transferTo(tempFile);
      //上传到云服务器
      cosManager.putObject(filepath, tempFile);
      //返回可访问路径
      return ResultUtils.success(filepath);
    } catch (Exception e) {
      log.error("file upload error, filepath={}", filepath);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "upload error");
    } finally {
      if (tempFile != null) {
        //删除临时文件
        boolean delete = tempFile.delete();
        if (!delete) {
          log.error("file delete error, filepath={}", filepath);
        }
      }
    }
  }
}
