/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.broker.tests;

import p4analyser.broker.tools.FailsHandler;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.util.ResourceBundle.Control;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.util.ArrayList;

public class AliasesITs {

    public static void main( String[] args )
    {
        System.out.println("Aliases - Tests have started.");
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];
        String p4ProgramSourceName = args[3];

        //ArrayList<Object> values = ControllerIT.jsonHandler.getFileTestValues(p4ProgramSourceName, "aliases-test");

//        Graph graph = TinkerGraph.open();
//        GraphTraversalSource g = graph.traversal();
        GraphTraversalSource g = 
            AnonymousTraversalSource
                    .traversal()
                    .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));
        
        Result result = JUnitCore.runClasses(Tests.class);
        FailsHandler.writeFails(p4ProgramSourceName, "aliases", result.getFailures());        

        System.out.println("Aliases - Tests have finished");
    }

    public static class Tests {
        @Test
        public void test() {
            System.out.println("alma");
            assertTrue("Elso teszt", 1 == 2);
        }
    }
    
}
