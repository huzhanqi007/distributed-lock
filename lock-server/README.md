## **lock-server**是基于redis的分布式锁工程
### 1. pom引入redis
```java
     <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-redis</artifactId>
        <version>1.4.5.RELEASE</version>
     </dependency>
```
### 2. 创建连接池
    RedisConfig
### 3. 引入RedisLockService
### 4. 测试
    执行test中contextLoads()方法
