package p4analyser.experts.callgraph;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import p4analyser.ontology.Dom;

public class Befores {

    public static GraphTraversalSource preTestWhoCallsTable() {
        TinkerGraph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        
        Vertex control1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ControlDeclarationContext").property("nodeId", 0).next();
        Vertex control2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ControlDeclarationContext").property("nodeId", 1).next();
        Vertex control3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ControlDeclarationContext").property("nodeId", 2).next();
        Vertex table1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "TableDeclarationContext").property("nodeId", 3).next();
        Vertex table2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "TableDeclarationContext").property("nodeId", 4).next();
        Vertex table3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "TableDeclarationContext").property("nodeId", 5).next();
        Vertex table4 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "TableDeclarationContext").property("nodeId", 6).next();
        Vertex tmp1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 7).next();
        Vertex tmp2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 8).next();
        Vertex tmp3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 9).next();
        Vertex tmp4 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 10).next();
        Vertex tmp5 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 11).next();
                                        
        g.addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(table2).to(tmp3)
        .addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(table3).to(tmp4)
        .addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(table4).to(tmp4)
        .addE(Dom.SYN).from(control2).to(tmp3)
        .addE(Dom.SYN).from(control3).to(tmp5)
        .addE(Dom.SYN).from(tmp5).to(tmp1)
        .addE(Dom.SYN).from(tmp1).to(tmp4)
        .addE(Dom.SYN).from(control3).to(tmp2)
        .addE(Dom.SYN).from(tmp2).to(tmp4).iterate();

        return g;
    }

    public static GraphTraversalSource preTestWhoCallsAction() {
        TinkerGraph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        
        Vertex table1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "TableDeclarationContext").property("nodeId", 0).next();
        Vertex table2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "TableDeclarationContext").property("nodeId", 1).next();
        Vertex table3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "TableDeclarationContext").property("nodeId", 2).next();
        Vertex action1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ActionDeclarationContext").property("nodeId", 3).next();
        Vertex action2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ActionDeclarationContext").property("nodeId", 4).next();
        Vertex action3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ActionDeclarationContext").property("nodeId", 5).next();
        Vertex action4 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ActionDeclarationContext").property("nodeId", 6).next();
        Vertex tmp1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 7).next();
        Vertex tmp2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 8).next();
        Vertex tmp3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 9).next();
        Vertex tmp4 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 10).next();
        Vertex tmp5 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 11).next();
                                        
        g.addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(action2).to(tmp3)
         .addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(action3).to(tmp4)
         .addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(action4).to(tmp4)
         .addE(Dom.SYN).from(table2).to(tmp3)
         .addE(Dom.SYN).from(table3).to(tmp5)
         .addE(Dom.SYN).from(tmp5).to(tmp1)
         .addE(Dom.SYN).from(tmp1).to(tmp4)
         .addE(Dom.SYN).from(table3).to(tmp2)
         .addE(Dom.SYN).from(tmp2).to(tmp4).iterate();

        return g;
    }

    public static GraphTraversalSource pretestWhoCallsFunctionPrototype() {
        TinkerGraph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        
        Vertex function1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "FunctionPrototypeContext").property("nodeId", 1).next();
        Vertex function2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "FunctionPrototypeContext").property("nodeId", 5).next();
        Vertex function3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "FunctionPrototypeContext").property("nodeId", 10).next();
        Vertex control1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ControlDeclarationContext").property("nodeId", 2).next();
        Vertex control2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ControlDeclarationContext").property("nodeId", 7).next();
        Vertex parser1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ParserStateContext").property("nodeId", 3).next();
        Vertex parser2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ParserStateContext").property("nodeId", 8).next();
        Vertex action1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ActionDeclarationContext").property("nodeId", 4).next();
        Vertex action2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ActionDeclarationContext").property("nodeId", 9).next();
        Vertex tmp1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 6).next();
        Vertex tmp2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 11).next();
        Vertex tmp3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 12).next();
        Vertex tmp4 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 13).next();
        Vertex tmp5 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 14).next();
                                        
        g.addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(function2).to(tmp1)
        .addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(function3).to(tmp2)
        .addE(Dom.SYMBOL).property(Dom.Symbol.ROLE, Dom.Symbol.Role.SCOPES).from(function3).to(tmp5)
        .addE(Dom.SYN).from(control2).to(tmp1)
        .addE(Dom.SYN).from(parser2).to(tmp1)
        .addE(Dom.SYN).from(action2).to(tmp1)
        .addE(Dom.SYN).from(parser2).to(tmp5)
        .addE(Dom.SYN).from(action2).to(tmp4)
        .addE(Dom.SYN).from(tmp4).to(tmp3)
        .addE(Dom.SYN).from(tmp3).to(tmp2)
        .iterate();

        return g;
    }

    public static GraphTraversalSource pretestWhoCallsParserState() {
        TinkerGraph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        
        Vertex ps1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ParserStateContext").property("nodeId", 1).next();
        Vertex ps2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ParserStateContext").property("nodeId", 3).next();
        Vertex ps3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ParserStateContext").property("nodeId", 5).next();
        Vertex pd1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ParserDeclarationContext").property("nodeId", 2).next();
        Vertex pd2 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ParserDeclarationContext").property("nodeId", 4).next();
        Vertex pd3 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "ParserDeclarationContext").property("nodeId", 6).next();
        Vertex tmp1 = g.addV(Dom.SYN).property(Dom.Syn.V.CLASS, "tmp").property("nodeId", 7).next();

                                        
        g.addE(Dom.SYN).from(pd2).to(ps2)
        .addE(Dom.SYN).from(pd3).to(ps3)
        .addE(Dom.SYN).from(pd3).to(tmp1)
        .addE(Dom.SYN).from(tmp1).to(ps2)
        .iterate();

        return g;
    }
}
