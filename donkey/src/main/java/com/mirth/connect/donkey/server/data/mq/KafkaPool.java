package com.mirth.connect.donkey.server.data.mq;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class KafkaPool<T, K> implements Serializable, Closeable {
    private static KafkaPool KAFKAPOOL;
    private static Properties producerProps;
    private static Properties consumerProps;

    public static KafkaPool getInstance() {
        if (KAFKAPOOL == null) {
            synchronized (KafkaPool.class) {
                if (KAFKAPOOL == null) {
                    KAFKAPOOL = new KafkaPool(producerProps, consumerProps);
                }
            }
        }
        return KAFKAPOOL;
    }


    private GenericObjectPool<KafkaProducer<T, K>> producerPool;
    private GenericObjectPool<KafkaConsumer<T, K>> consumerPool;

    protected KafkaPool(Properties producerProps, Properties consumerProps) {

        KafkaProducerPooledObjectFactory kafkaProducerPooledObjectFactory = new KafkaProducerPooledObjectFactory(producerProps);
        KafkaConsumerPooledObjectFactory kafkaConsumerPooledObjectFactory = new KafkaConsumerPooledObjectFactory(consumerProps);

        GenericObjectPoolConfig config = new GenericObjectPoolConfig(); // 池子配置文件
        config.setMaxTotal(100);                                        // 整个池最大值
        config.setMaxIdle(10);                                          // 最大空闲
        config.setMinIdle(0);                                           // 最小空闲
        config.setMaxWaitMillis(5000);                                  // 最大等待时间，-1表示一直等
        config.setBlockWhenExhausted(true);                             // 当对象池没有空闲对象时，新的获取对象的请求是否阻塞。true阻塞。默认值是true
        config.setTestOnBorrow(false);                                  // 在从对象池获取对象时是否检测对象有效，true是；默认值是false
        config.setTestOnReturn(false);                                  // 在向对象池中归还对象时是否检测对象有效，true是，默认值是false
        config.setTestWhileIdle(false);                                 // 在检测空闲对象线程检测到对象不需要移除时，是否检测对象的有效性。true是，默认值是false
        config.setMinEvictableIdleTimeMillis(60000L);                   // 可发呆的时间,10mins
        config.setTestWhileIdle(true);                                  // 发呆过长移除的时候是否test一下先
        config.setTimeBetweenEvictionRunsMillis(3000);                  // 回收资源线程的执行周期 3s
        config.setNumTestsPerEvictionRun(10);

        producerPool = new GenericObjectPool<>(kafkaProducerPooledObjectFactory, config);
        consumerPool = new GenericObjectPool<>(kafkaConsumerPooledObjectFactory, config);
    }

    public KafkaProducer<T, K> getProducer() {
        try {
            KafkaProducer<T, K> producer = producerPool.borrowObject();
            return producer;
        } catch (Exception e) {
            throw new RuntimeException("获取KafkaProducer连接异常", e);
        }
    }

    public void returnProducer(KafkaProducer<T, K> producer) {
        try {
            producerPool.returnObject(producer);// 将对象放回对象池
        } catch (Exception e) {
            throw new RuntimeException("释放KafkaProducer连接异常", e);
        }
    }

    public KafkaConsumer<T, K> getConsumer() {
        try {
            KafkaConsumer<T, K> kafkaConsumer = consumerPool.borrowObject();
            return kafkaConsumer;
        } catch (Exception e) {
            throw new RuntimeException("获取KafkaConsumer连接异常", e);
        }
    }

    public void returnConsumer(KafkaConsumer<T, K> consumer) {
        try {
            consumerPool.returnObject(consumer);
        } catch (Exception e) {
            throw new RuntimeException("释放KafkaConsumer连接异常", e);
        }
    }

    public static Properties getProducerProps() {
        return producerProps;
    }

    public static void setProducerProps(Properties producerProps) {
        KafkaPool.producerProps = producerProps;
    }

    public static Properties getConsumerProps() {
        return consumerProps;
    }

    public static void setConsumerProps(Properties consumerProps) {
        KafkaPool.consumerProps = consumerProps;
    }

    @Override
    public void close() throws IOException {
        producerPool.close();
        consumerPool.close();
    }
}