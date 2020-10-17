package parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.Operator;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalOptionParent.Pick;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.util.function.Lambda;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;

import p4analyser.ontology.Dom;

// TODO idea: method that execute queries should be named questions (whoUsesDeclaredVariable)
// TODO another idea: method should be named based on the edge it adds (but what about complex methods)
// TODO idea: documentation of each query should describe a precondition (what structures are traversed) and a postcondition (how is the syntax tree modified)
public class SemanticAnalysis {
    
    public static void analyse(GraphTraversalSource g){
        Parser.analyse(g);
        Control.analyse(g);
        Instantiation.analyse(g);

        Symbol.analyse(g);

        CallGraph.analyse(g);
        CallSites.analyse(g);

    }

    private static class Parser {
        private static void analyse(GraphTraversalSource g) {
            findParsers(g);
            findParserNames(g);
            findStates(g);
            findStateNames(g);
            findTransitions(g);
        }

        private static void findParsers(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ParserDeclarationContext")
            .addE(Dom.SEM).from(g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.NODE_ID, 0))
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).property(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER)
            
            .iterate();
        }

        // TODO this is redundant. just use Symbol.DECLARES_NAME
        private static void findParserNames(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER).inV()
                .as("parserRoot")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserTypeDeclaration").inV()
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                .addE(Dom.SEM).from("parserRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME)
                
                .iterate();
        }


        // TODO until -> emit
        private static void findStates(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER).inV()
                .as("parserRoot")
                .repeat(__.out(Dom.SYN))
                .until(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserStates").count().is(0))
                .emit(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserState"))
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserState").inV()
                .addE(Dom.SEM).from("parserRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE)
                
                .iterate();
        }

        private static void findStateNames(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
            .as("stateRoot")
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name")
            .inV()
            .repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .addE(Dom.SEM).from("stateRoot")
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME)
            
            .iterate();
        }

        private static void findTransitions(GraphTraversalSource g) {
            findTransitionNode(g);
            findTransitionTargetName(g);
            findTransitionSelectCase(g);
            findTransitionSelectHead(g);
            findTransitionSelectCaseName(g);
            findStartState(g);
            findNextState(g);
            findStatements(g);

        }


        private static void findTransitionSelectCaseName(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.CASE).inV()
                .as("caseRoot")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                .addE(Dom.SEM).from("caseRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER)
                .property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME)
                
                .iterate();
        }

        private static void findTransitionSelectHead(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.TRANSITION).inV()
                .as("transitionRoot")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "selectExpression").inV() 
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "expressionList").inV() 
                .addE(Dom.SEM)
                .from("transitionRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.HEAD)
                
                .iterate();
        }

        // TODO until -> emit
        private static void findTransitionSelectCase(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.TRANSITION).inV()
                .as("transitionRoot")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "selectExpression").inV() 
                .repeat(__.out(Dom.SYN))
                .until(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "selectCaseList").count().is(0))
                .emit(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "selectCase"))
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "selectCase").inV()
                .addE(Dom.SEM).from("transitionRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.CASE)
                
                .iterate();
        }

        private static void findTransitionTargetName(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.TRANSITION).inV()
                .as("transitionRoot")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                .addE(Dom.SEM).from("transitionRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME)
                
                .iterate();
        }

        private static void findTransitionNode(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
            .as("stateRoot")
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "transitionStatement").inV()
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "stateExpression").inV()
            .addE(Dom.SEM).from("stateRoot")
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.TRANSITION)
            
            .iterate();
        }

        private static void findStartState(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER).inV()
            .as("parserRoot")
            .outE(Dom.SEM).property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
            .filter(__.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME).inV()
                      .has(Dom.Syn.V.VALUE, "start"))
            .addE(Dom.SEM).from("parserRoot")
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.START)
            
            .iterate();
        }

        @SuppressWarnings("unchecked")
        private static void findNextState(GraphTraversalSource g) {
            List<Map<String,Vertex>> statesAndNextNames = 
                g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
                .as("sourceState")
                .outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.TRANSITION).inV()
                .union(
                    __.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE,Dom.Sem.Role.Parser.NAME).inV(),
                    __.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE,Dom.Sem.Role.Parser.CASE).inV()
                      .outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE,Dom.Sem.Role.Parser.NAME).inV())
                .as("nextName")
                .<Vertex>select("sourceState", "nextName")
                .toList();

            for (Map<String,Vertex> sn : statesAndNextNames) {
                Vertex state = sn.get("sourceState");
                Vertex nextNameV = sn.get("nextName");
                String nextName = (String) g.V(nextNameV).values(Dom.Syn.V.VALUE).next();

                if(nextName.equals("accept") || nextName.equals("reject")){

                    g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER).inV()
                    .addE(Dom.SEM).to(__.V(state))
                    .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.FINAL)
                    
                    .iterate();

                } else {

                    g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
                    .filter(__.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME).inV()
                            //    .sideEffect(t -> t.get().value(Dom.Syn.V.CLASS))
                                .has(Dom.Syn.V.VALUE, nextName))
                    .addE(Dom.SEM).from(__.V(state))
                    .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NEXT)
                    
                    .iterate();
                }
            }
        }

        // TODO until -> emit
        private static void findStatements(GraphTraversalSource g){

            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
            .as("synState")
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserStatements").inV()
            .repeat(__.out())
            .until(__.has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext").or()
                    .has(Dom.Syn.V.CLASS, "DirectApplicationContext").or()
                    .has(Dom.Syn.V.CLASS, "ConstantDeclarationContext").or()
                    .has(Dom.Syn.V.CLASS, "VariableDeclarationContext"))
            .addE(Dom.SEM).from("synState")
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATEMENT)
            
            .iterate();

        }
    }

    private static class Control {
        private static void analyse(GraphTraversalSource g) {
            findControl(g);
            findControlName(g);
            findControlBody(g);
            findBlockStatements(g);
            findConditionalBranches(g);
            findLastStatements(g);
            findReturnStatements(g);
        }

        // NOTE this is almost equivalent to the parser
        private static void findControl(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ControlDeclarationContext")
            .addE(Dom.SEM).from(g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.NODE_ID, 0))
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).property(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL)
            
            .iterate();
        }

        // TODO this is redundant. just use Symbol.DECLARES_NAME
        // NOTE this is almost equivalent to the parser
        private static void findControlName(GraphTraversalSource g) {

            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL).inV()
                .as("controlRoot")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "controlTypeDeclaration").inV()
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                .addE(Dom.SEM).from("controlRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL).property(Dom.Sem.ROLE, Dom.Sem.Role.Control.NAME)
                
                .iterate();
        }

        private static void findControlBody(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM)
             .has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL).inV()
             .addE(Dom.SEM).to(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "controlBody").inV().out(Dom.SYN))
             .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL).property(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY)
             
             .iterate();
        }

        private static void findBlockStatements(GraphTraversalSource g) {
        // Note: 
        // - The syntax tree has represents linked lists in reverse-order: the head is the leaf.
        // - Gremlin has no reverse operation. It can be simulated using fold() and Collections.reverse, but then path information (incl. names) is lost.
            List<Map<String, Vertex>> ms = 
                g.E().hasLabel(Dom.SEM)
                .has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL).inV()
                .repeat(__.out(Dom.SYN))
                .emit(__.has(Dom.Syn.V.CLASS, "BlockStatementContext"))
                .as("blockRoot")
                .repeat(__.out())
                .until(__.has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext").or()
                        .has(Dom.Syn.V.CLASS, "DirectApplicationContext").or()
                        .has(Dom.Syn.V.CLASS, "ConditionalStatementContext").or()
                        .has(Dom.Syn.V.CLASS, "BlockStatementContext").or()
                        .has(Dom.Syn.V.CLASS, "EmptyStatement").or()
                        .has(Dom.Syn.V.CLASS, "ExitStatement").or()
                        .has(Dom.Syn.V.CLASS, "ReturnStatement").or()
                        .has(Dom.Syn.V.CLASS, "SwitchStatement"))
                .as("statement")
                .<Vertex>select("blockRoot", "statement")
                .toList();

            Collections.reverse(ms);
            for (Map<String,Vertex> m : ms) {
                Vertex blockRoot = m.get("blockRoot");
                Vertex statement = m.get("statement");
                
                g.V(statement).choose(__.values(Dom.Syn.V.CLASS))
                .option("BlockStatementContext",
                    __.addE(Dom.SEM).from(__.V(blockRoot))
                    .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST))
                .option("ConditionalStatementContext",
                    __.addE(Dom.SEM).from(__.V(blockRoot))
                    .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST))
                .option(Pick.none,
                    __.addE(Dom.SEM).from(__.V(blockRoot))
                    .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.STATEMENT))
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
                
                .iterate();
            }
        }


        @SuppressWarnings("unchecked")
        private static void findConditionalBranches(GraphTraversalSource g) {

            g.E().hasLabel(Dom.SEM)
             .has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL).inV()
             .repeat(__.out(Dom.SYN))
             .emit(__.has(Dom.Syn.V.CLASS, "ConditionalStatementContext"))
             .as("cond")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "statement")
             .order().by(Dom.Syn.E.ORD)
             .inV().out(Dom.SYN)
             .<Vertex>union(
                 __.<Vertex>limit(1)
                    .addE(Dom.SEM).from("cond")
                    .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
                    .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH)
                    .inV(),
                __.<Vertex>skip(1)
                    .addE(Dom.SEM).from("cond")
                    .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
                    .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH)
                    .inV())
             .iterate();
        }

        // Sends a 'last' edge from each 'block statement' node to its last nested node.
        // This will be either a block, or a conditional.
        private static void findLastStatements(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM)
             .has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL).inV()
             .repeat(__.out(Dom.SYN))
             .emit(__.has(Dom.Syn.V.CLASS, "BlockStatementContext"))
             .as("block")
             .local(
                __.outE(Dom.SEM)
                // .has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST)
                  .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.STATEMENT),
                      __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST))
             .order().by(Dom.Sem.ORD, Order.desc)
             .limit(1)
             .inV()
             .addE(Dom.SEM).from("block"))
             .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
             .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.LAST)
             
             .iterate();
        }

        // For each block that nests other blocks:
        // Finds all those blocks of a control definition 
        // that can be the last block of that control.
        // Note that there can be multiple potential last blocks because of 
        // conditionals.
        // This is a transitive closure of 'body', 'trueBranch', 'falseBranch',
        // and those 'last' edges that point to the nested block
        // note: 'last' denotes the last position, so last can point to statements as well, but return only points to last blocks. (this way return is always a continuation, and can be used in control flow analysis.) 
        // IMPROVEMENT: not counting conditionals, this is now polynomial time but it could be linearized if higher nodes reused the return statements of their last-nodes.
        private static void findReturnStatements(GraphTraversalSource g) {

                g.E().hasLabel(Dom.SEM)
                 .or(__.has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP)
                       .has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL),
                     __.has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
                      .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY),
                          __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                          __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH),
                          __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST)))
                .inV().as("controlRoot") 

                .repeat(__.outE(Dom.SEM)
                          .or(__.has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP)
                                .has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL),
                              __.has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
                                .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY),
                                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH),
                                    __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.LAST).inV().inE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST)))
                        .inV())
                .until(__.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
                        .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY),
                            __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                            __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH),
                            __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.LAST)
                              .inV().inE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NEST))
                        .count().is(0))
                .addE(Dom.SEM).from("controlRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
                .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN)
                
                .iterate();

        }
    }

    // TODO this is technically just a function call (to an extern). name and type resolution belongs to Symbol.
    private static class Instantiation {

        private static void analyse(GraphTraversalSource g) {

            findInstantiation(g);
            findTypeRefName(g);
            findName(g);
            findArguments(g);
            findInvokedControls(g);
        }

        private static void findInstantiation(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "InstantiationContext")
            .addE(Dom.SEM).from(g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.NODE_ID, 0))
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).property(Dom.Sem.ROLE, Dom.Sem.Role.Top.INSTANTIATION)
            
            .iterate();
        }

        private static void findTypeRefName(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.INSTANTIATION).inV()
             .as("insta")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "typeRef").inV()
             .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
             .addE(Dom.SEM).from("insta")
             
             .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION)
             .property(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.TYPE)
             .iterate();
        }


        private static void findName(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.INSTANTIATION).inV()
             .as("insta")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
             .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
             .addE(Dom.SEM).from("insta")
             
             .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION)
             .property(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.NAME)
             .iterate();
        }

        private static void findArguments(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.INSTANTIATION).inV()
                .as("insta")
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "ArgumentContext"))
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "expression").inV()
                .addE(Dom.SEM).from("insta")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION).property(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.ARGUMENT)
                
                .iterate();
        }

        private static void findInvokedControls(GraphTraversalSource g) {
            List<Map<String, Vertex>> invoked = 
                g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION).has(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.ARGUMENT).inV()
                .as("arg")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "expression").inV()
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                .as("name")
                .<Vertex>select("arg", "name")
                .toList();

            for (Map<String,Vertex> m : invoked) {
                Object valueOfName = g.V(m.get("name")).values(Dom.Syn.V.VALUE).next();

                g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.NODE_ID, 0).outE(Dom.SEM)
                 .or(__.has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.PARSER),
                     __.has(Dom.Sem.DOMAIN, Dom.Sem.Domain.TOP).has(Dom.Sem.ROLE, Dom.Sem.Role.Top.CONTROL))
                 .inV()
                 .filter(__.outE(Dom.SEM)
                           .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME),
                              __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NAME))
                           .inV().has(Dom.Syn.V.VALUE, P.eq(valueOfName)))
                 .addE(Dom.SEM).from(__.V(m.get("arg")))
                 .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION).property(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.INVOKES)
                 
                 .iterate();
            }
        }
    }

    public static class Symbol {

        // NOTE syntax maybe too permissive with expressions: (~ (13 >> true) . etherType) is a syntactically valid expression, even though '~', '>>', and '.' are reserved tokens. for this reason I decided to handle case-by-case

        public static void analyse(GraphTraversalSource g){
            resolveNames(g);
            resolveTypeRefs(g);
            localScope(g);
            parameterScope(g);
            fieldAndMethodScope(g);
            actionRefs(g);
            tableApps(g);
            packageInstantiations(g);
            controlAndParserInstantiations(g);
        }

        public static void resolveNames(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN)
            .or(__.has(Dom.Syn.V.CLASS, "HeaderTypeDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "ExternDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "StructTypeDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "StructFieldContext"),
                __.has(Dom.Syn.V.CLASS, "VariableDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "ConstantDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "TableDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "FunctionPrototypeContext"),
                __.has(Dom.Syn.V.CLASS, "ActionDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "TableDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "PackageTypeDeclarationContext"),
                __.has(Dom.Syn.V.CLASS, "ParameterContext"))
            .as("root")
            .optional(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserTypeDeclaration").inV())
            .optional(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "controlTypeDeclaration").inV())
            .outE(Dom.SYN)
            .or(__.has(Dom.Syn.E.RULE, "name"),
                __.has(Dom.Syn.E.RULE, "nonTypeName"))
            .inV()
            .repeat(__.out())
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .addE(Dom.SYMBOL).from("root")
            .property(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME)
            
            .iterate();

        }

        // TODO typeRefs can be prefixed
        public static void resolveTypeRefs(GraphTraversalSource g){
            g.E().hasLabel(Dom.SYN).has(Dom.Syn.E.RULE, "typeRef").as("e")
            .outV().as("typedExpr")
            .select("e")
            .inV().outE(Dom.SYN).has(Dom.Syn.E.RULE, "typeName").inV() 

            .repeat(__.out())
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))

            .addE(Dom.SYMBOL).from("typedExpr")
            .property(Dom.Symbol.ROLE, Dom.Symbol.Role.HAS_TYPE)
            
            .iterate();

            g.E().hasLabel(Dom.SYMBOL)
             .has(Dom.Symbol.ROLE, Dom.Symbol.Role.HAS_TYPE).inV()
             .as("typeNameNode")
             .values("value").as("typeName")

             .V().hasLabel(Dom.SYN)
             .or(__.has(Dom.Syn.V.CLASS, "HeaderTypeDeclarationContext"),
                 __.has(Dom.Syn.V.CLASS, "ExternDeclarationContext"),
                 __.has(Dom.Syn.V.CLASS, "VariableDeclarationContext"),
                 __.has(Dom.Syn.V.CLASS, "ConstantDeclarationContext"),
                 __.has(Dom.Syn.V.CLASS, "StructTypeDeclarationContext")) 

             .filter(__.outE(Dom.SYMBOL).has(Dom.Sem.ROLE, Dom.Symbol.Role.DECLARES_NAME)
                       .inV().values("value").where(P.eq("typeName")))
             .addE(Dom.SYMBOL).to("typeNameNode")
             .property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES)
             
             .iterate();
        }


        // TODO prefixed names can introduce bugs
        // - e.g. local declaration of 'x' will scope struct fields names 'x' (in case they are used)
        // - not sure, but probably prefixed names can be omitted altogether 
        public static void localScope(GraphTraversalSource g){
            // inside a block, all statements to the right of the declaration are in the scope (until the end of the block)

            // select variable or constant declarations and their names
            g.V().hasLabel(Dom.SYN)
             .or(__.has(Dom.Syn.V.CLASS, "VariableDeclarationContext"),
                 __.has(Dom.Syn.V.CLASS, "ConstantDeclarationContext"))
             .as("decl")
             .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME).inV()
             .values("value")
             .as("declaredName")

            // select matching terminals inside the block after the declaration
            // NOTE: the syntax tree contains the statements list reversed (rightmost in code is topmost in tree)
            // - go up until the list-node of the declaration (to omit it for collection)
             .<Vertex>select("decl")
             .repeat(__.in(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "StatOrDeclListContext"))

             // - keep going up and collect the list-nodes
             .repeat(__.in(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "BlockStatementContext"))
             .emit(__.has(Dom.Syn.V.CLASS, "StatOrDeclListContext"))
             .outE().has(Dom.Syn.E.RULE, "statementOrDeclaration").inV()

             // - collect matching terminals under each list-node subtree
             .repeat(__.out(Dom.SYN))
             .emit(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")
                     .values("value")
                     .where(P.eq("declaredName")))
             .dedup()

             .addE(Dom.SYMBOL).from("decl")
             .property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES)
             
             .iterate();
        }

        // TODO is there variable covering? (e.g. action parameters cover control parameters?)
        // - if yes, start adding edges from the bottom, and don't add new edges to those who already have one
        public static void parameterScope(GraphTraversalSource g){
            // find parameters
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ParameterContext")
             .as("decl")
             .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME).inV()
             .values("value")
             .as("declaredName")

             .<Vertex>select("decl")

             // go up in the tree to find the procedure that owns the parameter
             .repeat(__.in(Dom.SYN))
             .until(__.or(__.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"),
                          __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                          __.has(Dom.Syn.V.CLASS, "ActionDeclarationContext")))

             // go down into the procedure body (or bodies, in case of parsers)
             .outE(Dom.SYN)
             .or(__.has(Dom.Syn.E.RULE, "parserLocalElements"),
                 __.has(Dom.Syn.E.RULE, "parserStates"),
                 __.has(Dom.Syn.E.RULE, "controlLocalDeclarations"),
                 __.has(Dom.Syn.E.RULE, "controlBody"),
                 __.has(Dom.Syn.E.RULE, "blockStatement")) // action
             .inV()

             // find all terminals that refer to the name declared by the parameter
             .repeat(__.out(Dom.SYN))
             .emit(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")
                     .values("value")
                     .where(P.eq("declaredName")))
             .dedup()

             .addE(Dom.SYMBOL).from("decl")
             .property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES)
             
             .iterate();
        }

        // TODO eliminate Lambda.function
        // NOTE this handles both lvalues and expressions that refer to struct fields and extern methods
        // NOTE does not handle expressions referring to parsers, actions, controls 
        @SuppressWarnings("unchecked")
        public static void fieldAndMethodScope(GraphTraversalSource g){
        // NOTE: possible gremlin bug: this was originally one query, but for some reason a select kept losing a variable

            List<Map<String, Object>> lvArities =
                g.V().hasLabel(Dom.SYN)
                    // select top-most lvalue elements (i.e. those whose lvalue parent has no lvalue parent)
                    .or(__.has(Dom.Syn.V.CLASS, "LvalueContext"),
                        __.has(Dom.Syn.V.CLASS, "ExpressionContext").outE(Dom.SYN).has(Dom.Syn.E.RULE, "DOT"),
                        __.has(Dom.Syn.V.CLASS, "ExpressionContext").outE(Dom.SYN).has(Dom.Syn.E.RULE, "argumentList"))
                    .filter(__.or(__.inE(Dom.SYN).has(Dom.Syn.E.RULE, "lvalue").outV()
                                    .inE(Dom.SYN).has(Dom.Syn.E.RULE, "lvalue"),
                                   __.inE(Dom.SYN).has(Dom.Syn.E.RULE, "expression").outV()
                                    .inE(Dom.SYN).has(Dom.Syn.E.RULE, "expression")
                                    )
                              .count().is(0))
                    .as("lv")

                    // in case this is a method call, find out the arity (otherwise this will return 0)
                    .map(__.inE(Dom.SYN)
                           .or(__.has(Dom.Syn.E.RULE, "lvalue"), 
                               __.has(Dom.Syn.E.RULE, "expression")).outV()
                           .outE(Dom.SYN).has(Dom.Syn.E.RULE, "argumentList").inV()
                           .repeat(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "nonEmptyArgList").inV())
                           .emit()
                           .count())
