package p4analyser.experts.syntaxtree;

import java.util.ArrayList;
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

import p4analyser.experts.syntaxtree.p4.P4BaseListener;
import p4analyser.experts.syntaxtree.p4.P4BaseVisitor;

import p4analyser.ontology.Dom;

public class TinkerGraphParseTree {
    public static void fromParseTree(GraphTraversalSource g, ParseTree tree, Vocabulary vocab, String[] ruleNames) {
//            g.getGraph().configuration().setProperty(Dom.Syn.V.NODE_ID, null);

//            GraphCreatorVisitor v = new GraphCreatorVisitor();
//            Element root = tree.accept(v);
//            v.doc.appendChild(root);
//            return v.doc;
            new ParseTreeWalker().walk(new GraphCreatorListener(g, vocab, ruleNames), tree);
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
            str = str.replaceAll("\"", "\\\\\"");
            return str;
        }

        public void visitTerminal(TerminalNode node){
            Vertex id = 
                g   .addV(Dom.SYN) //.sideEffect(GremlinUtils.setNodeId())
//                    .property("nodeId", Integer.toString(ids.size()))
                    .property(Dom.Syn.V.CLASS, node.getClass().getSimpleName())
                    .property(Dom.Syn.V.START, node.getSymbol().getStartIndex())
                    .property(Dom.Syn.V.END, node.getSymbol().getStopIndex())
                    .property(Dom.Syn.V.LINE, node.getSymbol().getLine())
                    .property(Dom.Syn.V.VALUE, sanitize(node.getText()))
                    .next();
            ids.put(node, id);
            if(node.getParent() == null) return;
            Object parentId = ids.get(node.getParent());
            if(parentId == null) throw new RuntimeException("parentId == null");
            g.V(id).addE(Dom.SYN).from(ids.get(node.getParent()))
                .property(Dom.Syn.E.RULE,vocab.getSymbolicName(node.getSymbol().getType()))
//                .sideEffect(GremlinUtils.setEdgeOrd())
                .iterate();
        }

        @Override
        public void enterEveryRule(ParserRuleContext ctx){
            // note: getStop is null, if the rule maps to the empty string. (e.g.  input : /* epsilon */)
            int stopIdx = ctx.getStop() == null 
                          ? ctx.getStart().getStopIndex() 
                          : ctx.getStop().getStopIndex();
            Vertex id =
                g   .addV(Dom.SYN) //.sideEffect(GremlinUtils.setNodeId())
//                    .property("nodeId", Integer.toString(ids.size()))
                    .property(Dom.Syn.V.CLASS, ctx.getClass().getSimpleName())
                    .property(Dom.Syn.V.START, ctx.getStart().getStartIndex())
                    .property(Dom.Syn.V.END, stopIdx)
                    .property(Dom.Syn.V.LINE, ctx.getStart().getLine())
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
//                   .property(Dom.Syn.E.ORD, childIdx)
                   .iterate();
        }

    }
    
}