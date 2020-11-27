package p4analyser.ontology.providers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

public interface Application {
    public String getUICommandName();
    public Object getUICommand();
    public String[] getUICommandAliases();
}
