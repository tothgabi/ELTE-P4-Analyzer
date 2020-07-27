package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

// root -> instantiation, parsers, controls
// 

public class ControlFlowAnalysis {
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();
        Vertex cfgRoot = g.addV("cfg").property("nodeId", "0").toList().get(0);
        // creates a cfg entry node for every function-like code segment. connects each entry node to its relevant syntactic node.
        // entryNodes are the associated syntax nodes nodes.

        List<Vertex> entryNodes = 
                g
                .withSack(new int[]{1})
                .V().hasLabel("syn").and().or(
                      __.has("class", "InstantiationContext"),
                      __.has("class", "ParserDeclarationContext"),
                      __.has("class", "ControlDeclarationContext"))

                .addE("cfg").property("role", "assoc").property("ord", "0")
                .from(__.addV("cfg")
                        .property("nodeId", 
                                  __.<Vertex, int[]>sack()
                                    .map(t -> Integer.toString(t.get()[0])))
                        .addE("cfg").from(cfgRoot)
                                      .property("ord", __.<Vertex, int[]>sack().map(t -> Integer.toString(t.get()[0] - 1)))
                                      .<int[], Edge>sack((a,b) -> { a[0]++; return a;})
                                      .property("role","flow").inV())
                .inV() // possible gremlin BUG: for some reason outV() returns the same nodes as inV()
                .toList(); 
        
// TODO would be more readable if it was partly in java. jumping back to nodes is unnecessary.

        // create CFG corresponding to each code segment
        g.V(entryNodes.toArray())
         .as("synEntry")
         .inE("cfg").has("role", "assoc").outV().as("cfEntry")
         .select("synEntry")
         .choose(__.values("class"))
         .option("ParserDeclarationContext", analyseParsers(__.<Vertex>identity()))
         .iterate();
    }

    @SuppressWarnings("unchecked")
    private static GraphTraversal<Vertex, Vertex> analyseParsers(GraphTraversal<Vertex, Vertex> t) {
        Map<String, Vertex> cfStates = new HashMap<>();
        t = t.repeat(__.out("syn")).until(__.has("class", "ParserStateContext")).as("synState")
             .addV("cfg").as("cfState")
             .addE("cfg").from(__.select("cfEntry")).property("role", "flow")
             .select("synState")
             .local(analyseStateName(__.identity(), cfStates))
//             
             .barrier() // states have to be added before we move on to add edges
             .select("synState")
//             .union(
//                analyseStateStatements(__.identity()),
//                analyseTransitions(__.identity(), cfStates))
//             .<Vertex>select("stateName")
//            .sideEffect(u -> System.out.println(u.get().value("value").toString()))
//            .sideEffect(u -> System.out.println(u.get().value("value").toString()))

//                analyseStateName(__.identity()).as("stateName"),
            ;

        return t;
    }

    private static GraphTraversal<Vertex, Vertex> analyseStateName(GraphTraversal<Vertex, Vertex> t, Map<String, Vertex> cfStates) {
        t.outE("syn").has("rule", "name").inV()
         .repeat(__.out("syn")).until(__.has("class", "TerminalNodeImpl"))
//         .sideEffect(x -> cfStates.put(x.sideEffects("cfState").toString(), x.get()))
         .addE("cfg").from(__.select("cfState")).property("role", "lab").property("ord", 0);
        return t;
    }
    private static GraphTraversal<Vertex, Vertex> analyseStateStatements(GraphTraversal<Vertex, Vertex> t) {
        return t;
    }


    @SuppressWarnings("unchecked")
    private static GraphTraversal<Vertex, Vertex> analyseTransitions(GraphTraversal<Vertex, Vertex> t,
            Map<String, Vertex> cfStates) {
        t = t.outE("syn").has("rule", "transitionStatement").inV()
             .outE("syn").has("rule", "stateExpression").inV()
             .outE("syn").union(
                 __.has("rule", "selectExpression").inV() 
                   .repeat(__.out("syn"))
                   .until(__.outE("syn").has("rule", "selectCase"))
                   .outE("syn").has("rule", "selectCase").inV(),
                 __.has("rule", "name").inV() 
                   .repeat(__.out("syn")).until(__.has("class", "TerminalNodeImpl"))
                   );

        return t;
    }
}