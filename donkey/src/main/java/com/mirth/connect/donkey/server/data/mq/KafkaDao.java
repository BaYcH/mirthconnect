package com.mirth.connect.donkey.server.data.mq;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.mq.*;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.server.data.buffered.DaoTaskType;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDao;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-10 16:40
 */
public class KafkaDao implements DonkeyDao {

    private JedisPool jedisPool;
    private JdbcDao jdbcDao;
    private String mirthServerKey;
    private String serverId;
    private String channelId;
    private long messageId;
    private String sessionId;

    public static final String TOPIC_MESSAGE = "MIRTH_DATA_MESSAGE";
    public static final String TOPIC_CHANNEL = "MIRTH_CHANNEL_ID";
    private static final String TOPIC_CONNECTOR_MESSAGE = "MIRTH_DATA_CONNECTOR_MESSAGE";
    private static final String TOPIC_MESSAGE_CONTENT = "MIRTH_DATA_MESSAGE_CONTENT";
    private static final String TOPIC_BATCH_MESSAGE_CONTENT = "MIRTH_DATA_BATCH_MESSAGE_CONTENT";
    private static final String TOPIC_BATCH_MESSAGE_CONTENT_CHANNEL_ID = "MIRTH_DATA_BATCH_MESSAGE_CONTENT_CHANNEL_ID";
    private static final String TOPIC_MESSAGE_ATTACHMENT = "MIRTH_DATA_MESSAGE_ATTACHMENT";
    private static final String TOPIC_INSERT_METADATA = "MIRTH_DATA_INSERT_METADATA";
    private static final String TOPIC_STORE_METADATA = "MIRTH_DATA_STORE_METADATA";
    private static final String TOPIC_STORE_MESSAGE_CONTENT = "MIRTH_DATA_STORE_MESSAGE_CONTENT";

    public KafkaDao() {
    }

    @Override
    public void insertMessage(Message message) {
        redisPush(DaoTaskType.INSERT_MESSAGE, message, message.getChannelId(), message.getMessageId());
    }

    @Override
    public void insertConnectorMessage(ConnectorMessage connectorMessage, boolean storeMaps, boolean updateStats) {
        ConnectorMessageRequest connectorMessageRequest = new ConnectorMessageRequest();
        connectorMessageRequest.setConnectorMessage(connectorMessage);
        connectorMessageRequest.setStoreMaps(storeMaps);
        connectorMessageRequest.setUpdateStats(updateStats);

        redisPush(DaoTaskType.INSERT_CONNECTOR_MESSAGE, connectorMessageRequest, connectorMessage.getChannelId(), connectorMessage.getMessageId());
    }

    @Override
    public void insertMessageContent(MessageContent messageContent) {
        redisPush(DaoTaskType.INSERT_MESSAGE_CONTENT, messageContent, messageContent.getChannelId(), messageContent.getMessageId());
    }

    @Override
    public void batchInsertMessageContent(MessageContent messageContent) {
        redisPush(DaoTaskType.BATCH_INSERT_MESSAGE_CONTENT, messageContent, messageContent.getChannelId(), messageContent.getMessageId());
    }

    @Override
    public void executeBatchInsertMessageContent(String channelId) {
        redisPush(DaoTaskType.EXECUTE_BATCH_INSERT_MESSAGE_CONTENT, channelId, channelId, -1);
        //jdbcDao.executeBatchInsertMessageContent(channelId);
    }

    @Override
    public void insertMessageAttachment(String channelId, long messageId, Attachment attachment) {
        MessageAttachmentRequest messageAttachmentRequest = new MessageAttachmentRequest();
        messageAttachmentRequest.setAttachment(attachment);
        messageAttachmentRequest.setChannelId(channelId);
        messageAttachmentRequest.setMessageId(messageId);

        redisPush(DaoTaskType.INSERT_MESSAGE_ATTACHMENT, messageAttachmentRequest, channelId, messageId);
    }

    @Override
    public void insertMetaData(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns) {
        MetaDataRequest metaDataRequest = new MetaDataRequest();
        metaDataRequest.setConnectorMessage(connectorMessage);
        metaDataRequest.setMetaDataColumns(metaDataColumns);

        redisPush(DaoTaskType.INSERT_META_DATA, metaDataRequest, connectorMessage.getChannelId(), connectorMessage.getMessageId());
    }

