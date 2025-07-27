package com.wart.wartpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.wart.wartpicturebackend.exception.BusinessException;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {
  
  @Override
  protected void validPicture(Object inputSource) {
    String fileUrl = (String) inputSource;
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
    ThrowUtils.throwIf(!(fileUrl.startsWith("http://")|| fileUrl.startsWith("https://")), ErrorCode.PARAMS_ERROR, "url protocol error");
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
  
  @Override
  protected String getOriginFilename(Object inputSource) {
    return FileUtil.mainName((String) inputSource);
  }
  
  @Override
  protected void processFile(Object inputSource, File file) throws Exception {
    String fileUrl = (String) inputSource;
    //  download temp  file
    HttpUtil.downloadFile(fileUrl, file);
  }
}
