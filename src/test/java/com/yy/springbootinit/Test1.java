package com.yy.springbootinit;


import org.springframework.context.annotation.Profile;

import static com.yy.springbootinit.constant.MessageConstant.header_message;

@Profile({"dev", "local"})
public class Test1 {
        public static void main(String[] args) {
            String content = header_message;

            // 按 "【【【【【" 分割字符串
            String[] parts = content.split("【【【【【");

            // 去除空元素（split 可能会在开头产生空字符串）
            if (parts.length > 0 && parts[0].isEmpty()) {
                String[] filteredParts = new String[parts.length - 1];
                System.arraycopy(parts, 1, filteredParts, 0, filteredParts.length);
                parts = filteredParts;
            }

            // 打印结果
            for (int i = 0; i < parts.length; i++) {
                System.out.println("Part " + i + ": " + parts[i].trim());
            }
        }
}