//                    .map(t -> (Long) t.get())
                    .as("arity")
                    .select("lv", "arity")
                    .toList();

            // process the lvalue chains
            for(Map<String, Object> lvArity : lvArities){
                Vertex lv = (Vertex) lvArity.get("lv");
                Long arity = (Long) lvArity.get("arity");

                // collect each element in the chain. reverse the chain. 
                g.V(lv)

                .emit()
                .repeat(__.outE(Dom.SYN)
                          .or(__.has(Dom.Syn.E.RULE, "lvalue"),
                              __.has(Dom.Syn.E.RULE, "expression"))
                          .inV())

//                .fold().map(t -> { List<Vertex> vs = t.get(); Collections.reverse(vs); return vs;}).unfold()
                .fold().map(Lambda.function("{t ->  List<Vertex> vs = t.get() \n Collections.reverse(vs) \n return vs \n }")).unfold()

                // for lvalues: the first (or the only) element is always "prefixedNonTypeName", the rest are "name"
                // for expressions, the first is 'nonTypeName'
                .outE(Dom.SYN).or(__.has(Dom.Syn.E.RULE, "prefixedNonTypeName"), 
                                  __.has(Dom.Syn.E.RULE, "nonTypeName"), 
                                  __.has(Dom.Syn.E.RULE, "name")).inV()
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                .<Vertex>coalesce(

                    // if an element already has a scope, set the current context to the enclosing type of the declaration 
                    // - e.g. in "hdr.ipv4.ttl" the hdr can be scoped by paramater, we need its type to resolve which field scopes ipv4
                    __.inE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).outV()
                       .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.HAS_TYPE).inV()
                       .inE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).outV()
                       .aggregate("currentType"),

                    // otherwise, resolve the scope (using the current context or the defaults), add an edge, and set the current context to the type of the declaration
                    // - e.g. to process "ipv4" in "hdr.ipv4.ttl", we will search for the field with a matching name among the fields of whatever type "hdr" was
                    // - e.g. in "mark_to_drop()" there is only one element and it is probably not scoped yet. we have to search for its name among the extern functions.
                    __.<Vertex>identity()
                        // store the name in use
                        .as("useNode")

