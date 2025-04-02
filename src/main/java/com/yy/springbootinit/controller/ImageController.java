package com.yy.springbootinit.controller;


import cn.hutool.core.io.FileUtil;
import com.yy.springbootinit.common.BaseResponse;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.common.ResultUtils;
import com.yy.springbootinit.exception.BusinessException;
import com.yy.springbootinit.exception.ThrowUtils;
import com.yy.springbootinit.manager.CosManager;
import com.yy.springbootinit.model.entity.User;
import com.yy.springbootinit.service.TeamService;
import com.yy.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/image")
@Slf4j
public class ImageController {


    @Autowired
    private CosManager cosManager;

    /**
     * 图片上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public String imgUpload(MultipartFile file) {
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

        url = cosManager.upLoadFile(file);
        ThrowUtils.throwIf(url == null, ErrorCode.PARAMS_ERROR, "文件上传失败");

        return url;
    }

}
