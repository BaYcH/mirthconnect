/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors.transformer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CenterCellRenderer;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.MapperDropData;
import com.mirth.connect.client.ui.MessageBuilderDropData;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TreeTransferable;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTree;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.EditorTableCellEditor;
import com.mirth.connect.client.ui.editors.MirthEditorPane;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class TransformerPane extends MirthEditorPane implements DropTargetListener {

    public static final String MAPPER = "Mapper";
    public static final String MESSAGE_BUILDER = "Message Builder";
    public static final String JAVASCRIPT = "Javascript";
    // used to load the pane
    private Transformer transformer;
    // fields
    private MirthTable transformerTable;
    private DefaultTableModel transformerTableModel;
    private JScrollPane transformerTablePane;
    private JSplitPane vSplitPane;
    private JSplitPane hSplitPane;
    private JXTaskPane transformerTasks;
    private JPopupMenu transformerPopupMenu;
    private JXTaskPane viewTasks;
    private Channel channel;
    private Connector connector;
    public boolean updating; // flow control
    public boolean invalidVar; // selection control
    // panels using CardLayout
    protected BasePanel stepPanel; // the card holder
    protected BasePanel blankPanel;
    public static final int NUMBER_OF_COLUMNS = 4;
    private DropTarget dropTarget;
    private boolean isResponse = false;

    private JTabbedPane tabbedPane;
    private MirthRTextScrollPane scriptTextArea;
    private JPanel generatedScriptPanel;

    private int lastSelectedIndex;
    boolean switchTab;

    private static int CODE_TAB = 1;

    /**
     * CONSTRUCTOR
     */
    public TransformerPane() {
        prevSelRow = -1;
        modified = false;
        new DropTarget(this, this);
        initComponents();
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void reload(Connector c) {
        connector = c;
        transformer = isResponse ? c.getResponseTransformer() : c.getTransformer();
        channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
    }

    /**
     * load( Transformer t ) now that the components have been initialized...
     */
    public boolean load(Connector c, Transformer t, boolean channelHasBeenChanged, boolean isResponse) {
        if (alertUnsupportedStepTypes(t)) {
            return false;
        }

        switchTab = false;
        lastSelectedIndex = 0;
        tabbedPane.setSelectedIndex(0);

        this.isResponse = isResponse;
        prevSelRow = -1;
        connector = c;
        transformer = t;
        channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;

        makeTransformerTable();

        // add any existing steps to the model
        List<Step> list = transformer.getSteps();
        ListIterator<Step> li = list.listIterator();
        while (li.hasNext()) {
            Step s = li.next();
            setRowData(s, s.getSequenceNumber(), false);
        }

        parent.setCurrentContentPage((JPanel) this);
        parent.setFocus(new JXTaskPane[]{viewTasks, transformerTasks}, false, true);

        tabTemplatePanel.setTransformerView();

        // select the first row if there is one
        int rowCount = transformerTableModel.getRowCount();
        if (rowCount > 0) {
            transformerTable.setRowSelectionInterval(0, 0);
            prevSelRow = 0;
        } else {
            transformerTable.getSelectionModel().clearSelection();
            stepPanel.showCard(BLANK_TYPE);

            for (TransformerStepPlugin plugin : LoadedExtensions.getInstance().getTransformerStepPlugins().values()) {
                plugin.getPanel().setData(null);
            }
            loadData(-1);
        }

        if (connector.getMode() == Connector.Mode.SOURCE) {
            tabTemplatePanel.setSourceView();
        } else if (connector.getMode() == Connector.Mode.DESTINATION) {
            tabTemplatePanel.setDestinationView(isResponse);
        }

        tabTemplatePanel.setDefaultComponent();

        tabTemplatePanel.setIncomingDataType((String) PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(transformer.getInboundDataType()));
        tabTemplatePanel.setOutgoingDataType(((String) PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(transformer.getOutboundDataType())));

        tabTemplatePanel.setIncomingDataProperties(transformer.getInboundProperties());
        tabTemplatePanel.setOutgoingDataProperties(transformer.getOutboundProperties());

        tabTemplatePanel.setIncomingMessage(transformer.getInboundTemplate());
        tabTemplatePanel.setOutgoingMessage(transformer.getOutboundTemplate());

        transformerTable.setBorder(BorderFactory.createEmptyBorder());

        updateStepNumbers();
        if (transformerTableModel.getRowCount() > 0) {
            updateTaskPane((String) transformerTableModel.getValueAt(0, STEP_TYPE_COL));
        }

        if (channelHasBeenChanged) {
            modified = true;
        } else {
            modified = false;
        }

        updateCodePanel(null);

        return true;
    }

    /**
     * @return Returns true if the transformer has unsupported step types and an alert was
     * generated, false otherwise.
     */
    private boolean alertUnsupportedStepTypes(Transformer transformer) {
        if (LoadedExtensions.getInstance().getTransformerStepPlugins().values().size() == 0) {
            parent.alertError(this, "No transformer step plugins loaded.\r\nPlease install plugins and try again.");
            return true;
        }

        Set<String> stepTypes = new HashSet<String>();

        for (Step step : transformer.getSteps()) {
            stepTypes.add(step.getType());
        }

        stepTypes.removeAll(LoadedExtensions.getInstance().getTransformerStepPlugins().keySet());

        if (!stepTypes.isEmpty()) {
            if (stepTypes.size() == 1) {
                parent.alertError(this, "The \"" + stepTypes.toArray()[0] + "\" step plugin is required by this transformer. Please install this plugin and try again.");
            } else {
                parent.alertError(this, "The following step type plugins are required by this transformer: " + StringUtils.join(stepTypes, ", ") + ". Please install these plugins and try again.");
            }

            return true;
        }

        return false;
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);

                List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                if (iterator.hasNext() && fileList.size() == 1) {
                    String fileName = ((File) iterator.next()).getName();
                    if (!fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".xml")) {
                        dtde.rejectDrag();
                    }
                } else {
                    dtde.rejectDrag();
                }
            } else if (tr.isDataFlavorSupported(TreeTransferable.MAPPER_DATA_FLAVOR) || tr.isDataFlavorSupported(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                dtde.rejectDrag();
            }
        } catch (Exception e) {
            dtde.rejectDrag();
        }
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<File> iterator = fileList.iterator();

                if (fileList.size() == 1) {
                    File file = (File) iterator.next();
                    importTransformer(parent.readFileToString(file));
                }
            } else if (tr.isDataFlavorSupported(TreeTransferable.MAPPER_DATA_FLAVOR) || tr.isDataFlavorSupported(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                Object mapperTransferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
                Object messageBuilderTransferData = tr.getTransferData(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);

                if (mapperTransferData != null && !parent.isAcceleratorKeyPressed()) {
                    Object transferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
                    MapperDropData data = (MapperDropData) transferData;
                    addNewStep(data.getVariable(), data.getVariable(), data.getMapping(), MAPPER);
                } else if (mapperTransferData != null && parent.isAcceleratorKeyPressed()) {
                    Object transferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
                    MapperDropData data2 = (MapperDropData) transferData;
                    MessageBuilderDropData data = new MessageBuilderDropData(data2.getNode(), MirthTree.constructPath(data2.getNode().getParent(), "msg", "").toString(), "");
                    addNewStep(MirthTree.constructMessageBuilderStepName(null, data.getNode()), data.getMessageSegment(), data.getMapping(), MESSAGE_BUILDER);
                } else if (messageBuilderTransferData != null) {
                    Object transferData = tr.getTransferData(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);
                    MessageBuilderDropData data = (MessageBuilderDropData) transferData;
                    addNewStep(MirthTree.constructMessageBuilderStepName(null, data.getNode()), data.getMessageSegment(), data.getMapping(), MESSAGE_BUILDER);
                }
            }
        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    public void initComponents() {

        // the available panels (cards)
        stepPanel = new BasePanel();
        blankPanel = new BasePanel();

        scriptTextArea = new MirthRTextScrollPane(null, true, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, false);
        scriptTextArea.setBackground(new Color(204, 204, 204));
        scriptTextArea.setBorder(BorderFactory.createEtchedBorder());
        scriptTextArea.getTextArea().setEditable(false);
        scriptTextArea.getTextArea().setDropTarget(null);

        generatedScriptPanel = new JPanel();
        generatedScriptPanel.setBackground(Color.white);
        generatedScriptPanel.setLayout(new CardLayout());
        generatedScriptPanel.add(scriptTextArea, "");

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Step", stepPanel);

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchTab = (lastSelectedIndex == 0 && tabbedPane.getSelectedIndex() == 1) ? true : false;
                updateCodePanel(null);
            }
        });

        for (TransformerStepPlugin transformerStepPlugin : LoadedExtensions.getInstance().getTransformerStepPlugins().values()) {
            transformerStepPlugin.initialize(this);
        }

        // establish the cards to use in the Transformer
        stepPanel.addCard(blankPanel, BLANK_TYPE);
        for (TransformerStepPlugin plugin : LoadedExtensions.getInstance().getTransformerStepPlugins().values()) {
            stepPanel.addCard(plugin.getPanel(), plugin.getPluginPointName());
        }
        transformerTablePane = new JScrollPane();
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());

        viewTasks = new JXTaskPane();
        viewTasks.setTitle("Mirth Views");
        viewTasks.setFocusable(false);
        viewTasks.add(initActionCallback("accept", "Return back to channel.", ActionFactory.createBoundAction("accept", "Back to Channel", "B"), new ImageIcon(Frame.class.getResource("images/resultset_previous.png"))));
        parent.setNonFocusable(viewTasks);
        viewTasks.setVisible(false);
        parent.taskPaneContainer.add(viewTasks, parent.taskPaneContainer.getComponentCount() - 1);

        transformerTasks = new JXTaskPane();
        transformerTasks.setTitle("Transformer Tasks");
        transformerTasks.setFocusable(false);

        transformerPopupMenu = new JPopupMenu();

        // add new step task
        transformerTasks.add(initActionCallback("addNewStep", "Add a new transformer step.", ActionFactory.createBoundAction("addNewStep", "Add New Step", "N"), new ImageIcon(Frame.class.getResource("images/add.png"))));
        JMenuItem addNewStep = new JMenuItem("Add New Step");
        addNewStep.setIcon(new ImageIcon(Frame.class.getResource("images/add.png")));
        addNewStep.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addNewStep();
            }
        });
        transformerPopupMenu.add(addNewStep);

        // delete step task
        transformerTasks.add(initActionCallback("deleteStep", "Delete the currently selected transformer step.", ActionFactory.createBoundAction("deleteStep", "Delete Step", "X"), new ImageIcon(Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteStep = new JMenuItem("Delete Step");
        deleteStep.setIcon(new ImageIcon(Frame.class.getResource("images/delete.png")));
        deleteStep.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                deleteStep();
            }
        });
        transformerPopupMenu.add(deleteStep);

        transformerTasks.add(initActionCallback("doImport", "Import a transformer from an XML file.", ActionFactory.createBoundAction("doImport", "Import Transformer", "I"), new ImageIcon(Frame.class.getResource("images/report_go.png"))));
        JMenuItem importTransformer = new JMenuItem("Import Transformer");
        importTransformer.setIcon(new ImageIcon(Frame.class.getResource("images/report_go.png")));
        importTransformer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doImport();
            }
        });
        transformerPopupMenu.add(importTransformer);

        transformerTasks.add(initActionCallback("doExport", "Export the transformer to an XML file.", ActionFactory.createBoundAction("doExport", "Export Transformer", "E"), new ImageIcon(Frame.class.getResource("images/report_disk.png"))));
        JMenuItem exportTransformer = new JMenuItem("Export Transformer");
        exportTransformer.setIcon(new ImageIcon(Frame.class.getResource("images/report_disk.png")));
        exportTransformer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doExport();
            }
        });
        transformerPopupMenu.add(exportTransformer);

        transformerTasks.add(initActionCallback("doValidate", "Validate the currently viewed script.", ActionFactory.createBoundAction("doValidate", "Validate Script", "V"), new ImageIcon(Frame.class.getResource("images/accept.png"))));
        JMenuItem validateStep = new JMenuItem("Validate Script");
        validateStep.setIcon(new ImageIcon(Frame.class.getResource("images/accept.png")));
        validateStep.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doValidate();
            }
        });
        transformerPopupMenu.add(validateStep);

        // move step up task
        transformerTasks.add(initActionCallback("moveStepUp", "Move the currently selected step up.", ActionFactory.createBoundAction("moveStepUp", "Move Step Up", "P"), new ImageIcon(Frame.class.getResource("images/arrow_up.png"))));
        JMenuItem moveStepUp = new JMenuItem("Move Step Up");
        moveStepUp.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_up.png")));
        moveStepUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveStepUp();
            }
        });
        transformerPopupMenu.add(moveStepUp);

        // move step down task
        transformerTasks.add(initActionCallback("moveStepDown", "Move the currently selected step down.", ActionFactory.createBoundAction("moveStepDown", "Move Step Down", "D"), new ImageIcon(Frame.class.getResource("images/arrow_down.png"))));
        JMenuItem moveStepDown = new JMenuItem("Move Step Down");
        moveStepDown.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_down.png")));
        moveStepDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveStepDown();
            }
        });
        transformerPopupMenu.add(moveStepDown);

        // add the tasks to the taskpane, and the taskpane to the mirth client
        parent.setNonFocusable(transformerTasks);
        transformerTasks.setVisible(false);
        parent.taskPaneContainer.add(transformerTasks, parent.taskPaneContainer.getComponentCount() - 1);

        makeTransformerTable();

        // BGN LAYOUT
        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setMinimumSize(new Dimension(0, 40));
        stepPanel.setBorder(BorderFactory.createEmptyBorder());

        hSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, transformerTablePane, tabbedPane);
        hSplitPane.setContinuousLayout(true);
        // hSplitPane.setDividerSize(6);
        hSplitPane.setOneTouchExpandable(true);
        vSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, hSplitPane, refPanel);
        // vSplitPane.setDividerSize(6);
        vSplitPane.setOneTouchExpandable(true);
        vSplitPane.setContinuousLayout(true);

        hSplitPane.setBorder(BorderFactory.createEmptyBorder());
        vSplitPane.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new BorderLayout());
        this.add(vSplitPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
        resizePanes();
        // END LAYOUT

    } // END initComponents()

    public void makeTransformerTable() {
        transformerTable = new MirthTable();
        transformerTable.setBorder(BorderFactory.createEmptyBorder());

        // Data Column is hidden
        transformerTable.setModel(new DefaultTableModel(new String[]{"#", "Name", "Type", "Data"}, 0) {

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                boolean[] canEdit;
                TransformerStepPlugin plugin;
                try {
                    plugin = getPlugin((String) transformerTableModel.getValueAt(rowIndex, STEP_TYPE_COL));
                    canEdit = new boolean[]{false, plugin.isNameEditable(), true, true};
                } catch (Exception e) {
                    canEdit = new boolean[]{false, false, true, true};
                }
                return canEdit[columnIndex];
            }
        });

        transformerTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        transformerTableModel = (DefaultTableModel) transformerTable.getModel();

        transformerTable.getColumnModel().getColumn(STEP_NAME_COL).setCellEditor(new EditorTableCellEditor(this));
        transformerTable.setCustomEditorControls(true);

        // Set the combobox editor on the type column, and add action listener
        String[] defaultComboBoxValues = new String[LoadedExtensions.getInstance().getTransformerStepPlugins().size()];
        TransformerStepPlugin[] pluginArray = LoadedExtensions.getInstance().getTransformerStepPlugins().values().toArray(new TransformerStepPlugin[0]);
        for (int i = 0; i < pluginArray.length; i++) {
            defaultComboBoxValues[i] = pluginArray[i].getPluginPointName();
        }

        MirthComboBoxTableCellEditor comboBox = new MirthComboBoxTableCellEditor(transformerTable, defaultComboBoxValues, 2, true, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (transformerTable.getEditingRow() != -1) {
                    int row = getSelectedRow();
                    String selectedType = ((JComboBox) evt.getSource()).getSelectedItem().toString();
                    String previousType = (String) transformerTable.getValueAt(row, STEP_TYPE_COL);

                    if (selectedType.equalsIgnoreCase(previousType)) {
                        return;
                    }

                    modified = true;
                    invalidVar = false;
                    TransformerStepPlugin plugin = null;
                    try {

                        if (stepPanel.isModified() && !PlatformUI.MIRTH_FRAME.alertOption(PlatformUI.MIRTH_FRAME, "Are you sure you would like to change this transformer step and lose all of the current transformer data?")) {
                            ((JComboBox) evt.getSource()).getModel().setSelectedItem(previousType);
                            return;
                        }

                        plugin = getPlugin(selectedType);
                        plugin.initData();
                        transformerTableModel.setValueAt(plugin.getNewName(), row, STEP_NAME_COL);
                        stepPanel.showCard(selectedType);
                        updateTaskPane(selectedType);
                        updateCodePanel(selectedType);
                    } catch (Exception e) {
                        parent.alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    }
                }
            }
        });

        transformerTable.setSelectionMode(0); // only select one row at a time

        transformerTable.getColumnExt(STEP_NUMBER_COL).setMaxWidth(UIConstants.MAX_WIDTH);
        transformerTable.getColumnExt(STEP_TYPE_COL).setMaxWidth(UIConstants.MAX_WIDTH);
        transformerTable.getColumnExt(STEP_TYPE_COL).setMinWidth(120);

        transformerTable.getColumnExt(STEP_NUMBER_COL).setPreferredWidth(30);
        transformerTable.getColumnExt(STEP_TYPE_COL).setPreferredWidth(120);

        transformerTable.getColumnExt(STEP_NUMBER_COL).setCellRenderer(new CenterCellRenderer());
        transformerTable.getColumnExt(STEP_TYPE_COL).setCellEditor(comboBox);
        transformerTable.getColumnExt(STEP_TYPE_COL).setCellRenderer(new MirthComboBoxTableCellRenderer(defaultComboBoxValues));

        transformerTable.getColumnExt(STEP_DATA_COL).setVisible(false);

        transformerTable.setRowHeight(UIConstants.ROW_HEIGHT);
        transformerTable.packTable(UIConstants.COL_MARGIN);
        transformerTable.setSortable(false);
        transformerTable.setOpaque(true);
        transformerTable.setRowSelectionAllowed(true);
        transformerTable.setDragEnabled(false);
        transformerTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            transformerTable.setHighlighters(highlighter);
        }

        transformerTable.setDropTarget(dropTarget);
        transformerTablePane.setDropTarget(dropTarget);

        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setViewportView(transformerTable);

        // listen for mouse clicks on the actual table
        transformerTable.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }
        });

        transformerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (!updating && !evt.getValueIsAdjusting()) {
                    TransformerListSelected(evt);
                    updateCodePanel(null);
                }
            }
        });
        transformerTable.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteStep();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = transformerTable.rowAtPoint(new Point(evt.getX(), evt.getY()));

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                transformerTable.setRowSelectionInterval(row, row);
            }
            transformerPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    // for the task pane
    public BoundAction initActionCallback(String callbackMethod, String toolTip, BoundAction boundAction, ImageIcon icon) {

        if (icon != null) {
            boundAction.putValue(Action.SMALL_ICON, icon);
        }

        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);
        return boundAction;
    }

    // called when a table row is (re)selected
    private void TransformerListSelected(ListSelectionEvent evt) {
        updating = true;

        int row = transformerTable.getSelectedRow();
        int last = evt.getLastIndex();

        if (row != prevSelRow) {
            saveData(prevSelRow);

            if (invalidVar) {
                row = prevSelRow;
                invalidVar = false;
            }

            if (isValid(row)) {
                loadData(row);
            } else if (isValid(last)) {
                loadData(last);
                row = last;
            }

            String type = (String) transformerTable.getValueAt(row, STEP_TYPE_COL);
            stepPanel.showCard(type);
            transformerTable.setRowSelectionInterval(row, row);
            prevSelRow = row;
            updateTaskPane(type);
        }

        updateCodePanel(null);
        updating = false;
    }

    // returns true if the row is a valid index in the existing model
    private boolean isValid(int row) {
        return (row >= 0 && row < transformerTableModel.getRowCount());
    }

    // returns true if the variable name is unique
    // if an integer is provided, don't check against
    // the var in that row
    public boolean isUnique(String var, boolean dontCheckCurrentRow) {
        return isUnique(var, -1, dontCheckCurrentRow);
    }

    public boolean isUnique(String var, int curRow, boolean dontCheckCurrentRow) {
        boolean unique = true;

        for (int i = 0; i < transformerTableModel.getRowCount(); i++) {
            if (!dontCheckCurrentRow || dontCheckCurrentRow && i != getSelectedRow()) {
                String temp = "";
                Map<Object, Object> data = (Map<Object, Object>) transformerTableModel.getValueAt(i, STEP_DATA_COL);

                if (data != null) {
                    temp = (String) data.get("Variable");
                }
                if (var != null && curRow != i) {
                    if (var.equalsIgnoreCase(temp)) {
                        unique = false;
                    }
                }
            }
        }

        return unique;
    }

    // sets the data from the previously used panel into the
    // previously selected Step object
    private void saveData(int row) {
        if (transformerTable.isEditing()) {
            transformerTable.getCellEditor(transformerTable.getEditingRow(), transformerTable.getEditingColumn()).stopCellEditing();
        }
        if (isValid(row)) {
            String type = (String) transformerTable.getValueAt(row, STEP_TYPE_COL);
            Map<Object, Object> data;
            try {
                data = getPlugin(type).getData(row);
                transformerTableModel.setValueAt(data, row, STEP_DATA_COL);
                List<Step> list = buildStepList(new ArrayList<Step>(), transformerTable.getRowCount());
                transformer.setSteps(list);
            } catch (Exception e) {
                parent.alertThrowable(this, e);
            }
        }
    }

    /**
     * loadData() loads the data object into the correct panel
     */
    private void loadData(int row) {
        if (isValid(row)) {
            String type = (String) transformerTableModel.getValueAt(row, STEP_TYPE_COL);
            Map<Object, Object> data = (Map<Object, Object>) transformerTableModel.getValueAt(row, STEP_DATA_COL);

            setPanelData(type, data);
        }

        if (connector.getMode() == Connector.Mode.SOURCE) {
            Set<String> concatenatedRules = new LinkedHashSet<String>();
            Set<String> concatenatedSteps = new LinkedHashSet<String>();
            VariableListUtil.getRuleVariables(concatenatedRules, connector, true);
            VariableListUtil.getStepVariables(concatenatedSteps, connector.getTransformer(), true, row);
            tabTemplatePanel.updateVariables(concatenatedRules, concatenatedSteps);
        } else {
            tabTemplatePanel.updateVariables(getRuleVariables(row), getStepVariables(row));
            tabTemplatePanel.populateConnectors(channel.getDestinationConnectors());
        }
    }

    private void setPanelData(String type, Map<Object, Object> data) {
        TransformerStepPlugin plugin;
        try {
            plugin = getPlugin(type);
            plugin.setData(connector.getMode(), data);
        } catch (Exception e) {
            parent.alertThrowable(this, e);
        }
    }

    private TransformerStepPlugin getPlugin(String name) throws Exception {
        TransformerStepPlugin plugin = LoadedExtensions.getInstance().getTransformerStepPlugins().get(name);
        if (plugin == null) {
            String message = "Unable to find Transformer Step Plugin: " + name;
            Exception e = new Exception(message);
            parent.alertError(this, message);
            throw new Exception(e);
        } else {
            return plugin;
        }
    }

    /**
     * prepData( int row ) works to move the data in a panel for moves or deletes
     */
    private void prepData(int row) {
        Map<Object, Object> d = (Map<Object, Object>) transformerTableModel.getValueAt(row, STEP_DATA_COL);
        String type = (String) transformerTableModel.getValueAt(row, STEP_TYPE_COL);
        setPanelData(type, d);
    }

    private void setRowData(Step step, int row, boolean selectRow) {
        // TODO: Check the logic of this with plugins
        Object[] tableData = new Object[NUMBER_OF_COLUMNS];

        tableData[STEP_NUMBER_COL] = step.getSequenceNumber();
        TransformerStepPlugin plugin;
        try {
            plugin = getPlugin(step.getType());
            String stepName = step.getName();
            if (stepName == null || stepName.equals("")) {
                plugin.setData(connector.getMode(), (Map<Object, Object>) step.getData());
                stepName = plugin.getStepName();
            }
            tableData[STEP_NAME_COL] = stepName;
            tableData[STEP_TYPE_COL] = step.getType();
            tableData[STEP_DATA_COL] = step.getData();

            updating = true;
            transformerTableModel.addRow(tableData);
            if (selectRow) {
                transformerTable.setRowSelectionInterval(row, row);
            }
            updating = false;
        } catch (Exception e) {
            parent.alertThrowable(this, e);
        }
    }

    /**
     * void addNewStep() add a new step to the end of the list
     */
    public void addNewStep() {
        addNewStep("", "", "", MAPPER);
        updateCodePanel(null);
    }

    /**
     * void addNewStep() add a new step to the end of the list
     */
    public void addNewStep(String name, String variable, String mapping, String type) {
        TransformerStepPlugin plugin;

        try {
            plugin = getPlugin(type);
        } catch (Exception e) {
            parent.alertThrowable(this, e);
            return;
        }

        saveData(transformerTable.getSelectedRow());

        if (!invalidVar || transformerTable.getRowCount() == 0) {
            int rowCount = transformerTable.getRowCount();
            modified = true;
            Step step = new Step();
            step.setSequenceNumber(rowCount);
            step.setScript("");
            step.setName(name);

            if (type.equals(MAPPER)) {
                if (LoadedExtensions.getInstance().getTransformerStepPlugins().containsKey(MAPPER)) {
                    step.setType(MAPPER); // mapper type by default, inbound
                    LoadedExtensions.getInstance().getTransformerStepPlugins().get(MAPPER).initData();
                } else {
                    System.out.println("Mapper Plugin not found");
                    step.setType(LoadedExtensions.getInstance().getTransformerStepPlugins().keySet().iterator().next());
                }
            } else if (type.equals(MESSAGE_BUILDER)) {
                if (LoadedExtensions.getInstance().getTransformerStepPlugins().containsKey(MESSAGE_BUILDER)) {
                    step.setType(MESSAGE_BUILDER); // mapper type by default,
                    // inbound
                    LoadedExtensions.getInstance().getTransformerStepPlugins().get(MESSAGE_BUILDER).initData();
                } else {
                    System.out.println("Message Builder Plugin not found");
                    step.setType(LoadedExtensions.getInstance().getTransformerStepPlugins().keySet().iterator().next());
                }
            }

            LinkedHashMap<Object, Object> data = new LinkedHashMap<Object, Object>();
            data.put("Mapping", mapping);
            data.put("Variable", variable);

            step.setData(data);

            if (plugin.isProvideOwnStepName()) {
                plugin.setData(connector.getMode(), data);
                step.setName(plugin.getStepName());
                plugin.clearData();
            }

            setRowData(step, rowCount, true);
            prevSelRow = rowCount;
            updateStepNumbers();
            transformerTable.setRowSelectionInterval(rowCount, rowCount);
            transformerTable.setVisibleRowCount(rowCount);
            transformerTablePane.getViewport().setViewPosition(new Point(0, transformerTable.getRowHeight() * rowCount));
        }
    }

    /**
     * void deleteStep(MouseEvent evt) delete all selected rows
     */
    public void deleteStep() {
        modified = true;
        if (transformerTable.isEditing()) {
            transformerTable.getCellEditor(transformerTable.getEditingRow(), transformerTable.getEditingColumn()).stopCellEditing();
        }
        updating = true;

        int row = transformerTable.getSelectedRow();
        if (isValid(row + 1)) {
            prepData(row + 1);
        }
        if (isValid(row)) {
            transformerTableModel.removeRow(row);
        }
        updating = false;

        if (isValid(row)) {
            transformerTable.setRowSelectionInterval(row, row);
        } else if (isValid(row - 1)) {
            transformerTable.setRowSelectionInterval(row - 1, row - 1);
        } else {
            stepPanel.showCard(BLANK_TYPE);
            for (TransformerStepPlugin plugin : LoadedExtensions.getInstance().getTransformerStepPlugins().values()) {
                plugin.clearData();
            }
        }
        updateStepNumbers();

        if (transformerTable.getRowCount() == 0) {
            scriptTextArea.setText("");
            tabbedPane.setSelectedIndex(0);
            transformerTable.getSelectionModel().clearSelection();
            updateCodePanel(null);
        }

        invalidVar = false;
    }

    /*
     * Import the transfomer
     */
    public void doImport() {
        String content = parent.browseForFileString("XML");

        if (content != null) {
            importTransformer(content);
        }
    }

    private void importTransformer(String content) {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        Transformer importTransformer = null;

        try {
            /*
             * Note: Transformers generated prior to version 3.0.0 cannot be imported and migrated,
             * because the lack of a version field poses problems with migration. However they can
             * still be imported as part of a connector.
             */
            importTransformer = serializer.deserialize(content, Transformer.class);
        } catch (Exception e) {
            parent.alertError(this, "Invalid transformer file.");
            return;
        }

        if (alertUnsupportedStepTypes(importTransformer)) {
            return;
        }

        prevSelRow = -1;
        modified = true;
        invalidVar = false;

        boolean append = (transformerTableModel.getRowCount() > 0 && parent.alertOption(parent, "Would you like to append the steps from the imported transformer to the existing transformer?"));

        /*
         * When appending, we merely add the steps from the transformer being imported. When not
         * appending, we replace the entire transformer with the one being imported.
         */
        if (append) {
            int row = transformerTableModel.getRowCount();

            for (Step step : importTransformer.getSteps()) {
                setRowData(step, row++, false);
            }

            updateStepNumbers();
        } else {
            if (isResponse) {
                connector.setResponseTransformer(importTransformer);
            } else {
                if (connector.getMetaDataId() > 0) {
                    Transformer previousTransformer = connector.getTransformer();
                    if (!previousTransformer.getInboundDataType().equals(importTransformer.getInboundDataType())) {
                        importTransformer.setInboundDataType(previousTransformer.getInboundDataType());
                        importTransformer.setInboundProperties(previousTransformer.getInboundProperties());
                    }
                }
                connector.setTransformer(importTransformer);

                if (connector.getMetaDataId() == 0) {
                    PlatformUI.MIRTH_FRAME.channelEditPanel.updateAttachmentHandler(importTransformer.getInboundDataType());
                }
            }

            /*
             * We don't need to check the boolean return value from load() because we already
             * checked for unsupported step types earlier in this method.
             */
            load(connector, importTransformer, modified, isResponse);
        }
    }

    /*
     * Export the transfomer
     */
    public void doExport() {
        accept(false);

        if (invalidVar) {
            return;
        }

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String transformerXML = serializer.serialize(transformer);

        parent.exportFile(transformerXML, null, "XML", "Transformer");
    }

    /*
     * Validate the current step
     */
    public void doValidate() {
        String type = (String) transformerTable.getValueAt(transformerTable.getSelectedRow(), STEP_TYPE_COL);
        try {
            TransformerStepPlugin stepPlugin = getPlugin(type);
            int selectedStep = transformerTable.getSelectedRow();
            String validationMessage = stepPlugin.doValidate(stepPlugin.getData(selectedStep));

            if (validationMessage == null) {
                parent.alertInformation(this, "Validation successful.");
            } else {
                parent.alertInformation(this, validationMessage);
            }
        } catch (Exception e) {
            parent.alertThrowable(this, e);
        }
    }

    /**
     * Run a specific step's validator.
     *
     * @param step
     * @return
     */
    public String validateStep(Step step) {
        try {
            TransformerStepPlugin stepPlugin = getPlugin(step.getType());
            return stepPlugin.doValidate((Map<Object, Object>) step.getData());
        } catch (Exception e) {
            parent.alertThrowable(this, e);
            return "Exception occurred during validation.";
        }
    }

    /**
     * void moveStep( int i ) move the selected row i places
     */
    public void moveStepUp() {
        moveStep(-1);
    }

    public void moveStepDown() {
        moveStep(1);
    }

    public void moveStep(int i) {
        modified = true;
        int selRow = transformerTable.getSelectedRow();
        int moveTo = selRow + i;

        // we can't move past the first or last row
        if (isValid(moveTo)) {
            for (TransformerStepPlugin plugin : LoadedExtensions.getInstance().getTransformerStepPlugins().values()) {
                plugin.moveStart();
            }

            saveData(selRow);

            // if the row was invalid, do not move the row.
            if (isInvalidVar()) {
                for (TransformerStepPlugin plugin : LoadedExtensions.getInstance().getTransformerStepPlugins().values()) {
                    plugin.moveEnd();
                }

                return;
            }
            loadData(moveTo);
            transformerTableModel.moveRow(selRow, selRow, moveTo);
            transformerTable.setRowSelectionInterval(moveTo, moveTo);

            for (TransformerStepPlugin plugin : LoadedExtensions.getInstance().getTransformerStepPlugins().values()) {
                plugin.moveEnd();
            }
        }

        updateStepNumbers();
        parent.setSaveEnabled(true);
    }

    public List<Step> buildStepList(List<Step> list, int endingRow) {
        for (int i = 0; i < endingRow; i++) {
            Step step = new Step();
            step.setSequenceNumber(Integer.parseInt(transformerTable.getValueAt(i, STEP_NUMBER_COL).toString()));
            step.setName((String) transformerTableModel.getValueAt(i, STEP_NAME_COL));
            step.setType((String) transformerTableModel.getValueAt(i, STEP_TYPE_COL));
            step.setData((LinkedHashMap<Object, Object>) transformerTableModel.getValueAt(i, STEP_DATA_COL));

            Map<Object, Object> map = (Map<Object, Object>) step.getData();
            try {
                step.setScript(getPlugin(step.getType()).getScript(map));
            } catch (Exception e) {
                parent.alertThrowable(this, e);
            }
            list.add(step);
        }
        return list;
    }

    private Set<String> getRuleVariables(int row) {
        Set<String> concatenatedRules = new LinkedHashSet<String>();
        VariableListUtil.getRuleVariables(concatenatedRules, channel.getSourceConnector(), false);

        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (connector == destination) {
                VariableListUtil.getRuleVariables(concatenatedRules, destination, true);
                seenCurrent = true;
            } else if (!seenCurrent) {
                VariableListUtil.getRuleVariables(concatenatedRules, destination, false);
                concatenatedRules.add(destination.getName());
            }
        }
        return concatenatedRules;
    }

    private Set<String> getStepVariables(int row) {
        Set<String> concatenatedSteps = new LinkedHashSet<String>();
        VariableListUtil.getStepVariables(concatenatedSteps, channel.getSourceConnector().getTransformer(), false);

        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (connector == destination) {
                if (isResponse) {
                    VariableListUtil.getStepVariables(concatenatedSteps, destination.getTransformer(), true);
                    VariableListUtil.getStepVariables(concatenatedSteps, destination.getResponseTransformer(), true, row);
                } else {
                    VariableListUtil.getStepVariables(concatenatedSteps, destination.getTransformer(), true, row);
                }
                seenCurrent = true;
            } else if (!seenCurrent) {
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getTransformer(), false);
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getResponseTransformer(), false);
                concatenatedSteps.add(destination.getName());
            }
        }
        return concatenatedSteps;
    }

    /**
     * void accept(MouseEvent evt) returns a vector of vectors to the caller of this.
     */
    public void accept() {
        accept(true);
    }

    public void accept(boolean returning) {
        saveData(transformerTable.getSelectedRow());

        if (!invalidVar) {
            List<Step> list = buildStepList(new ArrayList<Step>(), transformerTable.getRowCount());

            transformer.setSteps(list);

            String inboundDataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(tabTemplatePanel.getIncomingDataType());
            String outboundDataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(tabTemplatePanel.getOutgoingDataType());

            if (LoadedExtensions.getInstance().getDataTypePlugins().containsKey(inboundDataType)) {
                transformer.setInboundDataType(inboundDataType);
            }

            if (LoadedExtensions.getInstance().getDataTypePlugins().containsKey(outboundDataType)) {
                transformer.setOutboundDataType(outboundDataType);

                if (connector.getMode() == Connector.Mode.SOURCE) {
                    for (Connector c : channel.getDestinationConnectors()) {
                        if (!c.getTransformer().getInboundDataType().equals(outboundDataType)) {
                            c.getTransformer().setInboundDataType(outboundDataType);

                            c.getTransformer().setInboundProperties(LoadedExtensions.getInstance().getDataTypePlugins().get(outboundDataType).getDefaultProperties());
                        }
                    }
                }
            }

            transformer.setInboundTemplate(tabTemplatePanel.getIncomingMessage());
            transformer.setOutboundTemplate(tabTemplatePanel.getOutgoingMessage());

            transformer.setInboundProperties(tabTemplatePanel.getIncomingDataProperties());
            transformer.setOutboundProperties(tabTemplatePanel.getOutgoingDataProperties());

            // reset the task pane and content to channel edit page
            if (returning) {
                parent.channelEditPanel.setDestinationVariableList();
                parent.setCurrentContentPage(parent.channelEditPanel);
                parent.setFocus(parent.channelEditTasks);
                parent.setPanelName("Edit Channel - " + parent.channelEditPanel.currentChannel.getName());
                if (modified) {
                    parent.setSaveEnabled(true);
                }
                parent.channelEditPanel.updateComponentShown();
                modified = false;
            }

            invalidVar = false;
        }
    }

    public void setRowSelectionInterval(int index0, int index1) {
        // if (transformerTable.getRowCount() > index0 &&
        // transformerTable.getRowCount() >= index1){
        transformerTable.setRowSelectionInterval(index0, index1);
        // }
    }

    public Frame getParentFrame() {
        return parent;
    }

    /**
     * void updateStepNumbers() traverses the table and updates all data numbers, both in the model
     * and the view, after any change to the table
     */
    private void updateStepNumbers() {
        updating = true;

        int rowCount = transformerTableModel.getRowCount();
        int selRow = transformerTable.getSelectedRow();
        String type = new String();
        for (int i = 0; i < rowCount; i++) {
            transformerTableModel.setValueAt(i, i, STEP_NUMBER_COL);
        }
        if (isValid(selRow)) {
            transformerTable.setRowSelectionInterval(selRow, selRow);
            loadData(selRow);
            type = transformerTableModel.getValueAt(selRow, STEP_TYPE_COL).toString();
            stepPanel.showCard(type);
        } else if (rowCount > 0) {
            transformerTable.setRowSelectionInterval(0, 0);
            loadData(0);
            type = transformerTableModel.getValueAt(0, STEP_TYPE_COL).toString();
            stepPanel.showCard(type);

        }

        updateTaskPane(type);
        updating = false;
    }

    /**
     * updateTaskPane() configure the task pane so that it shows only relevant tasks
     */
    public void updateTaskPane(String newType) {
        int rowCount = transformerTableModel.getRowCount();
        if (rowCount <= 0) {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 1, -1, false);
        } else if (rowCount == 1) {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 0, -1, true);
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, -1, false);
        } else {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 0, -1, true);

            int selRow = transformerTable.getSelectedRow();
            if (selRow == 0) // hide move up
            {
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 5, 5, false);
            } else if (selRow == rowCount - 1) // hide move down
            {
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 6, 6, false);
            }
        }
        parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 2, 3, true);

        try {
            if (newType != null && !newType.equals("")) {
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, 4, getPlugin(newType).showValidateTask());
            } else {
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, 4, false);
            }
        } catch (Exception e) {
            parent.alertThrowable(this, e);
        }
    }

    public int getSelectedRow() {
        return transformerTable.getSelectedRow();
    }

    public DefaultTableModel getTableModel() {
        return transformerTableModel;
    }

    public void resizePanes() {
        hSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 2 - PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 3.5));
        vSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 2 + PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 6.7));
        tabTemplatePanel.resizePanes();
    }

    public boolean isInvalidVar() {
        return invalidVar;
    }

    public void setInvalidVar(boolean invalidVar) {
        this.invalidVar = invalidVar;
    }

    private void updateCodePanel(String stepType) {
        int row = transformerTable.getSelectedRow();

        if (row != -1) {
            String type = StringUtils.isNotEmpty(stepType) ? stepType : (String) transformerTable.getValueAt(row, STEP_TYPE_COL);
            boolean hideScriptTab = type.equalsIgnoreCase("Javascript") || type.equalsIgnoreCase("External Script");

            if (hideScriptTab && tabbedPane.getTabCount() == 2) {
                tabbedPane.removeTabAt(CODE_TAB);
            } else if (!hideScriptTab && tabbedPane.getTabCount() == 1) {
                tabbedPane.addTab("Generated Script", generatedScriptPanel);
            }

            if (!hideScriptTab) {
                TransformerStepPlugin plugin = null;
                try {
                    plugin = getPlugin(type);
                    Map<Object, Object> dataMap = stepPanel.getData();
                    scriptTextArea.setText(dataMap != null ? plugin.getGeneratedScript(dataMap) : "");
                } catch (Exception e) {
                    scriptTextArea.setText("");
                }
            }

            if (switchTab && tabbedPane.getTabCount() == 2) {
                tabbedPane.setSelectedIndex(CODE_TAB);
            }
        } else if (tabbedPane.getTabCount() == 2) {
            tabbedPane.removeTabAt(CODE_TAB);
        }
    }
}