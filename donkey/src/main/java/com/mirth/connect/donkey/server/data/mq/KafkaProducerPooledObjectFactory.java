package com.mirth.connect.donkey.server.data.mq;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.Serializable;
import java.time.Duration;
import java.util.Properties;

public class KafkaProducerPooledObjectFactory<T, K> implements PooledObjectFactory<KafkaProducer<T, K>>, Serializable {

    Properties props;

    public KafkaProducerPooledObjectFactory(Properties props) {
        this.props = props;
    }

    @Override
    public PooledObject<KafkaProducer<T, K>> makeObject() throws Exception {
        KafkaProducer<T, K> kafkaProducer = new KafkaProducer<>(props);
        return new DefaultPooledObject<KafkaProducer<T, K>>(kafkaProducer);
    }

    @Override
    public void destroyObject(PooledObject<KafkaProducer<T, K>> p) throws Exception {
        KafkaProducer<T, K> o = p.getObject();
        o.close(Duration.ofMillis(1000));
    }

    @Override
    public boolean validateObject(PooledObject<KafkaProducer<T, K>> p) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<KafkaProducer<T, K>> p) throws Exception {
        //System.out.println("activateObject");
    }

    @Override
    public void passivateObject(PooledObject<KafkaProducer<T, K>> p) throws Exception {
        //System.out.println("passivateObject");
    }
}