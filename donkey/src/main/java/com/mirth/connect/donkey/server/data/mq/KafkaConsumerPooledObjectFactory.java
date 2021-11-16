package com.mirth.connect.donkey.server.data.mq;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.Serializable;
import java.time.Duration;
import java.util.Properties;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-10 17:37
 */
public class KafkaConsumerPooledObjectFactory<T, K> implements PooledObjectFactory<KafkaConsumer<T, K>>, Serializable {

    Properties properties;

    public KafkaConsumerPooledObjectFactory(Properties properties) {
        this.properties = properties;
    }

    @Override
    public PooledObject<KafkaConsumer<T, K>> makeObject() throws Exception {
        KafkaConsumer<T, K> kafkaConsumer = new KafkaConsumer<T, K>(properties);
        return new DefaultPooledObject<>(kafkaConsumer);
    }

    @Override
    public void destroyObject(PooledObject<KafkaConsumer<T, K>> pooledObject) throws Exception {
        pooledObject.getObject().close(Duration.ofMillis(1000));
    }

    @Override
    public boolean validateObject(PooledObject<KafkaConsumer<T, K>> pooledObject) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<KafkaConsumer<T, K>> pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<KafkaConsumer<T, K>> pooledObject) throws Exception {

    }
}
