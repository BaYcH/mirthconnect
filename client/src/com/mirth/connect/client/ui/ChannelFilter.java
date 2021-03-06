/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.*;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import javax.swing.table.*;

import com.mirth.connect.client.ui.components.*;

import org.apache.commons.collections.CollectionUtils;

import com.mirth.connect.client.ui.components.ItemSelectionTableModel;

public class ChannelFilter extends MirthDialog {
    private Frame parent;
    private ItemSelectionTableModel<String, String> filterTableModel;
    private ChannelTagInfo channelTagInfo;
    private ChannelFilterSaveTask onSave;

    interface ChannelFilterSaveTask {
        public void save(ChannelTagInfo channelTagInfo);
    }

    /**
     * Creates new form DashboardFilter
     */
    public ChannelFilter(ChannelTagInfo channelTagInfo, ChannelFilterSaveTask onSave) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.channelTagInfo = channelTagInfo;
        this.onSave = onSave;

        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        pack();

        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        Map<String, String> tagMap = new LinkedHashMap<String, String>();

        for (String tag : channelTagInfo.getTags()) {
            tagMap.put(tag, tag);
        }

        List<String> selectedTags = null;

        if (CollectionUtils.isNotEmpty(channelTagInfo.getVisibleTags())) {
            selectedTags = new ArrayList<String>(channelTagInfo.getVisibleTags());
        } else {
            selectedTags = new ArrayList<String>(tagMap.values());
        }

        filterTableModel = new ItemSelectionTableModel<String, String>(tagMap, selectedTags, "Channel Tag", "Enabled");
        filterTable.setModel(filterTableModel);
        filterTable.setSortable(true);
        enableFilterCheckbox.setSelected(channelTagInfo.isEnabled());
        filterTable.setEnabled(channelTagInfo.isEnabled());
        invertButton.setEnabled(channelTagInfo.isEnabled());

        setVisible(true);
    }

    public ChannelTagInfo getChannelTagInfo() {
        return channelTagInfo;
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
        jScrollPane1 = new JScrollPane();
        filterTable = new ItemSelectionTable();
        okButton = new JButton();
        cancelButton = new JButton();
        invertButton = new JButton();
        enableFilterCheckbox = new JCheckBox();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Channel Filter");
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setBackground(Color.white);

            //======== jScrollPane1 ========
            {

                //---- filterTable ----
                filterTable.setModel(new DefaultTableModel());
                filterTable.setEnabled(false);
                jScrollPane1.setViewportView(filterTable);
            }

            //---- okButton ----
            okButton.setText("OK");
            okButton.addActionListener(e -> okButtonActionPerformed(e));

            //---- cancelButton ----
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(e -> cancelButtonActionPerformed(e));

            //---- invertButton ----
            invertButton.setText("Invert Selection");
            invertButton.setEnabled(false);
            invertButton.addActionListener(e -> invertButtonActionPerformed(e));

            //---- enableFilterCheckbox ----
            enableFilterCheckbox.setBackground(Color.white);
            enableFilterCheckbox.setText("Enable Channel Filter");
            enableFilterCheckbox.addActionListener(e -> enableFilterCheckboxActionPerformed(e));

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                    jPanel1Layout.createParallelGroup()
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(jPanel1Layout.createParallelGroup()
                                            .addComponent(jScrollPane1)
                                            .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                    .addComponent(invertButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 131, Short.MAX_VALUE)
                                                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addComponent(enableFilterCheckbox)
                                                    .addGap(0, 0, Short.MAX_VALUE)))
                                    .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                    jPanel1Layout.createParallelGroup()
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(enableFilterCheckbox)
                                    .addGap(8, 8, 8)
                                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(okButton)
                                            .addComponent(cancelButton)
                                            .addComponent(invertButton))
                                    .addGap(12, 12, 12))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(0, 0, 0))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(0, 0, 0))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        boolean tagFilterEnabled = enableFilterCheckbox.isSelected();

        if (tagFilterEnabled) {
            Set<String> visibleTags = new LinkedHashSet<String>();
            visibleTags.addAll(filterTableModel.getKeys(true));

            if (visibleTags.isEmpty()) {
                parent.alertError(parent, "Please select at least one tag");
                return;
            } else {
                channelTagInfo.setVisibleTags(visibleTags);
            }
        }

        channelTagInfo.setEnabled(tagFilterEnabled);
        onSave.save(channelTagInfo);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void enableFilterCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableFilterCheckboxActionPerformed
        filterTable.setEnabled(enableFilterCheckbox.isSelected());
        invertButton.setEnabled(enableFilterCheckbox.isSelected());
    }//GEN-LAST:event_enableFilterCheckboxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void invertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertButtonActionPerformed
        filterTableModel.invertSelection();
    }//GEN-LAST:event_invertButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private ItemSelectionTable filterTable;
    private JButton okButton;
    private JButton cancelButton;
    private JButton invertButton;
    private JCheckBox enableFilterCheckbox;
    // End of variables declaration//GEN-END:variables
}
