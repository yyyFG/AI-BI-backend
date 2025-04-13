package com.yy.springbootinit.model.dto.chat;

import com.yy.springbootinit.controller.DeepSeekController;
import lombok.Data;

import java.util.List;

@Data
public class DeepSeekRequest {

    private String model;

    private List<Message> messages;

    private boolean stream;

}
