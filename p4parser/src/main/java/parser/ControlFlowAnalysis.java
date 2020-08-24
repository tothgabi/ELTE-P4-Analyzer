package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
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
        .addE(Dom.CFG).from("entrySyn").property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
        .sideEffect(GremlinUtils.setEdgeOrd())
        .select("entryCf")
        .addE(Dom.CFG)
        .from(__.V().hasLabel(Dom.CFG).has(Dom.Syn.V.NODE_ID, 0))
        .property(Dom.Cfg.E.ROLE,Dom.Cfg.E.Role.ENTRY)
        .sideEffect(GremlinUtils.setEdgeOrd())
        .select("entryCf")
        .addE(Dom.CFG).to(__.addV(Dom.CFG)
                              .property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.EXIT)
                              .sideEffect(GremlinUtils.setNodeId()))
        .property(Dom.Cfg.E.ROLE,Dom.Cfg.E.Role.RETURN)
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
            .addE(Dom.CFG).from("stateSyn")
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
            .by(__.outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV())
            .by(__.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.START).inV()
                .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV())
            .addE(Dom.CFG)
            .from("entryCf").to("startCf")
            .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW).sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
        }

        private static void findTransitions(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NEXT)
            .<Vertex>project("cfSource", "cfDest")
            .by(__.outV().outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV())
            .by(__.inV().outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV())
            .addE(Dom.CFG).from("cfSource").to("cfDest")
            .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW).sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
        }

        private static void findFinals(GraphTraversalSource g){
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER).inV()
            .as("synParser")
            .outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.FINAL).inV()
            .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV()
            .as("finalCf")
            .select("synParser")
            .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV()
            .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN).inV()
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
            .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV()
            .addE(Dom.CFG).to("synStmt")
            .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.STATEMENT)
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();

        }
    }

    static class Control { 
        private static void analyse(GraphTraversalSource g) {
            // We traverse the hierarchical syntax tree to create a control flow graph.
            // The two core ideas for this are:
            // - A list of nested blocks can processed into a cfg if know where the nested blocks end (not trivial, because nested blocks themselves can be nesting blocks or conditionals): we just chain the cfg-block associated with the return point of any nested-block to the cfg-block associated with the start of the subsequent nested-block.
            // - Nested blocks are always below nesting blocks in the syntax tree: going in leaf-to-root direction, we can be assured that nested blocks are already fully processed (there is a state  associated with their return point).
            // Additionally, if a block contains both nested blocks and statements, we need to create further cfg-blocks in the chain for this block to contain the statements.

            // NOTE: We assume the children always have higher nodeIds than their parents.
            //      This way, if we process nodes in descending nodeIds, we can be assured
            //      that children are always processed before their parents
            List<Edge> assocEdges=
            g.E().hasLabel(Dom.SEM)
                 .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY),
                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH),
                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST))
                 .inV()
                 .order().by(Dom.Syn.V.NODE_ID, Order.desc)
                 .as("synNode")
                 .addV(Dom.CFG).property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.BLOCK)
                 .sideEffect(GremlinUtils.setNodeId())
                 .addE(Dom.CFG).from("synNode")
                 .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .toList();

                
            analyseEntry(g);

            for (Edge e : assocEdges) {
                Vertex syntaxBlock = e.outVertex(); 
                Vertex cfgBlock = e.inVertex();

                if(syntaxBlock.value(Dom.Syn.V.CLASS).equals("ConditionalStatementContext")){
                    analyseConditions(g, syntaxBlock, cfgBlock);

                } else {
                    analyseNesting(g, syntaxBlock, cfgBlock);
                }
            }
             
            analyseExit(g);

       }

        // Algorithm:
        // - Declare a stack and a variable called last block.
        // - Iterate over the nests and statements.
        // - If you find a statement put it into the stack
        // - Else if you find a nest:
        //   * If the stack is empty, get your nested cfg-block and link its returns to the last block (if there is one). 
        //   * If the stack is not empty, create a block and claim all statements in the stack. link the new block to the last block (if there is one). get your nested cfg-block and link its returns to the new block. 
        //   * Assign the nested cfg-block to the last block.
        // - Get the first cfg-block, claim all the statements in the stack and link it to the last block (if there is one)
        // - It is easy to keep track of composite blocks: the first time you assign the last block variable, copy that block to a separate place. Then after everything is done, send a continuation-edge from the first block to the copied block.

        // NOTE: For each syn-block, we possibly create multible cfg-blocks:
        // - Since we process the syn-block children in reverse order, the continuation cfg-block is the one created in the first iteration. This cfg-block needs to exist by the time we process the parent cfg-block. Processing syn-blocks of the syntax in bottom-to-top order satisfies this.

       private static void analyseNesting(GraphTraversalSource g, Vertex syntaxBlock, Vertex cfgBlock) {

            List<Edge> nestsAndStmts =
                g.V(syntaxBlock)
                .outE(Dom.SEM)
                .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST),
                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.STATEMENT))
                .order().by(Dom.Sem.ORD, Order.desc)
                .toList();

            Stack<Vertex> stack = new Stack<>();
            Vertex latestContinuation = null;
            Vertex firstContinuation = null;
            for (Edge e : nestsAndStmts) {
                if(e.value(Dom.Sem.ROLE).equals(Dom.Sem.Role.Control.STATEMENT)){
                    stack.push(e.inVertex());
                } else if (e.value(Dom.Sem.ROLE).equals(Dom.Sem.Role.Control.NEST)){
                    Vertex nestedSyn = e.inVertex();
                    Vertex nestedCfg = 
                        g.V(nestedSyn)
                        .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV()
                        .next();

                    Object[] nestedReturnsCfg = 
                        g.V(nestedCfg)
                            .optional(
                                __.outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN).inV())
                            .toList().toArray();

                    if(!stack.isEmpty()){

                        Vertex newBlock = 
                            g.addV(Dom.CFG).property(Dom.Cfg.V.TYPE, Dom.Cfg.V.Type.BLOCK)
                            .sideEffect(GremlinUtils.setNodeId())
                            .as("newCfgBlock")
                            .addE(Dom.CFG).from(__.V(syntaxBlock))
                            .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
                            .sideEffect(GremlinUtils.setEdgeOrd())
                            .<Vertex>select("newCfgBlock")
                            .next();

                        // possible gremlin bug: i tried to add the edges in bulk, but for some reason this made it recreate the statement vertices 
                        while(!stack.isEmpty())
                            g.V(stack.pop())                        
                            .addE(Dom.CFG).from(__.V(newBlock))
                            .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.STATEMENT)
                            .sideEffect(GremlinUtils.setEdgeOrd())
                            .iterate();

                        if(latestContinuation != null)
                            g.V(newBlock)
                            .addE(Dom.CFG).to(__.V(latestContinuation))
                            .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                            .sideEffect(GremlinUtils.setEdgeOrd())
                            .iterate();

                        latestContinuation = newBlock;
                        if(firstContinuation == null) 
                            firstContinuation = latestContinuation;
                    }

                    if(latestContinuation != null)
                        g.V(nestedReturnsCfg)
                        .addE(Dom.CFG).to(__.V(latestContinuation))
                        .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                        .sideEffect(GremlinUtils.setEdgeOrd())
                        .iterate();

                    latestContinuation = nestedCfg;
                    if(firstContinuation == null) 
                        firstContinuation = latestContinuation;

                } else {
                    throw new IllegalStateException("unexpected role");
                }
            }

            // possible gremlin bug: i tried to add the edges in bulk, but for some reason this made it recreate the statement vertices 
            while(!stack.isEmpty())
                g.V(stack.pop())                        
                .addE(Dom.CFG).from(__.V(cfgBlock))
                .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.STATEMENT)
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();

            if(latestContinuation != null)
                g.V(cfgBlock)
                .addE(Dom.CFG).to(__.V(latestContinuation))
                .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
            
            if(firstContinuation != null)
                g.V(cfgBlock)
                .addE(Dom.CFG).to(__.V(firstContinuation))
                .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN)
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
       }


       private static void analyseConditions(GraphTraversalSource g, Vertex syntaxBlock, Vertex cfgBlock) {

            // conditionals always have "nested" blocks so we always add conditionals
            g.V(syntaxBlock).outE(Dom.SEM)
             .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                 __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH))
             .as("e")
             .inV()
                .map(__.outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
                    .order().by(Dom.Cfg.E.ORD, Order.asc).limit(1)
                    .inV())
             .as("cfgBranch")
             .optional(
                __.outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN).inV())
             .as("cfgBranchCont")

             .V(cfgBlock)
             .addE(Dom.CFG).to("cfgBranch")
             .property(Dom.Cfg.E.ROLE, 
                __.choose(__.select("e").values(Dom.Sem.ROLE))
                  .option(Dom.Sem.Role.Control.TRUE_BRANCH, __.constant(Dom.Cfg.E.Role.TRUE_FLOW))
                  .option(Dom.Sem.Role.Control.FALSE_BRANCH, __.constant(Dom.Cfg.E.Role.FALSE_FLOW)))
             .sideEffect(GremlinUtils.setEdgeOrd())

             .V(cfgBlock)
             .addE(Dom.CFG).to("cfgBranchCont")
             .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN)
             .sideEffect(GremlinUtils.setEdgeOrd())
             .iterate();
        }

        private static void analyseEntry(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM)
                 .has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY).as("e")

                 // Identify entry and exit blocks of this control.
                 .<Edge>select("e").outV().outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV().as("cfgEntry")
                 // Add edge from entry node to the cfg-block associated with the node pointed by the body-edge.
                 .<Edge>select("e").inV()
                 .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV()
                 .addE(Dom.CFG).from("cfgEntry")
                 .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .iterate();

        }

        private static void analyseExit(GraphTraversalSource g) {
            // NOTE this was originally one query. for some reason Gremlin "as" stopped working there, but  decomposition circumvented the bug.

            // Add edge to exit node from the cfg-block associated with the return node of the control.
            // Note that the return-edge always exists here (even if the body is a leaf block).
            List<Vertex> decls = 
                g.E().hasLabel(Dom.SEM)
                .has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY).outV()
                .toList();

            for (Vertex d : decls) {
                Vertex cfgExit  =
                    g.V(d)
                    .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC).inV() 
                    .outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN).inV()
                    .next();

                g.V(d)
                .outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN).inV()
                .map(__.outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ASSOC)
                    .order().by(Dom.Cfg.E.ORD, Order.desc)
                    .limit(1))
                .inV()
                .addE(Dom.CFG).to(__.V(cfgExit))
                .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
            }
        }
    }
}