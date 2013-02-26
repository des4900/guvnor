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

package org.kie.guvnor.guided.rule.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.drools.guvnor.models.commons.backend.rule.BRDRLPersistence;
import org.drools.guvnor.models.commons.shared.rule.RuleModel;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.base.options.CommentedOption;
import org.kie.guvnor.commons.data.events.AssetEditedEvent;
import org.kie.guvnor.commons.data.events.AssetOpenedEvent;
import org.kie.guvnor.commons.data.workingset.WorkingSetConfigData;
import org.kie.guvnor.commons.service.metadata.model.Metadata;
import org.kie.guvnor.commons.service.source.SourceServices;
import org.kie.guvnor.commons.service.validation.model.BuilderResult;
import org.kie.guvnor.commons.service.verification.model.AnalysisReport;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.datamodel.service.DataModelService;
import org.kie.guvnor.datamodel.service.FileDiscoveryService;
import org.kie.guvnor.guided.rule.model.GuidedEditorContent;
import org.kie.guvnor.guided.rule.service.GuidedRuleEditorService;
import org.kie.guvnor.project.service.ProjectService;
import org.kie.guvnor.services.metadata.MetadataService;
import org.mvel2.MVEL;
import org.mvel2.templates.TemplateRuntime;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.security.Identity;

