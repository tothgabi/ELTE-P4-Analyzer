package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
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
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import parser.GraphUtils.Label;
import parser.p4.*;

public class AntlrP4 {

    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    public static final String GRAPHML2DOT_XSL = loader.getResource("graphml2dot.xsl").getPath().toString();
    public static final String EX1_P4 = loader.getResource("ex1.p4").getPath().toString();
    public static final String CORE_P4 = loader.getResource("core.p4").getPath().toString();
    public static final String V1MODEL_P4 = loader.getResource("v1model.p4").getPath().toString();

    // TODO formalize the analysis dependencies: https://github.com/j-easy/easy-flows
    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException,
            InterruptedException {


        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];


// // query printing
//        File f = File. createTempFile("query", ".tex");
//        PrintStream ps = new PrintStream(f);
//        ControlFlowAnalysis.Control3.printQuery(ps);
//        System.out.println("query printed to " + f.getAbsolutePath());
//        ps.close();
//        System.exit(0);


// // use this line to generate P4Lexer class and P4Parser class along with the P4BaseVisitor class:
//      org.antlr.v4.Tool.main(new String[]{"-visitor", "-o", "hmm/src/main/java/hmm/p4", "-package", "hmm.p4", "P4.g4"});

//  // To parse without resolving includes:
//        CharStream stream = CharStreams.fromFileName(PATH_PREFIX + "ex1.p4");
//        P4Lexer lexer  = new P4Lexer(stream);   

        // Using C preprocessor to resolve includes. 
        // JCPP-Antlr integration from here: https://stackoverflow.com/a/25358397
        // Note that includes are huge, they slow down everything, and many things can be analysed without them.
        Preprocessor pp = new Preprocessor(new File(EX1_P4));
        List<String> systemInclude = new ArrayList<String>();
        if(!new File(CORE_P4).getParent().equals(new File(V1MODEL_P4).getParent()))
                throw new IllegalStateException("!new File(CORE_P4).getParent().equals(new File(V1MODEL_P4).getParent())");
        systemInclude.add(new File(CORE_P4).getParent());            

        // systemInclude.add(V1MODEL_P4);            
        pp.setSystemIncludePath(systemInclude);
        
        P4Lexer lexer = new P4Lexer(CharStreams.fromReader(new CppReader(pp)));
        TokenStream tokenStream = new CommonTokenStream(lexer);

            
        P4Parser parser = new P4Parser(tokenStream);
        
        ParseTree tree = parser.start();
//        displayNativeAntlrTree(parser, tree);
//        antlrParseTreeToXML(tree);

//        Graph graph = TinkerGraph.open();
//        GraphTraversalSource g = graph.traversal();
        GraphTraversalSource g = 
            AnonymousTraversalSource
                    .traversal()
                    .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));

        TinkerGraphParseTree.fromParseTree(g, tree, lexer.getVocabulary(), parser.getRuleNames());
//        printSyntaxTree(g);


//      TODO Normalisation introduces new nodes mid-tree. If we need 
//           node numbering to follow depth-first order, then node ids have to 
//           be reseted. This should be possible to do on client-side, 
//           but it is not very important so I left it for later.
//           Find the original in p4analysis/experts-everything-until-now/src/main/java/parser/GremlinUtils.java 
//           https://github.com/daniel-lukacs/p4analysis/commit/5852c715060ecfcb22397120f9a588a3a7721848

//        Normalisation.analyse(g);
//        GremlinUtils.resetNodeIds(g, Dom.SYN);

//        printSyntaxTree(g);


        SemanticAnalysis.analyse(g);
//        printSemanticGraph(g);

        printSymbol(g);
        printCalls(g);
//        printCallSites(g);


//      TODO not migrated yet to gremlin-server
//        ControlFlowAnalysis.analyse(g);
//        printCfg(g);
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

    private static void displayNativeAntlrTree(P4Parser parser, ParseTree tree) {
        //show AST in GUI
        TreeViewer viewer = new TreeViewer( Arrays.asList(parser.getRuleNames()),tree);
        viewer.open();

        //show AST in console (LISP)
        System.out.println(tree.toStringTree(parser));
    }

    private static void antlrParseTreeToXML(ParseTree tree) throws TransformerException, ParserConfigurationException {
        XMLParseTree.toFile(XMLParseTree.fromParseTree(tree), "p4-antlr.xml", true);
    }

// // stress test case generators 
//    public static void stressTest(int n) {
//        try {
//            PrintStream out = new PrintStream("/tmp/filename.txt");
//        
//            System.out.println("");
//            System.out.println("// 511");
//            f(511);        
//
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        System.out.println();  
//        System.exit(0);
//    }

//    public static void f(int n) {
//        if (n == 1) {
//            System.out.print("{ ipv4_lpm.apply(); }");
//        } else if (n > 1) {
//            System.out.print("{ ipv4_lpm.apply(); ");
//            f(n - 1);
//            System.out.print("}");
//        }
//    }
//
//    public static void g(int n, PrintStream out) {
//
//            if(n == 0){
//            out.print("ipv4_lpm.apply();");
//            }
//            else if(n > 0){
//            out.print("if(hdr.ipv4.isValid()){ipv4_lpm.apply();"); 
//            g(n - 1, out);
//            out.print("}else{ipv4_lpm.apply();"); 
//            g(n - 1, out);
//            out.print("}"); 
//            }
//            out.close();
//    }
//    public static void h(int n){
//        for (int i = 0; i < n; i++) {
//          System.out.print("{ ipv4_lpm.apply(); }");
//        }
//    }

}