    @Override
    public void storeMetaData(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns) {
        MetaDataRequest metaDataRequest = new MetaDataRequest();
        metaDataRequest.setConnectorMessage(connectorMessage);
        metaDataRequest.setMetaDataColumns(metaDataColumns);

        redisPush(DaoTaskType.STORE_META_DATA, metaDataRequest, connectorMessage.getChannelId(), connectorMessage.getMessageId());
    }

    @Override
    public void storeMessageContent(MessageContent messageContent) {
        redisPush(DaoTaskType.STORE_MESSAGE_CONTENT, messageContent, messageContent.getChannelId(), messageContent.getMessageId());
    }

    @Override
    public void addChannelStatistics(Statistics statistics) {
        redisPush(DaoTaskType.STORE_CHANNEL_STATISTICS, statistics, "", -1);
        //jdbcDao.addChannelStatistics(statistics);
    }

    @Override
    public void updateSendAttempts(ConnectorMessage connectorMessage) {
        redisPush(DaoTaskType.UPDATE_SEND_ATTEMPTS, connectorMessage, connectorMessage.getChannelId(), connectorMessage.getMessageId());
    }

    @Override
    public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus) {
        UpDateStatusReuqest upDateStatusReuqest = new UpDateStatusReuqest();
        upDateStatusReuqest.setConnectorMessage(connectorMessage);
        upDateStatusReuqest.setPreviousStatus(previousStatus);
        redisPush(DaoTaskType.UPDATE_STATUS, upDateStatusReuqest, connectorMessage.getChannelId(), connectorMessage.getMessageId());
    }

    @Override
    public void updateErrors(ConnectorMessage connectorMessage) {
        redisPush(DaoTaskType.UPDATE_ERRORS, connectorMessage, connectorMessage.getChannelId(), connectorMessage.getMessageId());

    }

    @Override
    public void updateMaps(ConnectorMessage connectorMessage) {
        redisPush(DaoTaskType.UPDATE_MAPS, connectorMessage, connectorMessage.getChannelId(), connectorMessage.getMessageId());
    }

    @Override
    public void updateSourceMap(ConnectorMessage connectorMessage) {
        redisPush(DaoTaskType.UPDATE_SOURCE_MAP, connectorMessage, connectorMessage.getChannelId(), connectorMessage.getMessageId());
    }

    @Override
    public void updateResponseMap(ConnectorMessage connectorMessage) {
        redisPush(DaoTaskType.UPDATE_RESPONSE_MAP, connectorMessage, connectorMessage.getChannelId(), connectorMessage.getMessageId());
    }

    @Override
    public void markAsProcessed(String channelId, long messageId) {
        MessageIdRequest messageIdRequest = new MessageIdRequest();
        messageIdRequest.setChannelId(channelId);
        messageIdRequest.setMessageId(messageId);
        redisPush(DaoTaskType.MARK_AS_PROCESSED, messageIdRequest, channelId, messageId);
    }

    @Override
    public void resetMessage(String channelId, long messageId) {
        MessageIdRequest messageIdRequest = new MessageIdRequest();
        messageIdRequest.setChannelId(channelId);
        messageIdRequest.setMessageId(messageId);
        redisPush(DaoTaskType.RESET_MESSAGE, messageIdRequest, channelId, messageId);
    }

    @Override
    public void deleteMessage(String channelId, long messageId) {
        MessageIdRequest messageIdRequest = new MessageIdRequest();
        messageIdRequest.setChannelId(channelId);
        messageIdRequest.setMessageId(messageId);
        redisPush(DaoTaskType.DELETE_MESSAGE, messageIdRequest, channelId, messageId);
    }

    @Override
    public void deleteConnectorMessages(String channelId, long messageId, Set<Integer> metaDataIds) {
        DeleteConnectorMessageRequest deleteConnectorMessageRequest = new DeleteConnectorMessageRequest();
        deleteConnectorMessageRequest.setChannelId(channelId);
        deleteConnectorMessageRequest.setMessageId(messageId);
        deleteConnectorMessageRequest.setMetaDataIds(metaDataIds);
        redisPush(DaoTaskType.DELETE_CONNECTOR_MESSAGES, deleteConnectorMessageRequest, channelId, messageId);

    }

    @Override
    public void deleteMessageContent(String channelId, long messageId) {
        MessageIdRequest messageIdRequest = new MessageIdRequest();
        messageIdRequest.setChannelId(channelId);
        messageIdRequest.setMessageId(messageId);
        redisPush(DaoTaskType.DELETE_MESSAGE_CONTENT, messageIdRequest, channelId, messageId);
    }

    @Override
    public void deleteMessageContentByMetaDataIds(String channelId, long messageId, Set<Integer> metaDataIds) {
        DeleteConnectorMessageRequest deleteConnectorMessageRequest = new DeleteConnectorMessageRequest();
        deleteConnectorMessageRequest.setChannelId(channelId);
        deleteConnectorMessageRequest.setMessageId(messageId);
        deleteConnectorMessageRequest.setMetaDataIds(metaDataIds);
        redisPush(DaoTaskType.DELETE_MESSAGE_CONTENT_BY_META_DATA_IDS, deleteConnectorMessageRequest, channelId, messageId);
    }

    @Override
    public void deleteMessageAttachments(String channelId, long messageId) {
        MessageIdRequest messageIdRequest = new MessageIdRequest();
        messageIdRequest.setChannelId(channelId);
        messageIdRequest.setMessageId(messageId);
        redisPush(DaoTaskType.DELETE_MESSAGE_ATTACHMENTS, messageIdRequest, channelId, messageId);
    }

    @Override
    public void deleteMessageStatistics(String channelId, long messageId, Set<Integer> metaDataIds) {
        DeleteConnectorMessageRequest deleteConnectorMessageRequest = new DeleteConnectorMessageRequest();
        deleteConnectorMessageRequest.setChannelId(channelId);
        deleteConnectorMessageRequest.setMessageId(messageId);
        deleteConnectorMessageRequest.setMetaDataIds(metaDataIds);
        redisPush(DaoTaskType.DELETE_MESSAGE_STATISTICS, deleteConnectorMessageRequest, channelId, messageId);
    }

    @Override
    public void deleteAllMessages(String channelId) {
        jdbcDao.deleteAllMessages(channelId);
    }

    @Override
    public void createChannel(String channelId, long localChannelId) {
        jdbcDao.createChannel(channelId, localChannelId);
    }

    @Override
    public void checkAndCreateChannelTables() {
        jdbcDao.checkAndCreateChannelTables();
    }

    @Override
    public void removeChannel(String channelId) {
        jdbcDao.removeChannel(channelId);
    }

    @Override
    public void addMetaDataColumn(String channelId, MetaDataColumn metaDataColumn) {
        jdbcDao.addMetaDataColumn(channelId, metaDataColumn);
    }

    @Override
    public void removeMetaDataColumn(String channelId, String columnName) {
        jdbcDao.removeMetaDataColumn(channelId, columnName);
    }

    @Override
    public void resetStatistics(String channelId, Integer metaDataId, Set<Status> statuses) {
        jdbcDao.resetStatistics(channelId, metaDataId, statuses);
    }

    @Override
    public void resetAllStatistics(String channelId) {
        jdbcDao.resetAllStatistics(channelId);
    }

    @Override
    public Long selectMaxLocalChannelId() {
        return jdbcDao.selectMaxLocalChannelId();
    }

    @Override
    public Map<String, Long> getLocalChannelIds() {
        return jdbcDao.getLocalChannelIds();
    }

    @Override
    public long getMaxMessageId(String channelId) {
        return jdbcDao.getMaxMessageId(channelId);
    }

    @Override
    public long getMinMessageId(String channelId) {
        return jdbcDao.getMinMessageId(channelId);
    }

    @Override
    public long getNextMessageId(String channelId) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.incr(TOPIC_CHANNEL + ":" + channelId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            jedis.close();
        }
    }

    @Override
    public List<Message> getMessages(String channelId, List<Long> messageIds) {
        return jdbcDao.getMessages(channelId, messageIds);
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, String serverId, int metaDataId, Status status, int offset, int limit, Long minMessageId, Long maxMessageId) {
        return jdbcDao.getConnectorMessages(channelId, serverId, metaDataId, status, offset, limit, minMessageId, maxMessageId);
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, long messageId, Set<Integer> metaDataIds, boolean includeContent) {
        return jdbcDao.getConnectorMessages(channelId, messageId, metaDataIds, includeContent);
    }

    @Override
    public Map<Integer, ConnectorMessage> getConnectorMessages(String channelId, long messageId, List<Integer> metaDataIds) {
        return jdbcDao.getConnectorMessages(channelId, messageId, metaDataIds);
    }

    @Override
    public int getConnectorMessageCount(String channelId, String serverId, int metaDataId, Status status) {
        return jdbcDao.getConnectorMessageCount(channelId, serverId, metaDataId, status);
    }

    @Override
    public long getConnectorMessageMaxMessageId(String channelId, String serverId, int metaDataId, Status status) {
        return jdbcDao.getConnectorMessageMaxMessageId(channelId, serverId, metaDataId, status);
    }

    @Override
    public Map<Integer, Status> getConnectorMessageStatuses(String channelId, long messageId, boolean checkProcessed) {
        return jdbcDao.getConnectorMessageStatuses(channelId, messageId, checkProcessed);
    }

    @Override
    public List<Message> getUnfinishedMessages(String channelId, String serverId, int limit, Long minMessageId) {
        return jdbcDao.getUnfinishedMessages(channelId, serverId, limit, minMessageId);
    }

    @Override
    public List<Message> getPendingConnectorMessages(String channelId, String serverId, int limit, Long minMessageId) {
        return jdbcDao.getPendingConnectorMessages(channelId, serverId, limit, minMessageId);
    }

    @Override
    public List<MetaDataColumn> getMetaDataColumns(String channelId) {
        return jdbcDao.getMetaDataColumns(channelId);
    }

    @Override
    public List<Attachment> getMessageAttachment(String channelId, long messageId) {
        return jdbcDao.getMessageAttachment(channelId, messageId);
    }

    @Override
    public Attachment getMessageAttachment(String channelId, String attachmentId, Long messageId) {
        return jdbcDao.getMessageAttachment(channelId, attachmentId, messageId);
    }

    @Override
    public Statistics getChannelStatistics(String serverId) {
        return jdbcDao.getChannelStatistics(serverId);
    }

    @Override
    public Statistics getChannelTotalStatistics(String serverId) {
        return jdbcDao.getChannelTotalStatistics(serverId);
    }

    @Override
    public void setEncryptData(boolean encryptData) {
        jdbcDao.setEncryptData(encryptData);
    }

    @Override
    public void setDecryptData(boolean decryptData) {
        jdbcDao.setDecryptData(decryptData);
    }

    @Override
    public void setStatisticsUpdater(StatisticsUpdater statisticsUpdater) {
        jdbcDao.setStatisticsUpdater(statisticsUpdater);
    }

    @Override
    public void commit() {
        commit(true);
    }

    @Override
    public void commit(boolean durable) {
        jdbcDao.commit(durable);
    }

    @Override
    public void rollback() {
        jdbcDao.rollback();
    }

    @Override
    public void close() {
        jdbcDao.close();
    }

    @Override
    public boolean isClosed() {
        return jdbcDao.isClosed();
    }

    public JdbcDao getJdbcDao() {
        return jdbcDao;
    }

    public void setJdbcDao(JdbcDao jdbcDao) {
        this.jdbcDao = jdbcDao;
    }

    public String getMirthServerKey() {
        return mirthServerKey;
    }

    public void setMirthServerKey(String mirthServerKey) {
        this.mirthServerKey = mirthServerKey;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    private String getRedisMessageKey(String channelId, long messageId) {
        return TOPIC_MESSAGE + ":" + channelId + ":" + messageId;
    }

    public void redisPush(DaoTaskType daoTaskType, Object parameter, String channelId, long messageId) {
        MQMessage mqMessage = new MQMessage();
        mqMessage.setParameter(parameter);
        mqMessage.setDaoTaskType(daoTaskType);

        if (channelId != "" && messageId > -1) {
            setChannelId(channelId);
            setMessageId(messageId);
        }
        System.out.println(messageId + "：准备获取jedis");
        Jedis jedis = jedisPool.getResource();
        System.out.println(messageId + ":已获取");
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(byteArrayOutputStream);
            oo.writeObject(mqMessage);
            Base64.Encoder encoder = Base64.getEncoder();
            String encode = encoder.encodeToString(byteArrayOutputStream.toByteArray());
            jedis.rpush(getRedisMessageKey(getChannelId(), getMessageId()), encode);
            jedis.expire(getRedisMessageKey(channelId, messageId), Long.valueOf(3600 * 24));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    public void sendCommit() {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_MESSAGE, getRedisMessageKey(getChannelId(), getMessageId()));
        KafkaProducer<String, String> producer = KafkaPool.getInstance().getProducer();
        producer.send(producerRecord);
        KafkaPool.getInstance().returnProducer(producer);
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
