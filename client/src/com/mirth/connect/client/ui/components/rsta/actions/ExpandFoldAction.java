/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class ExpandFoldAction extends org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit.ChangeFoldStateAction {

    public ExpandFoldAction() {
        super(null, false);
        setProperties(MirthRSyntaxTextArea.getResourceBundle(), ActionInfo.FOLD_EXPAND.toString());
    }
}