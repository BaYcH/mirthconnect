/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.io.Serializable;

public class InvalidMapValue implements Serializable {

    private String valueXML;

    public InvalidMapValue(String valueXML) {
        this.valueXML = valueXML;
    }

    public String getValueXML() {
        return valueXML;
    }

    @Override
    public String toString() {
        return valueXML;
    }
}