//                        .sideEffect(t -> System.out.println(t.get().value("value").toString() + "/" + arity))
                        .values("value")
                        .as("useName")

                        // load the current context (struct, extern), or search among global extern functions
                        .coalesce(
                            __.flatMap(__.cap("currentType").<Vertex>unfold()), // unfold loses the name, but flatmap prevents it
                            __.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ExternDeclarationContext")
                                .filter(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "functionPrototype")))

                        // find the field and method declaration that declares the name (and has the right arity). add the use into the scope of the declaration.
                        .repeat(__.out(Dom.SYN))
                        .until(
                            __.or(__.has(Dom.Syn.V.CLASS, "StructFieldContext"),
                                  __.has(Dom.Syn.V.CLASS, "FunctionPrototypeContext"))
                                .as("declaration")
                                // match name
                                .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME).inV()
                                .values("value")
                                .where(P.eq("useName"))
//                                .sideEffect(t -> System.out.println("- name match"))

                                // match arity
                                .select("declaration")
                                .coalesce(
                                    __.outE(Dom.SYN).has(Dom.Syn.E.RULE, "parameterList").inV()
                                      .map(__.repeat(__.outE(Dom.SYN)
                                             .has(Dom.Syn.E.RULE, "nonEmptyParameterList").inV())
                                             .emit()
                                             .count()),
                                    __.constant(0L))
                                .is(P.eq(arity))
