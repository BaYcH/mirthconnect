package com.mirth.connect.donkey.model.mq;

import com.mirth.connect.donkey.model.message.ConnectorMessage;

import java.io.Serializable;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-11 15:36
 */
public class ConnectorMessageRequest implements Serializable {
    private ConnectorMessage connectorMessage;
    private boolean storeMaps;
    private boolean updateStats;

    public ConnectorMessage getConnectorMessage() {
        return connectorMessage;
    }

    public void setConnectorMessage(ConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }

    public boolean isStoreMaps() {
        return storeMaps;
    }

    public void setStoreMaps(boolean storeMaps) {
        this.storeMaps = storeMaps;
    }

    public boolean isUpdateStats() {
        return updateStats;
    }

    public void setUpdateStats(boolean updateStats) {
        this.updateStats = updateStats;
    }
}
