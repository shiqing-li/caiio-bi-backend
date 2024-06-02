package com.yupi.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/query")
@Slf4j
@Profile({"dev","local"})
public class QueryController {

    @Resource
    //注入一个线程池实例
    private ThreadPoolExecutor threadPoolExecutor;


    @GetMapping("/add")
    public void add(String name){
        CompletableFuture.runAsync(()->{
            log.info("" +  name + "执行中," + "ThreadName =" +  Thread.currentThread().getName());
            try{
                Thread.sleep(600000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        },threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get(){
        Map<String, Object> hashMap = new HashMap<>();
        //获取线程池队列长度
        int size = threadPoolExecutor.getQueue().size();

        hashMap.put("线程池队列长度",size);

        //获取线程池任务数
        long taskCount = threadPoolExecutor.getTaskCount();
        hashMap.put("线程池任务数",taskCount);

        //获取线程池完成任务数
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        hashMap.put("线程池已完成任务数",completedTaskCount);

        ////获取线程池正在执行任务数
        int activeCount = threadPoolExecutor.getActiveCount();
        hashMap.put("线程池正在执行任务数",activeCount);

        return JSONUtil.toJsonStr(hashMap);
    }

}