//                                .sideEffect(t -> System.out.println("- arity match"))
                                )

                        .sideEffect(
                            __.addE(Dom.SYMBOL).to("useNode")
                                .property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES)
                                )

                        // in case the type of the declaration was found before, make the type declaration the current context.
                        .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.HAS_TYPE).inV()
                        .inE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).outV()

//                        .sideEffect(t -> { ((BulkSet<Vertex>) t.sideEffects("currentType")).clear(); } )
                        .sideEffect(Lambda.consumer("{ t -> t.sideEffects(\"currentType\").clear() }"))

                        .aggregate("currentType"))

                .iterate();
            }
        }

        // TODO is it legal to refer to action in other namespaces?
        public static void actionRefs(GraphTraversalSource g){
            // from all table declarations
            g.V().hasLabel(Dom.SYN)
             .has(Dom.Syn.V.CLASS, "TableDeclarationContext")
             .repeat(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "tablePropertyList").inV())
             .emit()
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "tableProperty").inV()

            // select the name of each action action refs
             .repeat(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "actionList").inV())
             .emit()
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "actionRef").inV()
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()

             .repeat(__.out(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
             .as("actRef")
             .values("value").as("actRefName")

            // select those action declarations that declare the name of the currently selected action refs
             .V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ActionDeclarationContext").as("decl")
             .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME).inV()
             .values("value")
             .where(P.eq("actRefName"))

             .addE(Dom.SYMBOL).from("decl").to("actRef")
             .property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES)
             
             .iterate();
        }

        // TODO can table names be in other namespaces?
        public static void tableApps(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN)
             // select all table applications
             .has(Dom.Syn.V.CLASS, "DirectApplicationContext")

             // store the node and name of the applied table
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "typeName").inV()
             .repeat(__.out(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).as("tableRef")
             .values("value").as("tableRefName")

             // go up to the control declaration that contains the application
             .<Vertex>select("tableRef")
             .repeat(__.in(Dom.SYN))
             .until(__.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"))

             // find the table declaration that declares the name of the applied table
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "controlLocalDeclarations").inV()
             .emit()
             .repeat(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "controlLocalDeclarations").inV())
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "controlLocalDeclaration").inV()
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "tableDeclaration").inV().as("decl")

             .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME).inV()
             .values("value")
             .where(P.eq("tableRefName"))

             // add edge from declaration to the name node 
             .addE(Dom.SYMBOL).from("decl").to("tableRef")
             .property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES)
             
             .iterate();
        }

        public static void packageInstantiations(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN)
                 .has(Dom.Syn.V.CLASS, "InstantiationContext")
                 .repeat(__.out(Dom.SYN))
                 .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).as("pkgNode")
                 .values("value").as("pkgName")

                 .sideEffect(
                    __.V().hasLabel(Dom.SYN)
                      .has(Dom.Syn.V.CLASS, "PackageTypeDeclarationContext")
                      .filter(__.outE(Dom.SYMBOL)
                                .has(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME)
                                .inV()
                                .values("value")
                                .where(P.eq("pkgName"))) 
                      .addE(Dom.SYMBOL).to("pkgNode")
                      .property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES)
                      )
                .iterate();
        }
        
        public static void controlAndParserInstantiations(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN)
                 .has(Dom.Syn.V.CLASS, "InstantiationContext")
                // find the argument-instantiations of the package instantiation
                 .repeat(__.out(Dom.SYN))
                 .until(__.has(Dom.Syn.V.CLASS, "ArgumentContext"))
                 .outE(Dom.SYN).has(Dom.Syn.E.RULE, "expression").inV()
                 .outE(Dom.SYN).has(Dom.Syn.E.RULE, "expression").inV()
                 .repeat(__.out(Dom.SYN))
                 .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).as("argNode")
                 .values("value").as("argName")

                // find controls and parser that declare the same name
                 .sideEffect(
                    __.V().hasLabel(Dom.SYN)
                      .or(__.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                          __.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"))
                      .filter(__.outE(Dom.SYMBOL)
                                .has(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME)
                                .inV()
                                .values("value")
                                .where(P.eq("argName"))) 
                      .addE(Dom.SYMBOL).to("argNode")
                      .property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES)
                      )
                .iterate();
                      
        }
    }

    public static class CallGraph {
        public static void analyse(GraphTraversalSource g){
            whoCallsAction(g);
            whoCallsTable(g);
            whoCallsFunctionPrototype(g);
            whoCallsParserState(g);
            whoInvokesParsersAndControls(g);
        }
        public static void whoCallsTable(GraphTraversalSource g){
             g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "TableDeclarationContext").as("decl")
                  .flatMap(
                    __.outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).inV()
                    .repeat(__.in(Dom.SYN))
                    .until(__.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"))
                    .dedup())
                  .addE(Dom.CALL).to("decl")
                  .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
                  
                  .iterate();
        }

        public static void whoCallsAction(GraphTraversalSource g){
             g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ActionDeclarationContext").as("decl")
             .flatMap(
                __.outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).inV()
                .repeat(__.in(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "TableDeclarationContext"))
                .dedup())
            .addE(Dom.CALL).to("decl")
            .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
            
            .iterate();
        }

        public static void whoCallsFunctionPrototype(GraphTraversalSource g){
             g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "FunctionPrototypeContext").as("decl")
             .flatMap(
                __.outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).inV()
                .repeat(__.in(Dom.SYN))
                .until(
                    __.or(
                        __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                        __.has(Dom.Syn.V.CLASS, "ParserStateContext"),
                        __.has(Dom.Syn.V.CLASS, "ActionDeclarationContext")))
                .dedup())
            .addE(Dom.CALL).to("decl")
            .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
            
            .iterate();
        }

        public static void whoCallsParserState(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.CLASS, "ParserStateContext").as("st")
             .flatMap(
                __.repeat(__.in(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"))
                .dedup())
             .addE(Dom.CALL).to("st")
             .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
             
             .iterate();
        }

        // TODO this is technically wrong. first, they are "instantiated" by the top-level, and are passed to InstantiationContext which in turn is also an instantiation by the top-level. The functions are invoked by PackageTypeDeclaration which is an extern. 
        public static void whoInvokesParsersAndControls(GraphTraversalSource g){
            g.V().hasLabel(Dom.SYN)
                 .or(__.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                     __.has(Dom.Syn.V.CLASS, "ParserDeclarationContext")).as("invokee")
                 .flatMap(
                    __.inE(Dom.SEM).has(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.INVOKES).outV()
                    .repeat(__.in(Dom.SYN))
                    .until(__.has(Dom.Syn.V.CLASS, "InstantiationContext"))
                    .dedup())
                 .addE(Dom.CALL).to("invokee")
                 .property(Dom.Call.ROLE, Dom.Call.Role.CALLS)
                 
                 .iterate();

        }
    }
    
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

 // not sure if useful
