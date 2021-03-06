/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dicomviewer;

import ij.plugin.DICOM;

import java.awt.Dimension;
import java.awt.Point;
import java.io.ByteArrayInputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.StringUtils;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.plugins.AttachmentViewer;

public class DICOMViewer extends AttachmentViewer {

    public DICOMViewer(String name) {
        super(name);
    }

    public boolean handleMultiple() {
        return true;
    }

    public void viewAttachments(String channelId, Long messageId, String attachmentId) {
        // do viewing code
        try {
            ConnectorMessage message = parent.messageBrowser.getSelectedConnectorMessage();
            byte[] rawImage = StringUtils.getBytesUsAscii(parent.mirthClient.getDICOMMessage(message));
            ByteArrayInputStream bis = new ByteArrayInputStream(rawImage);
            DICOM dcm = new DICOM(new Base64InputStream(bis));
            // run() is required to create the dicom object. The argument serves multiple purposes. If it is null or empty, it opens a dialog to select a dicom file.
            // Otherwise, if dicom.show() is called, it is the title of the dialog. Since we are showing the dialog here, we pass the string we want to use as the title.
            dcm.run("DICOM Image Viewer");
            dcm.show();
            Dimension dlgSize = dcm.getWindow().getSize();
            Dimension frmSize = parent.getSize();
            Point loc = parent.getLocation();

            if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
                dcm.getWindow().setLocationRelativeTo(null);
            } else {
                dcm.getWindow().setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
            }

        } catch (Exception e) {
            parent.alertThrowable(parent, e);
        }

    }

    @Override
    public boolean isContentTypeViewable(String contentType) {
        return org.apache.commons.lang.StringUtils.containsIgnoreCase(contentType, "dicom");
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }

    @Override
    public String getPluginPointName() {
        return "DICOM Viewer";
    }
}
