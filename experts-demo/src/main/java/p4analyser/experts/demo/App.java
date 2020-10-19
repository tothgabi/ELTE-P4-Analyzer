package p4analyser.experts.demo;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

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
        Vertex v1 = app.g.addV("hello").property("a", 123).next();
        Vertex v2 = app.g.addV("hello").property("b", 123).next();
        Vertex v3 = app.g.addV("szia").property("b", 123).next();
        app.g.addE("alma").to(v1).from(v2).iterate();
        app.g.addE("alma").to(v1).from(v2).iterate();
        app.g.addE("alma").to(v2).from(v1).iterate();
        System.out.println(app.g.V().toList());
//        app.g.getGraph().configuration().setProperty("bond", "james bond");
//        System.out.println(app.g.getGraph().configuration().getProperty("bond"));
        app.close();

        app = new App(host, port, remoteTraversalSourceName);
        System.out.println(app.g.V().toList());
//        System.out.println(app.g.getGraph().configuration().getProperty("bond"));

        System.out.println(app.g.E().properties().toList());
        System.out.println(app.g.V().properties().toList());
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
