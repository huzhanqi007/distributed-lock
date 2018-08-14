package com.dzh.microservice.lock.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by herbert on 2018/8/13.
 */
@Service
public class LockService {
    private static JedisPool pool = null;

    private DistributedLock lock = new DistributedLock(pool);
    @Autowired
    RedisLockService redisLockService;
    int n = 200;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(200);
        // 设置最大空闲数
        config.setMaxIdle(8);
        // 设置最大等待时间
        config.setMaxWaitMillis(1000 * 100);
        // 在borrow一个jedis实例时，是否需要验证，若为true，则所有jedis实例均是可用的
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, "10.15.88.115", 6379, 3000,"123@456",1);
    }

    public void seckill() {
        // 返回锁的value值，供释放锁时候进行判断
        String identifier = lock.lockWithTimeout("resource", 5000, 1000);
        System.out.println("锁:"+identifier);
        System.out.println(Thread.currentThread().getName() + "获得了锁");
        boolean res = lock.releaseLock("resource", identifier);
    }
}
