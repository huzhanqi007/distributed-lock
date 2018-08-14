package com.dzh.microservice.lock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.UUID;

/**
 * Created by herbert on 2018/8/13.
 */
@Service
public class RedisLockService {
    private Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public synchronized String lock(String lockName){
        return lockWithTimeout(lockName, 3000L, 3000L);
    }

    /**
     * 获取锁
     * @param locaName
     * @param acquireTimeout
     * @param timeout
     * @return
     */
    public synchronized String lockWithTimeout(String locaName, long acquireTimeout, long timeout){
        String retIdentifier = null;
        RedisConnectionFactory connectionFactory = stringRedisTemplate.getConnectionFactory();
        RedisConnection redisConnection = connectionFactory.getConnection();
        try {
            // 获取连接
            // 随机生成一个value
            String identifier = UUID.randomUUID().toString();
            // 锁名，即key值
            String lockKey = "lock:" + locaName;
            // 超时时间，上锁后超过此时间则自动释放锁
            int lockExpire = (int)(timeout / 1000);
            // 获取锁的超时时间，超过这个时间则放弃获取锁
            long end = System.currentTimeMillis() + acquireTimeout;
            while (System.currentTimeMillis() < end) {
                if (redisConnection.setNX(lockKey.getBytes(), identifier.getBytes())) {
                    redisConnection.expire(lockKey.getBytes(), lockExpire);
                    // 返回value值，用于释放锁时间确认
                    retIdentifier = identifier;
                    return retIdentifier;
                }
                // 返回-1代表key没有设置超时时间，为key设置一个超时时间
                if (redisConnection.ttl(lockKey.getBytes()) == -1) {
                    redisConnection.expire(lockKey.getBytes(), lockExpire);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.warn("获取到分布式锁：线程中断！");
                    Thread.currentThread().interrupt();
                }
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } finally {
            if (redisConnection != null) {
                RedisConnectionUtils.releaseConnection(redisConnection, connectionFactory);
            }
        }
        return retIdentifier;
    }

    /**
     * 释放锁
     * @param lockName 锁的key
     * @param identifier 释放锁的标识
     * @return
     */
    public synchronized boolean releaseLock(String lockName, String identifier) {
        if(identifier == null || "".equals(identifier)){
            return false;
        }
        RedisConnectionFactory connectionFactory = stringRedisTemplate.getConnectionFactory();
        RedisConnection redisConnection = connectionFactory.getConnection();
        boolean releaseFlag = false;
        try {
            String lockKey = "lock:" + lockName;
            while (true) {
                try{
                    // 监视lock，准备开始事务
                    redisConnection.watch(lockKey.getBytes());
                    // 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
                    byte[] valueBytes = redisConnection.get(lockKey.getBytes());
                    if(valueBytes == null){
                        redisConnection.unwatch();
                        releaseFlag = false;
                        break;
                    }
                    String identifierValue = new String(valueBytes);
                    if (identifier.equals(identifierValue)) {
                        redisConnection.multi();
                        redisConnection.del(lockKey.getBytes());
                        List<Object> results = redisConnection.exec();
                        if (results == null) {
                            continue;
                        }
                        releaseFlag = true;
                    }
                    redisConnection.unwatch();
                    break;
                }catch(Exception e){
                    logger.warn("释放锁异常", e);
                    e.printStackTrace();
                }
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } finally {
            if (redisConnection != null) {
                RedisConnectionUtils.releaseConnection(redisConnection, connectionFactory);
            }
        }
        return releaseFlag;
    }
}
