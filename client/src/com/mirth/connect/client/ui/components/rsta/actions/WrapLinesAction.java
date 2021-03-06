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

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class WrapLinesAction extends MirthRecordableTextAction {

    public WrapLinesAction(MirthRSyntaxTextArea textArea) {
        super(textArea, ActionInfo.DISPLAY_WRAP_LINES);
    }

    @Override
    public void actionPerformedImpl(ActionEvent evt) {
        textArea.setLineWrap(!evt.getActionCommand().equals(FORCE_OFF_COMMAND) && (evt.getActionCommand().equals(FORCE_ON_COMMAND) || !textArea.getLineWrap()));
    }
}