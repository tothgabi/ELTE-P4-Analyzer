package parser;

import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class SemanticAnalysis {
    
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();
        Parser.analyse(g);
        Instantiation.analyse(g);
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
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).property(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER)
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
        }

        private static void findParserNames(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER).inV()
                .as("parserRoot")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserTypeDeclaration").inV()
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                .addE(Dom.SEM).from("parserRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME)
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
        }


        private static void findStates(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER).inV()
                .as("parserRoot")
                .repeat(__.out(Dom.SYN))
                .until(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserStates").count().is(0))
                .emit(__.outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserState"))
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "parserState").inV()
                .addE(Dom.SEM).from("parserRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE)
                .sideEffect(GremlinUtils.setEdgeOrd())
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
            .sideEffect(GremlinUtils.setEdgeOrd())
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
                .sideEffect(GremlinUtils.setEdgeOrd())
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
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
        }

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
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
        }

        private static void findTransitionTargetName(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.TRANSITION).inV()
                .as("transitionRoot")
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
                .addE(Dom.SEM).from("transitionRoot")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME)
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
        }

        private static void findTransitionNode(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
            .as("stateRoot")
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "transitionStatement").inV()
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "stateExpression").inV()
            .addE(Dom.SEM).from("stateRoot")
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.TRANSITION)
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
        }

        private static void findStartState(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER).inV()
            .as("parserRoot")
            .outE(Dom.SEM).property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
            .filter(__.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME).inV()
                      .has(Dom.Syn.V.VALUE, "start"))
            .addE(Dom.SEM).from("parserRoot")
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.START)
            .sideEffect(GremlinUtils.setEdgeOrd())
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
                String nextName = sn.get("nextName").value(Dom.Syn.V.VALUE);

                if(nextName.equals("accept") || nextName.equals("reject")){

                    g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER).inV()
                    .addE(Dom.SEM).to(__.V(state))
                    .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.FINAL)
                    .sideEffect(GremlinUtils.setEdgeOrd())
                    .iterate();

                } else {

                    g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.STATE).inV()
                    .filter(__.outE(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME).inV()
                                .sideEffect(t -> t.get().value(Dom.Syn.V.CLASS))
                            .has(Dom.Syn.V.VALUE, nextName))
                    .addE(Dom.SEM).from(__.V(state))
                    .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.PARSER).property(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NEXT)
                    .sideEffect(GremlinUtils.setEdgeOrd())
                    .iterate();
                }
            }
        }

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
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();

        }
    }

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
            .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).property(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.INSTANTIATION)
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
        }

        private static void findTypeRefName(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.INSTANTIATION).inV()
             .as("insta")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "typeRef").inV()
             .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
             .addE(Dom.SEM).from("insta")
             .sideEffect(GremlinUtils.setEdgeOrd())
             .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION)
             .property(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.INVOKER)
             .iterate();
        }


        private static void findName(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.INSTANTIATION).inV()
             .as("insta")
             .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
             .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
             .addE(Dom.SEM).from("insta")
             .sideEffect(GremlinUtils.setEdgeOrd())
             .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION)
             .property(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.NAME)
             .iterate();
        }

        private static void findArguments(GraphTraversalSource g) {
            g.E().hasLabel(Dom.SEM).has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.INSTANTIATION).inV()
                .as("insta")
                .repeat(__.out(Dom.SYN))
                .until(__.has(Dom.Syn.V.CLASS, "ArgumentContext"))
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "expression").inV()
                .addE(Dom.SEM).from("insta")
                .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION).property(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.ARGUMENT)
                .sideEffect(GremlinUtils.setEdgeOrd())
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
                g.V().hasLabel(Dom.SYN).has(Dom.Syn.V.NODE_ID, 0).outE(Dom.SEM)
                 .or(__.has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.PARSER),
                     __.has(Dom.Sem.DOMAIN, Dom.Sem.Domain.CROSSCUT).has(Dom.Sem.ROLE, Dom.Sem.Role.Crosscut.CONTROL))
                 .inV()
                 .filter(__.outE(Dom.SEM)
                           .or(__.has(Dom.Sem.ROLE, Dom.Sem.Role.Parser.NAME),
                              __.has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NAME))
                           .inV().has(Dom.Syn.V.VALUE, P.eq(m.get("name").value(Dom.Syn.V.VALUE))))
                 .addE(Dom.SEM).from(__.V(m.get("arg")))
                 .property(Dom.Sem.DOMAIN, Dom.Sem.Domain.INSTANTIATION).property(Dom.Sem.ROLE, Dom.Sem.Role.Instantiation.INVOKES)
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .iterate();
            }
        }
    }

}