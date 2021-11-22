package com.mirth.connect.donkey.server.data.mq;

import com.google.common.util.concurrent.RateLimiter;
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

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-16 17:50
 */
public class MessageConsumer implements Runnable {

    private Logger logger = Logger.getLogger(getClass());

    private final RateLimiter rateLimiter = RateLimiter.create(15);

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
        Base64.Decoder decoder = Base64.getDecoder();
        int index = 0;
        int pullTime = 500;
        while (true) {
            if (Thread.currentThread().interrupted()) {
                break;
            }

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pullTime));
            if (index > 60) {
                pullTime = 3000;
            }
            if (records.isEmpty()) {
                index++;
                continue;
            }

            index = 0;
            pullTime = 500;

            JdbcDao jdbcDao = jdbcDaoFactory.getDao();
            for (ConsumerRecord<String, String> msg : records) {
                try {
                    Jedis jedis = jedisPool.getResource();
                    String key = msg.value();
                    List<String> messages = Collections.EMPTY_LIST;
                    String message = "";
                    while ((message = jedis.lpop(key)) != null && null != message && message.length() > 0) {

                        while (!rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                            System.out.println("已限流!");
                        }

                        String base64 = message;//messages.get(1);
                        try {
                            byte[] decode = decoder.decode(base64);
                            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(decode));
                            MQMessage mqMessage = (MQMessage) objectInputStream.readObject();
                            switch (mqMessage.getDaoTaskType()) {
                                case INSERT_MESSAGE:
                                    jdbcDao.insertMessage((Message) mqMessage.getParameter());
                                    break;
                                case INSERT_CONNECTOR_MESSAGE:
                                    ConnectorMessageRequest connectorMessageRequest = (ConnectorMessageRequest) mqMessage.getParameter();
                                    jdbcDao.insertConnectorMessage(connectorMessageRequest.getConnectorMessage(), connectorMessageRequest.isStoreMaps(), connectorMessageRequest.isUpdateStats());
                                    break;
                                case INSERT_MESSAGE_CONTENT:
                                    jdbcDao.insertMessageContent((MessageContent) mqMessage.getParameter());
                                    break;
                                case BATCH_INSERT_MESSAGE_CONTENT:
                                    jdbcDao.batchInsertMessageContent((MessageContent) mqMessage.getParameter());
                                    break;
                                case EXECUTE_BATCH_INSERT_MESSAGE_CONTENT:
                                    jdbcDao.executeBatchInsertMessageContent(mqMessage.getParameter().toString());
                                    break;
                                case INSERT_MESSAGE_ATTACHMENT:
                                    MessageAttachmentRequest messageAttachmentRequest = (MessageAttachmentRequest) mqMessage.getParameter();
                                    jdbcDao.insertMessageAttachment(messageAttachmentRequest.getChannelId(), messageAttachmentRequest.getMessageId(), messageAttachmentRequest.getAttachment());
                                    break;
                                case INSERT_META_DATA:
                                    MetaDataRequest metadataRequest = (MetaDataRequest) mqMessage.getParameter();
                                    jdbcDao.insertMetaData(metadataRequest.getConnectorMessage(), metadataRequest.getMetaDataColumns());
                                    break;
                                case STORE_META_DATA:
                                    MetaDataRequest metadataRequest1 = (MetaDataRequest) mqMessage.getParameter();
                                    jdbcDao.storeMetaData(metadataRequest1.getConnectorMessage(), metadataRequest1.getMetaDataColumns());
                                    break;
                                case STORE_MESSAGE_CONTENT:
                                    MessageContent messageContent1 = (MessageContent) mqMessage.getParameter();
                                    jdbcDao.storeMessageContent(messageContent1);
                                    break;
                                case STORE_CHANNEL_STATISTICS:
                                    Statistics statistics = (Statistics) mqMessage.getParameter();
                                    jdbcDao.addChannelStatistics(statistics);
                                    break;
                                case UPDATE_STATUS:
                                    UpDateStatusReuqest upDateStatusReuqest = (UpDateStatusReuqest) mqMessage.getParameter();
                                    jdbcDao.updateStatus(upDateStatusReuqest.getConnectorMessage(), upDateStatusReuqest.getPreviousStatus());
                                    break;
                                case UPDATE_SEND_ATTEMPTS:
                                    ConnectorMessage connectorMessage4 = (ConnectorMessage) mqMessage.getParameter();
                                    jdbcDao.updateSendAttempts(connectorMessage4);
                                    break;
                                case UPDATE_ERRORS:
                                    ConnectorMessage connectorMessage3 = (ConnectorMessage) mqMessage.getParameter();
                                    jdbcDao.updateErrors(connectorMessage3);
                                    break;
                                case UPDATE_MAPS:
                                    ConnectorMessage connectorMessage2 = (ConnectorMessage) mqMessage.getParameter();
                                    jdbcDao.updateMaps(connectorMessage2);
                                    break;
                                case UPDATE_SOURCE_MAP:
                                    ConnectorMessage connectorMessage1 = (ConnectorMessage) mqMessage.getParameter();
                                    jdbcDao.updateSourceMap(connectorMessage1);
                                    break;
                                case UPDATE_RESPONSE_MAP:
                                    ConnectorMessage connectorMessage = (ConnectorMessage) mqMessage.getParameter();
                                    jdbcDao.updateResponseMap(connectorMessage);
                                    break;
                                case MARK_AS_PROCESSED:
                                    MessageIdRequest messageIdRequest = (MessageIdRequest) mqMessage.getParameter();
                                    jdbcDao.markAsProcessed(messageIdRequest.getChannelId(), messageIdRequest.getMessageId());
                                    break;
                                case RESET_MESSAGE:
                                    MessageIdRequest messageIdRequest1 = (MessageIdRequest) mqMessage.getParameter();
                                    jdbcDao.resetMessage(messageIdRequest1.getChannelId(), messageIdRequest1.getMessageId());
                                    break;
                                case DELETE_MESSAGE:
                                    MessageIdRequest messageIdRequest2 = (MessageIdRequest) mqMessage.getParameter();
                                    jdbcDao.deleteMessage(messageIdRequest2.getChannelId(), messageIdRequest2.getMessageId());
                                    break;
                                case DELETE_CONNECTOR_MESSAGES:
                                    DeleteConnectorMessageRequest deleteConnectorMessageRequest = (DeleteConnectorMessageRequest) mqMessage.getParameter();
                                    jdbcDao.deleteConnectorMessages(deleteConnectorMessageRequest.getChannelId(), deleteConnectorMessageRequest.getMessageId(), deleteConnectorMessageRequest.getMetaDataIds());
                                    break;
                                case DELETE_MESSAGE_STATISTICS:
                                    DeleteConnectorMessageRequest deleteConnectorMessageRequest1 = (DeleteConnectorMessageRequest) mqMessage.getParameter();
                                    jdbcDao.deleteMessageStatistics(deleteConnectorMessageRequest1.getChannelId(), deleteConnectorMessageRequest1.getMessageId(), deleteConnectorMessageRequest1.getMetaDataIds());
                                    break;
                                case DELETE_ALL_MESSAGES:
                                    jdbcDao.deleteAllMessages((String) mqMessage.getParameter());
                                    break;
                                case DELETE_MESSAGE_CONTENT:
                                    MessageIdRequest messageIdRequest3 = (MessageIdRequest) mqMessage.getParameter();
                                    jdbcDao.deleteMessageContent(messageIdRequest3.getChannelId(), messageIdRequest3.getMessageId());
                                    break;
                                case DELETE_MESSAGE_CONTENT_BY_META_DATA_IDS:
                                    DeleteConnectorMessageRequest deleteConnectorMessageRequest2 = (DeleteConnectorMessageRequest) mqMessage.getParameter();
                                    jdbcDao.deleteMessageContentByMetaDataIds(deleteConnectorMessageRequest2.getChannelId(), deleteConnectorMessageRequest2.getMessageId(), deleteConnectorMessageRequest2.getMetaDataIds());
                                    break;
                                case DELETE_MESSAGE_ATTACHMENTS:
                                    MessageIdRequest messageIdRequest4 = (MessageIdRequest) mqMessage.getParameter();
                                    jdbcDao.deleteMessageAttachments(messageIdRequest4.getChannelId(), messageIdRequest4.getMessageId());
                                    break;
                                case COMMIT:
                                    jdbcDao.commit(true);
                                    jedis.expire(key, Long.valueOf(60));
                                    break;
                                default:
                                    throw new RuntimeException("未知的处理模块！");
                            }
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                    jedis.close();

                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
            try {
                jdbcDao.commit(true);
                consumer.commitSync();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        consumer.close();
    }
}
