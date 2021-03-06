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

package org.apache.isis.core.metamodel.facets.object.callbacks;

import org.apache.isis.applib.services.eventbus.AbstractLifecycleEvent;
import org.apache.isis.applib.services.eventbus.ObjectCreatedEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public class CreatedLifecycleEventFacetForDomainObjectAnnotation extends SingleClassValueFacetAbstract implements CreatedLifecycleEventFacet {

    static Class<CreatedLifecycleEventFacet> type() {
        return CreatedLifecycleEventFacet.class;
    }

    public CreatedLifecycleEventFacetForDomainObjectAnnotation(
            final FacetHolder holder,
            final Class<? extends ObjectCreatedEvent<?>> value,
            final SpecificationLoader specificationLoader) {
        super(type(), holder, value, specificationLoader);
    }

    public Class<? extends AbstractLifecycleEvent<?>> getEventType() {
        //noinspection unchecked
        return eventType();
    }

    Class eventType() {
        return value();
    }
}
