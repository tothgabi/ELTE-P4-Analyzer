/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.applications.visualisation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.reflections.Reflections;
import org.codejargon.feather.Provides;

import p4analyser.ontology.providers.AppUI;
import p4analyser.ontology.providers.Application;
import p4analyser.applications.visualisation.GraphUtils.Label;
import p4analyser.ontology.Status;
import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.CallGraph;
import p4analyser.ontology.analyses.CallSites;
import p4analyser.ontology.analyses.ControlFlow;
import p4analyser.ontology.analyses.SymbolTable;
import p4analyser.ontology.analyses.SyntaxTree;

public class Printer implements Application {

    DrawCommand cmd = new DrawCommand();

    @Inject                      private GraphTraversalSource g; 
    @Inject @SyntaxTree          private Provider<Status> st;
    @Inject @AbstractSyntaxTree  private Provider<Status> ast;
    @Inject @SymbolTable         private Provider<Status> symtab;
    @Inject @CallGraph           private Provider<Status> cg;
    @Inject @ControlFlow         private Provider<Status> cfg;
    @Inject @CallSites           private Provider<Status> cs;

    @Override
    public DrawCommand getUI(){
        return cmd;
    }

    @Override
    public Status run() throws IOException, TransformerException, InterruptedException {

        Map<Class<? extends Annotation>, Provider<Status>> providers = new HashMap<>();
        providers.put(SyntaxTree.class, st);
        providers.put(AbstractSyntaxTree.class, ast);
        providers.put(SymbolTable.class, symtab);
        providers.put(CallGraph.class, cg);
        providers.put(ControlFlow.class, cfg);
        providers.put(CallSites.class, cs);

        Map<Class<? extends Annotation>, Label> labels = new HashMap<>();
        labels.put(SyntaxTree.class, Label.SYN);
        labels.put(AbstractSyntaxTree.class, Label.SEM);
        labels.put(SymbolTable.class, Label.SYMBOL);
        labels.put(CallGraph.class, Label.CALL);
        labels.put(ControlFlow.class, Label.CFG);
        labels.put(CallSites.class, Label.SITES);

        Reflections reflections = new Reflections("p4analyser.ontology.analyses");
        Set<Class<? extends Annotation>> analyses = 
            reflections.getSubTypesOf(Annotation.class);
        Map<String, Class<? extends Annotation>> analysesMap = 
            analyses.stream()
                    .collect(Collectors.toMap(c -> c.getSimpleName(), c -> c));

        if(cmd.names == null || cmd.names.isEmpty()){
            throw new IllegalArgumentException("draw: Add one or more argument from the following: " + analysesMap.keySet());
        }

        Collection<Label> selection = new ArrayList<>();
        for (String str : cmd.names) {
            Class<? extends Annotation> a = analysesMap.get(str);
            selection.add(labels.get(a));
        }
        System.out.println("selection: " + selection);

        // parameters are validated, start invoking the dependencies

        for (String str : cmd.names) {
            Class<? extends Annotation> a = analysesMap.get(str);
            providers.get(a).get();
        }

        GraphUtils.printGraph(GraphUtils.subgraph(g, selection.toArray(new Label[selection.size()])), 
                              "proba", 
                              true, 
                              GraphUtils.Extension.SVG);
        
        return new Status();
    }

}
