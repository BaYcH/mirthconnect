package com.mirth.connect.donkey.model.mq;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;

import java.io.Serializable;
import java.util.List;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-11 15:52
 */
public class MetaDataRequest implements Serializable {
    private ConnectorMessage connectorMessage;
    private List<MetaDataColumn> metaDataColumns;

    public ConnectorMessage getConnectorMessage() {
        return connectorMessage;
    }

    public void setConnectorMessage(ConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }

    public List<MetaDataColumn> getMetaDataColumns() {
        return metaDataColumns;
    }

    public void setMetaDataColumns(List<MetaDataColumn> metaDataColumns) {
        this.metaDataColumns = metaDataColumns;
    }
}
