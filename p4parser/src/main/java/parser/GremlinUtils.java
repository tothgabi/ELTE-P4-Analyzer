package parser;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.LazyBarrierStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.PathRetractionStrategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    @SuppressWarnings("unchecked")
    public static void resetNodeIds(Graph graph, String domain){

        // NOTE gremlin does not seem to support depth-first-traversal, so we do the following:
        //   1. get the path leading to each leaf of the syntax tree
        //   2. sort the list of the paths lexicographically based on in-edge ids
        //   3. go through each vertex of each path in order, and relabel them

        List<Path> paths = 
            graph.traversal()
                .V().hasLabel(Dom.SYN).has(Dom.Syn.V.NODE_ID, 0L)
//                .repeat(__.outE(Dom.SYN).order().by(Dom.Syn.E.ORD, Order.asc).inV()) // didn't work, maybe repeat puts them in a set anyway
                .repeat(__.out(Dom.SYN))
                .emit(__.outE(Dom.SYN).count().is(0))
                .path()
                .toList();

        Set<Vertex> visited = new HashSet<>();
        Vertex v;
        Collections.sort(paths, GremlinUtils::compareVertexPaths);

        for (Path path : paths) {
            Iterable<Object> it = () -> path.iterator();
            for (Object o : it) {
                if(o instanceof Vertex && !visited.contains(v = (Vertex) o)){
                    v.property(Dom.Syn.V.NODE_ID, visited.size());
                    visited.add(v);
                } 
            }
        }

        Map<String, Integer> m = 
            (Map<String, Integer>) graph.configuration().getProperty(Dom.Syn.V.NODE_ID);
        if(m == null){
            throw new IllegalStateException("Call initializeNodeIds before resetNodeIds!");
        }
        if(!m.containsKey(domain))
            throw new IllegalArgumentException("Call initializeNodeIds before resetNodeIds!");

        m.put(domain, visited.size() + 1);
    }

    private static int compareVertexPaths(Path p1, Path p2) {
        OfInt i1= vertexPathToInEdgeOrd(p1);
        OfInt i2= vertexPathToInEdgeOrd(p2);

        while(true){
            if(!i1.hasNext() && !i2.hasNext()) return 0;
            if(!i1.hasNext() && i2.hasNext()) return 1;
            if(i1.hasNext() && !i2.hasNext()) return -1;

            int n1 = i1.next();
            int n2 = i2.next();
            if(n1 == n2) continue;

            return n1 - n2;
        }
    }

    private static OfInt vertexPathToInEdgeOrd(Path p1) {
        return StreamSupport.stream(p1.spliterator(), false)
                    .filter(o -> ((Vertex) o).edges(Direction.IN, Dom.SYN).hasNext())
                    .map(o -> ((Vertex) o).edges(Direction.IN, Dom.SYN)
                                            .next()
                                            .value(Dom.Syn.E.ORD))
                    .mapToInt(o -> Integer.parseInt(o.toString()))
                    .iterator();
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