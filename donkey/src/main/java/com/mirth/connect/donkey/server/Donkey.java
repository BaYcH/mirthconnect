/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.DonkeyStatisticsUpdater;
import com.mirth.connect.donkey.server.data.jdbc.*;
import com.mirth.connect.donkey.server.data.jdbc.XmlQuerySource.XmlQuerySourceException;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.SerializerProvider;
import com.mirth.connect.donkey.util.xstream.XStreamSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Donkey {
    private static Donkey instance;

    public static Donkey getInstance() {
        synchronized (Donkey.class) {
            if (instance == null) {
                instance = new Donkey();
            }

            return instance;
        }
    }

    private Donkey() {

    }

    private Map<String, Channel> deployedChannels = new ConcurrentHashMap<String, Channel>();
    private DonkeyConfiguration donkeyConfiguration;
    private DonkeyDaoFactory daoFactory;
    private Serializer serializer = new XStreamSerializer();
    private Encryptor encryptor;
    private EventDispatcher eventDispatcher;
    private DonkeyStatisticsUpdater statisticsUpdater;
    private Logger logger = Logger.getLogger(getClass());
    private Thread messageConsumerThread;
    private boolean running = false;

    public void startEngine(DonkeyConfiguration donkeyConfiguration) throws StartException {
        this.donkeyConfiguration = donkeyConfiguration;

        initDaoFactory();

        DonkeyDao dao = null;
        try {
            dao = daoFactory.getDao();
            dao.checkAndCreateChannelTables();

            dao.commit();
        } catch (Exception e) {
            logger.error("Count not check and create channel tables on startup", e);
        } finally {
            if (dao != null) {
                dao.close();
            }
        }

        // load channel statistics into memory
        ChannelController.getInstance().loadStatistics(donkeyConfiguration.getServerId());

        encryptor = donkeyConfiguration.getEncryptor();

        eventDispatcher = donkeyConfiguration.getEventDispatcher();

        int updateInterval = NumberUtils.toInt(donkeyConfiguration.getDonkeyProperties().getProperty("donkey.statsupdateinterval"), DonkeyStatisticsUpdater.DEFAULT_UPDATE_INTERVAL);
        statisticsUpdater = new DonkeyStatisticsUpdater(daoFactory, updateInterval);
        statisticsUpdater.start();
        
        running = true;
    }

    private void initDaoFactory() throws StartException {
        Properties dbProperties = donkeyConfiguration.getDonkeyProperties();
        String database = dbProperties.getProperty("database");
        String driver = dbProperties.getProperty("database.driver");
        String url = dbProperties.getProperty("database.url");
        String username = dbProperties.getProperty("database.username");
        String password = dbProperties.getProperty("database.password");
        String pool = dbProperties.getProperty("database.pool");
        boolean jdbc4 = Boolean.parseBoolean(dbProperties.getProperty("database.jdbc4"));
        String testQuery = dbProperties.getProperty("database.test-query");
        int maxConnections;

        try {
            maxConnections = Integer.parseInt(dbProperties.getProperty("database.max-connections"));
        } catch (NumberFormatException e) {
            throw new StartException("Failed to read the database.max-connections configuration property");
        }

        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new StartException(e);
            }
        }

        JdbcDaoFactory jdbcDaoFactory = JdbcDaoFactory.getInstance(database);
        jdbcDaoFactory.setStatsServerId(donkeyConfiguration.getServerId());

        if (StringUtils.equalsIgnoreCase(pool, "DBCP")) {
            logger.debug("Initializing DBCP");
            jdbcDaoFactory.setConnectionPool(new DBCPConnectionPool(url, username, password, maxConnections));
        } else {
            logger.debug("Initializing HikariCP");
            jdbcDaoFactory.setConnectionPool(new HikariConnectionPool(driver, url, username, password, maxConnections, jdbc4, testQuery));
        }

        jdbcDaoFactory.setSerializerProvider(new SerializerProvider() {
            @Override
            public Serializer getSerializer(Integer metaDataId) {
                return serializer;
            }
        });

        XmlQuerySource xmlQuerySource = new XmlQuerySource();

        try {
            xmlQuerySource.load("default.xml");
            xmlQuerySource.load(dbProperties.getProperty("database") + ".xml");
        } catch (XmlQuerySourceException e) {
            throw new StartException(e);
        }

        jdbcDaoFactory.setQuerySource(xmlQuerySource);


        String configDatabase = dbProperties.getProperty("config.database");
        String configDriver = dbProperties.getProperty("config.database.driver");
        String configUrl = dbProperties.getProperty("config.database.url");
        String configUsername = dbProperties.getProperty("config.database.username");
        String configPassword = dbProperties.getProperty("config.database.password");
        String configPool = dbProperties.getProperty("config.database.pool");
        boolean configJdbc4 = Boolean.parseBoolean(dbProperties.getProperty("config.database.jdbc4"));
        String configTestQuery = dbProperties.getProperty("config.database.test-query");
        int configMaxConnections;

        try {
            configMaxConnections = Integer.parseInt(dbProperties.getProperty("config.database.max-connections"));
        } catch (NumberFormatException e) {
            throw new StartException("Failed to read the database.max-connections configuration property");
        }

        if (configDriver != null) {
            try {
                Class.forName(configDriver);
            } catch (ClassNotFoundException e) {
                throw new StartException(e);
            }
        }
        JdbcDaoFactory configJdbcDaoFactory = null;
        if (!StringUtils.isEmpty(configDatabase)) {
            configJdbcDaoFactory = JdbcDaoFactory.getInstance(configDatabase);
            configJdbcDaoFactory.setStatsServerId(donkeyConfiguration.getServerId());

            if (StringUtils.equalsIgnoreCase(configPool, "DBCP")) {
                logger.debug("Initializing DBCP");
                configJdbcDaoFactory.setConnectionPool(new DBCPConnectionPool(configUrl, configUsername, configPassword, configMaxConnections));
            } else {
                logger.debug("Initializing HikariCP");
                configJdbcDaoFactory.setConnectionPool(new HikariConnectionPool(configDriver, configUrl, configUsername, configPassword, configMaxConnections, configJdbc4, configTestQuery));
            }

            configJdbcDaoFactory.setSerializerProvider(new SerializerProvider() {
                @Override
                public Serializer getSerializer(Integer metaDataId) {
                    return serializer;
                }
            });

            XmlQuerySource xmlConfigQuerySource = new XmlQuerySource();

            try {
                xmlConfigQuerySource.load("default.xml");
                xmlConfigQuerySource.load(dbProperties.getProperty("config.database") + ".xml");
            } catch (XmlQuerySourceException e) {
                throw new StartException(e);
            }

            configJdbcDaoFactory.setQuerySource(xmlConfigQuerySource);
            jdbcDaoFactory.setConfigJdbcDaoFactory(configJdbcDaoFactory);
        }

        JdbcDao dao = jdbcDaoFactory.getDao();

        try {
            if (dao.initTableStructure()) {
                dao.commit();
            }
        } finally {
            dao.close();
        }

        daoFactory = jdbcDaoFactory;

        // 暂不启用消息队列
