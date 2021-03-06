/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.commons.ui.client.handlers;

import com.google.gwt.core.client.Callback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.kie.guvnor.project.service.ProjectService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.context.WorkbenchContext;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.widgets.events.PathChangeEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@ApplicationScoped
public class NewResourcePresenter {

    public interface View
            extends
            UberView<NewResourcePresenter> {

        void show();

        void hide();

        void setActiveHandler( final NewResourceHandler activeHandler );

        void addHandler( final NewResourceHandler handler );

        String getFileName();

        void showMissingNameError();

        void enableHandler( final NewResourceHandler handler,
                            final boolean enable );

    }

    @Inject
    protected WorkbenchContext context;

    @Inject
    private IOCBeanManager iocBeanManager;

    @Inject
    private Caller<ProjectService> projectService;

    @Inject
    private View view;

    private NewResourceHandler activeHandler = null;

    private final List<NewResourceHandler> handlers = new LinkedList<NewResourceHandler>();

    @PostConstruct
    private void setup() {
        view.init( this );
        final Collection<IOCBeanDef<NewResourceHandler>> handlerBeans = iocBeanManager.lookupBeans( NewResourceHandler.class );
        for ( IOCBeanDef<NewResourceHandler> handlerBean : handlerBeans ) {
            final NewResourceHandler handler = handlerBean.getInstance();
            handlers.add( handler );
            view.addHandler( handler );
        }
    }

    public void selectedPathChanged( @Observes final PathChangeEvent event ) {
        final Path path = event.getPath();
        enableNewResourceHandlers( path );
    }

    private void enableNewResourceHandlers( final Path path ) {
        for ( final NewResourceHandler handler : this.handlers ) {
            handler.acceptPath( path,
                                new Callback<Boolean, Void>() {
                                    @Override
                                    public void onFailure( Void reason ) {
                                        // Nothing to do there right now.
                                    }

                                    @Override
                                    public void onSuccess( final Boolean result ) {
                                        if ( result != null ) {
                                            view.enableHandler( handler,
                                                                result );
                                        }
                                    }
                                } );

        }
    }

    public void show() {
        show( null );
    }

    public void show( final NewResourceHandler handler ) {
        activeHandler = handler;
        if ( activeHandler == null ) {
            activeHandler = handlers.get( 0 );
        }
        view.show();
        view.setActiveHandler( activeHandler );
    }

    void setActiveHandler( final NewResourceHandler handler ) {
        activeHandler = handler;
    }

    public void makeItem() {
        if ( activeHandler != null ) {
            if ( validate() ) {
                if ( activeHandler.validate() ) {
                    activeHandler.create( context.getActivePath(), view.getFileName() );
                    view.hide();
                }
            }
        }
    }

    private boolean validate() {
        boolean isValid = true;
        if ( view.getFileName().isEmpty() ) {
            view.showMissingNameError();
            isValid = false;
        }
        return isValid;
    }

}
