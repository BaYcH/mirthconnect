package com.mirth.connect.donkey.model.mq;

import com.mirth.connect.donkey.model.message.attachment.Attachment;

import java.io.Serializable;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-11 15:46
 */
public class MessageAttachmentRequest implements Serializable {
    private String channelId;
    private long messageId;
    private Attachment attachment;

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

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}
