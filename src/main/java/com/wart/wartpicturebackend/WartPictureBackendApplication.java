package com.wart.wartpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.wart.wartpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)  // 开启aop代理
public class WartPictureBackendApplication {
  
  public static void main(String[] args) {
    SpringApplication.run(WartPictureBackendApplication.class, args);
  }
}
