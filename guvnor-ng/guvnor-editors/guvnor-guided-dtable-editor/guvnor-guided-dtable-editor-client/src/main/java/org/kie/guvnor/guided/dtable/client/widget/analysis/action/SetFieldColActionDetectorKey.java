/*
 * Copyright 2011 JBoss Inc
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

package org.kie.guvnor.guided.dtable.client.widget.analysis.action;

import org.drools.guvnor.models.guided.dtable.shared.model.ActionSetFieldCol52;

public class SetFieldColActionDetectorKey extends ActionDetectorKey {

    private String boundName;
    private String factField;

    public SetFieldColActionDetectorKey( ActionSetFieldCol52 actionCol ) {
        super( actionCol );
        this.boundName = actionCol.getBoundName();
        this.factField = actionCol.getFactField();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        } else if ( o instanceof SetFieldColActionDetectorKey ) {
            SetFieldColActionDetectorKey other = (SetFieldColActionDetectorKey) o;
            return boundName.equals( other.boundName ) && factField.equals( other.factField );
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return boundName.hashCode() * 37 + factField.hashCode();
    }

}
