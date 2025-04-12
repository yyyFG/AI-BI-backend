package com.yy.springbootinit.controller;


import cn.hutool.core.io.FileUtil;
import com.yy.springbootinit.common.BaseResponse;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.common.ResultUtils;
import com.yy.springbootinit.exception.BusinessException;
import com.yy.springbootinit.exception.ThrowUtils;
import com.yy.springbootinit.manager.CosManager;
import com.yy.springbootinit.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/image")
@CrossOrigin
public class ImageController {


    @Autowired
    private CosManager cosManager;


//    private String imagesFilePath = "\\home\\ubuntu\\picture\\BIpicture\\";
    private String imagesFilePath = "D:\\picture\\BI\\";


    /**
     * 图片上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> imgUpload(@RequestPart MultipartFile file) {
        // 文件校验
        long size = file.getSize();
        if (size > 5 * 1024 * 1024L) {
            throw new RuntimeException("文件大小不能超过5M");
        }
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        final List<String> validFileSuffixList = Arrays.asList("jpg", "png", "jpeg", "gif", "bmp");
        if (!validFileSuffixList.contains(fileSuffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件后缀不合法，请重新上传");
        }

        String url = null;

        try {
            if(!file.isEmpty()){
                // 获取文件名
                String originalFilename = file.getOriginalFilename();
                String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFileName = DateUtil.getCurrentDateStr()+suffixName;
                FileUtils.copyInputStreamToFile(file.getInputStream(), new File(imagesFilePath+newFileName));

                url = newFileName;
            }
        }catch (IOException e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        }

//        url = cosManager.upLoadFile(file);
//        ThrowUtils.throwIf(url == null, ErrorCode.PARAMS_ERROR, "文件上传失败");


        return ResultUtils.success(url);
    }

}
