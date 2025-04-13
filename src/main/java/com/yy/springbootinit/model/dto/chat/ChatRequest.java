package com.yy.springbootinit.model.dto.chat;

import lombok.Data;

@Data
public class ChatRequest {

    private String content;  // 只接收用户输入的内容
}
