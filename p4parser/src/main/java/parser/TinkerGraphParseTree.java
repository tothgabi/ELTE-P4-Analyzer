package parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Vocabulary;
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
    public static Graph fromParseTree(ParseTree tree, Vocabulary vocab, String[] ruleNames) {
            Graph graph = TinkerGraph.open();
            GremlinUtils.initializeNodeIds(graph, Dom.SYN);

            GraphTraversalSource g = graph.traversal();
//            GraphCreatorVisitor v = new GraphCreatorVisitor();
//            Element root = tree.accept(v);
//            v.doc.appendChild(root);
//            return v.doc;
            new ParseTreeWalker().walk(new GraphCreatorListener(g, vocab, ruleNames), tree);
            return graph;
    }

    static class GraphCreatorListener extends P4BaseListener {
        private GraphTraversalSource g;
        private Map<ParseTree, Vertex> ids = new HashMap<>();
        private Vocabulary vocab;
        private String[] ruleNames;
        public GraphCreatorListener(GraphTraversalSource g, Vocabulary vocab, String[] ruleNames) {
            this.g = g;
            this.vocab = vocab;
            this.ruleNames = ruleNames;
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
                g   .addV(Dom.SYN).sideEffect(GremlinUtils.setNodeId())
//                    .property("nodeId", Integer.toString(ids.size()))
                    .property(Dom.Syn.V.CLASS, node.getClass().getSimpleName())
                    .property(Dom.Syn.V.START, node.getSourceInterval().a)
                    .property(Dom.Syn.V.END, node.getSourceInterval().b)
                    .property(Dom.Syn.V.VALUE, sanitize(node.getText()))
                    .next();
            ids.put(node, id);
            if(node.getParent() == null) return;
            Object parentId = ids.get(node.getParent());
            if(parentId == null) throw new RuntimeException("parentId == null");
            g.V(id).addE(Dom.SYN).from(ids.get(node.getParent()))
                .property(Dom.Syn.E.RULE,vocab.getSymbolicName(node.getSymbol().getType()))
                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
        }

        public void enterEveryRule(ParserRuleContext ctx){
            Vertex id =
                g   .addV(Dom.SYN).sideEffect(GremlinUtils.setNodeId())
//                    .property("nodeId", Integer.toString(ids.size()))
                    .property(Dom.Syn.V.CLASS, ctx.getClass().getSimpleName())
                    .property(Dom.Syn.V.START, ctx.getSourceInterval().a)
                    .property(Dom.Syn.V.END, ctx.getSourceInterval().b)
                    .next();
            
            ids.put(ctx, id);
            if(ctx.parent == null) return;
            Object parentId = ids.get(ctx.parent);
            int childIdx = -1;
            for (int i = 0; i < ctx.parent.getChildCount(); i++) {
               if(!ctx.parent.getChild(i).equals(ctx)) continue;
               childIdx = i;
               break;
            }
            if(parentId == null) throw new RuntimeException("parentId == null");
            g.V(id).addE(Dom.SYN).from(ids.get(ctx.parent))
                   .property(Dom.Syn.E.RULE,ruleNames[ctx.getRuleIndex()])
//                   .sideEffect(GremlinUtils.setEdgeOrd())
                   .property(Dom.Syn.E.ORD, childIdx)
                   .iterate();
        }

    }
    
}