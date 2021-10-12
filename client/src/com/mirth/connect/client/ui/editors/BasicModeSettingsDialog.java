/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.*;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.GroupLayout;

import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import com.mirth.connect.client.ui.*;

import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.*;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.plugins.BasicModeClientProvider;
import com.mirth.connect.util.TcpUtil;

public class BasicModeSettingsDialog extends MirthDialog implements DocumentListener {

    private boolean saved;
    private String startOfMessageAbbreviation;
    private String endOfMessageAbbreviation;
    private ActionListener actionListener;

    /**
     * Creates new form BasicModeSettingsDialog
     */
    public BasicModeSettingsDialog(ActionListener actionListener) {
        super(PlatformUI.MIRTH_FRAME);
        initComponents();
        this.actionListener = actionListener;

        startOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, false, false));
        endOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, false, false));

        startOfMessageBytesField.getDocument().addDocumentListener(this);
        endOfMessageBytesField.getDocument().addDocumentListener(this);

        startOfMessageAbbreviation = "";
        endOfMessageAbbreviation = "";

        changeAbbreviation();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            Dimension dlgSize = getPreferredSize();
            Dimension frmSize = getParent().getSize();
            Point loc = getParent().getLocation();

            if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
                setLocationRelativeTo(null);
            } else {
                setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
            }

            saved = false;
            resetInvalidProperties();
        }

        super.setVisible(b);
    }

    public TransmissionModeProperties getProperties() {
        FrameModeProperties props = new FrameModeProperties();

        props.setStartOfMessageBytes(startOfMessageBytesField.getText());
        props.setEndOfMessageBytes(endOfMessageBytesField.getText());

        return props;
    }

    public void setProperties(TransmissionModeProperties properties) {
        FrameModeProperties props = (FrameModeProperties) properties;

        startOfMessageBytesField.setText(props.getStartOfMessageBytes());
        endOfMessageBytesField.setText(props.getEndOfMessageBytes());

        startOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(startOfMessageBytesField.getText());
        endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(endOfMessageBytesField.getText());
    }

    public String checkProperties() {
        resetInvalidProperties();
        String errors = "";

        if (!TcpUtil.isValidHexString(startOfMessageBytesField.getText())) {
            errors += "Invalid start of message bytes.\r\n";
            startOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (!TcpUtil.isValidHexString(endOfMessageBytesField.getText())) {
            errors += "Invalid end of message bytes.\r\n";
            endOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
        }

        return errors;
    }

    public void resetInvalidProperties() {
        startOfMessageBytesField.setBackground(null);
        endOfMessageBytesField.setBackground(null);
    }

    public boolean isSaved() {
        return saved;
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    private void changeAbbreviation(DocumentEvent evt) {
        String text = "";

        try {
            text = evt.getDocument().getText(0, evt.getDocument().getLength()).trim();
        } catch (BadLocationException e) {
        }

        if (evt.getDocument().equals(startOfMessageBytesField.getDocument())) {
            startOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(text);
            actionListener.actionPerformed(new ActionEvent(startOfMessageBytesField, ActionEvent.ACTION_PERFORMED, BasicModeClientProvider.CHANGE_START_BYTES_COMMAND));
        } else if (evt.getDocument().equals(endOfMessageBytesField.getDocument())) {
            endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(text);
            actionListener.actionPerformed(new ActionEvent(endOfMessageBytesField, ActionEvent.ACTION_PERFORMED, BasicModeClientProvider.CHANGE_END_BYTES_COMMAND));
        }

        changeAbbreviation();
    }

    private void changeAbbreviation() {
        startOfMessageBytesAbbrevLabel.setText(startOfMessageAbbreviation);
        endOfMessageBytesAbbrevLabel.setText(endOfMessageAbbreviation);
        pack();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new JPanel();
        jPanel2 = new JPanel();
        startOfMessageBytesAbbrevLabel = new JLabel();
        startOfMessageBytesLabel = new JLabel();
        endOfMessageBytesLabel = new JLabel();
        startOfMessageBytesField = new MirthTextField();
        startOfMessageBytes0XLabel = new JLabel();
        endOfMessageBytesAbbrevLabel = new JLabel();
        endOfMessageBytesField = new MirthTextField();
        endOfMessageBytes0XLabel = new JLabel();
        jSeparator1 = new JSeparator();
        cancelButton = new JButton();
        okButton = new JButton();
        byteAbbreviationList1 = new ByteAbbreviationList();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Transmission Mode Settings");
        setModal(true);
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setBackground(Color.white);

            //======== jPanel2 ========
            {
                jPanel2.setBackground(Color.white);
                jPanel2.setBorder(new TitledBorder("Basic Settings"));
                jPanel2.setMinimumSize(new Dimension(323, 0));

                //---- startOfMessageBytesAbbrevLabel ----
                startOfMessageBytesAbbrevLabel.setText("<VT>");
                startOfMessageBytesAbbrevLabel.setToolTipText("<html>The bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

                //---- startOfMessageBytesLabel ----
                startOfMessageBytesLabel.setText("Start of Message Bytes:");
                startOfMessageBytesLabel.setToolTipText("<html>The bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

                //---- endOfMessageBytesLabel ----
                endOfMessageBytesLabel.setText("End of Message Bytes:");
                endOfMessageBytesLabel.setToolTipText("<html>The bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

                //---- startOfMessageBytesField ----
                startOfMessageBytesField.setToolTipText("<html>The bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");
                startOfMessageBytesField.addActionListener(e -> startOfMessageBytesFieldActionPerformed(e));

                //---- startOfMessageBytes0XLabel ----
                startOfMessageBytes0XLabel.setText("0x");
                startOfMessageBytes0XLabel.setToolTipText("<html>The bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

                //---- endOfMessageBytesAbbrevLabel ----
                endOfMessageBytesAbbrevLabel.setText("<FS><CR>");
                endOfMessageBytesAbbrevLabel.setToolTipText("<html>The bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

                //---- endOfMessageBytesField ----
                endOfMessageBytesField.setToolTipText("<html>The bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");
                endOfMessageBytesField.addActionListener(e -> endOfMessageBytesFieldActionPerformed(e));

                //---- endOfMessageBytes0XLabel ----
                endOfMessageBytes0XLabel.setText("0x");
                endOfMessageBytes0XLabel.setToolTipText("<html>The bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

                GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel2Layout.createParallelGroup()
                                .addComponent(startOfMessageBytesLabel, GroupLayout.Alignment.TRAILING)
                                .addComponent(endOfMessageBytesLabel, GroupLayout.Alignment.TRAILING))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel2Layout.createParallelGroup()
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(startOfMessageBytes0XLabel)
                                    .addGap(3, 3, 3)
                                    .addComponent(startOfMessageBytesField, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(startOfMessageBytesAbbrevLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(endOfMessageBytes0XLabel)
                                    .addGap(3, 3, 3)
                                    .addComponent(endOfMessageBytesField, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(endOfMessageBytesAbbrevLabel, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
                            .addContainerGap())
                );
                jPanel2Layout.setVerticalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(startOfMessageBytesLabel)
                                .addComponent(startOfMessageBytes0XLabel)
                                .addComponent(startOfMessageBytesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(startOfMessageBytesAbbrevLabel))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(endOfMessageBytesLabel)
                                .addComponent(endOfMessageBytes0XLabel)
                                .addComponent(endOfMessageBytesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(endOfMessageBytesAbbrevLabel))
                            .addContainerGap(117, Short.MAX_VALUE))
                );
            }

            //---- cancelButton ----
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(e -> cancelButtonActionPerformed(e));

            //---- okButton ----
            okButton.setText("OK");
            okButton.addActionListener(e -> okButtonActionPerformed(e));

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(okButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup()
                                    .addComponent(jSeparator1)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(byteAbbreviationList1, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(byteAbbreviationList1, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cancelButton)
                            .addComponent(okButton))
                        .addContainerGap())
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    private void startOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_startOfMessageBytesFieldActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        String errors = checkProperties();
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error validating transmission mode settings.\r\n\r\n" + errors, "Validation Error", JOptionPane.ERROR_MESSAGE);
        } else {
            saved = true;
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void endOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_endOfMessageBytesFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JLabel startOfMessageBytesAbbrevLabel;
    private JLabel startOfMessageBytesLabel;
    private JLabel endOfMessageBytesLabel;
    private MirthTextField startOfMessageBytesField;
    private JLabel startOfMessageBytes0XLabel;
    private JLabel endOfMessageBytesAbbrevLabel;
    private MirthTextField endOfMessageBytesField;
    private JLabel endOfMessageBytes0XLabel;
    private JSeparator jSeparator1;
    private JButton cancelButton;
    private JButton okButton;
    private ByteAbbreviationList byteAbbreviationList1;
    // End of variables declaration//GEN-END:variables
}
