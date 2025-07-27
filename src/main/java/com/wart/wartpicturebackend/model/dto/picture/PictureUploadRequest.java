package com.wart.wartpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yips
 */
@Data
public class PictureUploadRequest implements Serializable {
  private static final long serialVersionUID = 1L;
  
  /**
   * 图片id
   */
  private Long id;
  
  /**
   * 文件地址
   */
  private String fileUrl;
}
