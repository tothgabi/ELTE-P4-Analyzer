package parser;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;


public class ControlFlowAnalysis {
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();
        GremlinUtils.initializeNodeIds(graph, "cfg");
        g.addV("cfg").sideEffect(GremlinUtils.setNodeId()).iterate();

        analyseParsers(g);

  }

    private static void analyseParsers(GraphTraversalSource g) {
        findEntryExit(g);
        findStates(g);
        findStart(g);
        findTransitions(g);
        findFinals(g);
        findStatements(g);
    }


    private static void findEntryExit(GraphTraversalSource g) {
        g.E().hasLabel("sem").has("domain", "parser").has("role", "parser").inV()
         .as("entrySyn")
         .addV("cfg").sideEffect(GremlinUtils.setNodeId())
         .as("entryCf")
         .addE("cfg").to("entrySyn").property("role", "assoc")
         .sideEffect(GremlinUtils.setEdgeOrd())
         .select("entryCf")
         .addE("cfg")
         .from(__.V().hasLabel("cfg").has("nodeId", 0))
         .property("role","entry")
         .sideEffect(GremlinUtils.setEdgeOrd())
         .select("entryCf")
         .addE("cfg").from(__.addV("cfg").sideEffect(GremlinUtils.setNodeId()))
         .property("role","exit")
         .sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findStates(GraphTraversalSource g) {
        g.E().hasLabel("sem").has("domain", "parser").has("role", "state").inV()
         .as("stateSyn")
         .addV("cfg").sideEffect(GremlinUtils.setNodeId())
         .as("stateCf")
         .addE("cfg").to("stateSyn")
         .property("role", "assoc")
         .sideEffect(GremlinUtils.setEdgeOrd())
         .select("stateCf")
         .addE("cfg")
         .to(__.select("stateSyn").outE("sem").has("domain", "parser").has("role", "name").inV())
         .property("role", "lab")
         .sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }


    private static void findStart(GraphTraversalSource g) {
        g.E().hasLabel("sem").has("domain", "parser").has("role", "parser").inV()
         .<Vertex>project("startCf", "entryCf")
         .by(__.outE("sem").has("domain", "parser").has("role", "start").inV()
               .inE("cfg").has("role", "assoc").outV())
         .by(__.inE("cfg").has("role", "assoc").outV())
         .addE("cfg")
         .from("entryCf").to("startCf")
         .property("role", "flow").sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findTransitions(GraphTraversalSource g) {
        g.E().hasLabel("sem").has("domain", "parser").has("role", "next")
         .<Vertex>project("cfSource", "cfDest")
         .by(__.outV().inE("cfg").has("role", "assoc").outV())
         .by(__.inV().inE("cfg").has("role", "assoc").outV())
         .addE("cfg").from("cfSource").to("cfDest")
         .property("role", "flow").sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findFinals(GraphTraversalSource g){
        g.E().hasLabel("sem").has("domain", "parser").has("role", "parser").inV()
         .as("synParser")
         .outE("sem").has("domain", "parser").has("role", "final").inV()
         .inE("cfg").has("role", "assoc").outV()
         .as("finalCf")
         .select("synParser")
         .inE("cfg").has("role", "assoc").outV()
         .inE("cfg").has("role", "exit").outV()
         .addE("cfg").from("finalCf")
         .property("role", "flow").sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findStatements(GraphTraversalSource g) {

        g.E().hasLabel("sem").has("domain", "parser").has("role", "state").inV()
         .as("synState")
         .outE("sem").has("domain", "parser").has("role", "statement").inV()
         .as("synStmt")
         .select("synState")
         .inE("cfg").has("role", "assoc").outV()
         .addE("cfg").to("synStmt")
         .property("role", "statement").sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();

    }
}