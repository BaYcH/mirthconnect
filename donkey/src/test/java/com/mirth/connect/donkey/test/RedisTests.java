package com.mirth.connect.donkey.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-15 16:09
 */
public class RedisTests {

    private static JedisPool jedisPool;

    @Before
    public final void beforeClass() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(30);
        jedisPoolConfig.setMaxIdle(8);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setMaxWaitMillis(3000);
        jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 3000);
    }

    @After
    public final void afterClass() {
        jedisPool.close();
    }

    @Test
    public final void run() {
        Jedis jedis = jedisPool.getResource();
        String key = "TestList";
        jedis.select(3);

        //jedis.del(key);

//        for (int i = 0; i < 20; i++) {
//            long currentTime = (new Date()).getTime();
//            jedis.hset(key, String.valueOf(currentTime), "this is a test:" + i);
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        Long llen = jedis.llen(key);
        List<String> list = jedis.lrange(key, 0, llen);
        for (String value : list) {
            System.out.println(value);
        }
        jedisPool.returnResource(jedis);
    }
}
