package com.mirth.connect.donkey.model.mq;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Status;

import java.io.Serializable;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-16 16:56
 */
public class UpDateStatusReuqest  implements Serializable {
    private ConnectorMessage connectorMessage;
    private Status previousStatus;

    public ConnectorMessage getConnectorMessage() {
        return connectorMessage;
    }

    public void setConnectorMessage(ConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }

    public Status getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Status previousStatus) {
        this.previousStatus = previousStatus;
    }
}
