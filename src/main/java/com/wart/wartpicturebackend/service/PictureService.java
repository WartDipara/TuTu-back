package com.wart.wartpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wart.wartpicturebackend.model.dto.picture.PictureQueryRequest;
import com.wart.wartpicturebackend.model.dto.picture.PictureReviewRequest;
import com.wart.wartpicturebackend.model.dto.picture.PictureUploadRequest;
import com.wart.wartpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wart.wartpicturebackend.model.entity.User;
import com.wart.wartpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author Wart
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-27 13:41:24
*/
public interface PictureService extends IService<Picture> {
  /**
   * 上传图片
   * @param multipartFile  图片文件
   * @param pictureUploadRequest 上传图片请求
   * @param loginUser 登录用户
   * @return PictureVO
   */
  PictureVO uploadPicture(MultipartFile multipartFile,
                          PictureUploadRequest pictureUploadRequest,
                          User loginUser);
  /**
   * 获取查询条件 并transfer to QueryWrapper查询结构
   * @param pictureQueryRequest 查询条件
   * @return QueryWrapper<Picture>
   */
  QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);
  
  /**
   * 获取pictureVO
   * @param picture picture
   * @param request request
   * @return PictureVO
   */
  PictureVO getPictureVO(Picture picture, HttpServletRequest  request);
  
  /**
   * 脱敏图片
   * @param picturePage
   * @param request
   * @return
   */
  Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);
  
  /**
   * 数据校验
   * @param picture
   */
  void validPicture(Picture picture);
  
  /**
   * 图片审核
   *
   * @param pictureReviewRequest
   * @param loginUser
   */
  void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);
  
}
