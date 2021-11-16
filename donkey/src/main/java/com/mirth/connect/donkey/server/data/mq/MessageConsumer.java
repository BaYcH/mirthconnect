package com.mirth.connect.donkey.server.data.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.mq.*;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDao;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDaoFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-16 17:50
 */
public class MessageConsumer implements Runnable {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void run() {
        DonkeyDaoFactory daoFactory = Donkey.getInstance().getDaoFactory();
        KafkaDaoFactory kafkaDaoFactory;
        if (daoFactory instanceof KafkaDaoFactory) {
            kafkaDaoFactory = (KafkaDaoFactory) daoFactory;
        } else {
            return;
        }
        logger.info("开始启动消息队列监听服务");

        JdbcDaoFactory jdbcDaoFactory = kafkaDaoFactory.getJdbcDaoFactory();
        JedisPool jedisPool = kafkaDaoFactory.getJedisPool();
        KafkaConsumer<String, String> consumer = KafkaPool.getInstance().getConsumer();
        consumer.subscribe(Collections.singleton(KafkaDao.TOPIC_MESSAGE));
        while (true) {
            if (Thread.currentThread().interrupted()) {
                break;
            }
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(2000));
            if (records.isEmpty()) {
                continue;
            }
            try {
                JdbcDao jdbcDao = jdbcDaoFactory.getDao();
                for (ConsumerRecord<String, String> msg : records) {
                    Jedis jedis = jedisPool.getResource();
                    MessageIdRequest record = JSON.parseObject(msg.value(), MessageIdRequest.class);
                    String key = KafkaDao.TOPIC_MESSAGE + ":" + Donkey.getInstance().getConfiguration().getServerId() + ":" + record.getChannelId() + ":" + record.getMessageId();
                    System.out.println(key);
                    List<String> messages = Collections.EMPTY_LIST;
                    while ((messages = jedis.blpop(3, key)).size() > 1) {
                        String json = messages.get(1);
                        System.out.println(json);
                        MQMessage mqMessage = JSON.parseObject(json, MQMessage.class);
                        switch (mqMessage.getDaoTaskType()) {
                            case INSERT_MESSAGE:
                                jdbcDao.insertMessage(((JSONObject) mqMessage.getParameter()).toJavaObject(Message.class));
                                break;
                            case INSERT_CONNECTOR_MESSAGE:
                                ConnectorMessageRequest connectorMessageRequest = ((JSONObject) mqMessage.getParameter()).toJavaObject(ConnectorMessageRequest.class);
                                jdbcDao.insertConnectorMessage(connectorMessageRequest.getConnectorMessage(), connectorMessageRequest.isStoreMaps(), connectorMessageRequest.isUpdateStats());
                                break;
                            case INSERT_MESSAGE_CONTENT:
                                jdbcDao.insertMessageContent(((JSONObject) mqMessage.getParameter()).toJavaObject(MessageContent.class));
                                break;
                            case BATCH_INSERT_MESSAGE_CONTENT:
                                jdbcDao.batchInsertMessageContent(((JSONObject) mqMessage.getParameter()).toJavaObject(MessageContent.class));
                                break;
                            case EXECUTE_BATCH_INSERT_MESSAGE_CONTENT:
                                jdbcDao.executeBatchInsertMessageContent(mqMessage.getParameter().toString());
                                break;
                            case INSERT_MESSAGE_ATTACHMENT:
                                MessageAttachmentRequest messageAttachmentRequest = ((JSONObject) mqMessage.getParameter()).toJavaObject(MessageAttachmentRequest.class);
                                jdbcDao.insertMessageAttachment(messageAttachmentRequest.getChannelId(), messageAttachmentRequest.getMessageId(), messageAttachmentRequest.getAttachment());
                                break;
                            case INSERT_META_DATA:
                                MetaDataRequest metadataRequest = ((JSONObject) mqMessage.getParameter()).toJavaObject(MetaDataRequest.class);
                                jdbcDao.insertMetaData(metadataRequest.getConnectorMessage(), metadataRequest.getMetaDataColumns());
                                break;
                            case STORE_META_DATA:
                                MetaDataRequest metadataRequest1 = ((JSONObject) mqMessage.getParameter()).toJavaObject(MetaDataRequest.class);
                                jdbcDao.storeMetaData(metadataRequest1.getConnectorMessage(), metadataRequest1.getMetaDataColumns());
                                break;
                            case STORE_MESSAGE_CONTENT:
                                MessageContent messageContent1 = ((JSONObject) mqMessage.getParameter()).toJavaObject(MessageContent.class);
                                jdbcDao.storeMessageContent(messageContent1);
                                break;
                            case STORE_CHANNEL_STATISTICS:
                                Statistics statistics = ((JSONObject) mqMessage.getParameter()).toJavaObject(Statistics.class);
                                jdbcDao.addChannelStatistics(statistics);
                                break;
                            case UPDATE_STATUS:
                                UpDateStatusReuqest upDateStatusReuqest = ((JSONObject) mqMessage.getParameter()).toJavaObject(UpDateStatusReuqest.class);
                                jdbcDao.updateStatus(upDateStatusReuqest.getConnectorMessage(), upDateStatusReuqest.getPreviousStatus());
                                break;
                            case UPDATE_SEND_ATTEMPTS:
                                ConnectorMessage connectorMessage4 = ((JSONObject) mqMessage.getParameter()).toJavaObject(ConnectorMessage.class);
                                jdbcDao.updateSendAttempts(connectorMessage4);
                                break;
                            case UPDATE_ERRORS:
                                ConnectorMessage connectorMessage3 = ((JSONObject) mqMessage.getParameter()).toJavaObject(ConnectorMessage.class);
                                jdbcDao.updateErrors(connectorMessage3);
                                break;
                            case UPDATE_MAPS:
                                ConnectorMessage connectorMessage2 = ((JSONObject) mqMessage.getParameter()).toJavaObject(ConnectorMessage.class);
                                jdbcDao.updateMaps(connectorMessage2);
                                break;
                            case UPDATE_SOURCE_MAP:
                                ConnectorMessage connectorMessage1 = ((JSONObject) mqMessage.getParameter()).toJavaObject(ConnectorMessage.class);
                                jdbcDao.updateSourceMap(connectorMessage1);
                                break;
                            case UPDATE_RESPONSE_MAP:
                                ConnectorMessage connectorMessage = ((JSONObject) mqMessage.getParameter()).toJavaObject(ConnectorMessage.class);
                                jdbcDao.updateResponseMap(connectorMessage);
                                break;
                            case MARK_AS_PROCESSED:
                                MessageIdRequest messageIdRequest = ((JSONObject) mqMessage.getParameter()).toJavaObject(MessageIdRequest.class);
                                jdbcDao.markAsProcessed(messageIdRequest.getChannelId(), messageIdRequest.getMessageId());
                                break;
                            case RESET_MESSAGE:
                                MessageIdRequest messageIdRequest1 = ((JSONObject) mqMessage.getParameter()).toJavaObject(MessageIdRequest.class);
                                jdbcDao.resetMessage(messageIdRequest1.getChannelId(), messageIdRequest1.getMessageId());
                                break;
                            case DELETE_MESSAGE:
                                MessageIdRequest messageIdRequest2 = ((JSONObject) mqMessage.getParameter()).toJavaObject(MessageIdRequest.class);
                                jdbcDao.deleteMessage(messageIdRequest2.getChannelId(), messageIdRequest2.getMessageId());
                                break;
                            case DELETE_CONNECTOR_MESSAGES:
                                DeleteConnectorMessageRequest deleteConnectorMessageRequest = ((JSONObject) mqMessage.getParameter()).toJavaObject(DeleteConnectorMessageRequest.class);
                                jdbcDao.deleteConnectorMessages(deleteConnectorMessageRequest.getChannelId(), deleteConnectorMessageRequest.getMessageId(), deleteConnectorMessageRequest.getMetaDataIds());
                                break;
                            case DELETE_MESSAGE_STATISTICS:
                                DeleteConnectorMessageRequest deleteConnectorMessageRequest1 = ((JSONObject) mqMessage.getParameter()).toJavaObject(DeleteConnectorMessageRequest.class);
                                jdbcDao.deleteMessageStatistics(deleteConnectorMessageRequest1.getChannelId(), deleteConnectorMessageRequest1.getMessageId(), deleteConnectorMessageRequest1.getMetaDataIds());
                                break;
                            case DELETE_ALL_MESSAGES:
                                jdbcDao.deleteAllMessages((String) mqMessage.getParameter());
                                break;
                            case DELETE_MESSAGE_CONTENT:
                                MessageIdRequest messageIdRequest3 = ((JSONObject) mqMessage.getParameter()).toJavaObject(MessageIdRequest.class);
                                jdbcDao.deleteMessageContent(messageIdRequest3.getChannelId(), messageIdRequest3.getMessageId());
                                break;
                            case DELETE_MESSAGE_CONTENT_BY_META_DATA_IDS:
                                DeleteConnectorMessageRequest deleteConnectorMessageRequest2 = ((JSONObject) mqMessage.getParameter()).toJavaObject(DeleteConnectorMessageRequest.class);
                                jdbcDao.deleteMessageContentByMetaDataIds(deleteConnectorMessageRequest2.getChannelId(), deleteConnectorMessageRequest2.getMessageId(), deleteConnectorMessageRequest2.getMetaDataIds());
                                break;
                            case DELETE_MESSAGE_ATTACHMENTS:
                                MessageIdRequest messageIdRequest4 = ((JSONObject) mqMessage.getParameter()).toJavaObject(MessageIdRequest.class);
                                jdbcDao.deleteMessageAttachments(messageIdRequest4.getChannelId(), messageIdRequest4.getMessageId());
                                break;
                            default:
                                throw new RuntimeException("未知的处理模块！");
                        }
                        jdbcDao.commit(true);
                    }
                    jedisPool.returnResource(jedis);
                }
                consumer.commitSync();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
