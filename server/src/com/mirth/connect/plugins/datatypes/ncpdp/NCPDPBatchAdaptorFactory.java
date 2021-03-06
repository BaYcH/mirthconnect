/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.ncpdp;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.model.ContextType;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class NCPDPBatchAdaptorFactory extends BatchAdaptorFactory {

    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private NCPDPBatchProperties batchProperties;

    public NCPDPBatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties serializerProperties) {
        super(sourceConnector);

        batchProperties = (NCPDPBatchProperties) serializerProperties.getBatchProperties();
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchMessageSource batchMessageSource) {
        NCPDPBatchAdaptor batchAdaptor = new NCPDPBatchAdaptor(this, sourceConnector, batchMessageSource);

        batchAdaptor.setBatchProperties(batchProperties);

        return batchAdaptor;
    }

    @Override
    public void onDeploy() throws DeployException {
        String batchScript = batchProperties.getBatchScript();

        if (StringUtils.isNotEmpty(batchScript)) {
            String batchScriptId = ScriptController.getScriptId(ScriptController.BATCH_SCRIPT_KEY, sourceConnector.getChannelId());

            try {
                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(sourceConnector.getChannel().getResourceIds());
                setContextFactoryId(contextFactory.getId());
                JavaScriptUtil.compileAndAddScript(sourceConnector.getChannelId(), contextFactory, batchScriptId, batchScript.toString(), ContextType.CHANNEL_BATCH);
            } catch (Exception e) {
                throw new DeployException("Error compiling " + sourceConnector.getConnectorProperties().getName() + " script " + batchScriptId + ".", e);
            }
        }
    }

    @Override
    public void onUndeploy() throws UndeployException {}
}
