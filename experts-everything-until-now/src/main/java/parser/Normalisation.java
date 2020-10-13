package parser;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;

public class Normalisation {
    // NOTE this is controversial, because it breaks 1-1 mapping between code and syntax tree.
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();

        oneWayCondsToTwoWayConds(g);
    }

    private static void oneWayCondsToTwoWayConds(GraphTraversalSource g) {
        g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ConditionalStatementContext")
         .filter(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "statement").count().is(1))
         .sideEffect(t -> System.out.println("a"))
         .as("cond")

         .addV(Dom.SYN)
         .property(Dom.Syn.V.CLASS, "TerminalNodeImpl")
         .property(Dom.Syn.V.START, -1)
         .property(Dom.Syn.V.END, -1)
         .property(Dom.Syn.V.VALUE, "else")
         .sideEffect(GremlinUtils.setNodeId())
         .as("elseTerm")

         .addE(Dom.SYN).from("cond")
         .property(Dom.Syn.E.RULE,"ELSE")
         .sideEffect(GremlinUtils.setEdgeOrd())

         .addV(Dom.SYN)
         .property(Dom.Syn.V.CLASS, "StatementContext")
         .property(Dom.Syn.V.START, -1)
         .property(Dom.Syn.V.END, -1)
         .sideEffect(GremlinUtils.setNodeId())
         .as("stmtCtx")

         .addE(Dom.SYN).from("cond")
         .property(Dom.Syn.E.RULE,"statement")
         .sideEffect(GremlinUtils.setEdgeOrd())

         .addV(Dom.SYN)
         .property(Dom.Syn.V.CLASS, "BlockStatementContext")
         .property(Dom.Syn.V.START, -1)
         .property(Dom.Syn.V.END, -1)
         .sideEffect(GremlinUtils.setNodeId())

         .addE(Dom.SYN).from("stmtCtx")
         .property(Dom.Syn.E.RULE,"blockStatement")
         .sideEffect(GremlinUtils.setEdgeOrd())

         .iterate();

    }
    
}