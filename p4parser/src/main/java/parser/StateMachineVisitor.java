package parser;


import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


import parser.p4.P4BaseVisitor;
import parser.p4.P4Parser.NameContext;
import parser.p4.P4Parser.ParserStateContext;
import parser.p4.P4Parser.SelectCaseContext;
import parser.p4.P4Parser.SelectCaseListContext;
import parser.p4.P4Parser.SelectExpressionContext;
import parser.p4.P4Parser.TransitionStatementContext;


public class StateMachineVisitor extends P4BaseVisitor<Map<String, List<String>>>{
    // - you can aggregate siblings, cousines by overriding the aggregateResult method. by default aggregateResult returns the last result.
    // - you can aggregate descendants by overriding the visit-methods. by default each node returns the aggregate results of its children
    // - XPath would be better for this, but ANTLR only supports a subset of XPath, and it's not clear what subset is that.

    static class NameVisitor extends P4BaseVisitor<List<String>> {
       @Override public List<String> visitName(NameContext ctx) { 
           List<String> ls = new LinkedList<String>();
           ls.add(ctx.getText());
           return ls;
       }

       @Override public List<String> aggregateResult(List<String> aggregate, List<String> nextResult) {
           if(nextResult == null) return aggregate;
           else if(aggregate == null) return nextResult;
           List<String> ls = new LinkedList<String>();
           ls.addAll(aggregate);
           ls.addAll(nextResult);
           return ls;
       }

       @Override public List<String> visitSelectExpression(SelectExpressionContext ctx) { 
           return ctx.getChild(SelectCaseListContext.class, 0).accept(this);
       }
       @Override public List<String> visitSelectCase(SelectCaseContext ctx) { 
           return ctx.getChild(NameContext.class, 0).accept(this);
       }
//       @Override public List<String> visitStateExpression(StateExpressionContext ctx) { 
//           return ctx.getText();
//       }
   
    }

    @Override
	public Map<String, List<String>> aggregateResult(Map<String, List<String>> aggregate, Map<String, List<String>> nextResult) {
        if(nextResult == null) return aggregate;
        else if(aggregate == null) return nextResult;
        else {
            Map<String, List<String>> agg2 = new HashMap<>();
            agg2.putAll(aggregate);
            agg2.putAll(nextResult);
            return agg2;
        }
    }

    @Override 
    public Map<String, List<String>> visitParserState(ParserStateContext ctx) { 
        String stateName = null;
        List<String> transitions = new LinkedList<>();
        for(ParseTree c : ctx.children){
            if(c.getPayload() instanceof NameContext){
                stateName = ((NameContext) c).accept(new NameVisitor()).get(0);
            } else if (c.getPayload() instanceof TransitionStatementContext){
                transitions.addAll(((TransitionStatementContext) c).accept(new NameVisitor()));
            } else {
                // skip;
            }
        }
        if(stateName == null) throw new IllegalStateException("stateName == null");
        Map<String, List<String>> vstate = new HashMap<>();
        vstate.put(stateName, transitions);
        return vstate;
    }
    
}