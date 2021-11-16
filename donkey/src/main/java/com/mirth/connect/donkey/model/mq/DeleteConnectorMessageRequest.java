package com.mirth.connect.donkey.model.mq;

import java.io.Serializable;
import java.util.Set;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-16 17:37
 */
public class DeleteConnectorMessageRequest implements Serializable {
    private String channelId;
    private long messageId;
    private Set<Integer> metaDataIds;

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

    public Set<Integer> getMetaDataIds() {
        return metaDataIds;
    }

    public void setMetaDataIds(Set<Integer> metaDataIds) {
        this.metaDataIds = metaDataIds;
    }
}