//    public static class Structure {
//        public static void analyse(GraphTraversalSource g){
//
//
//        }
//        public static void controlTables(GraphTraversalSource g){
//            g.V().hasLabel(Dom.SYN)
//             .has(Dom.Syn.V.CLASS, "ControlDeclarationContext").as("ctl")
//             .repeat(__.outE(Dom.SYN)
//                       .has(Dom.Syn.E.RULE, "controlLocalDeclarations").inV())
//             .emit(__.has(Dom.Syn.V.CLASS,"TableDeclarationContext"))
//             .addE(Dom.STRUCT).from("ctl")
//             .property(Dom.Struct.ROLE, Dom.Struct.Role.TABLE)
//             .sideEffect(GremlinUtils.setEdgeOrd())
//             .iterate();
//        }
//        public static void controlActions(GraphTraversalSource g){
//            g.V().hasLabel(Dom.SYN)
//             .has(Dom.Syn.V.CLASS, "ControlDeclarationContext").as("ctl")
//             .repeat(__.outE(Dom.SYN)
//                       .has(Dom.Syn.E.RULE, "controlLocalDeclarations").inV())
//             .emit(__.has(Dom.Syn.V.CLASS,"ActionDeclarationContext"))
//             .addE(Dom.STRUCT).from("ctl")
//             .property(Dom.Struct.ROLE, Dom.Struct.Role.ACTION)
//             .sideEffect(GremlinUtils.setEdgeOrd())
//             .iterate();
//        }
//    }

}