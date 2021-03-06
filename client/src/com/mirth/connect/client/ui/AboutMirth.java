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
import java.util.Calendar;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;

/**
 * Creates the About Mirth dialog. The content is loaded from about.txt.
 */
public class AboutMirth extends MirthDialog {

    private Frame parent;

    /**
     * Creates new form AboutMirth
     */
    public AboutMirth() {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        loadContent();
        aboutContent.setCaretPosition(0);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        setModal(true);
        pack();

        // If this is being called from the LoginPanel, parent may be null
        if (parent == null) {
            setLocationRelativeTo(null);
        } else {
            Dimension dlgSize = getPreferredSize();
            Dimension frmSize = parent.getSize();
            Point loc = parent.getLocation();

            if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
                setLocationRelativeTo(null);
            } else {
                setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
            }
        }

        setVisible(true);
    }

    /**
     * Loads the contents of about.txt
     */
    public void loadContent() {
        StringBuilder content = new StringBuilder();

        if (PlatformUI.SERVER_VERSION != null) {
            content.append("Mirth Connect Server " + PlatformUI.SERVER_VERSION + "\n\n");
        }

        if (PlatformUI.BUILD_DATE != null) {
            content.append("Built on " + PlatformUI.BUILD_DATE + "\n\n");
        }

        if (PlatformUI.SERVER_ID != null) {
            content.append("Server ID: " + PlatformUI.SERVER_ID + "\n\n");
        }

        content.append("Java version: " + System.getProperty("java.version") + "\n\n");

        content.append("(c) 2005-" + Calendar.getInstance().get(Calendar.YEAR) + " Mirth Corporation. All rights reserved. Visit http://www.mirthcorp.com\n\n");
        content.append("The following is a list of acknowledgements for third-party software that is included with Mirth Connect:\n\n");
        content.append("This product includes software developed by the Apache Software Foundation (http://www.apache.org/).\n\n");
        content.append("This product includes all or a portion of the HL7 Vocabulary database, or is derived from the HL7 Vocabulary database, subject to a license from Health Level Seven, Inc.\n\n");
        content.append("This product includes a portion of images from http://www.famfamfam.com/lab/icons/silk/.\n\n");
        content.append("This product includes software developed by the Indiana University Extreme! Lab (http://www.extreme.indiana.edu/).\n\n");
        content.append("This product includes the Flying Saucer XHTML renderer library, licensed under the LGPL version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).\n\n");
        content.append("This product includes the jTDS JDBC driver, licensed under the LGPL version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).\n\n");
        content.append("This product includes software developed by the JDOM Project (http://www.jdom.org/).\n\n");
        content.append("This product includes software developed by the SAXPath Project (http://www.saxpath.org/).\n\n");
        content.append("This product includes the SoapUI library version 4.0.1, copyright (C) 2004-2011 smartbear.com, licensed under the LGPL version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).\n\n");
        content.append("This product includes the JCIFS SMB client library in Java version 1.3.17, copyright (C) 2002  \"Michael B. Allen\" <jcifs at samba dot org> and \"Eric Glass\" <jcifs at samba dot org>, licensed under the LGPL version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).\n\n");
        content.append("This product includes the Pdf-renderer library (https://java.net/projects/pdf-renderer/), portions copyright Sun Microsystems, Inc., Pirion Systems Pty Ltd, intarsys consulting GmbH and Adobe Systems Incorporated. It is licensed under the LGPL version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).\n\n");
        content.append("This product includes software developed by the JDOM Project (http://www.jdom.org/).\n\n");
        content.append("This product includes software developed by xerial.org (Taro L. Saito) (https://bitbucket.org/xerial/sqlite-jdbc).\n\n");
        content.append("This product includes the SwingLabs SwingX library, copyright (c) 2005-2006 Sun Microsystems, Inc., licensed under the LGPL version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).\n\n");

        aboutContent.setText(content.toString());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new JPanel();
        jScrollPane1 = new JScrollPane();
        aboutContent = new JTextPane();
        mirthHeadingPanel1 = new MirthHeadingPanel();
        jLabel2 = new JLabel();
        jButton1 = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {

            //======== jScrollPane1 ========
            {

                //---- aboutContent ----
                aboutContent.setEditable(false);
                jScrollPane1.setViewportView(aboutContent);
            }

            //======== mirthHeadingPanel1 ========
            {

                //---- jLabel2 ----
                jLabel2.setText("About Mirth Connect");

                GroupLayout mirthHeadingPanel1Layout = new GroupLayout(mirthHeadingPanel1);
                mirthHeadingPanel1.setLayout(mirthHeadingPanel1Layout);
                mirthHeadingPanel1Layout.setHorizontalGroup(
                        mirthHeadingPanel1Layout.createParallelGroup()
                                .addGroup(mirthHeadingPanel1Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                                        .addContainerGap())
                );
                mirthHeadingPanel1Layout.setVerticalGroup(
                        mirthHeadingPanel1Layout.createParallelGroup()
                                .addGroup(mirthHeadingPanel1Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                                        .addContainerGap())
                );
            }

            //---- jButton1 ----
            jButton1.setText("Close");
            jButton1.addActionListener(e -> jButton1ActionPerformed(e));

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                    jPanel1Layout.createParallelGroup()
                            .addComponent(mirthHeadingPanel1, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                            .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                    .addContainerGap(309, Short.MAX_VALUE)
                                    .addComponent(jButton1)
                                    .addContainerGap())
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                                    .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                    jPanel1Layout.createParallelGroup()
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(mirthHeadingPanel1, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
                                    .addGap(14, 14, 14)
                                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton1)
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
                        .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton1ActionPerformed
    {// GEN-HEADEREND:event_jButton1ActionPerformed
        this.dispose();
    }// GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JTextPane aboutContent;
    private MirthHeadingPanel mirthHeadingPanel1;
    private JLabel jLabel2;
    private JButton jButton1;
    // End of variables declaration//GEN-END:variables
}
