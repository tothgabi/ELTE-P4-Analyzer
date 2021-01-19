/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */

package p4analyser.applications.verification;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.transform.TransformerException;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import p4analyser.ontology.providers.AppUI;
import p4analyser.ontology.providers.Application;
import p4analyser.ontology.providers.CLIArgs;
import p4analyser.ontology.Status;

import p4analyser.ontology.analyses.Verification;

public class VerifyApp implements Application {

    private final VerifyCommand cmd = new VerifyCommand();

    @Inject                      private GraphTraversalSource g;
    @Inject @Verification        private Provider<Status> v;
    @Inject @CLIArgs             private AppUI cli;

    @Override
    public VerifyCommand getUI(){
        return cmd;
    }

    @Override
    public Status run() throws IOException, TransformerException, InterruptedException {
        v.get();        
        
        return new Status();
    }

}
