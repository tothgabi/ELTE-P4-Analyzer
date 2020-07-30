package parser;

import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class SemanticAnalysis {
    
    public static void analyse(Graph graph){
        GraphTraversalSource g = graph.traversal();
        analyseParsers(g);
    }


    private static void analyseParsers(GraphTraversalSource g) {
        findParsers(g);
        findStates(g);
        findStateNames(g);
        findTransitions(g);
    }

    private static void findParsers(GraphTraversalSource g) {
        g.V().hasLabel("syn").has("class", "ParserDeclarationContext")
         .addE("sem").property("domain", "parser").property("role", "parser")
         .from(g.V().hasLabel("syn").has("nodeId", 0))
         .iterate();
    }

    private static void findStates(GraphTraversalSource g) {
        g.E().hasLabel("sem").property("domain", "parser").has("role", "parser").inV()
            .as("parserRoot")
            .repeat(__.out("syn"))
            .until(__.outE("syn").has("rule", "parserStates").count().is(0))
            .emit(__.outE("syn").has("rule", "parserState"))
            .outE("syn").has("rule", "parserState").inV()
            .addE("sem").from("parserRoot")
            .property("domain", "parser").property("role", "state")
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
    }

    private static void findStateNames(GraphTraversalSource g) {
        g.E().hasLabel("sem").property("domain", "parser").has("role", "state").inV()
        .as("stateRoot")
         .outE("syn").has("rule", "name")
         .inV()
         .repeat(__.out("syn"))
         .until(__.has("class", "TerminalNodeImpl"))
         .addE("sem").from("stateRoot")
         .property("domain", "parser").property("role", "name")
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
        g.E().hasLabel("sem").property("domain", "parser").has("role", "case").inV()
             .as("caseRoot")
             .outE("syn").has("rule", "name").inV()
             .repeat(__.out("syn"))
             .until(__.has("class", "TerminalNodeImpl"))
             .addE("sem").property("domain", "parser")
             .from("caseRoot")
             .property("role", "name")
             .sideEffect(GremlinUtils.setEdgeOrd())
             .iterate();
    }

    private static void findTransitionSelectHead(GraphTraversalSource g) {
        g.E().hasLabel("sem").property("domain", "parser").has("role", "transition").inV()
            .as("transitionRoot")
            .outE("syn").has("rule", "selectExpression").inV() 
            .outE("syn").has("rule", "expressionList").inV() 
            .addE("sem")
            .from("transitionRoot")
            .property("domain", "parser").property("role", "head")
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
    }

    private static void findTransitionSelectCase(GraphTraversalSource g) {
        g.E().hasLabel("sem").property("domain", "parser").has("role", "transition").inV()
            .as("transitionRoot")
            .outE("syn")
            .has("rule", "selectExpression").inV() 
            .repeat(__.out("syn"))
            .until(__.outE("syn").has("rule", "selectCaseList").count().is(0))
            .emit(__.outE("syn").has("rule", "selectCase"))
            .outE("syn").has("rule", "selectCase").inV()
            .addE("sem").from("transitionRoot")
            .property("domain", "parser").property("role", "case")
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
    }

    private static void findTransitionTargetName(GraphTraversalSource g) {
        g.E().hasLabel("sem").property("domain", "parser").has("role", "transition").inV()
            .as("transitionRoot")
            .outE("syn").has("rule", "name").inV()
            .repeat(__.out("syn")).until(__.has("class", "TerminalNodeImpl"))
            .addE("sem").from("transitionRoot")
            .property("domain", "parser").property("role", "name")
            .sideEffect(GremlinUtils.setEdgeOrd())
            .iterate();
    }

    private static void findTransitionNode(GraphTraversalSource g) {
        g.E().hasLabel("sem").property("domain", "parser").has("role", "state").inV()
         .as("stateRoot")
         .outE("syn").has("rule", "transitionStatement").inV()
         .outE("syn").has("rule", "stateExpression").inV()
         .addE("sem").from("stateRoot")
         .property("domain", "parser").property("role", "transition")
         .sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    private static void findStartState(GraphTraversalSource g) {
        g.E().hasLabel("sem").property("domain", "parser").has("role", "parser").inV()
         .as("parserRoot")
         .outE("sem").property("domain", "parser").has("role", "state").inV()
         .filter(__.outE("sem").has("domain", "parser").has("role", "name").inV()
                   .has("value", "start"))
         .addE("sem").from("parserRoot")
         .property("domain", "parser").property("role", "start")
         .sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();
    }

    @SuppressWarnings("unchecked")
    private static void findNextState(GraphTraversalSource g) {
        List<Map<String,Vertex>> statesAndNextNames = 
            g.E().hasLabel("sem").property("domain", "parser").has("role", "state").inV()
            .as("sourceState")
            .outE("sem").has("domain", "parser").has("role", "transition").inV()
            .union(
                __.outE("sem").has("domain", "parser").has("role", "name").inV(),
                __.outE("sem").has("domain", "parser").has("role", "case").inV()
                  .outE("sem").has("domain", "parser").has("role", "name").inV())
            .as("nextName")
            .<Vertex>select("sourceState", "nextName")
            .toList();

        for (Map<String,Vertex> sn : statesAndNextNames) {
            Vertex state = sn.get("sourceState");
            String nextName = sn.get("nextName").value("value");

            if(nextName.equals("accept") || nextName.equals("reject")){
                g.E().hasLabel("sem").property("domain", "parser").has("role", "parser").inV()
                 .addE("sem").to(__.V(state))
                 .property("domain", "parser").property("role", "final")
                 .sideEffect(GremlinUtils.setEdgeOrd())
                 .iterate();
            } else {
                g.E().hasLabel("sem").property("domain", "parser").has("role", "state").inV()
                .filter(__.outE("sem").has("domain", "parser").has("role", "name").inV()
                            .sideEffect(t -> t.get().value("class"))
                        .has("value", nextName))
                .addE("sem").from(__.V(state))
                .property("domain", "parser").property("role", "next")
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
            }
        }
    }

    private static void findStatements(GraphTraversalSource g){

        g.E().hasLabel("sem").has("domain", "parser").has("role", "state").inV()
         .as("synState")
         .outE("syn").has("rule", "parserStatements").inV()
         .repeat(__.out())
         .until(__.has("class", "AssignmentOrMethodCallStatementContext").or()
                  .has("class", "DirectApplicationContext").or()
                  .has("class", "ConstantDeclarationContext").or()
                  .has("class", "VariableDeclarationContext"))
         .addE("sem").from("synState")
         .property("domain", "parser").property("role", "statement")
         .sideEffect(GremlinUtils.setEdgeOrd())
         .iterate();

    }

}