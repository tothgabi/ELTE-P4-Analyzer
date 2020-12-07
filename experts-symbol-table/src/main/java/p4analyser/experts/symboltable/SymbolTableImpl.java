/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.experts.symboltable;

import p4analyser.ontology.Dom;
import p4analyser.ontology.Status;
import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.SymbolTable;
import p4analyser.ontology.analyses.SyntaxTree;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import org.apache.tinkerpop.gremlin.process.traversal.Operator;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalOptionParent.Pick;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.util.function.Lambda;
import org.codejargon.feather.Provides;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;


public class SymbolTableImpl 
{

    // NOTE syntax maybe too permissive with expressions: (~ (13 >> true) . etherType) is a syntactically valid expression, even though '~', '>>', and '.' are reserved tokens. for this reason I decided to handle case-by-case

    @Provides
    @Singleton
    @SymbolTable
    public Status analyse(GraphTraversalSource g, @SyntaxTree Status s, @AbstractSyntaxTree Status a){
        System.out.println(SymbolTable.class.getSimpleName() +" started.");

        resolveNames(g);
        resolveTypeRefs(g);
        parserStateScopes(g);
        localScope(g);
        parameterScope(g);
        fieldAndMethodScope(g);
        actionRefs(g);
        tableApps(g);
        packageInstantiations(g);
        controlAndParserInstantiations(g);

        System.out.println(SymbolTable.class.getSimpleName() +" complete.");
        return new Status();
    }


    public static void resolveNames(GraphTraversalSource g) {
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
            __.has(Dom.Syn.V.CLASS, "ParserStateContext"),
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

    @SuppressWarnings("unchecked")
    private static void parserStateScopes(GraphTraversalSource g) {
        g.V().hasLabel(Dom.SYN)
            // find all names that refer to parser states
            .has(Dom.Syn.V.CLASS, "StateExpressionContext")
            .coalesce(
                __.outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV(), 
                __.repeat(__.out(Dom.SYN))
                  .until(__.has(Dom.Syn.V.CLASS, "SelectCaseContext")) 
                  .outE(Dom.SYN).has(Dom.Syn.E.RULE, "name").inV()) 
            .repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "TerminalNodeImpl"))
            .as("nameNode")
            .values("value").as("name")

            // find all parsers states in that parser
            .<Vertex>select("nameNode")
            .repeat(__.in(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "ParserDeclarationContext"))

            .repeat(__.out(Dom.SYN))
            .until(__.has(Dom.Syn.V.CLASS, "ParserStateContext"))
            .as("decl")

            // select the one that declares that name
            .outE(Dom.SYMBOL).has(Dom.Symbol.ROLE, Dom.Symbol.Role.DECLARES_NAME).inV()
            .values("value")
            .where(P.eq("name"))
            .addE(Dom.SYMBOL).from("decl").to("nameNode")
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
