package com.yy.springbootinit.manager;

import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.lkeap.v20240522.LkeapClient;
import com.tencentcloudapi.lkeap.v20240522.models.ChatCompletionsRequest;
import com.tencentcloudapi.lkeap.v20240522.models.ChatCompletionsResponse;
import com.tencentcloudapi.lkeap.v20240522.models.Message;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class DeepSeekAiManager {

    @Resource
    private LkeapClient deepSeekClient;

    /**
     * AI 对话
     * @param message
     * @return
     */
    public String doChat(String message){
        // todo 写系统预设
        final String SYSTEM_PROMPT = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n" +
                "分析需求:\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据:\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据以上内容，帮我生成数据分析结论和可视化图表代码\n";

        try{
            // 实例化一个请求对象，每个接口都会对应一个 request 对象
            ChatCompletionsRequest req = new ChatCompletionsRequest();
            req.setModel("deepseek-v3");
            req.setStream(false);
            // 系统消息
            Message[] messages = new Message[2];
            Message message0 = new Message();
            message0.setRole("systemPrompt");
            message0.setContent(SYSTEM_PROMPT);
            messages[0] = message0;
            // 用户消息
            Message message1 = new Message();
            message1.setRole("user");
            message1.setContent(message);
            messages[1] = message1;
            req.setMessages(messages);
            // 返回的 resp 是一个 ChatCompletionsResponse 的实例，与请求对象对应
            ChatCompletionsResponse resp = deepSeekClient.ChatCompletions(req);
            // 输出 json 格式的字符拆回包
            return AbstractModel.toJsonString(resp);
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            log.error("AI 对话失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 对话失败");
        }

    }
}
