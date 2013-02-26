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

import com.google.gwt.user.client.ui.Widget;
import org.kie.guvnor.commons.ui.client.resources.ItemAltedImages;
import org.kie.guvnor.testscenario.client.resources.i18n.TestScenarioConstants;
import org.kie.guvnor.testscenario.client.resources.images.TestScenarioAltedImages;
import org.kie.guvnor.testscenario.model.ExecutionTrace;
import org.kie.guvnor.testscenario.model.Fixture;
import org.kie.guvnor.testscenario.model.Scenario;
import org.kie.guvnor.testscenario.model.VerifyFact;
import org.kie.guvnor.testscenario.model.VerifyField;
import org.kie.guvnor.testscenario.model.VerifyRuleFired;

import java.util.ArrayList;

public class ExpectationButton
        extends TestScenarioButton {

    private final String packageName;

    public ExpectationButton(final String packageName,
                             final ExecutionTrace previousEx,
                             final Scenario scenario,
                             ScenarioEditorPresenter scenarioWidget) {
        super(ItemAltedImages.INSTANCE.NewItem(),
                TestScenarioConstants.INSTANCE.AddANewExpectation(),
                previousEx,
                scenario,
                scenarioWidget);

        this.packageName = packageName;
    }

    @Override
    protected TestScenarioButtonPopup getPopUp() {
        return new NewExpectationPopup();
    }

    class NewExpectationPopup extends TestScenarioButtonPopup {
        public NewExpectationPopup() {
            super(TestScenarioAltedImages.INSTANCE.RuleAsset(),
                    TestScenarioConstants.INSTANCE.NewExpectation());

            Widget selectRule = parent.getRuleSelectionWidget(packageName,
                    new RuleSelectionEvent() {

                        public void ruleSelected(String name) {
                            VerifyRuleFired verifyRuleFired = new VerifyRuleFired(name,
                                    null,
                                    Boolean.TRUE);
                            scenario.insertBetween(previousEx,
                                    verifyRuleFired);
                            parent.renderEditor();
                            hide();
                        }
                    });

            addAttribute(TestScenarioConstants.INSTANCE.Rule(),
                    selectRule);

            addAttribute(TestScenarioConstants.INSTANCE.FactValue(),
                    new FactsPanel());

            //add in list box for anon facts
            addAttribute(TestScenarioConstants.INSTANCE.AnyFactThatMatches(),
                    new AnyFactThatMatchesPanel());

        }

        class AnyFactThatMatchesPanel extends ListBoxBasePanel {
            public AnyFactThatMatchesPanel() {
                super(dmo.getFactTypes());
            }

            @Override
            public Fixture getFixture() {
                String factName = valueWidget.getItemText(valueWidget.getSelectedIndex());
                return new VerifyFact(factName,
                        new ArrayList<VerifyField>(),
                        true);
            }
        }

        class FactsPanel extends ListBoxBasePanel {

            public FactsPanel() {
                super(scenario.getFactNamesInScope(previousEx,
                        true));
            }

            @Override
            public Fixture getFixture() {
                String factName = valueWidget.getItemText(valueWidget.getSelectedIndex());
                return new VerifyFact(factName,
                        new ArrayList<VerifyField>());
            }

        }
    }
}
