package com.yupi.springbootinit.manager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String answer = aiManager.doChat(1791920823773896705L,"分析需求：\n" +
                "网站用户增长情况\n" +
                "原始数据：\n" +
                "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30\n");

//        String answer = aiManager.doChat("周杰伦");
        System.out.println(answer);
    }
}