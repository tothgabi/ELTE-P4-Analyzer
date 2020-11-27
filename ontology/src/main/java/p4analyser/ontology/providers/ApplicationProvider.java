package p4analyser.ontology.providers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

public interface ApplicationProvider {
    public String getUICommandName();
    public Class<?> getUICommand();
    public String[] getUICommandAliases();

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Application  {
    }

}
