package p4analyser.experts.syntaxtree;

import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.codejargon.feather.Provides;

import p4analyser.experts.syntaxtree.p4.P4Lexer;
import p4analyser.experts.syntaxtree.p4.P4Parser;
import p4analyser.ontology.providers.SyntaxTreeProvider;
import p4analyser.ontology.providers.P4FileProvider.CoreP4File;
import p4analyser.ontology.providers.P4FileProvider.InputP4File;
import p4analyser.ontology.providers.P4FileProvider.V1ModelP4File;

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

public class AntlrP4 implements SyntaxTreeProvider {

    @Provides
    @SyntaxTree
    public Object analyse(GraphTraversalSource g, @InputP4File File inputP4, @CoreP4File File coreP4, @V1ModelP4File File v1Model) throws IOException
    {

// // Antlr4 P4 parser generation is now automatically managed by Maven. 
// // In case of emergency, this can also generate P4Lexer class and P4Parser class along with the P4BaseVisitor class:
//      org.antlr.v4.Tool.main(new String[]{"-visitor", "-o", "hmm/src/main/java/hmm/p4", "-package", "hmm.p4", "P4.g4"});

//  // To parse without resolving includes:
//        CharStream stream = CharStreams.fromFileName(BASIC_P4);
//        P4Lexer lexer  = new P4Lexer(stream);   

        // Using C preprocessor to resolve includes. 
        // JCPP-Antlr integration from here: https://stackoverflow.com/a/25358397
        // Note that includes are huge, they slow down everything, and many things can be analysed without them.
        Preprocessor pp = new Preprocessor(inputP4);
        List<String> systemInclude = new ArrayList<String>();

        // TODO it would be more elegant to add the parent dir of v1Model as well
        if(!coreP4.getParent().equals(v1Model.getParent())){
                pp.close();
                throw new IllegalStateException("!coreP4.getParent().equals(v1Model.getParent())");
        }
        systemInclude.add(coreP4.getParent());   

        pp.setSystemIncludePath(systemInclude);
        
        P4Lexer lexer = new P4Lexer(CharStreams.fromReader(new CppReader(pp)));
        pp.close();
        TokenStream tokenStream = new CommonTokenStream(lexer);

        P4Parser parser = new P4Parser(tokenStream);
        
        ParseTree tree = parser.start();
//        displayNativeAntlrTree(parser, tree);
//        antlrParseTreeToXML(tree);

        TinkerGraphParseTree.fromParseTree(g, tree, lexer.getVocabulary(), parser.getRuleNames());

        return true;
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