//        KafkaPool.setConsumerProps(donkeyConfiguration.getPropertiesByprefix("mq.kafka.consumer", true));
//        KafkaPool.setProducerProps(donkeyConfiguration.getPropertiesByprefix("mq.kafka.producer", true));
//
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        jedisPoolConfig.setMaxTotal(50);
//        jedisPoolConfig.setMaxIdle(50);
//        jedisPoolConfig.setMinIdle(8);
//        jedisPoolConfig.setMaxWaitMillis(30 * 1000);
//        jedisPoolConfig.setTestOnBorrow(true);
//        jedisPoolConfig.setTestOnReturn(true);
//        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(10 * 1000);
//        jedisPoolConfig.setMinEvictableIdleTimeMillis(30 * 1000);
//        jedisPoolConfig.setNumTestsPerEvictionRun(-1);
//        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 3000);
//
//        KafkaDaoFactory kafkaDaoFactory = new KafkaDaoFactory();
//        kafkaDaoFactory.setJdbcDaoFactory(jdbcDaoFactory);
//        kafkaDaoFactory.setJedisPool(jedisPool);
//        kafkaDaoFactory.setServerId(donkeyConfiguration.getServerId());
//        kafkaDaoFactory.setSerializerProvider(new SerializerProvider() {
//            @Override
//            public Serializer getSerializer(Integer metaDataId) {
//                return serializer;
//            }
//        });
//        daoFactory = kafkaDaoFactory;
    }

    public DonkeyDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public DonkeyStatisticsUpdater getStatisticsUpdater() {
        return statisticsUpdater;
    }

    public void stopEngine() {
        if (statisticsUpdater != null) {
            statisticsUpdater.shutdown();
        }

        running = false;
    }

    public Map<String, Channel> getDeployedChannels() {
        return deployedChannels;
    }

    public Set<String> getDeployedChannelIds() {
        List<Channel> channels = new ArrayList<Channel>(deployedChannels.values());

        Collections.sort(channels, new Comparator<Channel>() {

            @Override
            public int compare(Channel o1, Channel o2) {
                return o1.getDeployDate().compareTo(o2.getDeployDate());
            }

        });

        Set<String> channelIds = new LinkedHashSet<String>();

        for (Channel channel : channels) {
            channelIds.add(channel.getChannelId());
        }

        return channelIds;
    }

    public boolean isRunning() {
        return running;
    }

    public DonkeyConfiguration getConfiguration() {
        return donkeyConfiguration;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }
}
