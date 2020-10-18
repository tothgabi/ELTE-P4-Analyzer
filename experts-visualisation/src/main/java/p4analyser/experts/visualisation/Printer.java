package p4analyser.experts.visualisation;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import p4analyser.experts.visualisation.GraphUtils.Label;

public class Printer {

    public static void main( String[] args ) throws IOException, TransformerException, InterruptedException {

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];

//        Graph graph = TinkerGraph.open();
//        GraphTraversalSource g = graph.traversal();
        GraphTraversalSource g = 
            AnonymousTraversalSource
                    .traversal()
                    .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));

//        printSyntaxTree(g);
//        printSemanticGraph(g);
//        printSymbol(g);
        printCalls(g);
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
