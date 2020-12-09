/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.ontology.providers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface CLIArgs {
    
}
