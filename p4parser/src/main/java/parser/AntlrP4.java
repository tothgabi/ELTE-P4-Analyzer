package parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.tinkerpop.gremlin.structure.Graph;

import parser.GraphUtils.Label;
import parser.p4.*;

public class AntlrP4 {
    // TODO formalize the analysis dependencies: https://github.com/j-easy/easy-flows
    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException,
            InterruptedException {
// use this line to generate P4Lexer class and P4Parser class along with the P4BaseVisitor class:
//      org.antlr.v4.Tool.main(new String[]{"-visitor", "-o", "hmm/src/main/java/hmm/p4", "-package", "hmm.p4", "P4.g4"});

//  // To parse without resolving includes:
//        CharStream stream = CharStreams.fromFileName("ex1.p4");
//        P4Lexer lexer  = new P4Lexer(stream);   

        // Using C preprocessor to resolve includes. 
        // JCPP-Antlr integration from here: https://stackoverflow.com/a/25358397
        // Note that includes are huge, they slow down everything, and many things can be analysed without them.
        Preprocessor pp = new Preprocessor(new File("ex1.p4"));
        List<String> systemInclude = new ArrayList<String>();
        systemInclude.add(".");            
        pp.setSystemIncludePath(systemInclude);

        P4Lexer lexer = new P4Lexer(CharStreams.fromReader(new CppReader(pp)));
        TokenStream tokenStream = new CommonTokenStream(lexer);
        P4Parser parser = new P4Parser(tokenStream);
        
        ParseTree tree = parser.start();
//        displayNativeAntlrTree(parser, tree);
//        antlrParseTreeToXML(tree);


        Graph graph = TinkerGraphParseTree.fromParseTree(tree, lexer.getVocabulary(), parser.getRuleNames());
//        printSyntaxTree(graph);

        Normalisation.analyse(graph);
        GremlinUtils.resetNodeIds(graph, Dom.SYN);
//        printSyntaxTree(graph);


        SemanticAnalysis.analyse(graph);
//        printSemanticGraph(graph);

//        printSymbol(graph);
//        printCalls(graph);
//        printCallSites(graph);

        ControlFlowAnalysis.analyse(graph);
        printCfg(graph);
//        ExternControlFlow.analyse(graph);
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
    public static void printSymbol(Graph graph) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(graph, Label.SYMBOL), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printCalls(Graph graph) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(graph, Label.CALL), "proba", true, GraphUtils.Extension.SVG);
    }
    public static void printCallSites(Graph graph) throws IOException, TransformerException, InterruptedException {
        GraphUtils.printGraph(GraphUtils.subgraph(graph, Label.SITES), "proba", true, GraphUtils.Extension.SVG);
    }

    private static void displayNativeAntlrTree(P4Parser parser, ParseTree tree) {
        //show AST in GUI
        TreeViewer viewer = new TreeViewer(
            Arrays.asList(parser.getRuleNames()),tree);
        viewer.open();

        //show AST in console (LISP)
        System.out.println(tree.toStringTree(parser));
    }

    private static void antlrParseTreeToXML(ParseTree tree) throws TransformerException, ParserConfigurationException {
        XMLParseTree.toFile(XMLParseTree.fromParseTree(tree), "p4-antlr.xml", true);
    }
}