/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */

package p4analyser.experts.verification.pipeline_checking;

import p4analyser.ontology.Dom;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.HashSet;
import java.util.Map;
import java.util.List;

public class PreCalculations {

    private static String headerStructInst = "hdr";
    
    public static void analyse (GraphTraversalSource g) {
        setReadEdges(g);
        setWriteEdges(g);
        setTableKeys(g);
        setMethodEdges(g);
        addDirectApptoTable(g);    
        addParserHeaderEdge(g);
        setActionName(g);
        setTableName(g);
        headers(g);
    }

    private static void setReadEdges (GraphTraversalSource g) {
        g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext")
        .outE().has(Dom.Syn.E.RULE, "ASSIGN").outV().as("assignments")
        .flatMap(__.outE().has(Dom.Syn.E.RULE, "expression").inV()
        .until(__.has(Dom.Syn.V.CLASS, "ExpressionContext").outE(Dom.SYN).has(Dom.Syn.E.RULE, "DOT").outV())
        .repeat(__.out(Dom.SYN))
        .order().by("nodeId"))
        .dedup().by("nodeId")
        .as("readExpression")
        .addE(Dom.CHECKING).from("assignments").to("readExpression")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.READ)
        .iterate();

        g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext")
        .outE().has(Dom.Checking.ROLE, Dom.Checking.Role.READ).inV().as("readExpression")
        .flatMap(
            __.repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .inE().has(Dom.Syn.E.RULE, "IDENTIFIER").inV()
            .order().by("nodeId").dedup().by("nodeId")).as("read")    
        .addE(Dom.CHECKING).from("readExpression").to("read")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();
    }

    private static void setMethodEdges (GraphTraversalSource g) {

        g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext")
        .outE().has(Dom.Syn.E.RULE, "L_PAREN").outV().as("methodCalls")
        .flatMap(__.outE().has(Dom.Syn.E.RULE, "lvalue").inV()
        .until(__.has(Dom.Syn.V.CLASS, "LvalueContext").outE(Dom.SYN).has(Dom.Syn.E.RULE, "DOT").outV())
        .repeat(__.out(Dom.SYN))
        .outE(Dom.SYN).has(Dom.Syn.E.RULE, "lvalue").inV()
        .order().by("nodeId"))
        .dedup().by("nodeId")
        .as("headerName")
        .addE(Dom.CHECKING).from("methodCalls").to("headerName")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.CALL)
        .iterate();

        g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext")
        .outE().has(Dom.Checking.ROLE, Dom.Checking.Role.CALL).outV().as("methodCalls")
        .flatMap(
            __.outE(Dom.SYN).has(Dom.Syn.E.RULE, "lvalue").inV()
            .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
            .repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .inE().has(Dom.Syn.E.RULE, "IDENTIFIER").inV()
            .order().by("nodeId").dedup().by("nodeId")).as("header")    
        .addE(Dom.CHECKING).from("methodCalls").to("header")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.METHOD)
        .iterate();

        g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext")
        .outE().has(Dom.Checking.ROLE, Dom.Checking.Role.CALL).inV().as("headerName")
        .flatMap(
            __.repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .inE().has(Dom.Syn.E.RULE, "IDENTIFIER").inV()
            .order().by("nodeId").dedup().by("nodeId")).as("header")    
        .addE(Dom.CHECKING).from("headerName").to("header")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();
    }

    private static void setWriteEdges (GraphTraversalSource g) {

        g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext")
        .outE().has(Dom.Syn.E.RULE, "ASSIGN").outV().as("assignments")
        .flatMap(__.outE().has(Dom.Syn.E.RULE, "lvalue").inV()
        .until(__.has(Dom.Syn.V.CLASS, "LvalueContext").outE(Dom.SYN).has(Dom.Syn.E.RULE, "DOT").outV())
        .repeat(__.out(Dom.SYN))
        .order().by("nodeId"))
        .dedup().by("nodeId")
        .as("writeExpression")
        .addE(Dom.CHECKING).from("assignments").to("writeExpression")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.WRITE)
        .iterate();

        g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext")
        .outE().has(Dom.Checking.ROLE, Dom.Checking.Role.WRITE).inV().as("writtenExpression")
        .flatMap(
            __.repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .inE().has(Dom.Syn.E.RULE, "IDENTIFIER").inV()
            .order().by("nodeId").dedup().by("nodeId")).as("write")    
        .addE(Dom.CHECKING).from("writtenExpression").to("write")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();
    }

    public static void setTableKeys(GraphTraversalSource g) {

       g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "TableDeclarationContext").as("table")
        .flatMap(
        __.repeat(__.out()).until(__.has(Dom.Syn.V.CLASS, "TablePropertyContext"))
        .outE().has(Dom.Syn.E.RULE, "keyElementList").inV()
        .until(__.has(Dom.Syn.V.CLASS, "ExpressionContext"))
        .repeat(__.out(Dom.SYN)))
        .as("keyExpression")
        .addE(Dom.CHECKING).from("table").to("keyExpression")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.KEY)
        .iterate();

        g.V().hasLabel(Dom.SYN)
        .has(Dom.Syn.V.CLASS, "TableDeclarationContext")
        .outE().has(Dom.Checking.ROLE, Dom.Checking.Role.KEY).inV().as("keyExpression")
        .flatMap(
            __.repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .inE().has(Dom.Syn.E.RULE, "IDENTIFIER").inV()
            .order().by("nodeId").dedup().by("nodeId")).as("key")    
        .addE(Dom.CHECKING).from("keyExpression").to("key")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();
    }


    public static void addDirectApptoTable(GraphTraversalSource g) {

        List<Object> directApps = g.V().has(Dom.Syn.V.CLASS, "DirectApplicationContext").as("directApp")
        .outE().has(Dom.Syn.E.RULE, "typeName").inV()
        .repeat(__.out())
        .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).values("value", Dom.Syn.V.NODE_ID).toList();

        for (int i = 0; i < directApps.size(); i = i + 2) {            
            g.V().has(Dom.Syn.V.NODE_ID, directApps.get(i+1))                
                .repeat(__.in()).until(__.has(Dom.Syn.V.CLASS, "DirectApplicationContext"))
                .as("da")
                .V().has(Dom.Syn.V.VALUE, directApps.get(i))
                .flatMap(__.repeat(__.in()).until(__.has(Dom.Syn.V.CLASS, "TableDeclarationContext"))
                           .dedup().by("nodeId")).as("tc").dedup()
            .addE(Dom.CHECKING).from("da").to("tc").property(Dom.Checking.ROLE, Dom.Checking.Role.NEXT)
            .iterate();
        }
    }

    public static void addParserHeaderEdge (GraphTraversalSource g) {
        g.V().has(Dom.Syn.V.CLASS, "ParserStateContext").as("parser")
        .flatMap(__.outE(Dom.SEM).has(Dom.Sem.ROLE, "statement").inV()
          .outE(Dom.SYN).has(Dom.Syn.E.RULE, "argumentList").inV()
          .until(__.has(Dom.Syn.V.CLASS, "ExpressionContext"))
          .repeat(__.out(Dom.SYN))).as("exprContext")
        .addE(Dom.CHECKING).from("parser").to("exprContext")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.METHOD)
        .iterate();

        g.V().has(Dom.Syn.V.CLASS, "ParserStateContext")
        .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.METHOD).inV().as("expr")
        .flatMap(
            __.repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .inE().has(Dom.Syn.E.RULE, "IDENTIFIER").inV()
            .order().by("nodeId").dedup().by("nodeId")).as("key")    
        .addE(Dom.CHECKING).from("expr").to("key")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();
    }

    public static void headers (GraphTraversalSource g) {
 
        g.V().has(Dom.Syn.V.CLASS, "HeaderTypeDeclarationContext").as("headerDecl")
        .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
        .repeat(__.out()).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).as("name")
        .addE(Dom.CHECKING).from("headerDecl").to("name")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();

        g.V().has(Dom.Syn.V.CLASS, "HeaderTypeDeclarationContext").as("headerDecl")
        .flatMap(
          __.outE(Dom.SYN).has(Dom.Syn.E.RULE, "structFieldList").inV()
          .repeat(__.out()).until(__.has(Dom.Syn.V.CLASS, "StructFieldContext"))
          .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
          .repeat(__.out()).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))).as("fields")
        .addE(Dom.CHECKING).from("headerDecl").to("fields")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.KEY)
        .iterate();

        List<Object> typeNames = g.V().has(Dom.Syn.V.CLASS, "HeaderTypeDeclarationContext")
                                 .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.NAME).inV().values("value").toList();                             
    
        for (int i = 0; i < typeNames.size(); ++i) {

           g.V().has(Dom.Syn.V.CLASS, "HeaderTypeDeclarationContext")
            .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.NAME).inV()
            .has(Dom.Syn.V.VALUE, (String) typeNames.get(i))
            .inE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.NAME).outV().as("headerDecl")
            .flatMap(            
                __.V().has(Dom.Syn.V.CLASS, "TerminalNodeImpl").has(Dom.Syn.V.VALUE, (String) typeNames.get(i))
                .repeat(__.in()).until(__.has(Dom.Syn.V.CLASS, "StructFieldContext"))
                .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
                .repeat(__.out()).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")))
            .as("headerInstName")
            .addE(Dom.CHECKING).from("headerDecl").to("headerInstName")
            .property(Dom.Checking.ROLE, Dom.Checking.Role.INST)
            .iterate();
        }
        
    }

    public static HashSet<String> getIds (GraphTraversalSource g) {
        HashSet<String> ids = new HashSet<String>();

        List<Object> typeNames = g.V().has(Dom.Syn.V.CLASS, "HeaderTypeDeclarationContext")
                                 .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.NAME).inV().values("value").toList();

        for (int i = 0; i < typeNames.size(); ++i) {

            for (Object elem : g.E().has(Dom.Checking.ROLE, Dom.Checking.Role.INST).inV().values("value").toList()) {
                ids.add(headerStructInst + "." + elem);
            }

            for (Map<String, Object> elem : 
                g.V().has(Dom.Syn.V.CLASS, "HeaderTypeDeclarationContext")
                .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.NAME).inV()
                .has(Dom.Syn.V.VALUE, (String) typeNames.get(i))
                .inE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.NAME).outV()
                .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.INST).inV().as("headerinstName")
                .flatMap(
                    __.inE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.INST).outV()
                    .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.KEY).inV()
                ).as("headerFieldsName")
                .select("headerinstName", "headerFieldsName").by("value").toList()) {
                    ids.add(headerStructInst + "." + elem.get("headerinstName") + "." + (String) elem.get("headerFieldsName"));
                }
        }
        return ids;    
    }

    public static void setActionName(GraphTraversalSource g) {
        g.V().has(Dom.Syn.V.CLASS, "ActionDeclarationContext").as("actions")
        .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
        .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).as("names")
        .addE(Dom.CHECKING).from("actions").to("names")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();
    }

    public static void setDirectAppName(GraphTraversalSource g) {
        g.V().has(Dom.Syn.V.CLASS, "DirectApplicationContext").as("tableApp")
        .outE(Dom.SYN).has(Dom.Syn.E.RULE, "typeName").inV()
        .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).as("name")
        .addE(Dom.CHECKING).from("tableApp").to("name")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();
    }

    public static void setTableName(GraphTraversalSource g) {
        g.V().has(Dom.Syn.V.CLASS, "TableDeclarationContext").as("table")
        .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()
        .repeat(__.out(Dom.SYN)).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).as("name")
        .addE(Dom.CHECKING).from("table").to("name")
        .property(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
        .iterate();
    }
}