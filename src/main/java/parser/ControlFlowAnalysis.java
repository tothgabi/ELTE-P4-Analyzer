package parser;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;


public class ControlFlowAnalysis {
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();

        g.addV("cfg").property("nodeId", "0").iterate();

        int[] nodeId = new int[]{1};
        analyseParsers(g, nodeId);

  }

    private static void analyseParsers(GraphTraversalSource g, int[] nodeId) {
        findEntryExit(g, nodeId);
        findStates(g, nodeId);
        findStart(g);
        findTransitions(g);
        findFinals(g);
        findStatements(g);
    }


    private static void findEntryExit(GraphTraversalSource g, int[] nodeId) {
        g.E().hasLabel("sem").has("domain", "parser").has("role", "parser").inV()
         .as("entrySyn")
         .addV("cfg").property("nodeId", __.constant(-1).map(i -> nodeId[0]++))
         .as("entryCf")
         .addE("cfg").property("role", "assoc")
         .to("entrySyn")
         .select("entryCf")
         .addE("cfg").property("role","entry")
         .from(__.V().hasLabel("cfg").has("nodeId", "0"))
         .select("entryCf")
         .addE("cfg").property("role","exit")
         .from(__.addV("cfg"))
         .iterate();
    }

    private static void findStates(GraphTraversalSource g, int[] nodeId) {
        g.E().hasLabel("sem").has("domain", "parser").has("role", "state").inV()
         .as("stateSyn")
         .addV("cfg").property("nodeId", __.constant(-1).map(i -> nodeId[0]++))
         .as("stateCf")
         .addE("cfg").property("role", "assoc")
         .to("stateSyn")
         .select("stateCf")
         .addE("cfg").property("role", "lab")
         .to(__.select("stateSyn").outE("sem").has("domain", "parser").has("role", "name").inV())
         .iterate();
    }


    private static void findStart(GraphTraversalSource g) {
        g.E().hasLabel("sem").has("domain", "parser").has("role", "parser").inV()
         .<Vertex>project("startCf", "entryCf")
         .by(__.outE("sem").has("domain", "parser").has("role", "start").inV()
               .inE("cfg").has("role", "assoc").outV())
         .by(__.inE("cfg").has("role", "assoc").outV())
         .addE("cfg").property("role", "flow")
         .from("entryCf").to("startCf")
         .iterate();
    }

    private static void findTransitions(GraphTraversalSource g) {
        g.E().hasLabel("sem").has("domain", "parser").has("role", "next")
         .<Vertex>project("cfSource", "cfDest")
         .by(__.outV().inE("cfg").has("role", "assoc").outV())
         .by(__.inV().inE("cfg").has("role", "assoc").outV())
         .addE("cfg").property("role", "flow")
         .from("cfSource").to("cfDest")
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
         .addE("cfg").property("role", "flow")
         .from("finalCf")
         .iterate();
    }

    private static void findStatements(GraphTraversalSource g) {

        g.E().hasLabel("sem").has("domain", "parser").has("role", "state").inV()
         .as("synState")
         .outE("sem").has("domain", "parser").has("role", "statement").inV()
         .as("synStmt")
         .select("synState")
         .inE("cfg").has("role", "assoc").outV()
         .addE("cfg").property("role", "statement")
         .to("synStmt")
         .iterate();

    }
}