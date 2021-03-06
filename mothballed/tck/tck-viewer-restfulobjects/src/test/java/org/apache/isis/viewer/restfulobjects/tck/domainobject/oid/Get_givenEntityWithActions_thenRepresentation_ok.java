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
package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid;

import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.*;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectMemberRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class Get_givenEntityWithActions_thenRepresentation_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectResource domainObjectResource;
    private DomainObjectRepresentation domainObjectRepr;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        domainObjectResource = client.getDomainObjectResource();
        
    }

    @Test
    public void thenMembers() throws Exception {

        // when
        final Response jaxrsResponse = domainObjectResource.object("RTNE","78");
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = RestfulResponse.ofT(jaxrsResponse);
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));

        // then
        domainObjectRepr = restfulResponse.getEntity();
        assertThat(domainObjectRepr, is(not(nullValue())));


        final LinkRepresentation self = domainObjectRepr.getSelf();

        // then actions
        final JsonRepresentation actions = domainObjectRepr.getActions();
        assertThat(actions.size(), is(3));

        final DomainObjectMemberRepresentation containsAction = domainObjectRepr.getAction("contains");
        assertThat(containsAction.getString("memberType"), is("action"));
        assertThat(containsAction.getString("id"), is("contains"));

        LinkRepresentation containsActionLink = containsAction.getLinkWithRel(Rel.DETAILS.andParam("action", "contains"));
        assertThat(containsActionLink.getRel(), is(Rel.DETAILS.andParam("action", "contains")));
        assertThat(containsActionLink.getHref(), is(self.getHref() + "/actions/contains"));
        assertThat(containsActionLink.getType(), is(RepresentationType.OBJECT_ACTION.getMediaType()));
        assertThat(containsActionLink.getHttpMethod(), is(RestfulHttpMethod.GET));

        // can also look up with abbreviated parameterized version of Rel
        containsActionLink = containsAction.getLinkWithRel(Rel.DETAILS);
        assertThat(containsActionLink.getRel(), is("urn:org.restfulobjects:rels/details;action=\"contains\""));
        assertThat(containsActionLink.getHref(), is(self.getHref() + "/actions/contains"));
        assertThat(containsActionLink.getType(), is(RepresentationType.OBJECT_ACTION.getMediaType()));
        assertThat(containsActionLink.getHttpMethod(), is(RestfulHttpMethod.GET));

    }
    
}
