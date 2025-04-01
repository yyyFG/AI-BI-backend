package com.yy.springbootinit.config;


import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.lkeap.v20240522.LkeapClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tencent.deepseek.client")
@Data
public class DeepSeekClientConfig {

    /**
     *  腾讯云账号 secretId
     */
    private String secretId;

    /**
     *  腾讯云账号 secretKey
     */
    private String secretKey;

    /**
     * 初始化客户端
     * @return
     */
    @Bean
    public LkeapClient deepSeekClient(){
//        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性
            // 以下代码示例仅供参考，建议采用更安全的方式来使用密钥
            // 请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(secretId, secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("lkeap.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            LkeapClient client = new LkeapClient(cred, "ap-guangzhou", clientProfile);

            return client;
//            // 实例化一个请求对象,每个接口都会对应一个request对象
//            ChatCompletionsRequest req = new ChatCompletionsRequest();


//            // 返回的resp是一个ChatCompletionsResponse的实例，与请求对象对应
//            ChatCompletionsResponse resp = client.ChatCompletions(req);
//            // 输出json格式的字符串回包
//            if (resp.isStream()) { // 流式响应
//                for (SSEResponseModel.SSE e : resp) {
//                    System.out.println(e.Data);
//                }
//            } else { // 非流式响应
//                System.out.println(AbstractModel.toJsonString(resp));
//            }
//        } catch (TencentCloudSDKException e) {
//            System.out.println(e.toString());
//        }
    }
}
