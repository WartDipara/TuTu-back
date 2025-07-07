package com.wart.wartpicturebackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Yips
 */

//分类view
@Data
public class PictureTagCategory {
  /**
   * 标签列表
   */
  private List<String> tagList;
  /**
   * 分类列表
   */
  private List<String> categoryList;
}
