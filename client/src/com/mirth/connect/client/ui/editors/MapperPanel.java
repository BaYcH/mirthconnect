/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.syntax.jedit.SyntaxDocument;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;

public class MapperPanel extends BasePanel {

    public boolean updating = false;
    protected String label;
    protected static SyntaxDocument mappingDoc;
    protected MirthEditorPane parent;
    public final int REGEX_COLUMN = 0;
    public final int REPLACEWITH_COLUMN = 1;
    public final String REGEX_COLUMN_NAME = "Regular Expression";
    public final String REPLACEWITH_COLUMN_NAME = "Replace With";
    private int lastIndex = -1;
    private LinkedHashMap<String, String> variableTypes;
    private boolean documentListenerEnabled = true;

    /**
     * Creates new form MapperPanel
     */
    public MapperPanel(MirthEditorPane p) {
        parent = p;
        initComponents();
        variableTypes = new LinkedHashMap<String, String>();
        variableTypes.put("connector", "Connector Map");
        variableTypes.put("channel", "Channel Map");
        variableTypes.put("globalChannel", "Global Channel Map");
        variableTypes.put("global", "Global Map");
        variableTypes.put("response", "Response Map");

        addTo.setModel(new DefaultComboBoxModel(variableTypes.values().toArray()));

        variableTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent arg0) {
            }

            public void insertUpdate(DocumentEvent arg0) {
                if (documentListenerEnabled) {
                    updateTable();
                    parent.modified = true;
                }
            }

