package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;


@SpringBootTest
class RedisLimitManagerTest {

    @Resource
    private RedisLimitManager redisLimitManager;

    @Test
    void doRedisLimit() throws InterruptedException {
        String userId = "1";
        for (int i = 0; i < 2; i++) {
            redisLimitManager.doRedisLimit(userId);
            System.out.println("成功");
        }
        Thread.sleep(1000);
        for (int i = 0; i < 5; i++) {
            redisLimitManager.doRedisLimit(userId);
            System.out.println("成功");
        }
    }
}