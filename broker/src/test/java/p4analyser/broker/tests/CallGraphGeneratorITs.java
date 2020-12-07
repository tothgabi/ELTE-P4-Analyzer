/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.broker.tests;

import p4analyser.broker.ControllerIT;
import p4analyser.broker.tools.FailsHandler;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import p4analyser.ontology.Dom;

import java.util.ArrayList;

public class CallGraphGeneratorITs {
    
    public static GraphTraversalSource g;

    private static ArrayList<Object> values;


   public static void main( String[] args )
    {
        System.out.println("CallGraph - Tests have started");
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];
        String p4ProgramSourceName = args[3];
        values = ControllerIT.jsonHandler.getFileTestValues(p4ProgramSourceName, "call-graph-test");


        g = AnonymousTraversalSource
                .traversal()
                .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));

        Result result = JUnitCore.runClasses(Tests.class);

        FailsHandler.writeFails(p4ProgramSourceName, "callGraph", result.getFailures()); 
	 
        System.out.println("CallGraph - Tests have finished");
    }

    public static class Tests {
        @Test
        public void testAllEdgeNumber() {
            assertEquals("All-edge", ((Long) values.get(0)).intValue(), g.V().or(__.has(Dom.Syn.V.CLASS, "TableDeclarationContext"),
                                                 __.has(Dom.Syn.V.CLASS, "ControlDeclarationContext"),
                                                 __.has(Dom.Syn.V.CLASS, "ActionDeclarationContext")).count().next().intValue());
        }

        @Test
        public void testControlVerticesNumber() {
            assertEquals("control-vertices", ((Long) values.get(1)).intValue(), g.V().has(Dom.Syn.V.CLASS, "ControlDeclarationContext").count().next().intValue());
        }

        @Test
        public void testActionVerticesNumber() {
            assertEquals("action-vertices", ((Long) values.get(2)).intValue(), g.V().has(Dom.Syn.V.CLASS, "ActionDeclarationContext").count().next().intValue());
        }

        @Test
        public void testTableVerticesNumber() {
            assertEquals("table-vertices", ((Long) values.get(3)).intValue(), g.V().has(Dom.Syn.V.CLASS, "TableDeclarationContext").count().next().intValue());
        }

        @Test
        public void testIntantiationVerticesNumber() {
            assertEquals("instantiation-vertices", ((Long) values.get(4)).intValue(), g.V().has(Dom.Syn.V.CLASS, "InstantiationContext").count().next().intValue());
        }

            //assertEquals(17, basic.E().has(Dom.Call.ROLE, Dom.Call.Role.CALLS).count().next().intValue());
        @Test
        public void testFromTableToActionEdgeNumber() {
            assertEquals("table-action-edges", ((Long) values.get(5)).intValue(), g.V().has(Dom.Syn.V.CLASS, "TableDeclarationContext")
                            .outE(Dom.CALL).has(Dom.Call.ROLE, Dom.Call.Role.CALLS)
                            .inV().has(Dom.Syn.V.CLASS, "ActionDeclarationContext").count().next().intValue());
        }

        @Test
        public void testFromControlToTableEdgeNumber() {
            assertEquals("control-table-edges", ((Long) values.get(6)).intValue(), g.V().has(Dom.Syn.V.CLASS, "ControlDeclarationContext")
                            .outE(Dom.CALL).has(Dom.Call.ROLE, Dom.Call.Role.CALLS)
                            .inV().has(Dom.Syn.V.CLASS, "TableDeclarationContext").count().next().intValue());
        }

        @Test
        public void testFromIntantiationToControlEdgesNumber() {
            assertEquals("instantiation-control-edges", ((Long) values.get(7)).intValue(), g.V().has(Dom.Syn.V.CLASS, "InstantiationContext")
                            .outE(Dom.CALL).has(Dom.Call.ROLE, Dom.Call.Role.CALLS)
                            .inV().has(Dom.Syn.V.CLASS, "ControlDeclarationContext").count().next().intValue());
        }
    }
}
        
