package p4analyser.experts.visualisation;

import org.codejargon.feather.Provides;

import p4analyser.ontology.providers.ApplicationProvider;
import p4analyser.ontology.providers.SyntaxTreeProvider.SyntaxTree;

import java.io.IOException;

import javax.inject.Provider;
import javax.xml.transform.TransformerException;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

public class DrawApplicationProvider implements ApplicationProvider {

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
    public Object run(GraphTraversalSource g, @SyntaxTree Provider<Object> ensureSt, DrawCommand cmd)
            throws IOException, TransformerException, InterruptedException {
        new Printer(g, ensureSt, cmd);
        return true;
    }

    @Override
    public String[] getUICommandAliases() {
        return new String[]{};
    }
    
}
