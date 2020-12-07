/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.experts.callgraph;

import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.codejargon.feather.Provides;

import p4analyser.ontology.Dom;
import p4analyser.ontology.Status;
import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.CallGraph;
import p4analyser.ontology.analyses.SymbolTable;
import p4analyser.ontology.analyses.SyntaxTree;

/**
 * Hello world!
 *
 */
public class CallGraphGenerator {

    @Provides
    @Singleton
    @CallGraph
    public Status analyse(GraphTraversalSource g, @SyntaxTree Status s, @AbstractSyntaxTree Status a, @SymbolTable Status t){
        System.out.println(CallGraph.class.getSimpleName() +" started.");

        whoCallsAction(g);
        whoCallsTable(g);
        whoCallsFunctionPrototype(g);
        whoCallsParserState(g);
        whoInvokesParsersAndControls(g);

        System.out.println(CallGraph.class.getSimpleName() +" complete.");

        return new Status();
    }
    public static void whoCallsTable(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "TableDeclarationContext").as("decl")
                .flatMap(
                __.outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).inV()
                .repeat(__.in(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"))
                .dedup())
                .addE(Dom.CALL).to("decl")
                .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
                
                .iterate();
    }

    public static void whoCallsAction(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ActionDeclarationContext").as("decl")
            .flatMap(
            __.outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).inV()
            .repeat(__.in(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TableDeclarationContext"))
            .dedup())
        .addE(Dom.CALL).to("decl")
        .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
        
        .iterate();
    }

    public static void whoCallsFunctionPrototype(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "FunctionPrototypeContext").as("decl")
            .flatMap(
            __.outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).inV()
            .repeat(__.in(Dom.SYN))
            .until(
                __.or(
                    __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                    __.has(Dom.Syn.V.CLASS, "ParserStateContext"),
                    __.has(Dom.Syn.V.CLASS, "ActionDeclarationContext")))
            .dedup())
        .addE(Dom.CALL).to("decl")
        .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
        
        .iterate();
    }

    public static void whoCallsParserState(GraphTraversalSource g){
        g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ParserStateContext").as("st")
            .flatMap(
            __.repeat(__.in(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"))
            .dedup())
            .addE(Dom.CALL).to("st")
            .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
            
            .iterate();
    }

    // TODO this is technically wrong. first, they are "instantiated" by the top-level, and are passed to InstantiationContext which in turn is also an instantiation by the top-level. The functions are invoked by PackageTypeDeclaration which is an extern. 
    public static void whoInvokesParsersAndControls(GraphTraversalSource g){
        g.V().hasLabel(Dom.SYN)
                .or(__.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                    __.has(Dom.Syn.V.CLASS, "ParserDeclarationContext")).as("invokee")
                .flatMap(
                __.inE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.INVOKES).outV()
                .repeat(__.in(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "InstantiationContext"))
                .dedup())
                .addE(Dom.CALL).to("invokee")
                .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
                
                .iterate();

    }
}
