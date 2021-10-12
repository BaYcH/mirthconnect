/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.CardLayout;
import java.awt.Component;
import java.util.Map;

import javax.swing.JPanel;

public class BasePanel extends javax.swing.JPanel {

    private Map<Object, Object> data;

    /**
     * Creates new form BasePanel
     */
    public BasePanel() {
        initComponents();
        this.setLayout(new CardLayout());
        data = null;
    }

    // Adds a new card to the panel.
    public void addCard(JPanel panel, String type) {
        this.add(panel, type);
    }

    // Shows a certain card
    public void showCard(String type) {
        CardLayout cl = (CardLayout) this.getLayout();
        cl.show(this, type);
    }

    //TODO: Plugin panels should no longer extend this class
    public Map<Object, Object> getData() {
        for (Component component : this.getComponents()) {
            if (component.isVisible()) {
                return ((BasePanel) component).getData();
            }
        }

        return null;
    }

    // set the data object
    public void setData(Map<Object, Object> data) {
        this.data = data;
    }

    public boolean isModified() {
        for (Component component : this.getComponents()) {
            if (component.isVisible()) {
                return ((BasePanel) component).isModified();
            }
        }

        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
