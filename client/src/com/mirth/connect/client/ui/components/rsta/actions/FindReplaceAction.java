/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import java.awt.event.ActionEvent;

import com.mirth.connect.client.ui.components.rsta.FindReplaceDialog;
import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class FindReplaceAction extends MirthRecordableTextAction {

    public FindReplaceAction(MirthRSyntaxTextArea textArea) {
        super(textArea, ActionInfo.FIND_REPLACE);
    }

    @Override
    public void actionPerformedImpl(ActionEvent evt) {
        new FindReplaceDialog(textArea);
    }
}