package com.yy.springbootinit.manager;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.exception.BusinessException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Cos 对象存储操作
 *
 */
@Component
public class CosManager {

    private static String accessKey = "AKIDEKCqJZhbKp69zd4IKAmBMJ4cw33qQmur";

    private static String secretKey = "nOly0rkXJzUmvtfJrHgwAIExSksoX2JM";

    //上传的存储桶的地域
    private static String bucketAddr = "ap-guangzhou";


    private static String bucket = "bi-picture-1306543534";

    //文件上传后访问路径的根路径，后面要最佳文件名字与类型
    private static String folderName = "https://bi-picture-1306543534.cos.ap-guangzhou.myqcloud.com/images/";


    /**
     * 1.调用静态方法getCosClient()就会获得COSClient实例
     * 2.本方法根据永久密钥初始化 COSClient的，官方是不推荐，官方推荐使用临时密钥，是可以限制密钥使用权限，创建cred时有些区别
     *
     * @return COSClient实例
     */
    private static COSClient getCosClient() {
        // 1 初始化用户身份信息（secretId, secretKey）。
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 2.1 设置存储桶的地域（上文获得）
        Region region = new Region(bucketAddr);
        ClientConfig clientConfig = new ClientConfig(region);
        // 2.2 使用https协议传输
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        // 返回COS客户端
        return cosClient;

    }


    /**
     * 只要调用静态方法upLoadFile(MultipartFile multipartFile)就可以获取上传后文件的全路径
     *
     * @param file
     * @return 返回文件的浏览全路径
     */
    public String upLoadFile(MultipartFile file) {

        try {
            // 获取上传的文件的输入流
            InputStream inputStream = file.getInputStream();

            // 避免文件覆盖，获取文件的原始名称，如123.jpg,然后通过截取获得文件的后缀，也就是文件的类型
            String originalFilename = file.getOriginalFilename();
            //获取文件的类型
            String fileType = originalFilename.substring(originalFilename.lastIndexOf("."));

            //使用UUID工具  创建唯一名称，放置文件重名被覆盖，在拼接上上命令获取的文件类型
            String fileName = UUID.randomUUID().toString() + fileType;
            // 指定文件上传到 COS 上的路径，即对象键。最终文件会传到存储桶名字中的images文件夹下的fileName名字
            String key = "images/" + fileName;
            // 创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            // - 使用输入流存储，需要设置请求长度
            objectMetadata.setContentLength(inputStream.available());
            // - 设置缓存
            objectMetadata.setCacheControl("no-cache");
            // - 设置Content-Type
            objectMetadata.setContentType(fileType);

            //上传文件
//            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
            PutObjectResult putResult = getCosClient().putObject(bucket, key, inputStream, objectMetadata);

            System.out.println(putResult);

            // 创建文件的网络访问路径
            String url = folderName + key;
            //关闭 cosClient，并释放 HTTP 连接的后台管理线程
            getCosClient().shutdown();
            return url;

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
        }

    }
}
