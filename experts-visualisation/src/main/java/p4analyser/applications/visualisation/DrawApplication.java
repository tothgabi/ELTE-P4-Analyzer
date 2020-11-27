package p4analyser.applications.visualisation;

import org.codejargon.feather.Provides;

import p4analyser.ontology.providers.ApplicationProvider;
import p4analyser.ontology.analyses.SyntaxTree;

import java.io.IOException;

import javax.inject.Provider;
import javax.xml.transform.TransformerException;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

public class DrawApplication implements ApplicationProvider {

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
    @Application
    public Void run(GraphTraversalSource g, @SyntaxTree Provider<Void> ensureSt, DrawCommand cmd)
            throws IOException, TransformerException, InterruptedException {
        new Printer(g, ensureSt, cmd);
        return null;
    }

    @Override
    public String[] getUICommandAliases() {
        return new String[]{};
    }
    
}
