/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */

package p4analyser.experts.verification.pipeline_checking;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;


public class PipelineChecking {
    
    ModifierChecking modifierChecker;

    public PipelineChecking (String parser, String modifier, String deparser) {
        modifierChecker = new ModifierChecking(modifier);
    }

    public void analyse(GraphTraversalSource g) {
        modifierChecker.analyse(g);
    }

    public String getAllCond() {
        return modifierChecker.getAllCond();
    }
}
