package parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;


// TODO actions are function-like too, add them
public class ControlFlowAnalysis {
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();
        Vertex cfgRoot = g.addV("cfg").property("nodeId", "0").toList().get(0);

        // creates a cfg entry node for every function-like code segment. connects each entry node to its relevant syntactic node.
        // synEntries are the associated syntax nodes.
        int[] nodeId = new int[]{1};
        List<Vertex> synEntries = 
                g
                .V().hasLabel("syn").and().or(
                      __.has("class", "InstantiationContext"),
                      __.has("class", "ParserDeclarationContext"),
                      __.has("class", "ControlDeclarationContext"))
                .addE("cfg")
                .property("role", "assoc").property("ord", "0")
                .from(__.addV("cfg")
                        .property("nodeId", __.constant(-1).map(i -> nodeId[0]))
                        .addE("cfg")
                        .property("role","flow")
                        .property("ord", __.constant(-1).map(i -> nodeId[0] - 1))
                        .from(cfgRoot)
                        .sideEffect(x -> nodeId[0]++)
                        .inV())
                .inV() // possible gremlin BUG: for some reason outV() returns the same nodes as inV()
                .toList(); 

        // bug workaround
        List<Map<String, Vertex>> entries = 
            g   .V(synEntries.toArray()).as("entrySyn")
                .inE("cfg").has("role", "assoc")
                .outV().as("entryCf")
                .<Vertex>select("entrySyn", "entryCf")
                .toList();

        // create CFG corresponding to each function-like code segment
        for(Map<String, Vertex> s : entries){
           Vertex entrySyn = s.get("entrySyn");
           Vertex entryCf = s.get("entryCf");
           Vertex exitCf = 
                g   .V(entryCf)
                    .addE("cfg").from(__.addV("cfg")).property("role", "exit")
                    .outV().next();
            switch(entrySyn.value("class").toString()){
                case "ParserDeclarationContext": 
                    analyseParsers(g, entrySyn, entryCf, exitCf, nodeId);
                    break;
                case "InstantiationContext": 
                    System.err.println("warning: InstantiationContext not implemented yet");
                    break;
                case "ControlDeclarationContext": 
                    System.err.println("warning: ControlDeclarationContext not implemented yet");
                    break;
                default:
                    throw new IllegalStateException("unknown 'class' for node");
            }
        }
  }

    @SuppressWarnings("unchecked")
    private static void analyseParsers(GraphTraversalSource g, Vertex entrySyn, Vertex entryCf, Vertex exitCf, int[] nodeId) {
        List<Map<String, Vertex>> states = 
            g.V(entrySyn)
             .repeat(__.out("syn"))
             .until(__.has("class", "ParserStateContext")).as("stateSyn")
             .addE("cfg")
             .property("role", "assoc")
             .property("ord", "0")
             .from(__.addV("cfg")
                     .property("nodeId", __.constant(-1).map(i -> nodeId[0]++))
             )
//                     .addE("cfg").from(entryCf)
//                     .property("role", "flow")
//                     .property("ord", __.V(entryCf).out().count())
//                     .inV())
             .outV().as("stateCf")
             .<Vertex>select("stateSyn", "stateCf")
             .toList();

        for (Map<String,Vertex> s : states) {
           Vertex synState = s.get("stateSyn");
           Vertex cfState = s.get("stateCf");
           analyseStateName(g, synState, cfState);
//           analyseStateStatements(__.identity(), cfStates))
        }

        Collection<Vertex> allCfStates = 
            states  .stream()
                    .map(m -> m.get("stateCf"))
                    .collect(Collectors.toList());

        g.V(allCfStates)
         .filter(__.outE("cfg").has("role", "lab")
                   .inV().has("value", "start")
                   .count().is(P.gt(0)))
         .addE("cfg").from(entryCf).property("role", "flow")
         .iterate();

        String[] extraCfStateNames = { "accept", "reject"}; 
        for(String s : extraCfStateNames){
            Vertex v = 
                g   .addV("cfg")
                    .addE("cfg")
                    .to(__.addV("cfg").property("value", s))
                    .property("role", "lab")
                    .outV()
                    .addE("cfg")
                    .to(exitCf).property("role", "flow")
                    .outV()
                    .next();
            allCfStates.add(v);
        }

        for (Map<String,Vertex> s : states) {
           Vertex synState = s.get("stateSyn");
           Vertex cfState = s.get("stateCf");
           analyseTransitions(g, synState, cfState, allCfStates);
       }
    }

    private static void analyseStateName(GraphTraversalSource g, Vertex stateSyn, Vertex stateCf) {
        g.V(stateSyn)
         .outE("syn").has("rule", "name")
         .inV()
         .repeat(__.out("syn"))
         .until(__.has("class", "TerminalNodeImpl"))
         .addE("cfg")
         .from(stateCf).property("role", "lab")
         .property("ord", __.V(stateCf).out().count())
         .iterate();
    }
    private static GraphTraversal<Vertex, Vertex> analyseStateStatements(GraphTraversal<Vertex, Vertex> t) {
        return t;
    }


    @SuppressWarnings("unchecked")
    private static void analyseTransitions(GraphTraversalSource g,  Vertex stateSyn, Vertex stateCf, Collection<Vertex> otherStateCfs) {

        List<Map<String, Vertex>> ts = 
            g   .V(stateSyn)
                .outE("syn").has("rule", "transitionStatement").inV()
                .outE("syn").has("rule", "stateExpression").inV()

                .outE("syn").union(
                  __.has("rule", "name").inV()
                    .repeat(__.out("syn")).until(__.has("class", "TerminalNodeImpl"))
                    .<Vertex>project("targetName", "keyExpr"),

                  __.has("rule", "selectExpression").inV() 
                    .repeat(__.out("syn"))
                    .until(__.outE("syn").has("rule", "selectCaseList").count().is(0))
                    .emit(__.outE("syn").has("rule", "selectCase"))
//                    .until(__.outE("syn").has("rule", "selectCase"))
                    .outE("syn").has("rule", "selectCase").inV()

                    .<Vertex>project("targetName", "keyExpr")
                    .by(__.outE("syn").has("rule", "name").inV()
                          .repeat(__.out("syn"))
                          .until(__.has("class", "TerminalNodeImpl")))
                    .by(__.outE("syn").has("rule", "keysetExpression")
                          .inV()))
                .toList();


        for (Map<String,Vertex> target : ts) {
            String name = target.get("targetName").value("value").toString();
            g.V(otherStateCfs.toArray())
                .filter(__.outE("cfg").has("role", "lab")
                        .inV()
                        .values("value")
                        .is(name))
                .addE("cfg").from(stateCf).property("role", "flow")
                .iterate();
        }

    }
}