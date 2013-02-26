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

import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.testing.*;
import org.kie.guvnor.testscenario.service.model.ActivateRuleFlowGroup;
import org.kie.guvnor.testscenario.service.model.CallFixtureMap;
import org.kie.guvnor.testscenario.service.model.CallMethod;
import org.kie.guvnor.testscenario.service.model.ExecutionTrace;
import org.kie.guvnor.testscenario.service.model.FactData;
import org.kie.guvnor.testscenario.service.model.Field;
import org.kie.guvnor.testscenario.service.model.Fixture;
import org.kie.guvnor.testscenario.service.model.FixtureList;
import org.kie.guvnor.testscenario.service.model.FixturesMap;
import org.kie.guvnor.testscenario.service.model.RetractFact;
import org.kie.guvnor.testscenario.service.model.VerifyFact;
import org.kie.guvnor.testscenario.service.model.VerifyRuleFired;

import java.util.*;

/**
 * Some utility methods as the display logic is a bit hairy.
 */
public class ScenarioHelper {

    static final String RETRACT_KEY = "retract";
    static final String ACTIVATE_RULE_FLOW_GROUP = "activate_rule_flow_group";

    /**
     * Called lumpy map - as this takes a flat list of fixtures, and groups
     * things together. It will return a list - of which each element will
     * either be a list - or a map. If its a map - then its a map of FactData to
     * the fact type. If its a list, then it will be expectations or
     * retractions.
     * <p/>
     * Man, this will be so much nicer with generics.
     *
     * @return List<List<VeryifyRuleFired or VerifyFact or RetractFact> OR
     *         Map<String, List<FactData>> OR ExecutionTrace>
     */
    public List<Fixture> lumpyMap(List<Fixture> fixtures) {
        List<Fixture> output = new ArrayList<Fixture>();

        FixturesMap dataInput = new FixturesMap();
        CallFixtureMap callOnDataInput = new CallFixtureMap();
        FixtureList verifyFact = new FixtureList();
        FixtureList verifyRule = new FixtureList();
        FixtureList retractFacts = new FixtureList();
        for (Fixture fixture : fixtures) {
            if (fixture instanceof FactData) {
                accumulateDataForFactData(dataInput, (FactData) fixture);
            } else if (fixture instanceof CallMethod) {
                accumulateCallMethod(callOnDataInput, (CallMethod) fixture);
            } else if (fixture instanceof ActivateRuleFlowGroup) {
                accumulateDataForActivateRuleFlowGroup(dataInput, fixture);
            } else if (fixture instanceof RetractFact) {
                retractFacts.add(fixture);
            } else if (fixture instanceof VerifyRuleFired) {
                verifyRule.add(fixture);
            } else if (fixture instanceof VerifyFact) {
                verifyFact.add(fixture);
            } else if (fixture instanceof ExecutionTrace) {
                gatherFixtures(output, dataInput, callOnDataInput, verifyFact, verifyRule, retractFacts, false);

                output.add(fixture);

                verifyRule = new FixtureList();
                verifyFact = new FixtureList();
                retractFacts = new FixtureList();
                callOnDataInput = new CallFixtureMap();
                dataInput = new FixturesMap();
            }
        }
        gatherFixtures(output, dataInput, callOnDataInput, verifyFact, verifyRule, retractFacts, true);

        return output;
    }

    private void gatherFixtures(List<Fixture> output, FixturesMap dataInput, CallFixtureMap callOnDataInput, FixtureList verifyFact, FixtureList verifyRule, FixtureList retractFacts, boolean end) {
        if (verifyRule.size() > 0) output.add(verifyRule);
        if (verifyFact.size() > 0) output.add(verifyFact);
        if (retractFacts.size() > 0) dataInput.put(RETRACT_KEY, retractFacts);
        if (dataInput.size() > 0 || !end) output.add(dataInput); // want to have a place holder for the GUI
        if (callOnDataInput.size() > 0 || !end) output.add(callOnDataInput);
    }

    /**
     * Group the globals together by fact type.
     */
    public Map<String, FixtureList> lumpyMapGlobals(List<FactData> globals) {
        Map<String, FixtureList> map = new HashMap<String, FixtureList>();
        for (FactData factData : globals) {
            accumulateDataForFactData(map, factData);
        }
        return map;
    }

    private void accumulateDataForFactData(Map<String, FixtureList> dataInput, FactData fd) {
        if (!dataInput.containsKey(fd.getType())) {
            dataInput.put(fd.getType(), new FixtureList());
        }
        dataInput.get(fd.getType()).add(fd);

    }

    public List<ExecutionTrace> getExecutionTraceFor(List<Fixture> fixtures) {
        List<Fixture> processedFixtures = lumpyMap(fixtures);
        List<ExecutionTrace> listExecutionTrace = new ArrayList<ExecutionTrace>();
        for (int i = 0; i < processedFixtures.size(); i++) {
            final Object fixture = processedFixtures.get(i);
            if (fixture instanceof ExecutionTrace) {
                listExecutionTrace.add((ExecutionTrace) fixture);
            }
        }

        return listExecutionTrace;
    }

    private void accumulateDataForActivateRuleFlowGroup(Map<String, FixtureList> dataInput, Fixture f) {
        if (!dataInput.containsKey(ScenarioHelper.ACTIVATE_RULE_FLOW_GROUP)) {
            dataInput.put(ScenarioHelper.ACTIVATE_RULE_FLOW_GROUP, new FixtureList());
        }
        dataInput.get(ScenarioHelper.ACTIVATE_RULE_FLOW_GROUP).add(f);

    }

    private void accumulateCallMethod(Map<String, FixtureList> dataInput, CallMethod fd) {
        if (!dataInput.containsKey(fd.getVariable())) {
            dataInput.put(fd.getVariable(), new FixtureList());
        }
        dataInput.get(fd.getVariable()).add(fd);

    }

    static void removeFields(List<Fixture> factDatas, String field) {
        for (Fixture fixture : factDatas) {
            if (fixture instanceof FactData) {
                FactData factData = (FactData) fixture;
                for (Iterator<Field> fieldDataIterator = factData.getFieldData().iterator(); fieldDataIterator.hasNext(); ) {
                    if (fieldDataIterator.next().getName().equals(field)) {
                        fieldDataIterator.remove();
                    }
                }
            }
        }
    }
}
