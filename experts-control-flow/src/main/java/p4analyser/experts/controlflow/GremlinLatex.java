/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.experts.controlflow;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.lang.model.util.ElementScanner6;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal.Admin;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.ElementValueTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.TokenTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.ComparatorHolder;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.NoneStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.TailGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.WherePredicateStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.RangeGlobalStep.RangeBiOperator;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddEdgeStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.CoalesceStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.CountGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.FoldStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.LambdaFlatMapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.LambdaMapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.OrderGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.SelectOneStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.TraversalFlatMapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.UnfoldStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.AddPropertyStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.AggregateStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.IdentityStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.LambdaSideEffectStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.SackValueStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.SideEffectCapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.StartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.TraversalSideEffectStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Parameters;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;
import org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.step.sideEffect.TinkerGraphStep;
import org.javatuples.Pair;

public class GremlinLatex {
/*
    // note: Step is not prepared to accept Visitors, and with instanceof I had to
    // copy class names twice

    private final static Function<Step, String>[] dispatch = new Function[] 
{ (step) -> visit((TinkerGraphStep) step)
, (step) -> visit((VertexStep) step)
, (step) -> visit((EdgeVertexStep) step)
, (step) -> visit((AddVertexStep) step)
, (step) -> visit((AddPropertyStep) step)
, (step) -> visit((AddEdgeStep) step)
, (step) -> visit((IdentityStep) step)
, (step) -> visit((TraversalSideEffectStep) step)
, (step) -> visit((TraversalFlatMapStep) step)
, (step) -> visit((LambdaSideEffectStep) step)
, (step) -> visit((LambdaMapStep) step)
, (step) -> visit((LambdaFlatMapStep) step)
, (step) -> visit((HasStep) step)
, (step) -> visit((WherePredicateStep) step)
, (step) -> visit((CoalesceStep) step)
, (step) -> visit((SackValueStep) step)
, (step) -> visit((CountGlobalStep) step)
, (step) -> visit((OrderGlobalStep) step)
, (step) -> visit((TailGlobalStep) step)
, (step) -> visit((SelectOneStep) step)
, (step) -> visit((SelectOneStep) step)
, (step) -> visit((AggregateStep) step)
, (step) -> visit((SideEffectCapStep) step)
, (step) -> visit((NoneStep) step)
, (step) -> visit((UnfoldStep) step)
, (step) -> visit((StartStep) step) 
};

    public static String traversalToLatex(Traversal t) {
        return traversalToLatex(t.asAdmin().getSteps());
    }

    private static String traversalToLatex(List<Step> steps) {
        String delim = "";
        StringBuilder sb = new StringBuilder();

         sb.append("{ \\left ( ");
        sb.append("\\begin{aligned}" + System.lineSeparator() + "& \\ ");
        for (Step step : steps) {
            sb.append(delim);
            sb.append(dispatcher(step));
            if (!step.getLabels().isEmpty()) {
                if (step.getLabels().size() != 1)
                    throw new RuntimeException("unimplemented case found: " + step);
                sb.append("@");
                sb.append("\\mathtt{" + step.getLabels().iterator().next() + "}");
            }
            delim = "\\\\" + System.lineSeparator() + "&\\leadsto \\ ";
        }
        sb.append(System.lineSeparator() + "\\end{aligned}" );
         sb.append(" \\right ) }");
        return sb.toString();
    }

    public static String dispatcher(Step step) {
        for (Function<Step, String> f : dispatch) {
            try {
                String s = f.apply(step);
                return s;
            } catch (ClassCastException e) {
                continue;
            }
        }
        return visit(step);

    }

    public static String visit(TinkerGraphStep step) {
        if (step.returnsVertex()) {
            return "V_{" + visitHasContainers((List<HasContainer>) step.getHasContainers()) + "}";
        }
        throw new RuntimeException("unimplemented case found: " + step);
    }

    public static String visit(TraversalSideEffectStep<?> step) {

        if (step.getLocalChildren().size() != 1)
            throw new RuntimeException("unimplemented case found: " + step);
        List<Step> steps = step.getLocalChildren().get(0).getSteps();

        StringBuilder sb = new StringBuilder();
        sb.append("\\text{sideEffect}{");
        sb.append(traversalToLatex(steps));
        sb.append("}");

//         sb.append("\\text{sideEffect} \\left ( {\\begin{matrix} ");
//         sb.append(traversalToLatex(steps));
//         sb.append("\\end{matrix}} \\right )");

//        sb.append("\\text{sideEffect}^{ ");
//        sb.append(traversalToLatex(steps));
//        sb.append(" }");
        return sb.toString();
    }

    public static String visit(TraversalFlatMapStep step) {

        if (step.getLocalChildren().size() != 1)
            throw new RuntimeException("unimplemented case found: " + step);
        List<Step> steps = ((Admin) step.getLocalChildren().get(0)).getSteps();

        StringBuilder sb = new StringBuilder();

        sb.append("\\text{flatMap}{");
        sb.append(traversalToLatex(steps));
        sb.append("}");

//        sb.append("\\text{flatMap} \\left ( {\\begin{matrix} ");
//        sb.append(traversalToLatex(steps));
//        sb.append("\\end{matrix}} \\right )");

//        sb.append("\\text{flatMap}^{ ");
//        sb.append(traversalToLatex(steps));
//        sb.append(" }");
        return sb.toString();
    }

    public static String visit(VertexStep<?> step) {
        String s="";
        if(step.returnsEdge()){
            s="E";
        }
        String dir = step.getDirection().toString().toLowerCase();

        if (step.getEdgeLabels().length == 0)
            return String.format("\\text{%s%s}", dir, s);

        if (step.getEdgeLabels().length == 1){
            String lab = step.getEdgeLabels()[0];
            return String.format("\\text{%s%s}_\\mathtt{%s}", dir, s, lab);
        }

        throw new RuntimeException("unimplemented case found: " + step);
    }

    public static String visit(EdgeVertexStep step) {
        String dir = step.getDirection().toString().toLowerCase();

        return "\\text{" + dir + "V}";
    }

    public static String visit(AddVertexStep step) {
        return "\\text{addV}_{" + visitParameters(step.getParameters()) + "}";
    }

    public static String visit(AddEdgeStep step) {
        return "\\text{addE}_{" + visitParameters(step.getParameters()) + "}";
    }
    public static String visit(AddPropertyStep step){
        return "\\text{property}_{" + visitParameters(step.getParameters()) + "}";
    }
    public static String visit(IdentityStep step){
        return "\\text{identity}";
    }
    public static String visit(LambdaSideEffectStep step){
        return "\\text{sideEffect}(\\text{LAMBDA})";
    }
    public static String visit(LambdaMapStep step){
        return "\\text{map}(\\text{LAMBDA})";
    }
    public static String visit(LambdaFlatMapStep step){
        return "\\text{flatMap}(\\text{LAMBDA})";
    }
    public static String visit(HasStep step){
        return "\\text{has}_{" + visitHasContainers(step.getHasContainers()) + "}";
    }
    public static String visit(WherePredicateStep step){

        if(!step.getPredicate().isPresent())
            throw new RuntimeException("unimplemented case found: " + step);

        if(step.getLocalChildren().size() != 1)
            throw new RuntimeException("unimplemented case found: " + step);

        P pred = (P) step.getPredicate().get();

        if(pred.getBiPredicate().toString() != "eq")
            throw new RuntimeException("unimplemented case found: " + step);
        String t =((TokenTraversal) step.getLocalChildren().get(0)).getToken().toString();

        return String.format("\\text{where}_{%s=%s}", t, pred.getValue());
    }
    public static String visit(CountGlobalStep step){
        return "\\text{count}";
    }
    public static String visit(OrderGlobalStep step){

        if(step .getComparators().size() != 1)
            throw new RuntimeException("unimplemented case found: " + step);

        Pair p = (Pair) step .getComparators().iterator().next();

        String key = ((ElementValueTraversal) p.getValue(0)).getPropertyKey();
        return String.format("\\text{order}_{\\{\\mathit{in}=\\mathtt{%s}, \\mathit{by}=\\mathtt{%s}\\}}", p.getValue(1), key);
    }
    public static String visit(TailGlobalStep step){
        // note: limit is private field in TailGlobalStep, and the interface is missing
        String s = step.toString();
        return "\\text{tail}_{" + s.substring(s.indexOf("(") + 1, s.lastIndexOf(")")) +"}";
    }
    public static String visit(CoalesceStep step){

        List<Traversal> traversals = (List<Traversal>) step.getLocalChildren();

        StringBuilder sb = new StringBuilder();
//          sb.append("\\text{coalesce}^{");
//          sb.append("}");

//        sb.append("\\text{coalesce}{\\left.\\begin{cases}");
//        sb.append(System.lineSeparator());
//        for (Traversal t : traversals) {
//            sb.append(traversalToLatex(t));
//            sb.append("\\\\");
//        }
//        sb.append("\\end{cases}\\right\\}");

        sb.append("\\text{coalesce} { \\left \\downarrow \\begin{matrix}");
        for (Traversal t : traversals) {
            sb.append(traversalToLatex(t));
            sb.append("\\\\");
        }
        sb.append("\\end{matrix} \\right \\downarrow }");

        return sb.toString();
    }
    public static String visit(SackValueStep step){
        if (step.getLocalChildren().size() != 1)
            throw new RuntimeException("unimplemented case found: " + step);

        String t = traversalToLatex((Traversal) step.getLocalChildren().get(0));

        return "\\text{sack}{"+t+"}";
    }
    public static String visit(SelectOneStep step){
        if(step.getPop() != Pop.last)
            throw new RuntimeException("unimplemented case found: " + step);
        
        if (step.getScopeKeys().size() != 1)
            throw new RuntimeException("unimplemented case found: " + step);

        return "\\text{select}_\\mathtt{"+step.getScopeKeys().iterator().next()+"}";
    }
    public static String visit(AggregateStep step){
        return "\\text{aggregate}_\\mathtt{" + step.getSideEffectKey() + "}";
    }
    public static String visit(SideEffectCapStep step){
        if ( step.getSideEffectKeys().size() != 1)
            throw new RuntimeException("unimplemented case found: " + step);
        return "\\text{cap}_\\mathtt{" + step.getSideEffectKeys().iterator().next() + "}";
    }

    public static String visit(UnfoldStep step){
        return "\\text{unfold}";
    }
    public static String visit(NoneStep step){
        return "\\text{none}";
    }
    public static String visit(StartStep step){
        return "\\_\\_";
    }

    public static String visit(Step<?,?> step){
        throw new RuntimeException(step.getClass() + " is unimplemented when tried to process " + step);
    }

    private static String visitHasContainers(List<HasContainer> hasContainers) {

        StringBuilder sb = new StringBuilder();
        if(hasContainers.size() > 1)
            sb.append("\\{");

        String delim = "";
        for (HasContainer hasContainer : hasContainers) {
            sb.append(delim);
            sb.append(visitHasContainer(hasContainer));
            delim = ", ";
        }

        if(hasContainers.size() > 1)
            sb.append("\\}");


        return sb.toString();
    }

    private static String visitHasContainer(HasContainer hasContainer) {

        P pred = (P) hasContainer.getPredicate();
        if(pred.getBiPredicate().toString().equals("eq")){
            return String.format("\\mathtt{%s}%s\\mathtt{%s}", 
                    hasContainer.getKey(), 
                    "=", 
                    (String) hasContainer.getValue());
        } else if (pred instanceof OrP){
            // note: usually preds.size()>1  (otherwise why use an or?)
            List<P> preds = (List<P>) ((OrP) hasContainer.getPredicate()).getPredicates();

            StringBuilder sb = new StringBuilder();
            sb.append("\\{");

            String delim="";
            for (P p : preds) {
                if (!p.getBiPredicate().toString().equals("eq"))
                    throw new RuntimeException("unimplemented case found: " + hasContainer);
                sb.append(delim);
                sb.append(p.getValue());
                delim = ", ";
            }

            sb.append("\\}");

            return String.format("\\mathtt{%s} \\in \\mathtt{%s}", 
                    hasContainer.getKey(), 
                    sb.toString());
        } else {
            throw new RuntimeException(hasContainer.getBiPredicate().toString() + " predicate is unimplemented when tried to process " + hasContainer);

        }

    }

    private static String visitHasContainer(Object o) {
        throw new RuntimeException(o.getClass() + " is unimplemented when tried to process " + o);
    }

    private static String visitParameters(Parameters parameters) {
        StringBuilder sb = new StringBuilder();

        if(parameters.getRaw().size() > 1){
            sb.append("\\{");
        }

        String delim = "";
        for (Entry<Object, List<Object>> entry : parameters.getRaw().entrySet()) {
            if(entry.getValue().size() != 1)
                throw new RuntimeException("unimplemented case found: " + parameters);
            sb.append(delim);
            String value;
            if(entry.getValue().get(0) instanceof Traversal)
                value = traversalToLatex((Traversal) entry.getValue().get(0));
            else
                value = entry.getValue().get(0).toString();

            sb.append(String.format("\\mathtt{%s}=\\mathtt{%s}", entry.getKey(), value));
            delim = ", ";
        }

        if(parameters.getRaw().size() > 1){
            sb.append("\\}");
        }
        
        return sb.toString();
    }
*/
}
