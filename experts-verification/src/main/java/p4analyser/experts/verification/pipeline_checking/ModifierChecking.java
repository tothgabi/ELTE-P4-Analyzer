/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */

package p4analyser.experts.verification.pipeline_checking;

import p4analyser.ontology.Dom;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import io.netty.util.internal.EmptyArrays;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public class ModifierChecking 
{
    private String modifierName;
    private HashSet<String> ids;

    private String allCondition;

    public ModifierChecking (String modName) {
        modifierName = modName;
        allCondition = "";
    }

    public String getAllCond() {
        return allCondition;
    }

    public void analyse (GraphTraversalSource g) {
        ids = PreCalculations.getIds(g);

        List<Object> nodeIds =
            g.V().has("value", modifierName)
            .repeat(__.inE(Dom.SYN).outV())
            .until(__.has(Dom.Syn.V.CLASS, "ControlDeclarationContext")).as("controlPoint")
            .repeat(__.outE(Dom.CALL).has(Dom.Call.ROLE, Dom.Call.Role.CALLS).order().by("ord")
                       .inV()).emit(__.or(__.has(Dom.Syn.V.CLASS, "ActionDeclarationContext"),
                                                        __.has(Dom.Syn.V.CLASS, "TableDeclarationContext",
                                                        __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"))))
            .path().from("controlPoint").unfold().has("nodeId").dedup().values("nodeId").toList();
        
        for (int i = nodeIds.size()-1; i >= 0; i--) {
            analyseBlock(g, (int) (long) nodeIds.get(i));
        }
    }
   

    private  void analyseBlock (GraphTraversalSource g, int nodeId ) {
        switch (g.V().has(Dom.Syn.V.NODE_ID, nodeId).values(Dom.Syn.V.CLASS).toList().get(0).toString()) {
            case "ControlDeclarationContext": analyseControl(g, nodeId); break;
            case "TableDeclarationContext"  : analyseTable(g, nodeId); break;
            case "ActionDeclarationContext" : analyseAction(g, nodeId); break;
        }
    }

    private  String contextToString (GraphTraversalSource g, int nodeId) {

        List<Object> names = g.V().has(Dom.Syn.V.NODE_ID, nodeId)
        .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.NAME).order().by("ord").inV()
        .values(Dom.Syn.V.VALUE).toList();
        
        String result = "";
        for (int i = 0; i < names.size(); i++) {
            result += names.get(i) + ".";
        }
        return result.substring(0,result.length()-1);
    }

    private  String getHeader (String expr) {
        return expr.substring(0, expr.lastIndexOf("."));
    }

    private  void analyseAction (GraphTraversalSource g, int nodeId) {          
        String error = "";
        ConditionPair conditionAct = new ConditionPair();

        List<Object> flows = g.V().has(Dom.Syn.V.NODE_ID, nodeId)
            .repeat(__.outE(Dom.CFG).or(__.has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.ENTRY),
                                    __.has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.FLOW))
                       .inV()).emit(__.or(__.not(__.has(Dom.Syn.V.CLASS, "BlockStatementContext")),
                                                        __.not(__.has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext"))))
            .path().unfold().has("nodeId").not(__.has(Dom.Syn.V.CLASS, "BlockStatementContext")).dedup().values("nodeId").toList();
        
        for (int i = 1; i < flows.size(); i++) {
            Object actualNodeId =  flows.get(i);
            if (g.V().has(Dom.Syn.V.NODE_ID, actualNodeId).values(Dom.Syn.V.CLASS).toList().get(0).toString().equals("AssignmentOrMethodCallStatementContext")) {

                Boolean done = false;
                ConditionPair actCond = new ConditionPair();

                { //assignment -> read
                    List<Object> contextOfRead = g.V().has(Dom.Syn.V.NODE_ID, actualNodeId)
                                                .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.READ)
                                                .inV().values("nodeId").toList();
                    done = done || contextOfRead.size() > 0;
                    for (int j = 0; j < contextOfRead.size(); j++) {
                        String field = contextToString(g, (int) (long) contextOfRead.get(j));
                        
                        if (ids.contains(field)) {
                            actCond.addPreValid(field);
                            actCond.addPreValid(getHeader(field));
                            actCond.addPostValid(field);
                            actCond.addPostValid(getHeader(field));
                        }
                        
                    }
                }

                { //assignment -> write
                    List<Object> contextOfWrite = g.V().has(Dom.Syn.V.NODE_ID, actualNodeId)
                                                .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.WRITE)
                                                .inV().values("nodeId").toList();
                    done = done || contextOfWrite.size() > 0;
                    for (int j = 0; j < contextOfWrite.size(); j++) {
                        String field = contextToString(g, (int) (long) contextOfWrite.get(j));
                        if (ids.contains(field)) {
                            actCond.addPostValid(field);
                        }
                    }
                }

                { //drop
                    if (!done) {
                        List<Object> tmp = g.V().has(Dom.Syn.V.NODE_ID, actualNodeId).outE(Dom.SYN).has(Dom.Syn.E.RULE, "lvalue").inV()
                        .repeat(__.outE(Dom.SYN).inV()).until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl")).values(Dom.Syn.V.VALUE).toList();
                        if (tmp.size() > 0 && tmp.get(0).toString().equals("mark_to_drop")) {
                            done = true;
                            actCond = new ConditionPair(); actCond.addPostValid("drop");
                        }                            
                    }
                }

                { //method - setValid() and setInvalid()
                    if (!done) {
                        String methodName = (String) g.V().has(Dom.Syn.V.NODE_ID, actualNodeId)
                        .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.METHOD)
                        .inV().values("value").toList().get(0);
                        String nameContext = contextToString(g, (int) (long) g.V().has(Dom.Syn.V.NODE_ID, actualNodeId)
                                            .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.METHOD)
                                            .outV().outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.CALL).inV()
                                            .values("nodeId").toList().get(0));

                        if (methodName.equals("setValid") && ids.contains(nameContext)) {
                            actCond.addPostValid(nameContext);
                            for (String field : getAllFields(nameContext)) {
                                actCond.addPostInvalid(field);
                            }
                        } else if (methodName.equals("setInvalid") && ids.contains(nameContext)) {
                            actCond.addPostInvalid(nameContext);
                            for (String field : getAllFields(nameContext)) {
                                actCond.addPostInvalid(field);
                            }
                        } else System.out.println("I cannot handle this method: " + methodName); 
                    }
                }        
                
                error += conditionAct.continueMerge(actCond);
            }
        }        
        writeCondition(g, nodeId, conditionAct.toSet(), "Action", error);
    }

    private List<String> getAllFields(String headerPrefix) {
        ArrayList<String> result = new ArrayList<String>();

        for (String id : ids) {
            if (id.startsWith(headerPrefix + ".")) {
                result.add(id);
            }
        }
        return result;
    }

    private  void analyseTable (GraphTraversalSource g, int nodeId) {        
        Condition tablepreC = new Condition();
        List<Object> contextsNodeId = g.V().has(Dom.Syn.V.NODE_ID, nodeId)
        .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.KEY).inV().values("nodeId").toList();

        for (int i = 0; i < contextsNodeId.size(); i++) {
            String name = contextToString(g, (int) (long) contextsNodeId.get(i));
            if (ids.contains(name)) {
                tablepreC.addValid(name);
                tablepreC.addValid(getHeader(name));
            }
        }
        writeCondition(g, nodeId, appTable(g, nodeId, new ConditionPair(tablepreC, tablepreC)), "Table", "");
    }


    private HashSet<ConditionPair> appTable(GraphTraversalSource g, int nodeId, ConditionPair tableCondition) {

        HashSet<ConditionPair> result = new HashSet<ConditionPair>();
        List<Object> actionsCond = g.V().has(Dom.Syn.V.NODE_ID, nodeId).outE(Dom.CALL).has(Dom.Call.ROLE, Dom.Call.Role.CALLS).inV()
        .values("condition").toList();

        for (int i = 0; i < actionsCond.size(); i++) {
            HashSet<ConditionPair> actions = fromLabel(actionsCond.get(i).toString());
            for (ConditionPair actionCond : actions) {
                actionCond.checkMerge(tableCondition);
                result.add(actionCond);
            }
        }

        return result;
        
    }

    private  void analyseControl (GraphTraversalSource g, int nodeId) {
        String error = "";
        HashSet<ConditionPair> resultCondition = new HashSet<ConditionPair>();
        
        List<Object> flows = g.V().has(Dom.Syn.V.NODE_ID, nodeId)
        .repeat(__.outE(Dom.CFG)
                .not(__.has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN))
                .order().by("ord")
                .inV()).emit(__.or(__.not(__.has(Dom.Syn.V.CLASS, "BlockStatementContext")),
                                   __.not(__.has(Dom.Syn.V.CLASS, "ConditionalStatementContext")),
                                   __.not(__.has(Dom.Syn.V.CLASS, "DirectApplicationContext"))))
        .path().unfold().has("nodeId")
        .not(__.has(Dom.Syn.V.CLASS, "BlockStatementContext"))
        .not(__.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"))
        .values(Dom.Syn.V.CLASS, Dom.Syn.V.NODE_ID).toList();

        HashSet<ConditionPair> conditionControl = new HashSet<ConditionPair>();
        conditionControl = new HashSet<ConditionPair>();

        for (int i = 0; i < flows.size(); i = i + 2) {
            //later for if calculation
            /*List<Object> prevConds = g.V().has(Dom.Syn.V.NODE_ID, flows.get(i+1))
                                     .inE(Dom.CFG).not(__.has(Dom.Cfg.E.ROLE, Dom.Cfg.E.Role.RETURN)).outV()
                                     .until(__.or(__.has(Dom.Syn.V.CLASS, "DirectApplicationContext"),
                                                   __.has(Dom.Syn.V.CLASS, "ConditionalStatementContext"),
                                                   __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                                                   __.has(Dom.Syn.V.CLASS, "AssignmentOrMethodCallStatementContext")))
                                    .repeat(__.inE(Dom.CFG).outV()).unfold().values("condition").toList();
            
            if (prevConds.size() > 0) {
                for (Object elem : prevConds) {
                    conditionControl.addAll(fromLabel((String) elem));
                }
            } */

            Object actualNodeId =  flows.get(i+1);
            HashSet<ConditionPair> actCond = new HashSet<ConditionPair>();

            switch (flows.get(i).toString()) {
                case "ConditionalStatementContext": //TODO
                    break;
                case "DirectApplicationContext":
                    List<Object> tmpCond = g.V().has(Dom.Syn.V.NODE_ID, flows.get(i+1))
                    .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.NEXT)
                    .inV().values("condition").toList();
                    for (int j = 0; j < tmpCond.size(); ++j) {
                        if (j == 0) {
                            for (ConditionPair cp : fromLabel(tmpCond.get(j).toString())) {
                                actCond.add(cp);
                            }   
                        }                     
                    }                    
                    break;
                case "AssignmentOrMethodCallStatementContext":
                    ConditionPair act = new ConditionPair();

                    String methodName = (String) g.V().has(Dom.Syn.V.NODE_ID, actualNodeId)
                    .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.METHOD)
                    .inV().values("value").toList().get(0);
                    String nameContext = contextToString(g, (int) (long) g.V().has(Dom.Syn.V.NODE_ID, actualNodeId)
                                        .outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.METHOD)
                                        .outV().outE(Dom.CHECKING).has(Dom.Checking.ROLE, Dom.Checking.Role.CALL).inV()
                                        .values("nodeId").toList().get(0));

                    if (methodName.equals("setValid") && ids.contains(nameContext)) {
                        act.addPostValid(nameContext);
                        for (String field : getAllFields(nameContext)) {
                            act.addPostInvalid(field);
                        }
                    } else if (methodName.equals("setInvalid") && ids.contains(nameContext)) {
                        act.addPostInvalid(nameContext);
                        for (String field : getAllFields(nameContext)) {
                            act.addPostInvalid(field);
                        }
                    } else System.out.println("I cannot handle this method: " + methodName);
                    actCond.add(act);
                    break;
            }



            if (conditionControl.size() == 0) {
                resultCondition = actCond;
                
            } else {
                resultCondition = new HashSet<ConditionPair>();
                for (ConditionPair controlElem : conditionControl) {
                    for (ConditionPair actElem : actCond) {
                        ConditionPair newElem = new ConditionPair(controlElem);
                        error += newElem.continueMerge(actElem);
                        resultCondition.add(newElem);
                    }
                }
            }

            conditionControl = resultCondition;            
        }
        writeCondition(g, nodeId, resultCondition, "Control", error);
    }

    private  void writeCondition(GraphTraversalSource g, int nodeId, HashSet<ConditionPair> conds) {
        g.V().has(Dom.Syn.V.NODE_ID, nodeId)
        .property("condition", toLabel(conds))
        .iterate();        
    }

    private  void writeCondition(GraphTraversalSource g, int nodeId, HashSet<ConditionPair> conds, String type, String error) {
        String condStr = toLabel(conds);        
        String name;
        
        if (type.equals("Control")) {
            name = g.V().has(Dom.Syn.V.NODE_ID, nodeId)
                      .outE().has(Dom.Sem.ROLE, Dom.Sem.Role.Control.NAME)
                      .inV().values("value").toList().get(0).toString();
        } else {
            name = g.V().has(Dom.Syn.V.NODE_ID, nodeId)
                      .outE().has(Dom.Checking.ROLE, Dom.Checking.Role.NAME)
                      .inV().values("value").toList().get(0).toString();
        }

        g.V().has(Dom.Syn.V.NODE_ID, nodeId)
        .property("condition", condStr)
        .iterate();

        allCondition = allCondition + "\n\n" + type + " " + name + ": \n" + condStr;
        if (!error.isEmpty())
            allCondition = allCondition + "\nError with: " + error; 
    }
    
    private  String toLabel(HashSet<ConditionPair> conditions) {
        String result = "[";
        for (ConditionPair elem : conditions) {
            result += elem.toString() + ";";
        }

        if (conditions.size() > 0) {
            result = result.substring(0,result.length()-1);
        } 
            
        result = result + "\n]";

        return result;
    }

    private  HashSet<ConditionPair> fromLabel(String condStr) {        
        HashSet<ConditionPair> conditions = new HashSet<ConditionPair>();    
        String[] oneCondStr = (condStr.substring(1, condStr.length()-2)).split(";");        
        
        for (int i = 0; i < oneCondStr.length; i++) {
            conditions.add(getOneConditionPair(oneCondStr[i]));
        }

        return conditions;
    }

    private  ConditionPair getOneConditionPair (String oneCondPairStr) {
        Condition preCond;
        Condition postCond;

        if (!oneCondPairStr.isEmpty()) {
            int half = oneCondPairStr.indexOf('\n', 8);
            preCond = getOneCondition(oneCondPairStr.substring(8, half));
            postCond = getOneCondition(oneCondPairStr.substring(half + 8, oneCondPairStr.length()-1));
        } else {
            preCond = new Condition();
            postCond = new Condition();
        }
        
        return new ConditionPair(preCond, postCond);
    }

    private  String[] handleInput (String condStr) {
        if (condStr.length() == 2) return EmptyArrays.EMPTY_STRINGS;
        else return (condStr.substring(1, condStr.length()-1)).split(",");
    }

    private  Condition getOneCondition(String oneCondStr) {
        Condition result = new Condition();
        String[] valid = handleInput(oneCondStr.substring(oneCondStr.indexOf('['), oneCondStr.indexOf(']')+1));
        String[] invalid = handleInput( oneCondStr.substring(oneCondStr.lastIndexOf('['), oneCondStr.lastIndexOf(']')+1));

        for (int i = 0; i < valid.length; i++) {
            if (i == 0) { result.addValid(valid[i]); }
            else {
                result.addValid(valid[i].substring(1, valid[i].length()));
            }
        }

        for (int i = 0; i < invalid.length; i++) {
            if (i == 0) { result.addInvalid(invalid[i]); }
            else {
                result.addValid(invalid[i].substring(1, invalid[i].length()));
            }
        }
        
        return result;
    }
 }
