package p4analyser.broker;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import org.apache.commons.lang3.SystemUtils;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.codejargon.feather.Provides;

// NOTE For future reference, this guide was helpful: http://emehrkay.com/getting-started-with-tinkerpop-s-gremlin-server-and-gizmo-python
// NOTE The official way would have been to use gremlin-server.sh or gremlin-server.bat.

public class LocalGremlinServer {

    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private String GREMLIN_SERVER_CONF_PATH = loader.getResource("conf/gremlin-server-min.yaml").getPath();
    private String TINKERGRAPH_EMPTY_PROPERTIES_PATH = loader.getResource("conf/tinkergraph-empty.properties")
            .getPath();
    private String EMPTY_SAMPLE_GROOVY_PATH = loader.getResource("conf/empty-sample.groovy").getPath();

    {
        rightPaths();
        updateServerConfig();
    }

    private static Boolean isWindows = SystemUtils.OS_NAME.contains("Windows");
    transient private p4analyser.blackboard.App bb = null;
    transient private GraphTraversalSource g = null;

	private String defaultStateDirectory = null;

    // no-arg constructor is left-empty for serialiazation reasons
    public LocalGremlinServer(){

    }
    public LocalGremlinServer(String defaultStateDirectory){
        this.defaultStateDirectory = defaultStateDirectory;
    }

    public void init() throws IOException {
        if(defaultStateDirectory == null)
            bb = new p4analyser.blackboard.App(new String[] { "-c", GREMLIN_SERVER_CONF_PATH });
        else
            bb = new p4analyser.blackboard.App(
                new String[] { "-c", GREMLIN_SERVER_CONF_PATH, "-s", defaultStateDirectory });
        bb.start();
        connect();
    }

    private void connect() throws IOException {

        // cheap way:
        // Graph graph = TinkerGraph.open();
        // GraphTraversalSource g = graph.traversal();
        // return g;

        // TODO read these from gremlin-server.min.yaml, otherwise the info have to
        // maintained at two places
        String host = "localhost";
        int port = 8182;
        String remoteTraversalSourceName = "g";

        disconnect();

        g = AnonymousTraversalSource.traversal()
                .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));
    }

    private void disconnect() throws IOException {
        if (g != null) {
            try {
                g.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    @Provides
    @Singleton
    public GraphTraversalSource provideConnection() throws IOException {
        return g;
    }

    public void close() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        disconnect();

        if(bb!=null)
            bb.close();

    }
    
    // NOTE: The paths start with a "/". In windows it is a problem, we need to cut it out.
    private void rightPaths() {
        if (isWindows) {
            GREMLIN_SERVER_CONF_PATH = GREMLIN_SERVER_CONF_PATH.substring(1);
            TINKERGRAPH_EMPTY_PROPERTIES_PATH = TINKERGRAPH_EMPTY_PROPERTIES_PATH.substring(1);
            EMPTY_SAMPLE_GROOVY_PATH = EMPTY_SAMPLE_GROOVY_PATH.substring(1);
        }
    }

    // NOTE: GremlinServer does not seem to substitute "classpath:" inside the YAML, so we have to include the path manually
    private void updateServerConfig() {
        Path path = Paths.get(GREMLIN_SERVER_CONF_PATH);
        Charset charset = StandardCharsets.UTF_8;

        String content;
        try {
            content = new String(Files.readAllBytes(path), charset);
            
            //In windows we need these: ""
            if (isWindows) {
                content = content.replaceAll("TINKERGRAPH_EMPTY_PROPERTIES", "\"" + TINKERGRAPH_EMPTY_PROPERTIES_PATH + "\"");
                content = content.replaceAll("EMPTY_SAMPLE_GROOVY", "\"" + EMPTY_SAMPLE_GROOVY_PATH + "\"");
            } else {
                content = content.replaceAll("TINKERGRAPH_EMPTY_PROPERTIES", TINKERGRAPH_EMPTY_PROPERTIES_PATH);
                content = content.replaceAll("EMPTY_SAMPLE_GROOVY", EMPTY_SAMPLE_GROOVY_PATH);
            }
            
            Files.write(path, content.getBytes(charset));
//            System.out.println(Files.lines(Paths.get(GREMLIN_SERVER_CONF_PATH)).collect(Collectors.toList()));
        } catch (IOException e1) {
            throw new IllegalStateException("Failed to edit config file "+GREMLIN_SERVER_CONF_PATH);
        }
    }

}
