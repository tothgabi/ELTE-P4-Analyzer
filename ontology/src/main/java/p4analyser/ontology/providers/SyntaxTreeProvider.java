package p4analyser.ontology.providers;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

public interface SyntaxTreeProvider  {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SyntaxTree { }
}
