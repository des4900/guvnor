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

package org.kie.guvnor.guided.rule.client.widget;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.drools.guvnor.models.commons.shared.oracle.DataType;
import org.drools.guvnor.models.commons.shared.rule.ExpressionCollectionIndex;
import org.drools.guvnor.models.commons.shared.rule.ExpressionFieldVariable;
import org.drools.guvnor.models.commons.shared.rule.ExpressionFormLine;
import org.drools.guvnor.models.commons.shared.rule.ExpressionMethod;
import org.drools.guvnor.models.commons.shared.rule.ExpressionPart;
import org.drools.guvnor.models.commons.shared.rule.ExpressionText;
import org.drools.guvnor.models.commons.shared.rule.ExpressionVariable;
import org.drools.guvnor.models.commons.shared.rule.FactPattern;
import org.drools.guvnor.models.commons.shared.rule.RuleModel;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.guided.rule.client.editor.ExpressionChangeEvent;
import org.kie.guvnor.guided.rule.client.editor.ExpressionChangeHandler;
import org.kie.guvnor.guided.rule.client.editor.ExpressionTypeChangeEvent;
import org.kie.guvnor.guided.rule.client.editor.ExpressionTypeChangeHandler;
import org.kie.guvnor.guided.rule.client.editor.HasExpressionChangeHandlers;
import org.kie.guvnor.guided.rule.client.editor.HasExpressionTypeChangeHandlers;
import org.kie.guvnor.guided.rule.client.editor.RuleModeller;
import org.kie.guvnor.guided.rule.client.resources.i18n.Constants;
import org.uberfire.client.common.ClickableLabel;
import org.uberfire.client.common.FormStylePopup;
import org.uberfire.client.common.SmallLabel;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpressionBuilder extends RuleModellerWidget
        implements
        HasExpressionTypeChangeHandlers,
        HasExpressionChangeHandlers {

    private static final String DELETE_VALUE = "_delete_";
    private static final String FIElD_VALUE_PREFIX = "fl";
    private static final String VARIABLE_VALUE_PREFIX = "va";
    // private static final String GLOBAL_COLLECTION_VALUE_PREFIX = "gc";
    private static final String GLOBAL_VARIABLE_VALUE_PREFIX = "gv";
    private static final String METHOD_VALUE_PREFIX = "mt";
    private final SmallLabelClickHandler slch = new SmallLabelClickHandler();
    private HorizontalPanel panel = new HorizontalPanel();
    private ExpressionFormLine expression;
    private boolean readOnly;

    private boolean isFactTypeKnown;

    public ExpressionBuilder( RuleModeller modeller,
                              EventBus eventBus,
                              ExpressionFormLine expression ) {
        this( modeller,
              eventBus,
              expression,
              false );
    }

    public ExpressionBuilder( RuleModeller modeller,
                              EventBus eventBus,
                              ExpressionFormLine expression,
                              Boolean readOnly ) {
        super( modeller,
               eventBus );
        this.expression = expression;

        if ( this.expression.isEmpty() ) {
            this.isFactTypeKnown = true;
        } else {
            this.isFactTypeKnown = getModeller().getSuggestionCompletions().isFactTypeRecognized( getModeller().getSuggestionCompletions().getFactNameFromType( this.expression.getRootExpression().getClassType() ) );
        }

        if ( readOnly == null ) {
            this.readOnly = !this.isFactTypeKnown;
        } else {
            this.readOnly = readOnly;
        }

        panel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );

        StringBuilder bindingLabel = new StringBuilder();
        String binding = getBoundText();
        if ( binding != null && !binding.equals( "" ) ) {
            bindingLabel.append( "<b>" );
            bindingLabel.append( getBoundText() );
            bindingLabel.append( "</b>" );
        }
        bindingLabel.append( expression.getText( false ) );

        if ( expression == null || expression.isEmpty() ) {
            if ( this.readOnly ) {
                panel.add( new SmallLabel( "<b>-</b>" ) );
            } else {
                panel.add( createStartPointWidget() );
            }
        } else {
            if ( this.readOnly ) {
                panel.add( createWidgetForExpression( bindingLabel.toString() ) );
            } else {
                bindingLabel.append( "." );
                panel.add( createWidgetForExpression( bindingLabel.toString() ) );
                panel.add( getWidgetForCurrentType() );
            }
        }
        initWidget( panel );
    }

    private String getBoundText() {
        if ( expression.isBound() ) {
            return "[" + expression.getBinding() + "] ";
        }
        return "";
    }

    private Widget createStartPointWidget() {
        ListBox startPoint = new ListBox();
        panel.add( startPoint );

        startPoint.addItem( Constants.INSTANCE.ChooseDotDotDot(),
                            "" );

        // TODO {baunax} uncomment when global collections is implemented.
        // for (String gc : getCompletionEngine().getGlobalCollections()) {
        // startPoint.addItem(gc, GLOBAL_COLLECTION_VALUE_PREFIX + "." + gc);
        // }

        for ( String gv : getCompletionEngine().getGlobalVariables() ) {
            startPoint.addItem( gv,
                                GLOBAL_VARIABLE_VALUE_PREFIX + "." + gv );
        }

        for ( String v : getRuleModel().getAllLHSVariables() ) {
            startPoint.addItem( v,
                                VARIABLE_VALUE_PREFIX + "." + v );
        }

        startPoint.setVisibleItemCount( 1 );
        startPoint.addChangeHandler( new ChangeHandler() {

            public void onChange( ChangeEvent event ) {
                ListBox lb = (ListBox) event.getSource();
                int index = lb.getSelectedIndex();
                if ( index > 0 ) {
                    ExpressionBuilder.this.makeDirty();
                    startPointChange( lb.getValue( index ) );
                }
            }
        } );
        return startPoint;
    }

    @Override
    public void makeDirty() {
        super.makeDirty();
        setModified( true );
    }

    private void startPointChange( String value ) {
        setModified( true );
        panel.clear();
        Widget w;
        int dotPos = value.indexOf( '.' );
        String prefix = value.substring( 0,
                                         dotPos );
        String attrib = value.substring( dotPos + 1 );
        if ( prefix.equals( VARIABLE_VALUE_PREFIX ) ) {
            FactPattern fact = getRuleModel().getLHSBoundFact( attrib );
            ExpressionPart variable;
            if ( fact != null ) {
                variable = new ExpressionVariable( fact );
            } else {
                //if the variable is not bound to a Fact Pattern then it must be bound to a Field
                String lhsBindingType = getRuleModel().getLHSBindingType( attrib );
                variable = new ExpressionFieldVariable( attrib,
                                                        lhsBindingType );
            }
            expression.appendPart( variable );

        } else if ( prefix.equals( GLOBAL_VARIABLE_VALUE_PREFIX ) ) {
            expression.appendPart( ExpressionPartHelper.getExpressionPartForGlobalVariable( getCompletionEngine(),
                                                                                            attrib ) );
        }
        w = getWidgetForCurrentType();

        if ( !expression.isEmpty() ) {
            panel.add( createWidgetForExpression( expression.getText() + "." ) );
        }
        if ( w != null ) {
            panel.add( w );
        }
        fireExpressionChangeEvent();
        fireExpressionTypeChangeEvent();
    }

    private Widget getWidgetForCurrentType() {
        if ( expression.isEmpty() ) {
            return createStartPointWidget();
        }

        ChangeHandler ch = new ChangeHandler() {
            public void onChange( ChangeEvent event ) {
                ListBox box = (ListBox) event.getSource();
                panel.remove( box );
                if ( box.getSelectedIndex() > 0 ) {
                    onChangeSelection( box.getValue( box.getSelectedIndex() ) );
                }
            }
        };

        ListBox lb = new ListBox();
        lb.setVisibleItemCount( 1 );
        lb.addItem( Constants.INSTANCE.ChooseDotDotDot(),
                    "" );
        lb.addItem( "<==" + Constants.INSTANCE.DeleteItem(),
                    DELETE_VALUE );
        for ( Map.Entry<String, String> entry : getCompletionsForCurrentType( expression.getParts().size() > 1 ).entrySet() ) {
            lb.addItem( entry.getKey(),
                        entry.getValue() );
        }
        lb.addChangeHandler( ch );
        return lb;
    }

    private void onCollectionChange( String value ) {
        if ( "size".contains( value ) ) {
            expression.appendPart( new ExpressionMethod( "size",
                                                         "int",
                                                         DataType.TYPE_NUMERIC_INTEGER ) );
        } else if ( "isEmpty".equals( value ) ) {
            expression.appendPart( new ExpressionMethod( "isEmpty",
                                                         "boolean",
                                                         DataType.TYPE_BOOLEAN ) );
        } else {
            ExpressionCollectionIndex collectionIndex;
            String factName = getCompletionEngine().getFactNameFromType( getCurrentParametricType() );
            if ( getCurrentParametricType() != null && factName != null ) {
                collectionIndex = new ExpressionCollectionIndex( "get",
                                                                 getCurrentParametricType(),
                                                                 factName );
            } else {
                collectionIndex = new ExpressionCollectionIndex( "get",
                                                                 "java.lang.Object",
                                                                 DataType.TYPE_OBJECT );
            }
            if ( "first".equals( value ) ) {
                collectionIndex.putParam( "index",
                                          new ExpressionFormLine( new ExpressionText( "0" ) ) );
                expression.appendPart( collectionIndex );
            } else if ( "last".equals( value ) ) {
                ExpressionFormLine index = new ExpressionFormLine( expression );
                index.appendPart( new ExpressionMethod( "size",
                                                        "int",
                                                        DataType.TYPE_NUMERIC_INTEGER ) );
                index.appendPart( new ExpressionText( "-1" ) );

                collectionIndex.putParam( "index",
                                          index );
                expression.appendPart( collectionIndex );
            }
        }
    }

    private void onChangeSelection( String value ) {
        setModified( true );
        String oldType = getCurrentGenericType();
        String prevFactName = null;
        if ( DELETE_VALUE.equals( value ) ) {
            expression.removeLast();
        } else if ( DataType.TYPE_COLLECTION.equals( getCurrentGenericType() ) ) {
            onCollectionChange( value );
        } else if ( DataType.TYPE_STRING.equals( getCurrentGenericType() ) ) {
            if ( "size".equals( value ) ) {
                expression.appendPart( new ExpressionMethod( "size",
                                                             "int",
                                                             DataType.TYPE_NUMERIC_INTEGER ) );
            } else if ( "isEmpty".equals( value ) ) {
                expression.appendPart( new ExpressionText( ".size() == 0",
                                                           "",
                                                           DataType.TYPE_NUMERIC_INTEGER ) );
            }
        } else {
            int dotPos = value.indexOf( '.' );
            String prefix = value.substring( 0,
                                             dotPos );
            String attrib = value.substring( dotPos + 1 );

            prevFactName = getCompletionEngine().getFactNameFromType( getCurrentClassType() );
            // String genericType = SuggestionCompletionEngine.TYPE_OBJECT;
            if ( FIElD_VALUE_PREFIX.equals( prefix ) ) {
                expression.appendPart( ExpressionPartHelper.getExpressionPartForField( getCompletionEngine(),
                                                                                       prevFactName,
                                                                                       attrib ) );
            } else if ( METHOD_VALUE_PREFIX.equals( prefix ) ) {
                expression.appendPart( ExpressionPartHelper.getExpressionPartForMethod( getCompletionEngine(),
                                                                                        prevFactName,
                                                                                        attrib ) );
            }
        }
        Widget w = getWidgetForCurrentType();

        panel.clear();
        if ( !expression.isEmpty() ) {
            panel.add( createWidgetForExpression( expression.getText() + "." ) );
        }
        if ( w != null ) {
            panel.add( w );
        }
        fireExpressionChangeEvent();
        fireExpressionTypeChangeEvent( oldType );
    }

    private Map<String, String> getCompletionsForCurrentType( boolean isNested ) {
        Map<String, String> completions = new LinkedHashMap<String, String>();

        if ( DataType.TYPE_FINAL_OBJECT.equals( getCurrentGenericType() ) ) {
            return completions;
        }

        if ( DataType.TYPE_COLLECTION.equals( getCurrentGenericType() ) ) {
            completions.put( "size()",
                             "size" );
            completions.put( "first()",
                             "first" );
            completions.put( "last()",
                             "last" );
            completions.put( "isEmpty()",
                             "isEmpty" );
            return completions;
        }

        if ( DataType.TYPE_STRING.equals( getCurrentGenericType() ) ) {
            completions.put( "size()",
                             "size" );
            completions.put( "isEmpty()",
                             "isEmpty" );
            return completions;
        }

        if ( DataType.TYPE_BOOLEAN.equals( getCurrentGenericType() )
                || DataType.TYPE_NUMERIC_BIGDECIMAL.equals( getCurrentGenericType() )
                || DataType.TYPE_NUMERIC_BIGINTEGER.equals( getCurrentGenericType() )
                || DataType.TYPE_NUMERIC_BYTE.equals( getCurrentGenericType() )
                || DataType.TYPE_NUMERIC_DOUBLE.equals( getCurrentGenericType() )
                || DataType.TYPE_NUMERIC_FLOAT.equals( getCurrentGenericType() )
                || DataType.TYPE_NUMERIC_INTEGER.equals( getCurrentGenericType() )
                || DataType.TYPE_NUMERIC_LONG.equals( getCurrentGenericType() )
                || DataType.TYPE_NUMERIC_SHORT.equals( getCurrentGenericType() )
                || DataType.TYPE_DATE.equals( getCurrentGenericType() )
                || DataType.TYPE_OBJECT.equals( getCurrentGenericType() ) ) {
            return completions;
        }

        String factName = getCompletionEngine().getFactNameFromType( getCurrentClassType() );
        if ( factName != null ) {
            // we currently only support 0 param method calls
            List<String> methodNames = getCompletionEngine().getMethodNames( factName,
                                                                             0 );

            for ( String field : getCompletionEngine().getFieldCompletions( factName ) ) {

                //You can't use "this" in a nested accessor
                if ( !isNested || !field.equals( DataType.TYPE_THIS ) ) {

                    boolean changed = false;
                    for ( Iterator<String> i = methodNames.iterator(); i.hasNext(); ) {
                        String method = i.next();
                        if ( method.startsWith( field ) ) {
                            completions.put( method,
                                             METHOD_VALUE_PREFIX + "." + method );
                            i.remove();
                            changed = true;
                        }
                    }
                    if ( !changed ) {
                        completions.put( field,
                                         FIElD_VALUE_PREFIX + "." + field );
                    }
                }
            }
        }
        // else {We don't know anything about this type, so return empty map}
        return completions;
    }

    private RuleModel getRuleModel() {
        return this.getModeller().getModel();
    }

    private DataModelOracle getCompletionEngine() {
        return this.getModeller().getSuggestionCompletions();
    }

    private String getCurrentClassType() {
        return expression.getClassType();
    }

    private String getCurrentGenericType() {
        return expression.getGenericType();
    }

    private String getPreviousGenericType() {
        return expression.getPreviousGenericType();
    }

    private String getCurrentParametricType() {
        return expression.getParametricType();
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public boolean isFactTypeKnown() {
        return this.isFactTypeKnown;
    }

    /**
     * @see HasExpressionTypeChangeHandlers(ExpressionTypeChangeHandler)
     */
    public HandlerRegistration addExpressionTypeChangeHandler( ExpressionTypeChangeHandler handler ) {
        return addHandler( handler,
                           ExpressionTypeChangeEvent.getType() );
    }

    private void fireExpressionChangeEvent() {
        fireEvent( new ExpressionChangeEvent() );
    }

    private void fireExpressionTypeChangeEvent() {
        fireExpressionTypeChangeEvent( getPreviousGenericType() );
    }

    private void fireExpressionTypeChangeEvent( String previousGenericType ) {
        String currentGenericType = getCurrentGenericType();
        if ( ( previousGenericType == null || !previousGenericType.equals( currentGenericType ) ) || currentGenericType != null ) {
            fireEvent( new ExpressionTypeChangeEvent( previousGenericType,
                                                      currentGenericType ) );
        }
    }

    public HandlerRegistration addExpressionChangeHandler( ExpressionChangeHandler handler ) {
        return addHandler( handler,
                           ExpressionChangeEvent.getType() );
    }

    private void showBindingPopUp() {
        final FormStylePopup popup = new FormStylePopup();
        popup.setWidth( 500 + "px" );
        HorizontalPanel vn = new HorizontalPanel();
        final TextBox varName = new TextBox();
        Button ok = new Button( HumanReadableConstants.INSTANCE.Set() );
        vn.add( new Label( Constants.INSTANCE.BindTheExpressionToAVariable() ) );
        vn.add( varName );
        vn.add( ok );

        ok.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent event ) {
                String var = varName.getText();
                if ( getModeller().isVariableNameUsed( var ) ) {
                    Window.alert( Constants.INSTANCE.TheVariableName0IsAlreadyTaken( var ) );
                    return;
                }
                expression.setBinding( var );
                getModeller().refreshWidget();
                popup.hide();
            }
        } );

        popup.addRow( vn );
        popup.show();
    }

    private class SmallLabelClickHandler
            implements
            ClickHandler {

        public void onClick( ClickEvent event ) {
            showBindingPopUp();
        }
    }

    private ClickableLabel createWidgetForExpression( String text ) {
        ClickableLabel label = new ClickableLabel( text,
                                                   slch,
                                                   !this.readOnly );
        return label;
    }
}
