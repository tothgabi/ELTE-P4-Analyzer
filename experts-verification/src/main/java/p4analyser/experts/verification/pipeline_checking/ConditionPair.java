/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */

package p4analyser.experts.verification.pipeline_checking;

import java.util.HashSet;

public class ConditionPair {
    private Condition preCondition;
    private Condition postCondition;

    public ConditionPair(Condition pre, Condition post) {
        preCondition = pre;
        postCondition = post;
    }

    public ConditionPair() { 
        preCondition = new Condition();
        postCondition = new Condition();
    }

    public ConditionPair (ConditionPair full) {
        preCondition = full.getPreCondition();
        postCondition = full.getPostCondition();
     }

    public void setPreCondition(Condition pre) {
        preCondition = pre;
    }

    public ConditionPair mergePreConditionsWithCond (ConditionPair cond) {
        Condition resultPre = preCondition;

        for (String valid : cond.getPreCondition().getValid()) {
            resultPre.addValidWithCond(valid);
        }
        for (String invalid : cond.getPreCondition().getInvalid()) {
            resultPre.addInvalidWithCond(invalid);
        }

        System.out.println("Merge:\n" + new ConditionPair(resultPre, cond.getPostCondition()).toString());
        
        return new ConditionPair(resultPre, cond.getPostCondition());
    }

    public Condition getPreCondition () { return preCondition; }
    public Condition getPostCondition () { return postCondition; }

    public void setPostCondition(Condition post) {
        postCondition = post;
    }

    public void setConditions(Condition pre, Condition post) {
        preCondition = pre;
        postCondition = post;
    }

    public void addPreValid (String valid) {
        preCondition.addValid(valid);
    }

    public void addPreInvalid (String invalid) {
        preCondition.addInvalid(invalid);
    }

    public void addPostValid (String valid) {
        postCondition.addValid(valid);
    }

    public void addPostInvalid (String invalid) {
        postCondition.addInvalid(invalid);
    }

    public String continueMerge (ConditionPair nextCond) {
        String error = "";    

        for (String validElem : nextCond.getPreCondition().getValid()) {
            if (!postCondition.hasCond(validElem) || (postCondition.hasCond(validElem) && postCondition.whereCond(validElem).equals("valid"))) {
                preCondition.addValid(validElem);

            } else {
                error += validElem + " ";
            }
            
        }


        for (String vElem : nextCond.getPostCondition().getValid()) {
            postCondition.addValid(vElem);
        }

        for (String invElem : nextCond.getPostCondition().getInvalid()) {
            postCondition.addInvalid(invElem);
        }

        return error;
    }

    public void checkMerge (ConditionPair otherCond) {
        for (String validElem : otherCond.getPreCondition().getValid()) {
            if (!preCondition.hasCond(validElem) || (preCondition.hasCond(validElem) && preCondition.whereCond(validElem).equals("valid"))) {
                preCondition.addValid(validElem);
            } else {
                System.out.println("Error");
            }
        }

        for (String invalidElem : otherCond.getPreCondition().getInvalid()) {
            if (!preCondition.hasCond(invalidElem) || (preCondition.hasCond(invalidElem) && preCondition.whereCond(invalidElem).equals("invalid"))) {
                preCondition.addInvalid(invalidElem);
            } else {
                System.out.println("Error");
            }
        }

        for (String validElem : otherCond.getPostCondition().getValid()) {
            if (!postCondition.hasCond(validElem)) {
                postCondition.addValid(validElem);
            }
        }

        for (String invalidElem : otherCond.getPostCondition().getInvalid()) {
            if (!postCondition.hasCond(invalidElem)) {
                postCondition.addInvalid(invalidElem);
            }
        }
    }

    public HashSet<ConditionPair> toSet() {
        HashSet<ConditionPair> result = new HashSet<ConditionPair>();
        result.add(this);
        return result;
    }

    public String toString() {
        return "\n{Pre = " + preCondition.toString() + "\nPost = " + postCondition + "}";
    }

}
