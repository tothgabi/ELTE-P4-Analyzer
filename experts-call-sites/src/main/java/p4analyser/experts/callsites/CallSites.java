package p4analyser.experts.callsites;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

/**
 * Hello world!
 *
 */
public class CallSites 
{
    public static void main( String[] args )
    {
        System.out.println( "Not implemented yet!" );
        System.exit(0);

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];

//        Graph graph = TinkerGraph.open();
//        GraphTraversalSource g = graph.traversal();
        GraphTraversalSource g = 
            AnonymousTraversalSource
                    .traversal()
                    .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));

        SemanticAnalysis.CallSites.analyse(g);
    }
}
