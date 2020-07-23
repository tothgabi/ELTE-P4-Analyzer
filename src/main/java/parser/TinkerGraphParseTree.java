package parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.w3c.dom.Element;

import parser.p4.P4BaseListener;
import parser.p4.P4BaseVisitor;

public class TinkerGraphParseTree {
    public static Graph fromParseTree(ParseTree tree) {
            Graph graph = TinkerGraph.open();

            GraphTraversalSource g = graph.traversal();
//            GraphCreatorVisitor v = new GraphCreatorVisitor();
//            Element root = tree.accept(v);
//            v.doc.appendChild(root);
//            return v.doc;
            new ParseTreeWalker().walk(new GraphCreatorListener(g), tree);
            return graph;
    }

    static class GraphCreatorListener extends P4BaseListener {
        GraphTraversalSource g;
        Map<ParseTree, Vertex> ids = new HashMap<>();
        public GraphCreatorListener(GraphTraversalSource g)  {

            this.g = g;
//            g   .inject(gp.getNodes().values().stream().toArray(GraphNode[]::new))
//                .addV(__.map((Traverser<GraphNode> tver) -> tver.get().getId()))
//                .iterate();
        }

        // TODO this is output specific. Move to GraphUtils
        private static String sanitize(String str){
            str = str.replaceAll("<", "\\\\<");
            str = str.replaceAll(">", "\\\\>");
            str = str.replaceAll("\\{", "\\\\{");
            str = str.replaceAll("\\}", "\\\\}");
            return str;
        }

        public void visitTerminal(TerminalNode node){
            Vertex id = 
                g   .addV()
                    .property("type", node.getClass().getSimpleName())
                    .property("start", node.getSourceInterval().a)
                    .property("end", node.getSourceInterval().b)
                    .property("value", sanitize(node.getText()))
                    .toList().get(0);
            ids.put(node, id);
            if(node.getParent() == null) return;
            Object parentId = ids.get(node.getParent());
            if(parentId == null) throw new RuntimeException("parentId == null");
            g.V(id).addE("edge").from(ids.get(node.getParent())).property("type", "syn").iterate();
        }

        public void enterEveryRule(ParserRuleContext ctx){
            GraphTraversal<Vertex, Vertex> t =
                g   .addV()
                    .property("type", ctx.getClass().getSimpleName())
                    .property("start", ctx.getSourceInterval().a)
                    .property("end", ctx.getSourceInterval().b);
            
            Vertex id = 
                    t.toList().get(0);
            ids.put(ctx, id);
            if(ctx.parent == null) return;
            Object parentId = ids.get(ctx.parent);
            if(parentId == null) throw new RuntimeException("parentId == null");
            g.V(id).addE("edge").from(ids.get(ctx.parent)).property("type", "syn").iterate();
        }
    }
    
}