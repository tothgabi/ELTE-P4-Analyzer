package p4analyser.ontology.providers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

public interface P4FileProvider {
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CoreP4File {
        
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface V1ModelP4File {
        
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InputP4File {

    }
}
