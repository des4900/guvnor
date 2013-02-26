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

package org.kie.guvnor.commons.ui.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * EnumEditor I18N constants
 */
public interface CommonConstants
        extends
        Messages {

    public static final CommonConstants INSTANCE = GWT.create( CommonConstants.class );

    String InvalidDateFormatMessage();

    String January();

    String February();

    String March();

    String April();

    String May();

    String June();

    String July();

    String August();

    String September();

    String October();

    String November();

    String December();

    String OK();

    String Edit();

    String Error();

    String SorryAnItemOfThatNameAlreadyExistsInTheRepositoryPleaseChooseAnother();

    String ShowDetail();

    String Choose();

    String WaitWhileValidating();

    String File();

    String Save();

    String Validate();

    String Restore();
    
    String Copy();
    
    String Delete();
    
    String Rename();
    
    String Move();

    String EditTabTitle();

    String SourceTabTitle();

    String MetadataTabTitle();

    String ConfigTabTitle();

    String DiscardUnsavedData();

    String CantSaveReadOnly();

    String ItemCreatedSuccessfully();

    String ItemSavedSuccessfully();

    String ItemDeletedSuccessfully();
    
    String ItemRenamedSuccessfully();
    
    String ItemCopiedSuccessfully();
    
    String ItemMovedSuccessfully();
    
    String ItemPathSubheading();

    String ItemUndefinedPath();

    String MissingPath();

    String ItemRestored();

    String AddAnOptionalCheckInComment();

    String CheckIn();

    String RefreshingList();

    String UnableToLoadList();

    String Cancel();

    String Name();

    String Previous();

    String Next();

    String Finish();

    String WholeNumberInteger();

    String TrueOrFalse();

    String Text();

    String Date();

    String DecimalNumber();

    String DeleteItem();

    String NewItem();

    String RuleAsset();
}
