/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.testscenario.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.*;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.messages.Constants;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.testing.Scenario;
import org.drools.ide.common.client.modeldriven.testing.ExecutionTrace;
import org.drools.ide.common.client.modeldriven.testing.Fixture;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.testscenario.client.resources.i18n.TestScenarioConstants;
import org.kie.guvnor.testscenario.service.model.ExecutionTrace;
import org.kie.guvnor.testscenario.service.model.Fixture;
import org.kie.guvnor.testscenario.service.model.Scenario;
import org.uberfire.client.common.FormStylePopup;
import org.uberfire.client.common.ImageButton;

abstract class TestScenarioButton extends ImageButton {

    protected final Scenario scenario;
    protected final ScenarioEditorPresenter parent;
    protected final DataModelOracle            dmo;
    protected final ExecutionTrace previousEx;

    public TestScenarioButton(Image img,
                              String tooltip,
                              final ExecutionTrace previousEx,
                              final Scenario scenario,
                              ScenarioEditorPresenter scenarioWidget) {
        super( img,
               tooltip );
        this.previousEx = previousEx;
        this.scenario = scenario;
        this.parent = scenarioWidget;
        this.dmo = scenarioWidget.dmo;

        addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent event) {
                final FormStylePopup pop = getPopUp();
                pop.show();
            }
        } );
    }

    protected abstract TestScenarioButtonPopup getPopUp();

    protected abstract class TestScenarioButtonPopup extends FormStylePopup {
        public TestScenarioButtonPopup(Image image,
                                       String text) {
            super( image,
                   text );
        }

        protected abstract class BasePanel<T extends Widget> extends HorizontalPanel {
            protected final T      valueWidget;
            protected final Button add = new Button( TestScenarioConstants.INSTANCE.Add() );

            public BasePanel() {
                valueWidget = getWidget();

                addAddButtonClickHandler();

                initWidgets();
            }

            protected void initWidgets() {
                add( valueWidget );
                add( add );
            }

            protected void addAddButtonClickHandler() {
                add.addClickHandler( new ClickHandler() {

                    public void onClick(ClickEvent event) {
                        scenario.insertBetween( previousEx,
                                                getFixture() );
                        parent.renderEditor();
                        hide();
                    }
                } );
            }

            public abstract T getWidget();

            public abstract Fixture getFixture();

        }

        protected abstract class ListBoxBasePanel extends BasePanel<ListBox> {

            public ListBoxBasePanel(List<String> listItems) {
                super();
                fillWidget( listItems );
            }

            public ListBoxBasePanel(String[] listItems) {
                super();
                List<String> list = new ArrayList<String>();
                for ( String string : listItems ) {
                    list.add( string );
                }
                fillWidget( list );
            }

            protected void fillWidget(List<String> listItems) {
                for ( String item : listItems ) {
                    valueWidget.addItem( item );
                }
            }

            @Override
            public ListBox getWidget() {
                return new ListBox();
            }
        }
    }
}
