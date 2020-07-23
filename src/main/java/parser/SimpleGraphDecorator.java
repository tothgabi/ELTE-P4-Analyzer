package parser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import parser.Providers.Factory;

// TODO salvage this and then eliminate
public class SimpleGraphDecorator implements Graph {

  private Graph graph;

  private String dotGraph;
  private final String name;
  private final Factory ggf; 
  private Boolean hasCycle = null; // late initialized by hasCycle() 

  /**
   * shallow copy constructor for enabling decorator subclasses
   */
  protected SimpleGraphDecorator(SimpleGraphDecorator sg) throws IOException {
    this.graph = sg.graph;
    this.dotGraph = sg.dotGraph;
    this.name = sg.name;
    this.ggf = sg.ggf;
    this.hasCycle = sg.hasCycle;
  }

  private SimpleGraphDecorator(Graph graph, Factory ggf) throws IOException {
    this.graph = graph;
    this.dotGraph = null;
    this.name = "unnamed-subgraph";
    this.ggf = ggf;
    this.hasCycle = null;
  }

  public SimpleGraphDecorator(Path dotPath, Factory ggf) throws IOException {
        this.dotGraph = new String(Files.readAllBytes(dotPath));
        this.name = FilenameUtils.removeExtension(dotPath.getFileName().toString());

        this.ggf = ggf; 
        this.graph = parseDot();
  }

  private Graph parseDot() throws GraphParserException, IOException {

        GraphParser gp = new GraphParser(IOUtils.toInputStream(dotGraph, "UTF-8"));
        Graph graph = ggf.create();
        GraphTraversalSource g = graph.traversal();

        // uploads nodes with their labelV set to GraphNode.getId()
        g   .inject(gp.getNodes().values().stream().toArray(GraphNode[]::new))
            .addV(__.map((Traverser<GraphNode> tver) -> tver.get().getId()))
            .iterate();

        Function<Traverser<String>,String> f = 
            tver -> StringEscapeUtils.escapeCsv(
                        StringEscapeUtils.escapeJava(
                            gp.getNodes().get(tver.get()).getAttribute("label").toString()))
                    .replaceAll("[\\{\\}<>\"]", "\\\\$0");

        // sets the "code" property of each Vertex to its corresponding GraphNode.getAttribute("label")
        g.V().sideEffect(__.property("code", __.label() .map(f))) .iterate();


        // uploads edges with their labelE set to GraphEdge.getAttribute("label")
        // NOTE: according to the [docs](http://tinkerpop.apache.org/docs/current/recipes/#long-traversals), long traversals are an antipattern, because it can lead to an anti-pattern. in case this ever happens, just call iterate() inside the loop
        for(GraphEdge e : gp.getEdges().values()){
            String label = sanitizeEdgeLabel(e.getAttribute("label").toString());
            g   .addE(label)
                .from(__.V().hasLabel(e.getNode1().getId()))
                .to(__.V().hasLabel(e.getNode2().getId()))
                .iterate();
        }

        return graph;
  }

  private static String sanitizeEdgeLabel(String label){
      if(label.trim().equals(""))
          label = "_";

      label = label.replace("/", ""); // orientdb
      label = label.replace(";", ""); // orientdb

      return label;
  }


  // TODO
  public <S, T> Graph subgraph(GraphTraversal<S, T> t) {
    // TinkerGraph subgraph = (TinkerGraph) t.subgraph("alma").cap("alma").next();
    //
    // final Function<VertexProperty<Object>, Stream<String>>
    // toPairs = vp -> Stream.of(vp.key(), vp.value().toString());
    //
    // final Consumer<Vertex>
    // addVs = v ->
    // subgraph.addVertex(Streams.stream(v.properties()).flatMap(toPairs).toArray());
    // return graph;
    throw new NotImplementedException();
  }

  public Graph getGraph() {
    return graph;
  }
  public String getName() {
    return name;
  }
  public Factory getGraphFactory() {
    return ggf;
  }


  public boolean isEmptyGraph(){
    Iterator<Vertex> vs = this.getGraph().vertices();
    return !vs.hasNext();
  }

  public boolean isSingletonGraph(){
    Iterator<Vertex> vs = this.getGraph().vertices();
    if(!vs.hasNext()) return false;
    vs.next();
    return !vs.hasNext();
  }

  public boolean hasCycle() {
    if(hasCycle == null){
      GraphTraversalSource g = graph.traversal();

      // from: http://tinkerpop.apache.org/docs/current/recipes/#cycle-detection
      Long b =
            g .V().as("a")
              .emit()
              .repeat(__.outE().inV().simplePath())
              .outE().inV().where(P.eq("a"))
              .limit(1) 
              .count()
              .next();

      assert (b == 0 || b == 1);

      hasCycle = b == 1;
    }

    return hasCycle;
  }

  public SimpleGraphDecorator prefix(Object startVertexId, int length){
    return prefix(startVertexId, length, this.ggf);
  }

  /**
   * There is no guarantee that (longest path length in prefix == length). 
   * 
   * Example: Here, the longest path from s to e is 4-long, but the 3-prefix already contains the whole graph (as the union of suvw and suwe):
   *      v
   *     / \
   * s--u---w--e
   * 
   * @param startVertexId
   * @param length
   * @return
   */
  public SimpleGraphDecorator prefix(Object startVertexId, int length, Providers.Factory ggf){
    GraphTraversalSource g = graph.traversal();

    TinkerGraph subgraph = (TinkerGraph)   
        g .V(startVertexId)
          .times(length).emit()
          .repeat(__.outE().as("e").inV())
          .select("e")
          .subgraph("sg")
          .cap("sg")
          .next();

    if(!subgraph.vertices().hasNext()){
      Map<Object,Object> start = g.V(startVertexId).elementMap().next();
      Object[] startProps = start .entrySet()
                                  .stream()
                                  .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                                  .toArray();
                              
      subgraph.addVertex(startProps);
    }

    try {
      return new SimpleGraphDecorator(ggf.create(subgraph), ggf);
    } catch(IOException e){
      throw new IllegalStateException(e);
    }
  }



  /////////////////////////
  // delegated interface //
  /////////////////////////

  @Override
  public org.apache.tinkerpop.gremlin.structure.Vertex addVertex(Object... keyValues) {
    return graph.addVertex(keyValues);
  }

  @Override
  public <C extends GraphComputer> C compute(Class<C> graphComputerClass) throws IllegalArgumentException {
    return graph.compute(graphComputerClass);
  }

  @Override
  public GraphComputer compute() throws IllegalArgumentException {
    return graph.compute();
  }

  @Override
  public Iterator<org.apache.tinkerpop.gremlin.structure.Vertex> vertices(Object... vertexIds) {
    return graph.vertices(vertexIds);
  }

  @Override
  public Iterator<Edge> edges(Object... edgeIds) {
    return graph.edges(edgeIds);
  }

  @Override
  public Transaction tx() {
    return graph.tx();
  }

  @Override
  public void close() throws Exception {
    graph.close();
  }

  @Override
  public Variables variables() {
    return graph.variables();
  }

  @Override
  public Configuration configuration() {
    return graph.configuration();
  }

}