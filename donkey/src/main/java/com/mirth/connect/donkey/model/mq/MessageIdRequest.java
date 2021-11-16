package com.mirth.connect.donkey.model.mq;

import java.io.Serializable;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-16 17:35
 */
public class MessageIdRequest implements Serializable {
    private String channelId;
    private long messageId;

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
