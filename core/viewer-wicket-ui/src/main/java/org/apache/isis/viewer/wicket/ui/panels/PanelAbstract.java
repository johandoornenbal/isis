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

package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryAware;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.isis.PersistenceSessionProvider;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.util.Components;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;

/**
 * Convenience adapter for {@link Panel}s built up using {@link ComponentType}s.
 */
// TODO mgrigorov: extend GenericPanel and make T the type of the model object, not the model
public abstract class PanelAbstract<T extends IModel<?>> extends Panel implements IHeaderContributor, PersistenceSessionProvider, AuthenticationSessionProvider,
        DeploymentCategoryProvider {

    private static final long serialVersionUID = 1L;

    private ComponentType componentType;

    public PanelAbstract(final ComponentType componentType) {
        this(componentType, null);
    }

    public PanelAbstract(final String id) {
        this(id, null);
    }

    public PanelAbstract(final ComponentType componentType, final T model) {
        this(componentType.getWicketId(), model);
    }

    public PanelAbstract(final String id, final T model) {
        super(id, model);
        this.componentType = ComponentType.lookup(id);
    }


    /**
     * Will be null if created using {@link #PanelAbstract(String, IModel)}.
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    @SuppressWarnings("unchecked")
    public T getModel() {
        return (T) getDefaultModel();
    }

    /**
     * For subclasses
     * 
     * @return
     */
    protected Component addOrReplace(final ComponentType componentType, final IModel<?> model) {
        return getComponentFactoryRegistry().addOrReplaceComponent(this, componentType, model);
    }

    /**
     * For subclasses
     */
    protected void permanentlyHide(final ComponentType... componentIds) {
        Components.permanentlyHide(this, componentIds);
    }

    /**
     * For subclasses
     */
    public void permanentlyHide(final String... ids) {
        Components.permanentlyHide(this, ids);
    }


    // ///////////////////////////////////////////////////////////////////
    // Hint support
    // ///////////////////////////////////////////////////////////////////

    public UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this);
    }

    public <T extends UiHintContainer> T getUiHintContainer(final Class<T> additionalConstraint) {
        return UiHintContainer.Util.hintContainerOf(this, additionalConstraint);
    }


    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    /**
     * The underlying {@link AuthenticationSession Isis session} wrapped in the
     * {@link #getWebSession() Wicket session}.
     * 
     * @return
     */
    @Override
    public AuthenticationSession getAuthenticationSession() {
        final AuthenticationSessionProvider asa = (AuthenticationSessionProvider) Session.get();
        return asa.getAuthenticationSession();
    }

    @Override
    public DeploymentCategory getDeploymentCategory() {
        return IsisContext.getDeploymentType().getDeploymentCategory();
    }


    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////

    @Override
    public PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected ServicesInjectorSpi getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }

    // /////////////////////////////////////////////////
    // Dependency Injection
    // /////////////////////////////////////////////////

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) getApplication()).getComponentFactoryRegistry();
    }

    
    // /////////////////////////////////////////////////
    // *Provider impl.
    // /////////////////////////////////////////////////
    
    @Override
    public void injectInto(final Object candidate) {
        if (AuthenticationSessionProviderAware.class.isAssignableFrom(candidate.getClass())) {
            final AuthenticationSessionProviderAware cast = AuthenticationSessionProviderAware.class.cast(candidate);
            cast.setAuthenticationSessionProvider(this);
        }
        if (DeploymentCategoryAware.class.isAssignableFrom(candidate.getClass())) {
            final DeploymentCategoryAware cast = DeploymentCategoryAware.class.cast(candidate);
            cast.setDeploymentCategory(this.getDeploymentCategory());
        }
    }

    /**
     * Helper method that looks up a domain service by type
     *
     * @param serviceClass The class of the domain service to lookup
     * @param <S> The type of the domain service to lookup
     * @return The found domain service
     */
    protected <S> S lookupService(final Class<S> serviceClass) {
        return getPersistenceSession().getServicesInjector().lookupService(serviceClass);
    }


    protected void addConfirmationDialogIfAreYouSureSemantics(final Component component, final SemanticsOf semanticsOf) {
        if (!semanticsOf.isAreYouSure()) {
            return;
        }

        final TranslationService translationService =
                getPersistenceSession().getServicesInjector().lookupService(TranslationService.class);

        ConfirmationConfig confirmationConfig = new ConfirmationConfig();

        final String context = IsisSystem.class.getName();
        final String areYouSure = translationService.translate(context, IsisSystem.MSG_ARE_YOU_SURE);
        final String confirm = translationService.translate(context, IsisSystem.MSG_CONFIRM);
        final String cancel = translationService.translate(context, IsisSystem.MSG_CANCEL);

        confirmationConfig
                .withTitle(areYouSure)
                .withBtnOkLabel(confirm)
                .withBtnCancelLabel(cancel)
                .withPlacement(TooltipConfig.Placement.right)
                .withBtnOkClass("btn btn-danger")
                .withBtnCancelClass("btn btn-default");

        component.add(new ConfirmationBehavior(confirmationConfig));
    }



}
