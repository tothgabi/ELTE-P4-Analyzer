package p4analyser.experts.visualisation;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import p4analyser.experts.visualisation.GraphUtils.Label;
import p4analyser.ontology.providers.SyntaxTreeProvider.SyntaxTree;

public class Printer {

    public Printer(GraphTraversalSource g, Provider<Object> ensureSt, DrawCommand cmd)
            throws IOException, TransformerException, InterruptedException {
        ensureSt.get();
        
        printSyntaxTree(g);
//        printSemanticGraph(g);
//        printSymbol(g);
//        printCalls(g);
//        printCallSites(g);
//        printCfg(g);

        System.out.println("OK");
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
