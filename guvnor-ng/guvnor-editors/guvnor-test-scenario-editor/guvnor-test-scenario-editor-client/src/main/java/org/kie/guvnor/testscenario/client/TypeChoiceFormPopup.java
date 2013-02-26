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

package org.kie.guvnor.testscenario.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.InfoPopup;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.DroolsGuvnorImageResources;
import org.drools.guvnor.client.resources.DroolsGuvnorImages;
import org.drools.ide.common.client.modeldriven.testing.FieldData;

public class TypeChoiceFormPopup
        extends FormStylePopup
        implements HasSelectionHandlers<Integer> {


    public TypeChoiceFormPopup(FieldConstraintHelper helper) {
        super(DroolsGuvnorImages.INSTANCE.Wizard(),
                Constants.INSTANCE.FieldValue());


        addLiteralValueSelection();

        addRow(new HTML("<hr/>"));
        addRow(new SmallLabel(Constants.INSTANCE.AdvancedOptions()));

        // If we are here, then there must be a bound variable compatible with
        // me
        if (helper.isThereABoundVariableToSet()) {
            addBoundVariableSelection();
        }
        if (helper.isItAList() && !helper.isTheParentAList()) {
            addListSelection();
        }

        if (!helper.isTheParentAList()) {
            addCreateNewObject();
        }
    }

    private void addCreateNewObject() {
        Button button = new Button(Constants.INSTANCE.CreateNewFact());
        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent w) {
                fireSelection(FieldData.TYPE_FACT);
            }


        });
        addAttribute(Constants.INSTANCE.Fact(),
                widgets(button,
                        new InfoPopup(Constants.INSTANCE.Fact(),
                                Constants.INSTANCE.Fact())));
    }

    private void addLiteralValueSelection() {
        Button lit = new Button(Constants.INSTANCE.LiteralValue());
        lit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent w) {
                fireSelection(FieldData.TYPE_LITERAL);
            }
        });
        addAttribute(Constants.INSTANCE.LiteralValue() + ":",
                widgets(lit,
                        new InfoPopup(Constants.INSTANCE.LiteralValue(),
                                Constants.INSTANCE.LiteralValTip())));
    }

    private void addListSelection() {
        Button variable = new Button(Constants.INSTANCE.GuidedList());
        variable.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent w) {
                fireSelection(FieldData.TYPE_COLLECTION);
            }
        });
        addAttribute(Constants.INSTANCE.AVariable(),
                widgets(variable,
                        new InfoPopup(Constants.INSTANCE.AGuidedList(),
                                Constants.INSTANCE.AGuidedListTip())));
    }

    private void addBoundVariableSelection() {
        Button variable = new Button(Constants.INSTANCE.BoundVariable());
        variable.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent w) {
                fireSelection(FieldData.TYPE_VARIABLE);
            }
        });
        addAttribute(Constants.INSTANCE.AVariable(),
                widgets(variable,
                        new InfoPopup(Constants.INSTANCE.ABoundVariable(),
                                Constants.INSTANCE.BoundVariableTip())));
    }

    private void fireSelection(int type) {
        SelectionEvent.fire(this, type);
        hide();
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Integer
            > selectionHandler) {
        return addHandler(selectionHandler, SelectionEvent.getType());
    }

    private Panel widgets(Widget left,
                          Widget right) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(left);
        panel.add(right);
        panel.setWidth("100%");
        return panel;
    }
}
