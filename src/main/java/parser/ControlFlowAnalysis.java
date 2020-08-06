package parser;

import java.util.List;
import java.util.stream.Collectors;

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

        // TODO turns out traversal order is not good. this must be fixed where syntax ORD is set, the rest should be ok.
        // TODO the last segment of statements (first, given the previous bug) is not copied to state
        // TODO there is an empty state added between the state of the conditional and the state of the previous block. this may be related to the first bug.
        private static void analyse(GraphTraversalSource g) {
            // The core idea is that the semantic analysis sets last-edges and return-edges for each block. 
            // Then, if you are nesting a block in the middle of your code, you know where does that nested block starts, and you also know that it ends at it return point(s).
            // In case you nest a block in the end of your code, you don't need to know where it ends: your return point(s) will be the same as the nested blocks return point(s). You don't need to do anything with that because the block who in turn nests your code will use that point to know where your code ends.

            // Assumption: the set of nodes pointed by return are a subset of those pointed by nest-, trueBranch-, or falseBranch-edges.

            // First, each syn-node pointed by a body-, nest-, trueBranch-, or falseBranch-edge gets a cfg-block (associated to that syn-node). 
            // The cfg-block associated with the body-edge endpoint gets a flow-edge from the entry. The cfg-block associated with the return point of the body-edge endpoint gets a flow-edge into the exit.
            // In a second iteration, we analyse each cfg-blocks separately:
            // - The cfg-block gets all statements of the associated syn-node until there is a nest-edge (or the end).
            // - For each nest-edge a new cfg-block is created. This new cfg-block again gets all statements from the original associated syn-node until there is a nest-edge (or the end).
            // - The first cfg-block is linked to the (first) cfg-block associated with the syn-node pointed by the first nest-edge. We then check which cfg-block corresponds to the return point of the nested syn-node, and link that to the cfg-block created for the second-edge.
            // Finally, the analysis of the root syn-node of the control, and analysis the conditionals is straighforward

            // The second iteration step-by-step:
            // - Find all the nest-edges of the syn-block. For each nest-edge:
            //   * Create a cfg-block and assign the corresponding statements.
            //   * Find the cfg-block associated with the endpoint of the nest-edge.
            //   * Find the cfg-block associated with the return point of the endpoint of the nest edge.
            //   * Link the three cfg-blocks appropriately.
            
            // Note: A block can be nested either in the middle, or in the end.
            // If the block is nested in the middle, then a new block for the continuation is needed, and the nested block will flow into the new block.
            // If the block is nested in the end, then the return point of the nested block is the same as the return point of the nesting block. In this case, no need to create a continuation block, and no need to set the flow of the nested block. Instead, a higher block will set it (when it asks for the return point of the current block).

            List<Edge> assocEdges=
                g.E().hasLabel(Dom.SEM)
                 .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY),
                     __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                     __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH),
                     __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST))
                 .inV().as("synNode")
                 .addV(Dom.CFG).property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.BLOCK)
                 .sideEffect(GremlinUtils.setNodeId())
                 .addE(Dom.CFG).to("synNode")
                 .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .toList();
                
            analyseEntryExit(g);
            analyseConditions(g);

            for (Edge e : assocEdges) {
                Vertex syntaxBlock = e.inVertex(); 
                Vertex cfgBlock = e.outVertex();

                List<Edge> nests =
                    g.V(syntaxBlock)
                    .outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST)                
                    .toList();
                
                List<Long> intervals = 
                    nests.stream()
                         .map(n -> (Long) n.value(Dom.Sem.ORD))
                         .collect(Collectors.toList());

                         

                g.V(syntaxBlock).outE(Dom.SEM)
                 .filter(intervals.isEmpty() ? __.identity() : __.has(Dom.Sem.ORD, P.lt(intervals.get(0))))
                 .has(Dom.Sem.ROLE, Dom.Sem.Role.Control.STATEMENT).inV()
                 .addE(Dom.CFG).from(__.V(cfgBlock))
                 .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.STATEMENT)
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .iterate();


                // Initially, lastCfgBlock is the cfg-block associated with the nesting nodes. 
                // Then, it will be assigned those cfg-block that were created to contain the 
                // code after the nested nodes.
                Vertex lastCfgBlock = cfgBlock;
                for (int i = 0; i < nests.size(); i++) {
                    Vertex nestedSyn = nests.get(i).inVertex();

                    // Select the cfg-block associated with the nested node,
                    // and send into it a flow-edge from the last cfg-block that belongs to the nesting node.
                    g.V(nestedSyn)
                     .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
                     .addE(Dom.CFG).from(lastCfgBlock)
                     .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                     .sideEffect(GremlinUtils.setEdgeOrd())
                     .iterate();

                    // If the nested node is in the middle of the nesting node (not the end),
                    // then create a new cfg-block, and send a flow-edge to the new cfg-block 
                    // from the cfg-block associated with the return point of the nested node.
                    // In case the nested node is a leaf (i.e. there is no return-edge), send the flow-edge from the cfg-block of this node. 
                    // Set the last cfg-block to the new cfg-block.
                
                    boolean isMidPosition =
                      !(g.V(nestedSyn).inE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.LAST)
                       .hasNext());

                    if(isMidPosition){
                        Vertex newBlock = 
                            g.addV(Dom.CFG).property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.BLOCK)
                            .sideEffect(GremlinUtils.setNodeId())
                            .as("newCfgBlock")
                            .addE(Dom.CFG).to(__.V(syntaxBlock))
                            .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
                            .sideEffect(GremlinUtils.setEdgeOrd())
                            .<Vertex>select("newCfgBlock")
                            .next();

                        g.V(syntaxBlock).outE(Dom.SEM)
                        .has(Dom.Sem.ORD, 
                             i < intervals.size() - 1 ? P.between(intervals.get(i), intervals.get(i + 1))
                                                      : P.gte(intervals.get(i)))
                        .has(Dom.Sem.ROLE, Dom.Sem.Role.Control.STATEMENT).inV()
                        .addE(Dom.CFG).from(__.V(cfgBlock))
                        .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.STATEMENT)
                        .sideEffect(GremlinUtils.setEdgeOrd())
                        .iterate();

                        g.V(nestedSyn)
                        .optional(
                            __.outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN).inV())
                        .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
                        .addE(Dom.CFG).to(newBlock)
                        .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                        .sideEffect(GremlinUtils.setEdgeOrd())
                        .iterate();

                        lastCfgBlock = newBlock;
                    }
                }
            }
             
       }

        private static void analyseConditions(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM)
             .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                 __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH))
             .as("e")
             .outV()
             .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
             .as("cfgHead")
             .select("e").inV()
             .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
             .addE(Dom.CFG).from("cfgHead")
             .property(Dom.Cfg.E.ROLE, 
                __.choose(__.select("e").values(Dom.Sem.ROLE))
                  .option(Dom.Sem.Role.Control.TRUE_BRANCH, __.constant(Dom.Cfg.E.Role.TRUE_FLOW))
                  .option(Dom.Sem.Role.Control.FALSE_BRANCH, __.constant(Dom.Cfg.E.Role.FALSE_FLOW)))
             .sideEffect(GremlinUtils.setEdgeOrd())
             .iterate();
        }

        private static void analyseEntryExit(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM)
                 .has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY).as("e")

                 // Identify entry and exit blocks of this control.
                 .<Edge>select("e").outV().inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV().as("cfgEntry")
                 .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.EXIT).outV().as("cfgExit")

                 // Add edge from entry node to the cfg-block associated with the node pointed by the body-edge.
                 .<Edge>select("e").inV()
                 .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
                 .addE(Dom.CFG).from("cfgEntry")
                 .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                 .sideEffect(GremlinUtils.setEdgeOrd())

                 // Add edge to exit node from the cfg-block associated with the return node of the control.
                 // Note that the return-edge always exists here (even if the body is a leaf block).
                 .<Vertex>select("e").outV()
                 .outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN).inV()
                 .inE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).outV()
                 .addE(Dom.CFG).to("cfgExit")
                 .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .iterate();
        }
    }
}