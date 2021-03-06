/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.html.crumb;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.viewer.html.request.Request;

public class ObjectFieldCrumb implements Crumb {
    private final String fieldName;

    public ObjectFieldCrumb(final String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public void debug(final DebugBuilder string) {
        string.appendln("Object Field Crumb");
        string.appendln("field name", fieldName);
    }

    @Override
    public String title() {
        return fieldName;
    }

    @Override
    public String toString() {
        return new ToString(this).append(title()).toString();
    }

    @Override
    public Request changeContext() {
        throw new NotYetImplementedException();
    }
}
