package com.wart.wartpicturebackend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.wart.wartpicturebackend.annotation.AuthCheck;
import com.wart.wartpicturebackend.common.BaseResponse;
import com.wart.wartpicturebackend.common.ResultUtils;
import com.wart.wartpicturebackend.constant.UserConstant;
import com.wart.wartpicturebackend.exception.BusinessException;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
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
  
  /**
   * 测试下载
   *
   * @param filepath 文件路径
   * @param response 响应
   */
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @GetMapping("/test/download")
  public void testDownloadFile(String filepath, HttpServletResponse response) {
    COSObjectInputStream cosObjectInput = null;
    try {
      COSObject cosObject = cosManager.getObject(filepath);
      cosObjectInput = cosObject.getObjectContent();
      byte[] bytes = IOUtils.toByteArray(cosObjectInput);
      
      //响应头
      response.setContentType("application/octet-stream;charset=UTF-8");
      response.setHeader("Content-Disposition", "attachment;filename=" + filepath);
      
      //写入响应
      response.getOutputStream().write(bytes);
      response.getOutputStream().flush();
    } catch (Exception e) {
      log.error("file download error, filepath={}", filepath);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "download error");
    }finally {
      //关闭流
      if(cosObjectInput != null)
        IOUtils.release(cosObjectInput, log); //自带的close封装方法
    }
  }
}
