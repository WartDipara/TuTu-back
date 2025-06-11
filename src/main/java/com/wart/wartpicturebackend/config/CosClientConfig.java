package com.wart.wartpicturebackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yips
 */

/**
 * 腾讯云cos服务配置
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "cos.client")
public class CosClientConfig {
  /**
   * 域名
   */
  private String host;
  
  /**
   * 密钥id
   */
  private String secretId;
  
  /**
   * 密钥key
   */
  private String secretKey;
  
  /**
   * 存储桶名称
   */
  private String bucket;
  
  /**
   * 存储桶区域
   */
  private String region;
  
  /**
   * 创建cosClient
   * @return COSClient 对象
   */
  @Bean
  public COSClient cosClient(){
    //1 初始化用户身份信息(secretId, secretKey)
    COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
    //2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
    ClientConfig clientConfig = new ClientConfig(new Region( region));
    //3 生成cosClient
    return new COSClient(credentials, clientConfig);
  }
}
