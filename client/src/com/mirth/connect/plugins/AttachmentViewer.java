/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

public abstract class AttachmentViewer extends ClientPlugin {

    public AttachmentViewer(String name) {
        super(name);
    }

    public abstract void viewAttachments(String channelId, Long messageId, String attachmentId);

    public abstract boolean isContentTypeViewable(String contentType);

    public abstract boolean handleMultiple();
}
