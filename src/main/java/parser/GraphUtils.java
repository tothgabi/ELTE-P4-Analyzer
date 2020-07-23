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
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GraphUtils {
    
    public static enum Extension {
        GRAPH_ML(0), DOT(1), SVG(2), PDF(3); 
        public Integer ord;

        private Extension(Integer ord) {
            this.ord = ord;
        }

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

            if(exts.contains(Extension.GRAPH_ML))
                ret.put(Extension.GRAPH_ML, graphmlPath);
            
            if(max.ord <= Extension.DOT.ord) return ret;

            dotPath = toDot(graph, name, graphmlPath);
            if(exts.contains(Extension.DOT))
                ret.put(Extension.DOT, dotPath);

            if(exts.contains(Extension.SVG)){
                Path svgPath = graphviz(graph, name, dotPath, Extension.SVG);
                ret.put(Extension.SVG, svgPath);
                Desktop.getDesktop().open(svgPath.toFile());
            }

            if(exts.contains(Extension.PDF)){
                Path pdfPath = graphviz(graph, name, dotPath, Extension.PDF);
                ret.put(Extension.PDF, pdfPath);
                Desktop.getDesktop().open(pdfPath.toFile());
            }

        } finally {
            if(!exts.contains(Extension.GRAPH_ML)){
                graphmlPath.toFile().delete();
            }
            if(!exts.contains(Extension.DOT)){
                dotPath.toFile().delete();
            }
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
    Source xslt = new StreamSource(new File("graphml2dot.xsl"));
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