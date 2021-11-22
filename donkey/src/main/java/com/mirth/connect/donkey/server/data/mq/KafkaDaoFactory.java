package com.mirth.connect.donkey.server.data.mq;

import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDaoFactory;
import com.mirth.connect.donkey.util.SerializerProvider;
import redis.clients.jedis.JedisPool;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-10 16:47
 */
public class KafkaDaoFactory implements DonkeyDaoFactory {

    private JdbcDaoFactory jdbcDaoFactory;
    private JedisPool jedisPool;
    private boolean encryptData;
    private boolean decryptData;
    private StatisticsUpdater statisticsUpdater;
    private String serverId;
    private SerializerProvider serializerProvider;

    @Override
    public KafkaDao getDao() {
        return getDao(serializerProvider);
    }

    @Override
    public KafkaDao getDao(SerializerProvider serializerProvider) {
        KafkaDao kafkaDao = new KafkaDao();
        kafkaDao.setJedisPool(jedisPool);
        kafkaDao.setJdbcDao(jdbcDaoFactory.getDao(serializerProvider));
        kafkaDao.setDecryptData(this.decryptData);
        kafkaDao.setEncryptData(this.encryptData);
        kafkaDao.setStatisticsUpdater(this.statisticsUpdater);
        kafkaDao.setServerId(serverId);
        return kafkaDao;
    }

    @Override
    public void setEncryptData(boolean encryptData) {
        this.encryptData = encryptData;
    }

    @Override
    public void setDecryptData(boolean decryptData) {
        this.decryptData = decryptData;
    }

    @Override
    public void setStatisticsUpdater(StatisticsUpdater statisticsUpdater) {
        this.statisticsUpdater = statisticsUpdater;
    }

    public JdbcDaoFactory getJdbcDaoFactory() {
        return jdbcDaoFactory;
    }

    public void setJdbcDaoFactory(JdbcDaoFactory jdbcDaoFactory) {
        this.jdbcDaoFactory = jdbcDaoFactory;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public boolean isEncryptData() {
        return encryptData;
    }

    public boolean isDecryptData() {
        return decryptData;
    }

    public StatisticsUpdater getStatisticsUpdater() {
        return statisticsUpdater;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public SerializerProvider getSerializerProvider() {
        return serializerProvider;
    }

    public void setSerializerProvider(SerializerProvider serializerProvider) {
        this.serializerProvider = serializerProvider;
    }
}
