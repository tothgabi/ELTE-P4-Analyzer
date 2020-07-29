package parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.tinkerpop.gremlin.structure.Graph;

import parser.GraphUtils.Label;
import parser.p4.*;

public class AntlrP4 {
    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException,
            InterruptedException {
// use this line to generate P4Lexer class and P4Parser class along with the P4BaseVisitor class:
//      org.antlr.v4.Tool.main(new String[]{"-visitor", "-o", "hmm/src/main/java/hmm/p4", "-package", "hmm.p4", "P4.g4"});


        CharStream stream = CharStreams.fromFileName("ex1.p4");
        P4Lexer lexer  = new P4Lexer(stream);   
        TokenStream tokenStream = new CommonTokenStream(lexer);
        P4Parser parser = new P4Parser(tokenStream);
        
        ParseTree tree = parser.start();
 //show AST in console
//        System.out.println(tree.toStringTree(parser));


        Graph graph =TinkerGraphParseTree.fromParseTree(tree, lexer.getVocabulary(), parser.getRuleNames());
        SemanticAnalysis.analyse(graph);
        ControlFlowAnalysis.analyse(graph);

//        printSemanticGraph(graph);
//        printSyntaxTree(graph);
        printCfg(graph);

//        //show AST in GUI
//        TreeViewer viewer = new TreeViewer(Arrays.asList(
//                parser.getRuleNames()),tree);
//                viewer.open();
//
//        XMLParseTree.toFile(XMLParseTree.fromParseTree(tree), "p4-antlr.xml", true);
//
//        Map<String, List<String>> res = tree.accept(new StateMachineVisitor());
//        System.out.println(res);

    }

    public static void printSyntaxTree(Graph graph) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(graph, Label.SYN), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printSemanticGraph(Graph graph) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(graph, Label.SEM), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printCfg(Graph graph) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(graph, Label.CFG), "proba", true, GraphUtils.Extension.SVG);
    }

}