package p4analyser.experts.visualisation;

import org.codejargon.feather.Provides;

import p4analyser.ontology.providers.Application;
import p4analyser.ontology.providers.SyntaxTreeAnalysis;

import java.io.IOException;

import javax.inject.Provider;
import javax.xml.transform.TransformerException;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

public class DrawApplication implements Application {

    @Override
    public String getUICommandName() {
        return "draw";
    }

    @Override
    @Provides
    public DrawCommand getUICommand() {
        return new DrawCommand();
    }

    @Provides
    public Application run(GraphTraversalSource g, Provider<SyntaxTreeAnalysis> ensureSt, DrawCommand cmd)
            throws IOException, TransformerException, InterruptedException {
        new Printer(g, ensureSt, cmd);
        return null;
    }

    @Override
    public String[] getUICommandAliases() {
        return new String[]{};
    }
    
}
