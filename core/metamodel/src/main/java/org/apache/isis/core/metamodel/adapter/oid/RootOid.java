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

package org.apache.isis.core.metamodel.adapter.oid;

import java.io.IOException;
import java.io.Serializable;

import com.google.common.base.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.commons.url.UrlEncodingUtils;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

public class RootOid implements TypedOid, Serializable {

    //region > fields
    private final static Logger LOG = LoggerFactory.getLogger(RootOid.class);

    private static final long serialVersionUID = 1L;

    private final ObjectSpecId objectSpecId;
    private final String identifier;
    private final State state;

    // not part of equality check
    private Version version;

    private int cachedHashCode;
    //endregion

    //region > Constructor, factory methods
    public static RootOid createTransient(final ObjectSpecId objectSpecId, final String identifier) {
        return new RootOid(objectSpecId, identifier, State.TRANSIENT);
    }

    public static RootOid create(final Bookmark bookmark) {
        return new RootOid(ObjectSpecId.of(bookmark.getObjectType()), bookmark.getIdentifier(), State.from(bookmark));
    }

    public static RootOid create(final ObjectSpecId objectSpecId, final String identifier) {
        return create(objectSpecId, identifier, null);
    }

    public static RootOid create(final ObjectSpecId objectSpecId, final String identifier, final Long versionSequence) {
        return create(objectSpecId, identifier, versionSequence, null, null);
    }

    public static RootOid create(final ObjectSpecId objectSpecId, final String identifier, final Long versionSequence, final String versionUser) {
        return create(objectSpecId, identifier, versionSequence, versionUser, null);
    }

    public static RootOid create(final ObjectSpecId objectSpecId, final String identifier, final Long versionSequence, final Long versionUtcTimestamp) {
        return create(objectSpecId, identifier, versionSequence, null, versionUtcTimestamp);
    }

    public static RootOid create(final ObjectSpecId objectSpecId, final String identifier, final Long versionSequence, final String versionUser, final Long versionUtcTimestamp) {
        return new RootOid(objectSpecId, identifier, State.PERSISTENT, Version.create(versionSequence,
                versionUser, versionUtcTimestamp));
    }

    public RootOid(final ObjectSpecId objectSpecId, final String identifier, final State state) {
        this(objectSpecId, identifier, state, (Version)null);
    }

    public RootOid(final ObjectSpecId objectSpecId, final String identifier, final State state, final Long versionSequence) {
        this(objectSpecId, identifier, state, versionSequence, null, null);
    }

    /**
     * If specify version sequence, can optionally specify the user that changed the object.  This is used for informational purposes only.
     */
    public RootOid(final ObjectSpecId objectSpecId, final String identifier, final State state, final Long versionSequence, final String versionUser) {
        this(objectSpecId, identifier, state, versionSequence, versionUser, null);
    }

    /**
     * If specify version sequence, can optionally specify utc timestamp that the oid was changed.  This is used for informational purposes only.
     */
    public RootOid(final ObjectSpecId objectSpecId, final String identifier, final State state, final Long versionSequence, final Long versionUtcTimestamp) {
        this(objectSpecId, identifier, state, versionSequence, null, versionUtcTimestamp);
    }

    /**
     * If specify version sequence, can optionally specify user and/or utc timestamp that the oid was changed.  This is used for informational purposes only.
     */
    public RootOid(final ObjectSpecId objectSpecId, final String identifier, final State state, final Long versionSequence, final String versionUser, final Long versionUtcTimestamp) {
        this(objectSpecId, identifier, state, Version.create(versionSequence, versionUser, versionUtcTimestamp));
    }

    public RootOid(final ObjectSpecId objectSpecId, final String identifier, final State state, final Version version) {
        Ensure.ensureThatArg(objectSpecId, is(not(nullValue())));
        Ensure.ensureThatArg(identifier, is(not(nullValue())));
        Ensure.ensureThatArg(identifier, is(not(IsisMatchers.contains("#"))), "identifier '" + identifier + "' contains a '#' symbol");
        Ensure.ensureThatArg(identifier, is(not(IsisMatchers.contains("@"))), "identifier '" + identifier + "' contains an '@' symbol");
        Ensure.ensureThatArg(state, is(not(nullValue())));

        this.objectSpecId = objectSpecId;
        this.identifier = identifier;
        this.state = state;
        this.version = version;
        initialized();
    }

    private void initialized() {
        cacheState();
    }

    //endregion

    //region > Encodeable
    public RootOid(final DataInputExtended input) throws IOException {
        final String oidStr = input.readUTF();
        final RootOid oid = getEncodingMarshaller().unmarshal(oidStr, RootOid.class);
        this.objectSpecId = oid.objectSpecId;
        this.identifier = oid.identifier;
        this.state = oid.state;
        this.version = oid.version;
        cacheState();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(enString(getEncodingMarshaller()));
    }


    /**
     * Cannot be a reference because Oid gets serialized by wicket viewer
     */
    private OidMarshaller getEncodingMarshaller() {
        return new OidMarshaller();
    }

    //endregion

    //region > deString'able, enString
    public static RootOid deStringEncoded(final String urlEncodedOidStr, final OidMarshaller oidMarshaller) {
        final String oidStr = UrlEncodingUtils.urlDecode(urlEncodedOidStr);
        return deString(oidStr, oidMarshaller);
    }

    public static RootOid deString(final String oidStr, final OidMarshaller oidMarshaller) {
        return oidMarshaller.unmarshal(oidStr, RootOid.class);
    }

    @Override
    public String enString(final OidMarshaller oidMarshaller) {
        return oidMarshaller.marshal(this);
    }

    @Override
    public String enStringNoVersion(final OidMarshaller oidMarshaller) {
        return oidMarshaller.marshalNoVersion(this);
    }
    //endregion

    //region > Properties
    public ObjectSpecId getObjectSpecId() {
        return objectSpecId;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isTransient() {
        return state.isTransient();
    }

    @Override
    public boolean isViewModel() {
        return state.isViewModel();
    }

    @Override
    public boolean isPersistent() {
        return state.isPersistent();
    }

    //endregion

    //region > Version

    public Version getVersion() {
        return version;
    }

    @Override
    public void setVersion(final Version version) {
        this.version = version;
    }
    //endregion

    //region > bookmark
    public Bookmark asBookmark() {
        final String objectType = state.asBookmarkObjectState().getCode() + getObjectSpecId().asString();
        final String identifier = getIdentifier();
        return new Bookmark(objectType, identifier);
    }
    //endregion

    //region > equals, hashCode

    private void cacheState() {
        cachedHashCode = 17;
        cachedHashCode = 37 * cachedHashCode + objectSpecId.hashCode();
        cachedHashCode = 37 * cachedHashCode + identifier.hashCode();
        cachedHashCode = 37 * cachedHashCode + (isTransient() ? 0 : 1);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return equals((RootOid) other);
    }

    public boolean equals(final RootOid other) {
        return Objects.equal(objectSpecId, other.getObjectSpecId()) && Objects.equal(identifier, other.getIdentifier()) && Objects.equal(isTransient(), other.isTransient());
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    //endregion

    //region > toString
    @Override
    public String toString() {
        return enString(new OidMarshaller());
    }

    //endregion

}
