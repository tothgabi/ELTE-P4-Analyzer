package parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;


public class Providers {

    public static abstract class Factory {

        public abstract String getName();
        public abstract Graph create() throws IOException;

        public Graph create(Graph oldGraph) throws IOException {
            Graph newGraph = this.create();

            GraphTraversalSource og = oldGraph.traversal();

            // NOTE this approach requires the least amount of maintenance. if execution is
            // slow, use a virtual file system.
            Path f = Files.createTempFile("graph", ".json");
            og.io(f.toString()).with(IO.writer, IO.graphson).write().iterate();

            GraphTraversalSource ng = newGraph.traversal();
            try (GraphTraversal<Object, Object> t = ng.io(f.toString()).with(IO.reader, IO.graphson).read();) {

                t.iterate();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // newGraph.tx().commit();
            f.toFile().delete();

            return newGraph;
        }
    }

    public static class TinkerGraphFactory extends Factory {

        @Override
        public Graph create() throws IOException {
            TinkerGraph graph = TinkerGraph.open();
            return graph;
        }

        @Override
        public String getName() {
            return "tinker";
        }
    }

}