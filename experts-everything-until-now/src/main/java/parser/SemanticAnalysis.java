package parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalOptionParent.Pick;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import p4analyser.ontology.Dom;

public class SemanticAnalysis {

//        CallSites.analyse(g);
    
    // NOTE P4 spec has almost nothing about type instantiations and method dispatch mechanisms.
    //      It is not clear whether packet.extract(...) refers to the extract method in the 'packet' namespace, where 'packet' is just an alias to 'packet_in', or
    //      it is actually a method call extract(packet, ...), where the definition of extract is selected based on the static type of 'packet'.
    //      The first case is simpler, so I went with this for now.
    public static class CallSites {
        // TODO make this work for other kind of calls and functions
        public static void analyse(GraphTraversalSource g){
            whichCallInvokesWhichFunction(g);
            whichCallOwnsWhichArguments(g);
            whichFunctionOwnsWhichParameters(g);
            whichArgumentsInstantiateWhichParameters(g);
        }
        
        private static void whichCallInvokesWhichFunction(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN)
            .or(__.has(Dom.Syn.V.CLASS, "FunctionPrototypeContext"),
                __.has(Dom.Syn.V.CLASS, "TableDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "PackageTypeDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"))
            .as("decl")
            .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).inV()
            .repeat(__.in(Dom.SYN))
            .until(
                __.or(__.has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext"),
                      __.has(Dom.Syn.V.CLASS, "DirectApplicationContext"),
                      __.has(Dom.Syn.V.CLASS, "InstantiationContext"),
                      __.has(Dom.Syn.V.CLASS, "ExpressionContext")
                        .outE(Dom.SYN).has(Dom.Syn.E.RULE, "argumentList")))
            .addE(Dom.SITES).to("decl")
            .property(Dom.Sites.ROLE, Dom.Sites.Role.CALLS)
            
            .iterate();

        }

        private static void whichCallOwnsWhichArguments(GraphTraversalSource g) {
            // TODO couldn't check but the arguments are probably in reverse order
            g.V().hasLabel(Dom.SYN)
            .or(__.has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext"),
                __.has(Dom.Syn.V.CLASS, "InstantiationContext")).as("call")
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "argumentList").inV()
            .repeat(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "nonEmptyArgList").inV())
            .emit()
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "argument").inV()
            .outE(Dom.SYN).inV()
            .addE(Dom.SITES).from("call")
            .property(Dom.Sites.ROLE, Dom.Sites.Role.HAS_ARGUMENT)
            
            .iterate();
        }
        private static void whichFunctionOwnsWhichParameters(GraphTraversalSource g) {
            // TODO couldn't check but the parameters are probably in reverse order
            g.V().or(__.has(Dom.Syn.V.CLASS, "FunctionPrototypeContext"),
                     __.has(Dom.Syn.V.CLASS, "ActionDeclarationContext"),
                     __.has(Dom.Syn.V.CLASS, "PackageTypeDeclarationContext")).as("func")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "parameterList").inV()
             .repeat(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "nonEmptyParameterList").inV())
             .emit()
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "parameter").inV()
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
             .repeat(__.out(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
             .addE(Dom.SITES).from("func")
             .property(Dom.Sites.ROLE, Dom.Sites.Role.HAS_PARAMETER)
             
             .iterate();
        }

        private static void whichArgumentsInstantiateWhichParameters(GraphTraversalSource g) {

           List<Edge> es = g.E().hasLabel(Dom.SITES).has(Dom.Sites.ROLE, Dom.Sites.Role.CALLS).toList();
           for (Edge edge : es) {
                Vertex func = edge.inVertex(); 
                Vertex call = edge.outVertex(); 

                List<Vertex> args = 
                    g.V(call).outE(Dom.SITES)
                    .has(Dom.Sites.ROLE, Dom.Sites.Role.HAS_ARGUMENT).inV()
                    .toList();

                List<Vertex> pars = 
                    g.V(func).outE(Dom.SITES)
                    .has(Dom.Sites.ROLE, Dom.Sites.Role.HAS_PARAMETER).inV()
                    .toList();

                if(args.size() != pars.size()) 
                    throw 
                        new IllegalStateException("args.size() != pars.size()");

                for (int i = 0; i < args.size(); i++) {
                    g.addE(Dom.SITES)
                     .from(args.get(i)).to(pars.get(i))
                     .property(Dom.Sites.ROLE, Dom.Sites.Role.INSTANTIATES)
                     
                     .iterate();
                }
           }



        }

    }


}