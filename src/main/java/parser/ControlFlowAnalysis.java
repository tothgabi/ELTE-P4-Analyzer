package parser;

import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;


public class ControlFlowAnalysis {
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();
        GremlinUtils.initializeNodeIds(graph, Dom.CFG);
        g.addV(Dom.CFG).sideEffect(GremlinUtils.setNodeId()).iterate();

        findEntryExit(g);
        Parser.analyse(g);
        Control.analyse(g);

    }

    // Finds top-level parser and control declarations. 
    // Creates a corresponding entry block in the CFG and links it to the declaration.
    // Also links the entry with the CFG root.
    private static void findEntryExit(GraphTraversalSource g) {
        g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP)
        .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER),
            __.has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL))
        .inV()
        .as("entrySyn")
        .addV(Dom.CFG) 
        .property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.ENTRY)
        .sideEffect(GremlinUtils.setNodeId())
        .as("entryCf")
        .addE(Dom.CFG).to("entrySyn").property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
        .sideEffect(GremlinUtils.setEdgeOrd())
        .select("entryCf")
        .addE(Dom.CFG)
        .from(__.V().hasLabel(Dom.CFG).has(Dom.Syn.V.NODE_ID, 0))
        .property(Dom.Cfg.E.ROLE,Dom.Cfg.E.Role.ENTRY)
        .sideEffect(GremlinUtils.setEdgeOrd())
        .select("entryCf")
        .addE(Dom.CFG).from(__.addV(Dom.CFG)
                              .property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.EXIT)
                              .sideEffect(GremlinUtils.setNodeId()))
        .property(Dom.Cfg.E.ROLE,Dom.Cfg.E.Role.EXIT)
        .sideEffect(GremlinUtils.setEdgeOrd())
        .iterate();
    }

    static class Parser { 
        private static void analyse(GraphTraversalSource g) {
            findStates(g);
            findStart(g);
            findTransitions(g);
            findFinals(g);
            findStatements(g);
        }


        // Finds state declaration nodes.
        // Creates a corresponding block in the CFG and links it to the declaration.
        // Also links the block with the name of the state.
        private static void findStates(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
            .as("stateSyn")
            .addV(Dom.CFG).property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.BLOCK)
            .sideEffect(GremlinUtils.setNodeId())
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

        // Finds the entry block and the start state block in the CFG. 
        // Sends an edge from the entry to the start.
        private static void findStart(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER).inV()
            .<Vertex>project("entryCf", "startCf")
            .by(__.inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV())
            .by(__.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.START).inV()
                .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV())
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
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER).inV()
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
            .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.STATEMENT)
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();

        }
    }

    static class Control { 
        private static void analyse(GraphTraversalSource g) {

            // First find composite-blocks by finding each nest-edge that is not a last-edge. 
            // Each nest-edge and the last-edge will get a cfg-block. If a nest-edge and the last-edge points to the node, only one cfg-block is created.
            // The cfg-block will get all the statements to its left until the statements of its neighbour, or until the beginning of the edges.
            // Create an assoc-edge between the new cfg-block and the node of the composite-block. 
            // Return the list of these assoc-edges.
            List<Edge> assocEdges=
                g.E().hasLabel(Dom.SEM)
                 .has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL).inV()
                 .repeat(__.out(Dom.SYN))
                 .emit(__.has(Dom.Syn.V.CLASS, "BlockStatementContext"))

                 // Keep a block if it has at least one nest-edge, that points to a node that is not pointed by a last-edge.
                 .filter(
                     __.outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST)
                     .filter(__.inV().inE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.LAST).count().is(0)))
                 .as("blockRoot")
                 .outE(Dom.SEM).or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST),
                                 __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.LAST))
                 .as("edge")
                 .inV().dedup()

                 .addV(Dom.CFG).property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.BLOCK)
                 .sideEffect(GremlinUtils.setNodeId())

                 .addE(Dom.CFG).to("blockRoot")
                 .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
                 .property(Dom.Cfg.E.EDGE_ORD, __.select("edge").values(Dom.Sem.ORD))
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .toList();


            Long intervalStart = 0L;
            for (Edge e : assocEdges) {
                if(!(e.value(Dom.Sem.ORD) instanceof Long)) 
                    throw new IllegalStateException("long expected");
                Long intervalEnd = e.value(Dom.Cfg.E.EDGE_ORD);
                Vertex syntaxBlock = e.inVertex(); // assertion: the same for each of the edges 
                Vertex cfgBlock = e.outVertex();

                g.V(syntaxBlock).outE(Dom.SEM)
                 .has(Dom.Sem.ORD, P.between(intervalStart, intervalEnd))
                 .has(Dom.Sem.ROLE, Dom.Sem.Role.Control.STATEMENT)
                 .inV()
                 .addE(Dom.CFG).from(cfgBlock)
                 .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.STATEMENT)
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .iterate();

                intervalStart = intervalEnd + 1;
            }

            // In a second iteration, link the cfg-blocks.
       }
    }
}