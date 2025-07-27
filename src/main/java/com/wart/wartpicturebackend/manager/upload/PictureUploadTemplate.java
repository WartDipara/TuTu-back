package com.wart.wartpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.transfer.Upload;
import com.wart.wartpicturebackend.config.CosClientConfig;
import com.wart.wartpicturebackend.exception.BusinessException;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.manager.CosManager;
import com.wart.wartpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {
  @Resource
  protected CosManager cosManager;
  @Resource
  protected CosClientConfig cosClientConfig;
  
  /**
   * 模板 定义上传流程
   * @param inputSource
   * @param uploadPathPrefix
   * @return
   */
  public final UploadPictureResult uploadPicture(Object inputSource,String uploadPathPrefix){
    //校验
    validPicture(inputSource);
    
    //上传路径
    String uuid = RandomUtil.randomString(16);
    String originFilename= getOriginFilename(inputSource);
    String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid, FileUtil.getSuffix(originFilename));
    String uploadPath = String.format("%s%s",uploadPathPrefix,uploadFilename);
    File file = null;
    try{
      //创建临时文件
      file = File.createTempFile(uploadPath,null);
      //处理文件来源
      processFile(inputSource,file);
      //upload to cos
      PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath,file);
      ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
      //return
      return buildResult(originFilename,file,uploadPath,imageInfo);
    }catch (Exception e){
      log.error("file upload to COS error",e);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR,"upload fail");
    }
  }
  
  /**
   * 输入校验
   * @param inputSource
   */
  protected abstract void validPicture(Object inputSource);
  
  /**
   * 获取输入原始文件名
   * @param inputSource
   * @return
   */
  protected abstract String getOriginFilename(Object inputSource);
  
  /**
   * 处理输入源并生成本地临时文件
   * @param inputSource
   * @param file
   * @throws Exception
   */
  protected abstract void processFile(Object inputSource, File file) throws Exception;
  
  private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo){
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
  }
  
  /**
   * 删除临时文件
   * @param file
   */
  public void deleteTempFile(File file){
    if (file == null) {
      return;
    }
    boolean deleteResult = file.delete();
    if (!deleteResult) {
      log.error("file delete error, filepath = {}", file.getAbsolutePath());
    }
  }
}
