package com.yy.springbootinit.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfigurer implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/user/**").addResourceLocations("file:D:\\picture\\BI\\");
//        registry.addResourceHandler("/image/team/**").addResourceLocations("file:F:\\picture\\teamAvatar\\");
//        registry.addResourceHandler("/image/user/**").addResourceLocations("file:\\home\\ubuntu\\picture\\BIpicture\\");
    }


}
