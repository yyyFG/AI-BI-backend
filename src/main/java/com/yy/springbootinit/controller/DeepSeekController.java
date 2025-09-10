package com.yy.springbootinit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.springbootinit.model.dto.chat.ChatRequest;
import com.yy.springbootinit.model.dto.chat.DeepSeekRequest;
import com.yy.springbootinit.model.dto.chat.Message;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/ai")
public class DeepSeekController {

    private final String DEEPSEEK_API_URL = "";
    private final String API_KEY = ""; // 替换为你的实际API密钥

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/stream")
    public ResponseEntity<StreamingResponseBody> streamChat(
            @RequestBody ChatRequest chatRequest) {

        // 1. 构建固定结构的DeepSeek请求
        DeepSeekRequest deepSeekRequest = new DeepSeekRequest();
        deepSeekRequest.setModel("deepseek-chat");
        deepSeekRequest.setStream(true);

        // 2. 构建消息（固定role为user）
        Message message = new Message();
        message.setRole("user");
        message.setContent(chatRequest.getContent());
        deepSeekRequest.setMessages(Collections.singletonList(message)); // 使用Java 8兼容方法

        // 3. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        // 4. 创建流式响应
        StreamingResponseBody responseBody = outputStream -> {
            new RestTemplate().execute(
                    DEEPSEEK_API_URL,
                    HttpMethod.POST,
                    req -> {
                        req.getHeaders().addAll(headers);
                        req.getBody().write(objectMapper.writeValueAsBytes(deepSeekRequest));
                    },
                    response -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("data: ")) {
                                    String jsonStr = line.substring(6).trim();
                                    if (!jsonStr.equals("[DONE]")) {
                                        JsonNode node = objectMapper.readTree(jsonStr);
                                        String content = node.path("choices")
                                                .path(0)
                                                .path("delta")
                                                .path("content")
                                                .asText("");
                                        if (!content.isEmpty()) {
                                            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                                            outputStream.flush();
                                        }
                                    }
                                }
                            }
                        }
                        return null;
                    }
            );
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(responseBody);
    }


}
