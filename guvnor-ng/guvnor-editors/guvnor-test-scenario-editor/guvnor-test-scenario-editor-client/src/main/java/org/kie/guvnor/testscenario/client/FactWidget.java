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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.DroolsGuvnorImageResources;
import org.drools.guvnor.client.resources.DroolsGuvnorImages;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.testing.Scenario;
import org.drools.ide.common.client.modeldriven.testing.ExecutionTrace;
import org.drools.ide.common.client.modeldriven.testing.Fixture;
import org.drools.ide.common.client.modeldriven.testing.FixtureList;
import org.kie.guvnor.commons.ui.client.resources.CommonAltedImages;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.testscenario.client.resources.i18n.TestScenarioConstants;
import org.kie.guvnor.testscenario.service.model.ExecutionTrace;
import org.kie.guvnor.testscenario.service.model.Fixture;
import org.kie.guvnor.testscenario.service.model.FixtureList;
import org.kie.guvnor.testscenario.service.model.Scenario;
import org.uberfire.client.common.ImageButton;

public abstract class FactWidget extends HorizontalPanel {

    protected final ScenarioParentWidget parent;
    protected final Scenario scenario;
    protected final FixtureList definitionList;

    public FactWidget(String factType,
                      FixtureList definitionList,
                      Scenario scenario,
                      DataModelOracle dmo,
                      ScenarioParentWidget parent,
                      ExecutionTrace executionTrace,
                      String headerText) {
        this.parent = parent;
        this.scenario = scenario;
        this.definitionList = definitionList;

        add(new DataInputWidget(factType,
                definitionList,
                scenario,
                dmo,
                parent,
                executionTrace,
                headerText));
        add(new DeleteButton(definitionList));
    }

    protected void onDelete() {
        if (Window.confirm(TestScenarioConstants.INSTANCE.AreYouSureYouWantToRemoveThisBlockOfData())) {
            for (Fixture f : definitionList)
                scenario.removeFixture(f);
            parent.renderEditor();
        }
    }

    class DeleteButton
            extends ImageButton {

        public DeleteButton(final FixtureList definitionList) {
            super(CommonAltedImages.INSTANCE.DeleteItemSmall(),
                   TestScenarioConstants.INSTANCE.RemoveThisBlockOfData() );

            addClickHandler( new ClickHandler() {

                public void onClick(ClickEvent event) {
                    onDelete();
                }
            } );
        }
    }
}
