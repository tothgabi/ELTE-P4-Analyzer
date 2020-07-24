package parser;

import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

// root -> instantiation, parsers, controls
// 

public class ControlFlowAnalysis {
    public static void analyse(Graph graph){
        Vertex cfgRoot = graph.traversal().addV("cflow").property("nodeId", "0").toList().get(0);

//        System.out.println(
//            graph.traversal()
//                .withSack(new int[]{1})
//                .V().hasLabel("syn").and().or(
//                    __.has("class", "InstantiationContext"),
//                    __.has("class", "ParserDeclarationContext"),
//                    __.has("class", "ControlDeclarationContext"))
//                .sack((a,b))
//                .toList());
//        System.exit(1);
        List<Vertex> entryNodes = 
            graph.traversal()
                .withSack(new int[]{1})
                .V().hasLabel("syn").and().or(
                      __.has("class", "InstantiationContext"),
                      __.has("class", "ParserDeclarationContext"),
                      __.has("class", "ControlDeclarationContext"))

                .addE("cflow").property("role", "assoc").property("ord", "0")
                .from(__.addV("cflow").property("nodeId", __.<Vertex, int[]>sack().map(t -> Integer.toString(t.get()[0])))
                        .addE("cflow").from(cfgRoot)
                                      .property("ord", __.<Vertex, int[]>sack().map(t -> Integer.toString(t.get()[0] - 1)))
                                      .property("role","flow").inV()
                        )
                .inV()
.<int[], Vertex>sack((a,b) -> { a[0]++; return a;})
                .toList(); 
        
    } 
}