package parser;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;

import p4analyser.ontology.Dom;

public class Normalisation {
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