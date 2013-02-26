package org.kie.guvnor.testscenario.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.DroolsGuvnorImageResources;
import org.drools.guvnor.client.resources.DroolsGuvnorImages;
import org.drools.ide.common.client.modeldriven.testing.CollectionFieldData;
import org.drools.ide.common.client.modeldriven.testing.FieldData;
import org.kie.guvnor.commons.ui.client.resources.ItemAltedImages;
import org.kie.guvnor.testscenario.client.resources.i18n.TestScenarioConstants;
import org.kie.guvnor.testscenario.service.model.CollectionFieldData;
import org.kie.guvnor.testscenario.service.model.FieldData;

public class ListEditor extends VerticalPanel {


    public ListEditor(final CollectionFieldData field, FieldConstraintHelper helper, final ScenarioParentWidget parent) {
        if (field.getCollectionFieldList().isEmpty()) {
            Image image = ItemAltedImages.INSTANCE.NewItem();
            image.setAltText(TestScenarioConstants.INSTANCE.AElementToAddInCollectionList());
            image.setTitle(TestScenarioConstants.INSTANCE.AElementToAddInCollectionList());
            image.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent w) {
                    FieldData fieldData = new FieldData();
                    fieldData.setName(field.getName());
                    field.getCollectionFieldList().add(fieldData);
                    parent.renderEditor();
                }
            });

            add(image);
        } else {
            int i = 0;
            for (final FieldData fieldData : field.getCollectionFieldList()) {
                add(new ListEditorRow(i, field, fieldData, helper, parent));
                i++;
            }
        }
    }

}
