/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.ontology.providers;

import p4analyser.ontology.Status;

public interface Application {

    public AppUI getUI();
    public Status run() throws Exception;

}