@Service
@ApplicationScoped
public class GuidedRuleEditorServiceImpl
        implements GuidedRuleEditorService {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private Paths paths;

    @Inject
    private DataModelService dataModelService;

    @Inject
    private MetadataService metadataService;

    @Inject
    private FileDiscoveryService fileDiscoveryService;

    @Inject
    private ProjectService projectService;

    @Inject
    private Identity identity;

    @Inject
    private SourceServices sourceServices;

    @Inject
    private Event<AssetEditedEvent> assetEditedEvent;

    @Inject
    private Event<AssetOpenedEvent> assetOpenedEvent;

    public GuidedRuleEditorServiceImpl() {
    }

    @Override
    public GuidedEditorContent loadContent( final Path path ) {
        //De-serialize model
        final RuleModel model = loadRuleModel( path );

        final DataModelOracle oracle = dataModelService.getDataModel( path );

        assetOpenedEvent.fire( new AssetOpenedEvent( path ) );

        return new GuidedEditorContent( oracle,
                                        model );
    }

    @Override
    public RuleModel loadRuleModel( Path path ) {
        final String drl = ioService.readAllString( paths.convert( path ) );
        final String[] dsls = loadDslsForPackage( path );
        final List<String> globals = loadGlobalsForPackage( path );

        assetOpenedEvent.fire( new AssetOpenedEvent( path ) );

        return BRDRLPersistence.getInstance().unmarshalUsingDSL( drl,
                                                                 globals,
                                                                 dsls );
    }

    private String[] loadDslsForPackage( final Path path ) {
        final List<String> dsls = new ArrayList<String>();
        final Path packagePath = projectService.resolvePackage( path );
        final org.kie.commons.java.nio.file.Path nioPackagePath = paths.convert( packagePath );
        final Collection<org.kie.commons.java.nio.file.Path> dslPaths = fileDiscoveryService.discoverFiles( nioPackagePath,
                                                                                                            ".dsl" );
        for ( final org.kie.commons.java.nio.file.Path dslPath : dslPaths ) {
            final String dslDefinition = ioService.readAllString( dslPath );
            dsls.add( dslDefinition );
        }
        final String[] result = new String[ dsls.size() ];
        return dsls.toArray( result );
    }

    private List<String> loadGlobalsForPackage( final Path path ) {
        final List<String> globals = new ArrayList<String>();
        final Path packagePath = projectService.resolvePackage( path );
        final org.kie.commons.java.nio.file.Path nioPackagePath = paths.convert( packagePath );
        final Collection<org.kie.commons.java.nio.file.Path> globalPaths = fileDiscoveryService.discoverFiles( nioPackagePath,
                                                                                                               ".global.drl" );
        for ( final org.kie.commons.java.nio.file.Path globalPath : globalPaths ) {
            final String globalDefinition = ioService.readAllString( globalPath );
            globals.add( globalDefinition );
        }
        final String[] result = new String[ globals.size() ];
        return globals;
    }

    @Override
    public void save( final Path path,
                      final RuleModel model,
                      final String comment ) {
        assetEditedEvent.fire( new AssetEditedEvent( path ) );

        ioService.write( paths.convert( path ),
                         toSource( path,
                                   model ),
                         makeCommentedOption( comment ) );
    }

    @Override
    public Path save( final Path context,
                      final String fileName,
                      final RuleModel model,
                      final String comment ) {
        final Path newPath = paths.convert( paths.convert( context ).resolve( fileName ), false );

        save( newPath, model, comment );

        return newPath;
    }

    @Override
    public void save( final Path path,
                      final RuleModel model,
                      final Metadata metadata,
                      final String comment ) {

        ioService.write( paths.convert( path ),
                         toSource( path,
                                   model ),
                         metadataService.setUpAttributes( path,
                                                          metadata ),
                         makeCommentedOption( comment ) );

        assetEditedEvent.fire( new AssetEditedEvent( path ) );
    }

    @Override
    public void delete( final Path path,
                        final String comment ) {
        System.out.println( "USER:" + identity.getName() + " DELETING asset [" + path.getFileName() + "]" );

        ioService.delete( paths.convert( path ) );

        assetEditedEvent.fire( new AssetEditedEvent( path ) );
    }

    @Override
    public Path rename( final Path path,
                        final String newName,
                        final String comment ) {
        System.out.println( "USER:" + identity.getName() + " RENAMING asset [" + path.getFileName() + "] to [" + newName + "]" );
        String targetName = path.getFileName().substring( 0, path.getFileName().lastIndexOf( "/" ) + 1 ) + newName;
        String targetURI = path.toURI().substring( 0, path.toURI().lastIndexOf( "/" ) + 1 ) + newName;
        Path targetPath = PathFactory.newPath( path.getFileSystem(), targetName, targetURI );
        ioService.move( paths.convert( path ), paths.convert( targetPath ), new CommentedOption( identity.getName(), comment ) );

        assetEditedEvent.fire( new AssetEditedEvent( path ) );
        return targetPath;
    }

    @Override
    public Path copy( final Path path,
                      final String newName,
                      final String comment ) {
        System.out.println( "USER:" + identity.getName() + " COPYING asset [" + path.getFileName() + "] to [" + newName + "]" );
        String targetName = path.getFileName().substring( 0, path.getFileName().lastIndexOf( "/" ) + 1 ) + newName;
        String targetURI = path.toURI().substring( 0, path.toURI().lastIndexOf( "/" ) + 1 ) + newName;
        Path targetPath = PathFactory.newPath( path.getFileSystem(), targetName, targetURI );
        ioService.copy( paths.convert( path ), paths.convert( targetPath ), new CommentedOption( identity.getName(), comment ) );

        assetEditedEvent.fire( new AssetEditedEvent( path ) );
        return targetPath;
    }

    @Override
    public String toSource( Path path,
                            final RuleModel model ) {
        return sourceServices.getServiceFor( paths.convert( path ) ).getSource( paths.convert( path ), model );
    }

    @Override
    public AnalysisReport verify( final Path path,
                                  final RuleModel content,
                                  final Collection<WorkingSetConfigData> activeWorkingSets ) {
        //TODO {porcelli} verify
        return new AnalysisReport();
    }

    @Override
    public BuilderResult validate( final Path path,
                                   final RuleModel content ) {
        //TODO {porcelli} validate
        return new BuilderResult();
    }

    @Override
    public boolean isValid( final Path path,
                            final RuleModel content ) {
        return !validate( path, content ).hasLines();
    }

    private CommentedOption makeCommentedOption( final String commitMessage ) {
        final String name = identity.getName();
        final Date when = new Date();
        final CommentedOption co = new CommentedOption( name,
                                                        null,
                                                        commitMessage,
                                                        when );
        return co;
    }
}
