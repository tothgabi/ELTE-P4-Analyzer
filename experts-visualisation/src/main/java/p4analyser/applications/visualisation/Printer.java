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

import p4analyser.ontology.providers.ApplicationProvider;
import p4analyser.applications.visualisation.GraphUtils.Label;
import p4analyser.ontology.Status;
import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.CallGraph;
import p4analyser.ontology.analyses.CallSites;
import p4analyser.ontology.analyses.ControlFlow;
import p4analyser.ontology.analyses.SymbolTable;
import p4analyser.ontology.analyses.SyntaxTree;

public class Printer implements ApplicationProvider {

    @Override
    public String getUICommandName() {
        return "draw";
    }

    @Override
    public Class<? extends DrawCommand> getUICommand() {
        return DrawCommand.class;
    }

    @Override
    public String[] getUICommandAliases() {
        return new String[]{};
    }

    @Provides
    @Singleton
    @Application
    public Status run(DrawCommand cmd, 
                    Provider<GraphTraversalSource> pg, 
                    @SyntaxTree         Provider<Status> st,
                    @AbstractSyntaxTree Provider<Status> ast,
                    @SymbolTable        Provider<Status> symtab,
                    @CallGraph          Provider<Status> cg,
                    @ControlFlow        Provider<Status> cfg,
                    @CallSites          Provider<Status> cs)
            throws IOException, TransformerException, InterruptedException {


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
            throw new IllegalArgumentException("Add one or more argument from the following: " + analysesMap.keySet());
        }

        Collection<Label> selection = new ArrayList<>();
        for (String str : cmd.names) {
            Class<? extends Annotation> a = analysesMap.get(str);
            selection.add(labels.get(a));
        }
        System.out.println("selection: " + selection);

        // parameters are validated, start invoking the dependencies

        GraphTraversalSource g = pg.get();

        for (String str : cmd.names) {
            Class<? extends Annotation> a = analysesMap.get(str);
            providers.get(a).get();
        }

        GraphUtils.printGraph(GraphUtils.subgraph(g, selection.toArray(n -> new Label[n])), 
                              "proba", 
                              true, 
                              GraphUtils.Extension.SVG);
        
//        printSyntaxTree(g);
//        printSemanticGraph(g);
//        printSymbol(g);
//        printCalls(g);
//        printCallSites(g);
//        printCfg(g);

        return new Status();
    }

    public static void printSyntaxTree(GraphTraversalSource g) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(g, Label.SYN), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printSemanticGraph(GraphTraversalSource g) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(g, Label.SEM), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printCfg(GraphTraversalSource g) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(g, Label.CFG), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printSymbol(GraphTraversalSource g) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(g, Label.SYMBOL), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printCalls(GraphTraversalSource g) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(g, Label.CALL), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printCallSites(GraphTraversalSource g) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(g, Label.SITES), "proba", true, GraphUtils.Extension.SVG);
    }
}
