package p4analyser.experts.aliases;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalOptionParent.Pick;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.codejargon.feather.Provides;

import p4analyser.ontology.Dom;
import p4analyser.ontology.Status;
import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.SyntaxTree;

/**
 * Hello world!
 *
 */
// TODO rename this to abstract syntax tree
public class Aliases {

    // TODO idea: method that execute queries should be named questions (whoUsesDeclaredVariable)
    // TODO another idea: method should be named based on the edge it adds (but what about complex methods)
    // TODO idea: documentation of each query should describe a precondition (what structures are traversed) and a postcondition (how is the syntax tree modified)
    
    @Provides
    @Singleton
    @AbstractSyntaxTree
    public Status analyse(GraphTraversalSource g, @SyntaxTree Status s){
        System.out.println(AbstractSyntaxTree.class.getSimpleName() +" started.");

        Parser.analyse(g);
        Control.analyse(g);
        Instantiation.analyse(g);

        System.out.println(AbstractSyntaxTree.class.getSimpleName() +" complete.");
        return new Status();
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
//            findStatements(g);

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

        // I included this in Control. Delete this if that works.
        // TODO until -> emit
//        private static void findStatements(GraphTraversalSource g){
//
//            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
//            .as("synState")
//            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserStatements").inV()
//            .repeat(__.out())
//            .until(__.has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext").or()
//                    .has(Dom.Syn.V.CLASS, "DirectApplicationContext").or()
//                    .has(Dom.Syn.V.CLASS, "ConstantDeclarationContext").or()
//                    .has(Dom.Syn.V.CLASS, "VariableDeclarationContext"))
//            .addE(Dom.SEM).from("synState")
//            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATEMENT)
//            
//            .iterate();
//
//        }
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

        // NOTE: this now handles actions as well
        private static void findControlBody(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN)
             .has(Dom.Syn.V.CLASS, "ControlDeclarationContext")
             .addE(Dom.SEM).to(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "controlBody").inV().out(Dom.SYN))
             .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
             .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY)
             
             .iterate();

            g.V().hasLabel(Dom.SYN)
             .has(Dom.Syn.V.CLASS, "ActionDeclarationContext")
             .addE(Dom.SEM).to(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "blockStatement").inV())
             .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CONTROL)
             .property(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY)
             
             .iterate();
        }

        // NOTE: this now handles parsers as well. 
        //   the grammar seems somewhat inconsistent here (why didn't they reuse 
        //   BlockStatement in the parser as well?). it looks like ParserStates
        //   act like special blocks, but there is also ParserBlockStatement for
        //   nesting blocks inside ParserStates.
        private static void findBlockStatements(GraphTraversalSource g) {
        // Note: 
        // - The syntax tree has represents linked lists in reverse-order: the head is the leaf.
        // - Gremlin has no reverse operation. It can be simulated using fold() and Collections.reverse, but then path information (incl. names) is lost.
            List<Map<String, Vertex>> ms = 
                g.V().hasLabel(Dom.SYN)
                .or(__.has(Dom.Syn.V.CLASS, "BlockStatementContext"),
                    __.has(Dom.Syn.V.CLASS, "ParserStateContext"),
                    __.has(Dom.Syn.V.CLASS, "ParserBlockStatementContext"))
                .as("blockRoot")
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext").or()
                        .has(Dom.Syn.V.CLASS, "DirectApplicationContext").or()
                        .has(Dom.Syn.V.CLASS, "ConditionalStatementContext").or()
                        .has(Dom.Syn.V.CLASS, "BlockStatementContext").or()
                        .has(Dom.Syn.V.CLASS, "ParserBlockStatementContext").or()
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
                .option("ParserBlockStatementContext",
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
            g.V().hasLabel(Dom.SYN)
             .or(__.has(Dom.Syn.V.CLASS, "BlockStatementContext"), 
                 __.has(Dom.Syn.V.CLASS, "ParserBlockStatementContext"))
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

        // Finds return points of blocks. If a block has no children, it
        //   has no return point. 
        //  Otherwise, the return points of the block are return points of 
        //   its "returning children" in case they have return points, otherwise
        //   the return points are the returning children themselves.
        // The returning children of a block is defined per block type:
        //   - ConditionalStatement: both branch are returning children
        //   - BlockStatement: its last statement (possibly a nest) is the returning child
        //   - Declaration: its body is the returning child.
        private static void findReturnStatements(GraphTraversalSource g) {
            g.V().hasLabel(Dom.SYN)
                 .or(__.has(Dom.Syn.V.CLASS, "ConditionalStatementContext"),
                     __.has(Dom.Syn.V.CLASS, "BlockStatementContext"),
                     __.has(Dom.Syn.V.CLASS, "ParserBlockStatementContext"),
                     __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                     __.has(Dom.Syn.V.CLASS, "ActionDeclarationContext"))
                 .order().by(Dom.Syn.V.NODE_ID, Order.desc)
                 .as("source")
                 .outE(Dom.SEM)
                 .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.BODY),
                     __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.TRUE_BRANCH),
                     __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.FALSE_BRANCH),
                     __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.LAST))
                 .inV()
                 .optional(__.outE(Dom.SEM)
                             .has(Dom.Sem.ROLE, Dom.Sem.Role.Control.RETURN)
                             .inV()) 
                 .addE(Dom.SEM).from("source")
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
