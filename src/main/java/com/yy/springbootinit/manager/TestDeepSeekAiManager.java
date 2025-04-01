package com.yy.springbootinit.manager;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yy.springbootinit.common.ErrorCode;
import com.yy.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class TestDeepSeekAiManager {


        @Value("${deepseek.apiKey}")
        private String apiKey ;

        @Value("${deepseek.API_URL}")
        private String API_URL;


        public String doChat(String message) {

            JSONObject requestBody = new JSONObject();
            requestBody.set("model", "deepseek-chat");
            JSONArray messages = new JSONArray();
            String systemPrompt = "\"你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\\n\" +\n" +
                    "                \"分析需求：\\n\" +\n" +
                    "                \"{数据分析的需求或者目标}\\n\" +\n" +
                    "                \"原始数据：\\n\" +\n" +
                    "                \"{csv格式的原始数据，用,作为分隔符}\\n\" +\n" +
                    "                \"请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\\n\" +\n" +
                    "                \"【【【【【\\n\" +\n" +
                    "                \"{前端 Echarts V5 的 option 配置对象的json代码,字符串必须用双引号包裹，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\\n\" +\n" +
                    "                \"【【【【【\\n\" +\n" +
                    "                \"{明确的数据分析结论、越详细越好，不要生成多余的注释}\"";
            messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
            messages.add(new JSONObject().set("role", "user").set("content", message));
            requestBody.set("messages",messages);
//        requestBody.set("temperature", 0.7);
//        requestBody.set("top_p", 0.9);

//            System.out.println(requestBody.toString());
            HttpResponse response = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();
            if (response.getStatus() != 200){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, response.getStatus()+ " AI 响应错误,"+response.body());
            }
            JSONObject responseJson = JSONUtil.parseObj(response.body());
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.isEmpty()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"API返回结果为空");
            }
            // 提取生成的文本
            return choices.getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");
        }

}


