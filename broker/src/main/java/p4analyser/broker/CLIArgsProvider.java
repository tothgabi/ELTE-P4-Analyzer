package p4analyser.broker;

import javax.inject.Singleton;

import org.codejargon.feather.Provides;

import p4analyser.ontology.providers.AppUI;
import p4analyser.ontology.providers.CLIArgs;

public class CLIArgsProvider {

    public final AppUI args;

    public CLIArgsProvider(AppUI args) {
        this.args = args;
    }

    
    @Provides
    @Singleton
    @CLIArgs
    public AppUI provideArgs(){
        return args;
    }

    
}
