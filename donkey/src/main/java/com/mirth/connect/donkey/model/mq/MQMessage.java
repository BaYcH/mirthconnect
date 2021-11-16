package com.mirth.connect.donkey.model.mq;

import com.mirth.connect.donkey.server.data.buffered.DaoTaskType;

import java.io.Serializable;

/**
 * @author BaYcT
 * @version 1.0
 * @description:
 * @date 2021-11-15 15:29
 */
public class MQMessage implements Serializable {
    private DaoTaskType daoTaskType;
    private Object parameter;

    public Object getParameter() {
        return parameter;
    }

    public void setParameter(Object parameter) {
        this.parameter = parameter;
    }

    public DaoTaskType getDaoTaskType() {
        return daoTaskType;
    }

    public void setDaoTaskType(DaoTaskType daoTaskType) {
        this.daoTaskType = daoTaskType;
    }
}
