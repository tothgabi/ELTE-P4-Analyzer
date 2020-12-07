/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.broker.tests;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import p4analyser.broker.tools.FailsHandler;

import static org.junit.Assert.assertTrue;

public class SymbolTableITs {

   public static void main( String[] args )
    {
        System.out.println("SymbolTable - Tests have started");
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];
        String p4ProgramSourceName = args[3];

        GraphTraversalSource g = 
            AnonymousTraversalSource
                    .traversal()
                    .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));

        Result result = JUnitCore.runClasses(Tests.class);
        FailsHandler.writeFails(p4ProgramSourceName, "symbolTable", result.getFailures());  

        System.out.println("SymbolTable - Tests have finished");
    }

    public static class Tests {
        @Test
        public void test() {
            assertTrue("Elso teszt", 1 == 1);
        }
    }
    
}
