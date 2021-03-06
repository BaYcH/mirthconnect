/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.xsltstep;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.XsltStepPanel;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class XsltStepPlugin extends TransformerStepPlugin {

    private XsltStepPanel panel;
    private TransformerPane parent;

    public XsltStepPlugin(String name) {
        super(name);
    }

    @Override
    public void initialize(TransformerPane pane) {
        this.parent = pane;
        panel = new XsltStepPanel(parent);
    }

    @Override
    public BasePanel getPanel() {
        return panel;
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    @Override
    public Map<Object, Object> getData(int row) {
        Map<Object, Object> data = panel.getData();
        String sourceVar = data.get("Source").toString();
        String resultVar = data.get("Result").toString();

        // check for empty variable names
        if (sourceVar == null || sourceVar.equals("")) {

            parent.setInvalidVar(true);
            String msg = "The source field cannot be blank.\nPlease enter a new source.\n";
            parent.setRowSelectionInterval(row, row);
            parent.getParentFrame().alertWarning(parent.parent, msg);

        } else if (resultVar == null || resultVar.equals("")) {

            parent.setInvalidVar(true);
            String msg = "The result field cannot be blank.\nPlease enter a new result.\n";
            parent.setRowSelectionInterval(row, row);
            parent.getParentFrame().alertWarning(parent.parent, msg);

        } else {
            parent.setInvalidVar(false);
        }

        return data;
    }

    @Override
    public void setData(Mode mode, Map<Object, Object> data) {
        panel.setData(data);
    }

    @Override
    public void clearData() {
        panel.setData(null);
    }

    @Override
    public void initData() {
        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("Source", "");
        data.put("Result", "");
        data.put("XsltTemplate", "");
        data.put("Factory", "");
        panel.setData(data);
    }

    @Override
    public String getScript(Map<Object, Object> data) {

        StringBuilder script = new StringBuilder();

        String factory = (String) data.get("Factory");
        if (StringUtils.isNotBlank(factory)) {
            script.append("tFactory = Packages.javax.xml.transform.TransformerFactory.newInstance(\"" + factory + "\", null);\n");
        } else {
            script.append("tFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();\n");
        }

        script.append("xsltTemplate = new Packages.java.io.StringReader(" + data.get("XsltTemplate") + ");\n");
        script.append("transformer = tFactory.newTransformer(new Packages.javax.xml.transform.stream.StreamSource(xsltTemplate));\n");
        script.append("sourceVar = new Packages.java.io.StringReader(" + data.get("Source") + ");\n");
        script.append("resultVar = new Packages.java.io.StringWriter();\n");
        script.append("transformer.transform(new Packages.javax.xml.transform.stream.StreamSource(sourceVar), new Packages.javax.xml.transform.stream.StreamResult(resultVar));\n");
        script.append("channelMap.put('" + data.get("Result") + "', resultVar.toString());\n");

        return script.toString();
    }

    @Override
    public String getGeneratedScript(Map<Object, Object> data) {
        return getScript(data);
    }

    @Override
    public boolean showValidateTask() {
        return true;
    }

    @Override
    public String doValidate(Map<Object, Object> data) {
        try {
            Context context = Context.enter();
            Script compiledFilterScript = context.compileString("function rhinoWrapper() {" + getScript(data) + "\n}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null);
        } catch (EvaluatorException e) {
            return "Error on line " + e.lineNumber() + ": " + e.getMessage() + ".";
        } catch (Exception e) {
            return "Unknown error occurred during validation.";
        } finally {
            Context.exit();
        }
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }

    @Override
    public String getPluginPointName() {
        return "XSLT Step";
    }
}
