package com.atguigu.gmall.config;


import org.jboss.netty.util.Timeout;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    // 创建JedisPool
    private JedisPool jedisPool;

   public  void initJedisPool(String host, int port,int timeOut,int database){
       JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
       // 设置连接池最大核心数
       jedisPoolConfig.setMaxIdle(200);
       // 设置等待时间
       jedisPoolConfig.setMaxWaitMillis(10*1000);
       // 最少剩余数
       jedisPoolConfig.setMinIdle(10);
       // 排队等待 开启缓冲池
       jedisPoolConfig.setBlockWhenExhausted(true);
       // 设置当用户获取到jedis 时，做自检看当前获取到的jedis 是否可以使用！
       jedisPoolConfig.setTestOnBorrow(true);

       jedisPool =new JedisPool(jedisPoolConfig,host,port, timeOut);

   }
   public Jedis getJedis(){
       Jedis jedis = jedisPool.getResource();

       return jedis;
   }    

}
