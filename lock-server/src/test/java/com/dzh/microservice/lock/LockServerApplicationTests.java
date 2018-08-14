package com.dzh.microservice.lock;

import com.dzh.microservice.lock.service.LockService;
import com.dzh.microservice.lock.service.RedisLockService;
import com.dzh.microservice.lock.thread.MultiThread;
import com.dzh.microservice.lock.thread.SpikeThread;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LockServerApplicationTests {
	@Autowired
	RedisLockService redisLockService;
	@Test
	public void contextLoads() {
		for (int i = 0; i < 200; i++) {
			//使用方法，创建RedisLock对象
			try {
				MultiThread multiThread =new MultiThread(redisLockService);
				multiThread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


}
