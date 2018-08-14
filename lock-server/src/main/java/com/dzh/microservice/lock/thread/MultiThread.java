package com.dzh.microservice.lock.thread;

import com.dzh.microservice.lock.service.RedisLockService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by herbert on 2018/8/13.
 */
public class MultiThread extends Thread
{
    @Autowired
    RedisLockService redisLockService;
    public MultiThread(RedisLockService redisLockService) {
        this.redisLockService = redisLockService;
    }
    public void run()
    {
        String identifier = redisLockService.lockWithTimeout("resource", 5000, 1000);
        System.out.println("锁:"+identifier);
        System.out.println(Thread.currentThread().getName() + "获得了锁");
        boolean res = redisLockService.releaseLock("resource", identifier);
    }
}