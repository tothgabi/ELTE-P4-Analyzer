package p4analyser.experts.controlflow;

import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.util.function.Lambda;

import p4analyser.ontology.Dom;
import p4analyser.ontology.Status;

import org.codejargon.feather.Provides;

import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.ControlFlow;
import p4analyser.ontology.analyses.SymbolTable;
import p4analyser.ontology.analyses.SyntaxTree;

public class ControlFlowAnalysis {


    // NOTE: parsers are not structured language elements, we don't add return edges between parser states
    // NOTE: parser control flow is complicated, but it is because of the grammar
    // TODO: test on 1-way conditionals
        @Provides
        @Singleton
        @ControlFlow
        public Status analyse(GraphTraversalSource g, 
                            @SyntaxTree Status st, 
                            @AbstractSyntaxTree Status ast, 
                            @SymbolTable Status sym) {

        // // query printing
        //        File f = File. createTempFile("query", ".tex");
        //        PrintStream ps = new PrintStream(f);
        //        ControlFlowAnalysis.Control3.printQuery(ps);
        //        System.out.println("query printed to " + f.getAbsolutePath());
        //        ps.close();
        //        System.exit(0);

            System.out.println(ControlFlow.class.getSimpleName() + " started.");

            addFlowToFirstStatement(g);
            addFlowToConditionalBranches(g);
            addFlowBetweenSiblings(g);
            addFlowBetweenParserStates(g);
            addParserEntry(g);
            addParserExit(g);
            addEntryExit(g);

            System.out.println(ControlFlow.class.getSimpleName() +" complete.");
            return new Status();
        }

        // send flow from each block to its first statement (possibly another block)
        private static void addFlowToFirstStatement(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN)
             .or(__.has(Dom.Syn.V.CLASS, "BlockStatementContext"),
                 __.has(Dom.Syn.V.CLASS, "ParserBlockStatementContext"),
                 __.has(Dom.Syn.V.CLASS, "ParserStateContext"))
             .as("b")
             .map(
                __.outE(Dom.SEM).or(
                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.STATEMENT),
                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST))
                .order().by(Dom.Cfg.E.ORD, Order.asc)
                .limit(1))
             .inV()
             .addE(Dom.CFG).from("b")
             .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
             .iterate();
        }

            // send flow from each conditional to both of its branches 
        private static void addFlowToConditionalBranches(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ConditionalStatementContext").as("b")
             .outE(Dom.SEM).or(
                __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH))
             .as("e")
             .inV()
             .addE(Dom.CFG).from("b")
//             .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
             .property(Dom.Cfg.E.ROLE, 
                       __.select("e")
                         .choose(__.values(Dom.Sem.ROLE))
                         .option(Dom.Sem.Role.Control.TRUE_BRANCH, __.constant(Dom.Cfg.E.Role.TRUE_FLOW))
                         .option(Dom.Sem.Role.Control.FALSE_BRANCH, __.constant(Dom.Cfg.E.Role.FALSE_FLOW)))
             .iterate();
        }

        // for each child c, send flow from c's return point to c's sibling
        private static void addFlowBetweenSiblings(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN).or(
                __.has(Dom.Syn.V.CLASS, "BlockStatementContext"),
                __.has(Dom.Syn.V.CLASS, "ParserBlockStatementContext"))
             .local(
                __
                  .sideEffect(Lambda.consumer("{ t -> t.sideEffects(\"r\").clear() }"))
                  .outE(Dom.SEM)
                  .or(
                      __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.STATEMENT),
                      __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST))
                  .order().by(Dom.Cfg.E.ORD, Order.asc)
                  .inV()

                    // NOTE: this is in sideEffect for the depth-first effect:
                    //   each child is fully processed before we start  
                    //   processing the next one
                  .sideEffect(
                      __.as("b")
                      // NOTE: this is in sideEffect so b is aggregated even  
                      //   if r is empty. we also don't need the result.
                      .sideEffect(
                        __.flatMap(__.cap("r").unfold()) // cuts traversal if r empty
//                          .sideEffect(Lambda.consumer("{ t -> System.out.println t.get().value(\"nodeId\") }"))
                          .optional(__.outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN).inV())
                          .addE(Dom.CFG).to("b")
                          .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW))
                      .sideEffect(Lambda.consumer("{ t -> t.sideEffects(\"r\").clear() }"))
                      .aggregate("r")))
             .iterate();
        }

        // TODO "inline" into this the query from aliases (eliminate NEXT)
        @SuppressWarnings("unchecked")
        private static void addFlowBetweenParserStates(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN)
             .has(Dom.Syn.V.CLASS, "ParserStateContext").as("psc")
