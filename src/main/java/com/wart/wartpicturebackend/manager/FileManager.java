package com.wart.wartpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.wart.wartpicturebackend.config.CosClientConfig;
import com.wart.wartpicturebackend.exception.BusinessException;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.exception.ThrowUtils;
import com.wart.wartpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Yips
 */
// 已废弃
@Deprecated
@Service
@Slf4j
public class FileManager {
  @Resource
  private CosClientConfig cosClientConfig;
  @Resource
  private CosManager cosManager;
  
  /**
   * 上传图片
   *
   * @param multipartFile    文件
   * @param uploadPathPrefix 上传路径前缀
   * @return
   */
  public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
    // 校验图片
    validPicture(multipartFile);
    // 图片上传地址
    String uuid = RandomUtil.randomString(16);
    String originFilename = multipartFile.getOriginalFilename();
    String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
        FileUtil.getSuffix(originFilename));
    String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
    File file = null;
    try {
      // 创建临时文件
      file = File.createTempFile(uploadPath, null);
      multipartFile.transferTo(file);
      // 上传图片
      PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
      ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
      // 封装返回结果
      UploadPictureResult uploadPictureResult = new UploadPictureResult();
      int picWidth = imageInfo.getWidth();
      int picHeight = imageInfo.getHeight();
      double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
      uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
      uploadPictureResult.setPicWidth(picWidth);
      uploadPictureResult.setPicHeight(picHeight);
      uploadPictureResult.setPicScale(picScale);
      uploadPictureResult.setPicFormat(imageInfo.getFormat());
      uploadPictureResult.setPicSize(FileUtil.size(file));
      uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
      return uploadPictureResult;
    } catch (Exception e) {
      log.error("failed to upload tencent cos", e);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "upload failed");
    } finally {
      this.deleteTempFile(file);
    }
  }
  
  /**
   * 校验文件
   *
   * @param multipartFile multipart 文件
   */
  public void validPicture(MultipartFile multipartFile) {
    ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
    // 1. 校验文件大小
    long fileSize = multipartFile.getSize();
    final long ONE_M = 1024 * 1024L;
    ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
    // 2. 校验文件后缀
    String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
    // 允许上传的文件后缀
    final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
    ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
  }
  
  /**
   * 删除临时文件
   */
  public void deleteTempFile(File file) {
    if (file == null) {
      return;
    }
    // 删除临时文件
    boolean deleteResult = file.delete();
    if (!deleteResult) {
      log.error("file delete error, filepath = {}", file.getAbsolutePath());
    }
  }
  //todo new function
  
  /**
   * 通过url上传图片
   *
   * @param fileUrl
   * @param uploadPathPrefix
   * @return
   */
  public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
    // todo 校验图片
    validPicture(fileUrl);
    // 图片上传地址
    String uuid = RandomUtil.randomString(16);
//    String originFilename = multipartFile.getOriginalFilename();
    //todo 文件名
    String originFilename = FileUtil.mainName(fileUrl);
    String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
        FileUtil.getSuffix(originFilename));
    String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
    File file = null;
    try {
      // 创建临时文件
      file = File.createTempFile(uploadPath, null);
      // todo 后续处理
      // multipartFile.transferTo(file);
      //下载文件
      HttpUtil.downloadFile(fileUrl, file);
      // 上传图片
      PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
      ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
      // 封装返回结果
      UploadPictureResult uploadPictureResult = new UploadPictureResult();
      int picWidth = imageInfo.getWidth();
      int picHeight = imageInfo.getHeight();
      double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
      uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
      uploadPictureResult.setPicWidth(picWidth);
      uploadPictureResult.setPicHeight(picHeight);
      uploadPictureResult.setPicScale(picScale);
      uploadPictureResult.setPicFormat(imageInfo.getFormat());
      uploadPictureResult.setPicSize(FileUtil.size(file));
      uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
      return uploadPictureResult;
    } catch (Exception e) {
      log.error("failed to upload tencent cos", e);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "upload failed");
    } finally {
      this.deleteTempFile(file);
    }
  }
  
  /**
   * 根据url来校验
   *
   * @param fileUrl
   */
  private void validPicture(String fileUrl) {
    // check empty
    ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "url cannot be empty");
    // check format
    try {
      //用java自带URL对象来解析
      new URL(fileUrl);
    } catch (MalformedURLException e) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "url format incorrect");
    }
    // check protocol
    ThrowUtils.throwIf(!fileUrl.startsWith("http://") || !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "url protocol error");
    // send HEAD request to check exists
    HttpResponse resp = null;
    try {
      resp = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
      // 非正常返回，无需做其他判断 (考虑部分网站没有HEAD请求）
      if (resp.getStatus() != HttpStatus.HTTP_OK)
        return;
      
      //exist
      String contentType = resp.header("Content-type");
      // 不为空再校验
      if (StrUtil.isNotBlank(contentType)) {
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/png", "image/webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(contentType.toLowerCase()), ErrorCode.PARAMS_ERROR, "file type error");
      }
      String contentLength = resp.header("Content-Length");
      if (StrUtil.isNotBlank(contentLength)) {
        try {
          final long ONE_M = 1024 * 1024L;
          ThrowUtils.throwIf(Long.parseLong(contentLength) > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "file size error");
        } catch (NumberFormatException e) {
          throw new BusinessException(ErrorCode.PARAMS_ERROR, "number format error");
        }
      }
    } finally {
      if (resp != null)
        resp.close();
    }
  }
}
