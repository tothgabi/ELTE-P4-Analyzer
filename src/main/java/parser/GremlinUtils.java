package parser;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Map;

public class GremlinUtils {

    @SuppressWarnings("unchecked")
    public static void initializeNodeIds(Graph graph, String domain){
        Map<String, Integer> m = 
            (Map<String, Integer>) graph.configuration().getProperty(Dom.Syn.V.NODE_ID);

        if(m == null){
            m = new HashMap<String, Integer>();
            graph.configuration().setProperty(Dom.Syn.V.NODE_ID, m);
        }

        if(m.containsKey(domain))
            throw new IllegalArgumentException("m.containsKey(domain)");

        m.put(domain, 0);
    }

    public static GraphTraversal<Vertex, Vertex> setNodeId(){
        return 
            __.<Vertex>property(Dom.Syn.V.NODE_ID, getNodeId()).sideEffect(incrementNodeId());
    }

    public static GraphTraversal<Edge, Edge> setEdgeOrd(){
        return 
            __.<Edge>property(Dom.Syn.E.ORD,  __.<Edge>as("e").outV().outE()
                 .where(P.eq("e")).by(T.label)   // select edges that has the same label as 'e'
                 .count()
                 .map(t -> t.get() - 1));
    }

    @SuppressWarnings("unchecked")
    public static GraphTraversal<Element, Integer> getNodeId(){
        return 
            __.<Element, Integer>map(t -> 
                ((Map<String, Integer>) t.get().graph().configuration().getProperty(Dom.Syn.V.NODE_ID))
                .get(t.get().label()));
    }
    @SuppressWarnings("unchecked")
    public static GraphTraversal<Element, Element> incrementNodeId(){
        return 
            __.<Element>sideEffect(t -> 
                {  
                    String domain = t.get().label();
                    Map<String, Integer> m = (Map<String, Integer>) 
                        t.get().graph().configuration().getProperty(Dom.Syn.V.NODE_ID);
                    m.put(domain, m.get(domain) + 1);
                });
    }
}