//           .sideEffect(Lambda.consumer("{ t -> System.out.println \"S\" }"))

            // this depends on addFlowToFirstStatement, but its easy to eliminate
             .optional(__.outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW).inV()
                         .optional(__.outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN).inV()))
             .as("body")
             .select("psc")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "transitionStatement").inV()
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "stateExpression").inV()

//             .sideEffect(Lambda.consumer("{ t -> System.out.println \"A\" }"))
             .coalesce(

                // if stateExpression has a name, just resolve it to the next state
                __.outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                  .repeat(__.out(Dom.SYN))
                  .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
//                  .sideEffect(Lambda.consumer("{ t -> System.out.println \"B1\" }"))
                  .inE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).outV()
//                  .sideEffect(Lambda.consumer("{ t -> System.out.println \"B2\" }"))
                  .addE(Dom.CFG).from("body")
                  .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                  .inV(),

                // if stateExpression has a select expression, 
                //    resolve all names in the branches to their next state 
                __.outE(Dom.SYN).has(Dom.Syn.E.RULE, "selectExpression").inV()
                  .as("sel")
 //                 .sideEffect(Lambda.consumer("{ t -> System.out.println \"C1\" }"))
                  .addE(Dom.CFG).from("body")
                  .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)

                  // find the cases and add a flow
                  .<Vertex>select("sel")
                  .repeat(__.out(Dom.SYN))
                  .until(__.has(Dom.Syn.V.CLASS, "SelectCaseContext")) 
                  .as("case")
                  .addE(Dom.CFG).from("sel")
                  .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)

                  // resolve the name in the case to its next state
                  .select("case")
                  .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                  .repeat(__.out(Dom.SYN))
                  .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                  .inE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).outV()
//                  .sideEffect(Lambda.consumer("{ t -> System.out.println \"C2\" }"))
                  .addE(Dom.CFG).from("case")
                  .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW)
                  .inV())
             .iterate();
            
        }

        // TODO "inline" into this the query from aliases (eliminate START)
        private static void addParserEntry(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN)
             .has(Dom.Syn.V.CLASS, "ParserDeclarationContext")
             .as("pdc")
             .outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.START).inV()
             .addE(Dom.CFG).from("pdc")
             .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ENTRY)
             .iterate();
        }

        @SuppressWarnings("unchecked")
        private static void addParserExit(GraphTraversalSource g) {
            // find all parser state references that are either "accept" or "reject"

            // this query handles simple transitions
            g.V().hasLabel(Dom.SYN)

             // find final states
             .has(Dom.Syn.V.CLASS, "StateExpressionContext").as("sec")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
             .repeat(__.out(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
             .filter(__.values("value").is(P.within("accept", "reject")))

             .<Vertex>select("sec")
             .repeat(__.in(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "ParserStateContext"))

              // select the return point and add the edge
              // this depends on addFlowToFirstStatement, but its easy to eliminate
             .optional(__.outE(Dom.CFG).has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW).inV()
                         .optional(__.outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN).inV()))
             .as("ret")
             .<Vertex>select("sec")
             .repeat(__.in(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"))
             .addE(Dom.CFG).to("ret")
             .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN)
             .iterate();

            // this query handles selects
            g.V().hasLabel(Dom.SYN)
             .has(Dom.Syn.V.CLASS, "StateExpressionContext")

             .repeat(__.out(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "SelectCaseContext"))
             .as("scc")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()

             .repeat(__.out(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
             .filter(__.values("value").is(P.within("accept", "reject")))

             // go up until you find the top-level declaration
             .repeat(__.in(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"))
             .addE(Dom.CFG).to("scc")
             .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN)
             .iterate();

        }

        // add entry and exit edges to declaration
        private static void addEntryExit(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN).or(
                    __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                    __.has(Dom.Syn.V.CLASS, "ActionDeclarationContext"))
                .as("cdc")
                .outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY).inV()
                .addE(Dom.CFG).from("cdc")
                .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ENTRY)
                .select("cdc")
                .outE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN).inV()
                .addE(Dom.CFG).from("cdc")
                .property(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN)
                .iterate();
        }

}