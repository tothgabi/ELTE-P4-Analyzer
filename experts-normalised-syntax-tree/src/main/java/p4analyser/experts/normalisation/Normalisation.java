package p4analyser.experts.normalisation;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;

import p4analyser.ontology.Dom;

/**
 * Hello world!
 *
 */
public class Normalisation 
{
    public static void main( String[] args )
    {

        System.out.println( "Not implemented yet!" );
        System.exit(0);

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];

//        Graph graph = TinkerGraph.open();
//        GraphTraversalSource g = graph.traversal();
        GraphTraversalSource g = 
            AnonymousTraversalSource
                    .traversal()
                    .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));

//      TODO Normalisation introduces new nodes mid-tree. If we need 
//           node numbering to follow depth-first order, then node ids have to 
//           be reseted. This should be possible to do on client-side, 
//           but it is not very important so I left it for later.
//           Find the original in p4analysis/experts-everything-until-now/src/main/java/parser/GremlinUtils.java 
//           https://github.com/daniel-lukacs/p4analysis/commit/5852c715060ecfcb22397120f9a588a3a7721848

//        Normalisation.analyse(g);
//        GremlinUtils.resetNodeIds(g, Dom.SYN);
    }

    // NOTE this is controversial, because it breaks 1-1 mapping between code and syntax tree.
    public static void analyse(GraphTraversalSource g){
        oneWayCondsToTwoWayConds(g);
    }

    private static void oneWayCondsToTwoWayConds(GraphTraversalSource g) {
        g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ConditionalStatementContext")
         .filter(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "statement").count().is(1))
//         .sideEffect(t -> System.out.println("a"))
         .as("cond")

         .addV(Dom.SYN)
         .property(Dom.Syn.V.CLASS, "TerminalNodeImpl")
         .property(Dom.Syn.V.START, -1)
         .property(Dom.Syn.V.END, -1)
         .property(Dom.Syn.V.VALUE, "else")
         
         .as("elseTerm")

         .addE(Dom.SYN).from("cond")
         .property(Dom.Syn.E.RULE,"ELSE")
         

         .addV(Dom.SYN)
         .property(Dom.Syn.V.CLASS, "StatementContext")
         .property(Dom.Syn.V.START, -1)
         .property(Dom.Syn.V.END, -1)
         
         .as("stmtCtx")

         .addE(Dom.SYN).from("cond")
         .property(Dom.Syn.E.RULE,"statement")
         

         .addV(Dom.SYN)
         .property(Dom.Syn.V.CLASS, "BlockStatementContext")
         .property(Dom.Syn.V.START, -1)
         .property(Dom.Syn.V.END, -1)
         

         .addE(Dom.SYN).from("stmtCtx")
         .property(Dom.Syn.E.RULE,"blockStatement")
         

         .iterate();

    }
    
}