            public void removeUpdate(DocumentEvent arg0) {
                if (documentListenerEnabled) {
                    updateTable();
                    parent.modified = true;
                }
            }
        });

        mappingTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent arg0) {
            }

            public void insertUpdate(DocumentEvent arg0) {
                parent.modified = true;
            }

            public void removeUpdate(DocumentEvent arg0) {
                parent.modified = true;
            }
        });

        defaultValueTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent arg0) {
            }

            public void insertUpdate(DocumentEvent arg0) {
                parent.modified = true;
            }

            public void removeUpdate(DocumentEvent arg0) {
                parent.modified = true;
            }
        });

        regularExpressionsScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows();
            }
        });
        deleteButton.setEnabled(false);
    }

    public void setDocumentListenerEnabled(boolean documentListenerEnabled) {
        this.documentListenerEnabled = documentListenerEnabled;
    }

    public void updateTable() {
        if (parent.getSelectedRow() != -1) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    parent.getTableModel().setValueAt(variableTextField.getText(), parent.getSelectedRow(), parent.STEP_NAME_COL);
                    parent.updateTaskPane(parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString());
                }
            });
        }
    }

    @Override
    public boolean isModified() {
        if (StringUtils.isNotBlank(variableTextField.getText()) || StringUtils.isNotBlank(mappingTextField.getText()) || StringUtils.isNotBlank(defaultValueTextField.getText()) || regularExpressionsTable.getRowCount() > 0 || !addTo.getSelectedItem().toString().equals("Channel Map")) {
            return true;
        }

        return false;
    }

    public Map<Object, Object> getData() {
        Map<Object, Object> m = new LinkedHashMap<Object, Object>();
        m.put("Variable", variableTextField.getText().trim());
        m.put("Mapping", mappingTextField.getText().trim());
        m.put("DefaultValue", defaultValueTextField.getText().trim());
        m.put("RegularExpressions", getRegexProperties());

        for (String s : variableTypes.keySet()) {
            if (variableTypes.get(s).equals(addTo.getSelectedItem())) {
                m.put(UIConstants.IS_GLOBAL, s);
            }
        }

        return m;
    }

    public void setData(Map<Object, Object> data) {
        boolean modified = parent.modified;

        if (data != null) {
            variableTextField.setText((String) data.get("Variable"));
            mappingTextField.setText((String) data.get("Mapping"));
            defaultValueTextField.setText((String) data.get("DefaultValue"));

            if (data.get(UIConstants.IS_GLOBAL) == null) {
                addTo.setSelectedItem(variableTypes.get("channel"));
            } else {
                addTo.setSelectedItem(variableTypes.get((String) data.get(UIConstants.IS_GLOBAL)));
            }

            ArrayList<String[]> p = (ArrayList<String[]>) data.get("RegularExpressions");
            if (p != null) {
                setRegexProperties(p);
            } else {
                setRegexProperties(new ArrayList<String[]>());
            }
        } else {
            variableTextField.setText("");
            mappingTextField.setText("");
            defaultValueTextField.setText("");
            addTo.setSelectedIndex(0);
            setRegexProperties(new ArrayList<String[]>());
        }

        parent.modified = modified;
    }

    public void setRegexProperties(ArrayList<String[]> properties) {
        Object[][] tableData = new Object[properties.size()][2];

        regularExpressionsTable = new MirthTable();

        for (int i = 0; i < properties.size(); i++) {
            tableData[i][REGEX_COLUMN] = properties.get(i)[0];
            tableData[i][REPLACEWITH_COLUMN] = properties.get(i)[1];
        }

        regularExpressionsTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{REGEX_COLUMN_NAME, REPLACEWITH_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        regularExpressionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow() != -1) {
                    lastIndex = getSelectedRow();
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });

        class RegExTableCellEditor extends TextFieldCellEditor {

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    deleteButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                parent.modified = true;
                deleteButton.setEnabled(true);
                return true;
            }
        }

        regularExpressionsTable.getColumnModel().getColumn(regularExpressionsTable.getColumnModel().getColumnIndex(REGEX_COLUMN_NAME)).setCellEditor(new RegExTableCellEditor());
        regularExpressionsTable.getColumnModel().getColumn(regularExpressionsTable.getColumnModel().getColumnIndex(REPLACEWITH_COLUMN_NAME)).setCellEditor(new RegExTableCellEditor());
        regularExpressionsTable.setCustomEditorControls(true);

        regularExpressionsTable.setSelectionMode(0);
        regularExpressionsTable.setRowSelectionAllowed(true);
        regularExpressionsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        regularExpressionsTable.setDragEnabled(false);
        regularExpressionsTable.setOpaque(true);
        regularExpressionsTable.setSortable(false);
        regularExpressionsTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            regularExpressionsTable.setHighlighters(highlighter);
        }

        regularExpressionsScrollPane.setViewportView(regularExpressionsTable);
    }

    public ArrayList<String[]> getRegexProperties() {
        ArrayList<String[]> properties = new ArrayList<String[]>();

        for (int i = 0; i < regularExpressionsTable.getRowCount(); i++) {
            if (((String) regularExpressionsTable.getValueAt(i, REGEX_COLUMN)).length() > 0) {
                properties.add(new String[]{((String) regularExpressionsTable.getValueAt(i, REGEX_COLUMN)), ((String) regularExpressionsTable.getValueAt(i, REPLACEWITH_COLUMN))});
            }
        }

        return properties;
    }

    /**
     * Clears the selection in the table and sets the tasks appropriately
     */
    public void deselectRows() {
        regularExpressionsTable.clearSelection();
        deleteButton.setEnabled(false);
    }

    /**
     * Get the currently selected destination index
     */
    public int getSelectedRow() {
        if (regularExpressionsTable.isEditing()) {
            return regularExpressionsTable.getEditingRow();
        } else {
            return regularExpressionsTable.getSelectedRow();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        regularExpressionsScrollPane = new javax.swing.JScrollPane();
        regularExpressionsTable = new com.mirth.connect.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        variableTextField = new javax.swing.JTextField();
        mappingTextField = new javax.swing.JTextField();
        defaultValueTextField = new javax.swing.JTextField();
        addTo = new com.mirth.connect.client.ui.components.MirthComboBox();
        jLabel5 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Variable:");

        jLabel2.setText("Mapping:");

        jLabel3.setText("Default Value:");

        regularExpressionsTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "Property", "Value"
                }
        ));
        regularExpressionsScrollPane.setViewportView(regularExpressionsTable);

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("String Replacement:");

        addTo.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));

        jLabel5.setText("Add to:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(39, 39, 39)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jLabel3)
                                                        .addComponent(jLabel2)
                                                        .addComponent(jLabel1))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                .addComponent(regularExpressionsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(newButton)
                                                                        .addComponent(deleteButton)))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                .addComponent(variableTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel5)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(addTo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(mappingTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                                                        .addComponent(defaultValueTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel4)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{deleteButton, newButton});

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(variableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5)
                                        .addComponent(addTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(mappingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(defaultValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel4)
                                                        .addComponent(newButton))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(deleteButton))
                                        .addComponent(regularExpressionsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_deleteButtonActionPerformed
    {// GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow() != -1 && !regularExpressionsTable.isEditing()) {
            ((DefaultTableModel) regularExpressionsTable.getModel()).removeRow(getSelectedRow());

            if (regularExpressionsTable.getRowCount() != 0) {
                if (lastIndex == 0) {
                    regularExpressionsTable.setRowSelectionInterval(0, 0);
                } else if (lastIndex == regularExpressionsTable.getRowCount()) {
                    regularExpressionsTable.setRowSelectionInterval(lastIndex - 1, lastIndex - 1);
                } else {
                    regularExpressionsTable.setRowSelectionInterval(lastIndex, lastIndex);
                }
            }

            parent.modified = true;
        }
    }// GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_newButtonActionPerformed
    {// GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) regularExpressionsTable.getModel()).addRow(new Object[]{"", ""});
        regularExpressionsTable.setRowSelectionInterval(regularExpressionsTable.getRowCount() - 1, regularExpressionsTable.getRowCount() - 1);
        parent.modified = true;
    }// GEN-LAST:event_newButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthComboBox addTo;
    private javax.swing.JTextField defaultValueTextField;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField mappingTextField;
    private javax.swing.JButton newButton;
    private javax.swing.JScrollPane regularExpressionsScrollPane;
    private com.mirth.connect.client.ui.components.MirthTable regularExpressionsTable;
    private javax.swing.JTextField variableTextField;
    // End of variables declaration//GEN-END:variables
}
