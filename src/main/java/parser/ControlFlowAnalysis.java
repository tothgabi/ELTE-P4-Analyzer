package parser;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;


public class ControlFlowAnalysis {
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();
        GremlinUtils.initializeNodeIds(graph, Dom.CFG);
        g.addV(Dom.CFG).sideEffect(GremlinUtils.setNodeId()).iterate();

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
        g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER).inV()
         .as("entrySyn")
         .addV(Dom.CFG).sideEffect(GremlinUtils.setNodeId())
         .as("entryCf")
         .addE(Dom.CFG).to("entrySyn").property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
         .sideEffect(GremlinUtils.setEdgeOrd())
         .select("entryCf")
         .addE(Dom.CFG)
         .from(__.V().hasLabel(Dom.CFG).has(Dom.Syn.V.NODE_ID, 0))
         .property(Dom.Cfg.E.ROLE,Dom.Cfg.E.Role.ENTRY)
         .sideEffect(GremlinUtils.setEdgeOrd())
         .select("entryCf")
         .addE(Dom.CFG).from(__.addV(Dom.CFG).sideEffect(GremlinUtils.setNodeId()))
         .property(Dom.Cfg.E.ROLE,Dom.Cfg.E.Role.EXIT)
         .sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findStates(GraphTraversalSource g) {
        g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
         .as("stateSyn")
         .addV(Dom.CFG).sideEffect(GremlinUtils.setNodeId())
         .as("stateCf")
         .addE(Dom.CFG).to("stateSyn")
         .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
         .sideEffect(GremlinUtils.setEdgeOrd())
         .select("stateCf")
         .addE(Dom.CFG)
         .to(__.select("stateSyn").outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME).inV())
         .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.LAB)
         .sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }


    private static void findStart(GraphTraversalSource g) {
        g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER).inV()
         .<Vertex>project("startCf", "entryCf")
         .by(__.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.START).inV()
               .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV())
         .by(__.inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV())
         .addE(Dom.CFG)
         .from("entryCf").to("startCf")
         .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW).sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findTransitions(GraphTraversalSource g) {
        g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NEXT)
         .<Vertex>project("cfSource", "cfDest")
         .by(__.outV().inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV())
         .by(__.inV().inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV())
         .addE(Dom.CFG).from("cfSource").to("cfDest")
         .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW).sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findFinals(GraphTraversalSource g){
        g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER).inV()
         .as("synParser")
         .outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.FINAL).inV()
         .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
         .as("finalCf")
         .select("synParser")
         .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
         .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.EXIT).outV()
         .addE(Dom.CFG).from("finalCf")
         .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW).sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findStatements(GraphTraversalSource g) {

        g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
         .as("synState")
         .outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATEMENT).inV()
         .as("synStmt")
         .select("synState")
         .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
         .addE(Dom.CFG).to("synStmt")
         .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.STATEMENT).sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();

    }
}