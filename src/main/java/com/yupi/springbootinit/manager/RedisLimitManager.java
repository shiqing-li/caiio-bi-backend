package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import io.swagger.v3.oas.annotations.servers.Server;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专门提供RedisLimiter限流基础服务的（提供通用的能力，放在其他项目中都能使用）
 */

@Service
public class RedisLimitManager {

    @Resource
    private RedissonClient redissonClient;


    public void doRedisLimit(String key){
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //限流器的统计规则（每秒2个请求，最多只能有一个请求被通过）
        //RateTypy.OVERALL 表示限制速率作用于整个令牌桶，即限制所有的请求
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        //每当一个操作来了之后，请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        //如果没有令牌，还想继续操作就抛出异常
        if(!canOp){
            throw new BusinessException(ErrorCode.TO_MANY_REQUEST);
        }
    }
}
