/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */

package p4analyser.experts.verification;

import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.codejargon.feather.Provides;

import p4analyser.ontology.Status;
import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.CallGraph;
import p4analyser.ontology.analyses.ControlFlow;
import p4analyser.ontology.analyses.Verification;

import p4analyser.experts.verification.pipeline_checking.PipelineChecking;
import p4analyser.experts.verification.pipeline_checking.PreCalculations;


public class VerificationImpl 
{

    @Provides
    @Singleton
    @Verification
    public Status analyse(GraphTraversalSource g, @CallGraph Status t, @ControlFlow Status cfg, @AbstractSyntaxTree Status ast){
        System.out.println(Verification.class.getSimpleName() +" started.");

        PipelineChecking pipelineChecker = new PipelineChecking("MyParser", "MyIngress", "MyDeparser");

        PreCalculations.analyse(g);
        
        pipelineChecker.analyse(g);
        System.out.println("=============\nConditions:\n=============\n" + pipelineChecker.getAllCond());

        System.out.println(Verification.class.getSimpleName() + " complete.");

        return new Status();
    }
}
