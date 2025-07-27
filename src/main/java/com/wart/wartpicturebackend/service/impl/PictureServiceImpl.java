package com.wart.wartpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wart.wartpicturebackend.exception.BusinessException;
import com.wart.wartpicturebackend.exception.ErrorCode;
import com.wart.wartpicturebackend.exception.ThrowUtils;
import com.wart.wartpicturebackend.manager.FileManager;
import com.wart.wartpicturebackend.manager.upload.FilePictureUpload;
import com.wart.wartpicturebackend.manager.upload.PictureUploadTemplate;
import com.wart.wartpicturebackend.manager.upload.UrlPictureUpload;
import com.wart.wartpicturebackend.model.dto.file.UploadPictureResult;
import com.wart.wartpicturebackend.model.dto.picture.PictureQueryRequest;
import com.wart.wartpicturebackend.model.dto.picture.PictureReviewRequest;
import com.wart.wartpicturebackend.model.dto.picture.PictureUploadRequest;
import com.wart.wartpicturebackend.model.entity.Picture;
import com.wart.wartpicturebackend.model.entity.User;
import com.wart.wartpicturebackend.model.enums.PictureReviewStatusEnum;
import com.wart.wartpicturebackend.model.vo.PictureVO;
import com.wart.wartpicturebackend.model.vo.UserVO;
import com.wart.wartpicturebackend.service.PictureService;
import com.wart.wartpicturebackend.mapper.PictureMapper;
import com.wart.wartpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Wart
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-06-27 13:41:24
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
  @Resource
  FileManager fileManager;
  
  @Resource
  private FilePictureUpload filePictureUpload;
  
  @Resource
  private UrlPictureUpload urlPictureUpload;
  
  @Resource
  private UserService userService;
  
  @Override
  public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
    // 用于判断是新增还是更新图片
    Long pictureId = null;
    if (pictureUploadRequest != null) {
      pictureId = pictureUploadRequest.getId();
    }
    // 如果是更新图片，需要校验图片是否存在
    if (pictureId != null) {
      Picture oldPicture = this.getById(pictureId);
      ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "picture not exist");
      // 限制本人操作
      if (!Objects.equals(oldPicture.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser))
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }
    // 上传图片，得到信息
    // 按照用户 id 划分目录
    String uploadPathPrefix = String.format("public/%s", loginUser.getId());
    // 根据 inputSource类型判断调用上传方法
    PictureUploadTemplate pictureUploadTemplate = inputSource instanceof String ? urlPictureUpload : filePictureUpload;
    UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
    // 构造要入库的图片信息
    Picture picture = new Picture();
    picture.setUrl(uploadPictureResult.getUrl());
    picture.setName(uploadPictureResult.getPicName());
    picture.setPicSize(uploadPictureResult.getPicSize());
    picture.setPicWidth(uploadPictureResult.getPicWidth());
    picture.setPicHeight(uploadPictureResult.getPicHeight());
    picture.setPicScale(uploadPictureResult.getPicScale());
    picture.setPicFormat(uploadPictureResult.getPicFormat());
    picture.setUserId(loginUser.getId());
    // 如果 pictureId 不为空，表示更新，否则是新增
    if (pictureId != null) {
      // 如果是更新，需要补充 id 和编辑时间
      picture.setId(pictureId);
      picture.setEditTime(new Date());
    }
    //补充审核参数
    this.fillReviewParams(picture, loginUser);
    boolean result = this.saveOrUpdate(picture);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "picture upload failed");
    return PictureVO.objToVo(picture);
  }
  
  /**
   * 获取查询并转换查询结构
   *
   * @param pictureQueryRequest 查询条件
   * @return QueryWrapper<Picture>
   */
  @Override
  public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    if (pictureQueryRequest == null)
      return queryWrapper;
    
    //取值
    Long id = pictureQueryRequest.getId();
    String name = pictureQueryRequest.getName();
    String introduction = pictureQueryRequest.getIntroduction();
    String category = pictureQueryRequest.getCategory();
    List<String> tags = pictureQueryRequest.getTags();
    Long picSize = pictureQueryRequest.getPicSize();
    Integer picWidth = pictureQueryRequest.getPicWidth();
    Integer picHeight = pictureQueryRequest.getPicHeight();
    Double picScale = pictureQueryRequest.getPicScale();
    String picFormat = pictureQueryRequest.getPicFormat();
    String searchText = pictureQueryRequest.getSearchText();
    Long userId = pictureQueryRequest.getUserId();
    String sortFiled = pictureQueryRequest.getSortField();
    String sortOrder = pictureQueryRequest.getSortOrder();
    Integer reviewStatus = pictureQueryRequest.getReviewStatus();
    String reviewMessage = pictureQueryRequest.getReviewMessage();
    Long reviewerId = pictureQueryRequest.getReviewerId();
    
    if (StrUtil.isNotBlank(searchText)) {
      // 模糊搜索
      queryWrapper
          .and(qw -> qw.like("name", searchText))
          .or().like("introduction", searchText);
    }
    
    queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
    queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
    queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
    queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
    queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
    queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
    queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
    queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
    queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
    queryWrapper.eq(ObjectUtil.isNotEmpty(picScale), "picScale", picScale);
    queryWrapper.eq(ObjectUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
    queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
    queryWrapper.eq(ObjectUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
    
    //JSON 数组查询
    if (CollUtil.isNotEmpty(tags))
      for (String tag : tags)
        queryWrapper.like("tags", "\"" + tag + "\"");
    
    // sort
    queryWrapper.orderBy(StrUtil.isNotEmpty(sortFiled), sortOrder.equals("ascend"), sortFiled);
    return queryWrapper;
  }
  
  /**
   * 获取 PictureVO
   *
   * @param picture picture
   * @param request request
   * @return
   */
  @Override
  public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
    // 对象转封装类
    PictureVO pictureVO = PictureVO.objToVo(picture);
    // 关联查询用户信息
    Long userId = picture.getUserId();
    if (userId != null && userId > 0) {
      User user = userService.getById(userId);
      UserVO userVO = userService.getUserVO(user);
      pictureVO.setUser(userVO);
    }
    return pictureVO;
  }
  
  /**
   * 分页获取图片封装
   */
  @Override
  public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
    List<Picture> pictureList = picturePage.getRecords();
    Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
    if (CollUtil.isEmpty(pictureList)) {
      return pictureVOPage;
    }
    // 对象列表 => 封装对象列表
    List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
    // 1. 关联查询用户信息
    Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
    // 一次性解决，避免多次请求数据库造成数据库压力
    Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
        .collect(Collectors.groupingBy(User::getId));
    // 2. 填充信息
    pictureVOList.forEach(pictureVO -> {
      Long userId = pictureVO.getUserId();
      User user = null;
      if (userIdUserListMap.containsKey(userId)) {
        user = userIdUserListMap.get(userId).get(0);
      }
      pictureVO.setUser(userService.getUserVO(user));
    });
    pictureVOPage.setRecords(pictureVOList);
    return pictureVOPage;
  }
  
  //数据校验
  @Override
  public void validPicture(Picture picture) {
    ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
    
    Long id = picture.getId();
    String url = picture.getUrl();
    String introduction = picture.getIntroduction();
    
    // 修改数据时，id 不能为空，有参数则校验
    ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id cannot be empty");
    if (StrUtil.isNotBlank(url)) {
      ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "too long for url");
    }
    if (StrUtil.isNotBlank(introduction)) {
      ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "too long for introduction");
    }
  }
  
  /**
   * 图片审核
   *
   * @param pictureReviewRequest
   * @param loginUser
   */
  @Override
  public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
    Long id = pictureReviewRequest.getId();
    Integer reviewStatus = pictureReviewRequest.getReviewStatus();
    PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
    // 不能将状态改回待审核
    if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    // 判断是否存在
    Picture oldPicture = this.getById(id);
    ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
    // 已是该状态
    if (oldPicture.getReviewStatus().equals(reviewStatus)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "no duplicate review");
    }
    // 更新审核状态
    Picture updatePicture = new Picture();
    BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
    updatePicture.setReviewerId(loginUser.getId());
    updatePicture.setReviewTime(new Date());
    boolean result = this.updateById(updatePicture);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
  }
  
  /**
   * 填充审核参数
   *
   * @param picture
   * @param loginUser
   */
  @Override
  public void fillReviewParams(Picture picture, User loginUser) {
    if (userService.isAdmin(loginUser)) {
      //管理员不用审
      picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
      picture.setReviewTime(new Date());
      picture.setReviewerId(loginUser.getId());
      picture.setReviewMessage("Auto Pass(Admin)");
    } else {
      //非管理员 设置审核信息
      picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
    }
  }
  
}




