/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.syntax.jedit.tokenmarker.TokenMarker;

import com.mirth.connect.model.ContextType;

public class ScriptPanel extends BasePanel {

    protected MirthEditorPane parent;

    public ScriptPanel(MirthEditorPane p, TokenMarker tokenMarker) {
        super();
        parent = p;
        initComponents();
        scriptTextPane.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent arg0) {
            }

            public void insertUpdate(DocumentEvent arg0) {
                parent.modified = true;
            }

            public void removeUpdate(DocumentEvent arg0) {
                parent.modified = true;
            }
        });
    }

    @Override
    public boolean isModified() {
        return StringUtils.isNotEmpty(scriptTextPane.getText());
    }

    @Override
    public Map<Object, Object> getData() {
        Map<Object, Object> m = new LinkedHashMap<Object, Object>();
        m.put("Script", scriptTextPane.getText().trim());
        return m;
    }

    @Override
    public void setData(Map<Object, Object> m) {
        boolean modified = parent.modified;

        if (m != null) {
            scriptTextPane.setText((String) m.get("Script"));
        } else {
            scriptTextPane.setText("");
        }

        parent.modified = modified;
    }

    public String getScript() {
        return scriptTextPane.getText();
    }

    public void setContextType(ContextType contextType) {
        scriptTextPane.setContextType(contextType);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scriptTextPane = new com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane(null, true);

        setBackground(new java.awt.Color(255, 255, 255));

        scriptTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scriptTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scriptTextPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane scriptTextPane;
    // End of variables declaration//GEN-END:variables
}
