/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.broker;

public class LocalGremlinServerException extends Exception {

    public LocalGremlinServerException(String message) {
        super(message);
    }

    public LocalGremlinServerException(Throwable cause) {
        super(cause);
    }
    
}
