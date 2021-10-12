/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.Set;

import javax.swing.SwingWorker;

import com.mirth.connect.client.core.ClientException;

public class RemoveMessagesDialog extends MirthDialog {
    private Frame parent;
    private Set<String> channelIds;

    public RemoveMessagesDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.parent = parent;
    }

    public void init(Set<String> selectedChannelIds, boolean restartCheckboxEnabled) {
        yesButton.requestFocus();
        clearStatsCheckBox.setSelected(true);
        includeRunningChannels.setSelected(false);
        channelIds = selectedChannelIds;

        includeRunningChannels.setEnabled(restartCheckboxEnabled);
    }

    // @formatter:off

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        clearStatsCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        includeRunningChannels = new com.mirth.connect.client.ui.components.MirthCheckBox();
        buttonPanel = new javax.swing.JPanel();
        yesButton = new com.mirth.connect.client.ui.components.MirthButton();
        noButton = new com.mirth.connect.client.ui.components.MirthButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Remove All Messages");
        setResizable(false);

        messageLabel.setText("Are you sure you want to remove all messages (including QUEUED) for the selected stopped channel(s)?");

        clearStatsCheckBox.setText("Clear statistics for affected channel(s)");

        includeRunningChannels.setText("Include selected channels that are not stopped (channels will be temporarily stopped while messages are being removed) ");

        yesButton.setText("Yes");
        yesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        yesButton.setMaximumSize(new java.awt.Dimension(75, 22));
        yesButton.setMinimumSize(new java.awt.Dimension(75, 22));
        yesButton.setPreferredSize(new java.awt.Dimension(75, 22));
        yesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        yesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yesButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(yesButton);

        noButton.setText("No");
        noButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        noButton.setMaximumSize(new java.awt.Dimension(75, 22));
        noButton.setMinimumSize(new java.awt.Dimension(75, 22));
        noButton.setPreferredSize(new java.awt.Dimension(75, 22));
        noButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        noButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(noButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(includeRunningChannels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(messageLabel)
                                                        .addComponent(clearStatsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(129, 129, 129))))
                        .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(messageLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(includeRunningChannels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearStatsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void noButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_noButtonActionPerformed

    private void yesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yesButtonActionPerformed
        final String workingId = parent.startWorking("Removing messages...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                try {
                    parent.mirthClient.removeAllMessages(channelIds, includeRunningChannels.isSelected(), clearStatsCheckBox.isSelected());
                } catch (ClientException e) {
                    parent.alertThrowable(PlatformUI.MIRTH_FRAME, e);
                }

                return null;
            }

            public void done() {
                parent.doRefreshStatuses(true);

                if (parent.currentContentPage == parent.messageBrowser) {
                    parent.messageBrowser.refresh(1, true);
                }

                parent.stopWorking(workingId);
            }
        };

        worker.execute();
        setVisible(false);
    }//GEN-LAST:event_yesButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private com.mirth.connect.client.ui.components.MirthCheckBox clearStatsCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox includeRunningChannels;
    private javax.swing.JLabel messageLabel;
    private com.mirth.connect.client.ui.components.MirthButton noButton;
    private com.mirth.connect.client.ui.components.MirthButton yesButton;
    // End of variables declaration//GEN-END:variables
    // @formatter:on
}
