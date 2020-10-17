package p4analyser.experts.syntaxtree;

import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import p4analyser.experts.syntaxtree.p4.P4Lexer;
import p4analyser.experts.syntaxtree.p4.P4Parser;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

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

public class AntlrP4 
{
    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    public static final String EX1_P4 = loader.getResource("ex1.p4").getPath().toString();
    public static final String CORE_P4 = loader.getResource("core.p4").getPath().toString();
    public static final String V1MODEL_P4 = loader.getResource("v1model.p4").getPath().toString();

    public static void main( String[] args ) throws IOException
    {
       String host = args[0];
       int port = Integer.parseInt(args[1]);
       String remoteTraversalSourceName = args[2];

// // Antlr4 P4 parser generation is now automatically managed by Maven. 
// // In case of emergency, this can also generate P4Lexer class and P4Parser class along with the P4BaseVisitor class:
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
    }

    private static void displayNativeAntlrTree(P4Parser parser, ParseTree tree) {
        //show AST in GUI
        TreeViewer viewer = new TreeViewer( Arrays.asList(parser.getRuleNames()),tree);
        viewer.open();

        //show AST in console (LISP)
        System.out.println(tree.toStringTree(parser));
    }

    private static void antlrParseTreeToXML(ParseTree tree)  {
        
        try {
            XMLParseTree.toFile(XMLParseTree.fromParseTree(tree), "p4-antlr.xml", true);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
