package p4analyser.blackboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.server.GremlinServer;
import org.apache.tinkerpop.gremlin.server.Settings;

public class App {

    private static final String PERSIST_NAME = "graph.gryo";

    public static void main(String[] args) throws IOException {
        new App(args).start();
    }

    @Parameter(names = { "--config",
            "-c" }, description = "Configuration file for GremlinServer (e.g. gremlin-server.yaml)")
    private String gremlinConfigYaml;

    private Settings gremlinConfig;

    private GremlinServer server = null;

    @Parameter(names = { "--store",
            "-s" }, description = "Directory where database is stored. If not specified, in-memory database is launched. If no database exists, one is created.")
    private String databaseLocation;

    public App(String[] args) {
        JCommander.newBuilder().addObject(this).build().parse(args);

        try {
            this.gremlinConfig = Settings.read(gremlinConfigYaml);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Error parsing Gremlin server config file at %s:", gremlinConfigYaml), e);
        }
    }

    public void start() throws IOException {
        server = new GremlinServer(gremlinConfig);
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start GremlinServer.", e);
        }

        if (databaseLocation != null) {
            readFromFile();
        }
    }

    private void readFromFile() throws IOException {
        if (databaseLocation == null) {
            throw new IllegalStateException("databaseLocation == null");
        }

        File graphmlPath = Paths.get(databaseLocation, PERSIST_NAME).toFile();

        GraphTraversalSource g = createClient();
        g.io(graphmlPath.toString()).with(IO.reader, IO.gryo).read().iterate();
        try {
            g.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void saveToFile() throws IOException {
        if(databaseLocation == null){
            throw new IllegalStateException("databaseLocation == null");
        }

        File graphmlPath = Paths.get(databaseLocation, PERSIST_NAME).toFile();

        GraphTraversalSource g = createClient();
        g.io(graphmlPath.toString()).with(IO.writer, IO.gryo).write().iterate();
        try {
            g.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }


    private static GraphTraversalSource createClient() {
        // TODO read these from gremlin-server.min.yaml, otherwise the info have to
        // maintained at two places
        String host = "localhost";
        int port = 8182;
        String remoteTraversalSourceName = "g";

        return AnonymousTraversalSource.traversal()
               .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));
    }

    public void close() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        if(databaseLocation != null){
            saveToFile();
        }

        server.stop().get();
    }

}
