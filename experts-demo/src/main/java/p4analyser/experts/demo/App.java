package p4analyser.experts.demo;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];

        App app = new App(host, port, remoteTraversalSourceName);
        System.out.println(app.g.V().toList());
        app.close();
    }

    private GraphTraversalSource g;

    public App(Configuration c) {
            g = AnonymousTraversalSource.traversal()
                    .withRemote(c);
    }

    public App(String host, int port, String remoteTraversalSourceName) {
            g = AnonymousTraversalSource.traversal()
                    .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));
    }

    public void close() throws IOException {
        try {
            g.close();
        } catch (Exception e) {
            throw new IOException("Failed to close remote traversal. ", e);
        }
 
    }
}
