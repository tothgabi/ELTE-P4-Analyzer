package parser;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FilenameUtils;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class GraphUtils {

    // TODO this is redundant. eliminate it and use the names in Dom
    public static enum Label {
        SYN, CFG, SEM, SYMBOL, CALL, SITES
    }
    
    public static enum Extension {
        GRAPH_ML(0), DOT(1), SVG(2), PDF(3); 
        public Integer ord;

        private Extension(Integer ord) {
            this.ord = ord;
        }

    }

    // includes all vertices and all edges. moreover includes those vertices that don't have the label but are incident with edges that do have it.
    public static TinkerGraph subgraph(GraphTraversalSource g0, Label... labels0){
        String firstLab = labels0[0].toString().toLowerCase();
        String[] labels = Arrays.stream(labels0).skip(1).map(lab -> lab.toString().toLowerCase()).toArray(String[]::new);
        List<Vertex> vs = g0.V().hasLabel(firstLab, labels).toList();
        List<Edge> es = g0.E().hasLabel(firstLab, labels).toList();

        TinkerGraph newGraph = TinkerGraph.open();
        GraphTraversalSource g = newGraph.traversal();
        for (Vertex v : vs) {
            copyProperties(g.addV(v.label()), g0, v).iterate();
        }
        for (Edge e : es) {
            Vertex inVertex = e.inVertex();
            Vertex outVertex = e.outVertex();
            if(g.V(inVertex.id()).toList().isEmpty()) 
                inVertex = copyProperties(g.addV(inVertex.label()), g0, inVertex).next();
            if(g.V(outVertex.id()).toList().isEmpty()) 
                outVertex = copyProperties(g.addV(outVertex.label()), g0, outVertex).next();

            copyProperties(g.V(outVertex.id()).addE(e.label()).to(inVertex), g0, e).iterate();
        }

        return newGraph;
    }

    private static <E extends Element> GraphTraversal<?, E> copyProperties(GraphTraversal<?, E> t, GraphTraversalSource g0, E v){
//        ArrayList<Property<?>> props = new ArrayList<>();
//        v.properties().forEachRemaining(props::add);
        List<? extends Property<Object>> props = null;
        if(v instanceof Vertex)
            props = g0.V(v).properties().toList();
        if(v instanceof Edge)
            props = g0.E(v).properties().toList();

        t = t.property(T.id, v.id());
        for(Property<?> p : props){
            t.property(p.key(), p.value());
        }
        return t;
    }

    public static Map<Extension, Path> printGraph(Graph graph, String name, boolean display, Extension... exts0) throws IOException,
            TransformerException, InterruptedException {
        Set<Extension> exts = new LinkedHashSet<>(Arrays.asList(exts0));
        Extension max = Collections.max(exts, (a,b) -> a.ord - b.ord) ;
        Map<Extension, Path> ret = new HashMap<>();
        // TODO this is compact but wasteful

        Path graphmlPath = null;
        Path dotPath = null;
        try {
            graphmlPath = toGraphML(graph, name);

            if(exts.contains(Extension.GRAPH_ML)){
                ret.put(Extension.GRAPH_ML, graphmlPath);
                System.out.println(String.format("\"%s\" created.", graphmlPath.toString()));
            }
            
            if(max.ord < Extension.GRAPH_ML.ord) return ret;

            dotPath = toDot(graph, name, graphmlPath);
            if(exts.contains(Extension.DOT)){
                ret.put(Extension.DOT, dotPath);
                System.out.println(String.format("\"%s\" created.", dotPath.toString()));
            }

            if(max.ord <= Extension.DOT.ord) return ret;

            if(exts.contains(Extension.SVG)){
                Path svgPath = graphviz(graph, name, dotPath, Extension.SVG);
                ret.put(Extension.SVG, svgPath);
                System.out.println(String.format("\"%s\" created.", svgPath.toString()));
                Desktop.getDesktop().open(svgPath.toFile());
            }

            if(exts.contains(Extension.PDF)){
                Path pdfPath = graphviz(graph, name, dotPath, Extension.PDF);
                ret.put(Extension.PDF, pdfPath);
                System.out.println(String.format("\"%s\" created.", pdfPath.toString()));
                Desktop.getDesktop().open(pdfPath.toFile());
            }

            // don't delete it if there is an exception, because it can be useful
            if(!exts.contains(Extension.GRAPH_ML)){
                graphmlPath.toFile().delete();
            }
            if(max.ord > Extension.DOT.ord && !exts.contains(Extension.DOT)){
                dotPath.toFile().delete();
            }
        } finally {
        }

        return ret;
    }

    /**
     * Deletion of the file on the resulting path is the responsibility of the
     * caller.
     * 
     * @return
     * @throws IOException
     */
  private static Path toGraphML(Graph graph, String name) throws IOException {
    Path graphmlPath = Files.createTempFile(name, ".xml");
    graph.traversal().io(graphmlPath.toString()).with(IO.writer, IO.graphml).write().iterate();

    return graphmlPath;
  }

  /**
   * Deletion of the file on the resulting path is the responsibility of the
   * caller.
   * 
   * @return
   * @throws TransformerException
   * @throws IOException
   */
  private static Path toDot(Graph graph, String name, Path graphmlPath) throws TransformerException, IOException {
    Path dotPath = null;
    dotPath = Paths.get(FilenameUtils.removeExtension(graphmlPath.toString()) + ".dot");
    TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
    Source xslt = new StreamSource(new File(AntlrP4.GRAPHML2DOT_XSL));
    Transformer transformer = factory.newTransformer(xslt);
    Source text = new StreamSource(graphmlPath.toFile());
    transformer.transform(text, new StreamResult(dotPath.toFile()));

    return dotPath;
  }


  /**
   * Deletion of the file on the resulting path is the responsibility of the
   * caller.
   * 
   * @return
   * @throws ExecuteException
   * @throws IOException
   * @throws InterruptedException
   * @throws TransformerException
   */
  private static Path graphviz(Graph graph, String name, Path dotPath, Extension ext) throws ExecuteException, IOException, InterruptedException, TransformerException {

    String extString = null;
    switch(ext){
        case PDF: 
            extString = "pdf";
            break;
        case SVG:
            extString = "svg";
            break;
        default:
            throw new UnsupportedOperationException(ext.toString());
    }

    Path pdfPath;
    pdfPath = Paths.get(FilenameUtils.removeExtension(dotPath.toString()) + "." + extString);

    SimpleProcess proc = new SimpleProcess(Paths.get("dot"), "-o", pdfPath.toString(), "-T" + extString, dotPath.toString());
    int exitCode = proc.run();
    if (exitCode != 0)
    throw new IllegalStateException(dotPath.getFileName().toString());

    return pdfPath;
  }
}