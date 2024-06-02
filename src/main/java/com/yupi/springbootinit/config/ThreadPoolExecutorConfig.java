package com.yupi.springbootinit.config;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        //创建一个线程工厂
        ThreadFactory threadFactory =  new ThreadFactory() {
           //初始化线程数为1
           private int count = 1;

           /**
            * 每当要创建一个新线程时，就会调用new++Thread方法
            * @Not Null Runnable r表示参数不能为空，如果这个参数调用时传了一个空的参数就会报错
            * @param r
            * @return
            */
            @Override
            public Thread newThread(@NotNull Runnable r) {
                //创建一个新线程
                Thread thread = new Thread();
                //线程命名
                thread.setName("Thread " + count);

                count++;

                return thread;
            }

        };
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(2, 4, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), threadFactory);

        return threadPoolExecutor;
    }
}
