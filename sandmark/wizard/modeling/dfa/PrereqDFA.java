package sandmark.wizard.modeling.dfa;
/**
 * PrereqDFA.java
 *
 *
 * Created: Mon Sep  9 14:13:30 2002
 *
 * @author Kelly T Heffner
 */

public class PrereqDFA extends DFA implements IntermediateDFA
{
    /**
       Creates a DFA modeling alg1 prerequiring property prop.
       @param alg1 the algorithm with a prerequisite
       @param prop the property that alg1 prerequires
       @param allAlgs all of the obfuscation algorithms
       @param allObjects all of the application objects
    */


    public PrereqDFA(java.util.HashMap rangeMap,
                     sandmark.Algorithm alg1, 
                     sandmark.config.RequisiteProperty prop,
                     sandmark.Algorithm [] allAlgs,
                     sandmark.program.Object[] allObjects)
    {
        super(rangeMap, alg1, prop, allAlgs, allObjects, true);
        //System.out.println("Number of states & edges before trim" + myStates.length + ", " + myEdges.length);
        trimUnreachable();
        //System.out.println("Number of states & edges after trim unreachable" + myStates.length + ", " + myEdges.length);
        trimDeadStates();
        //System.out.println("Number of states & edges after trim dead" + myStates.length + ", " + myEdges.length);

    }





    public boolean isNonLoopEdge(sandmark.Algorithm alg1, sandmark.Algorithm a,
                                 sandmark.program.Object t,
                                    sandmark.config.RequisiteProperty prop,
                                    java.util.ArrayList s_q,
                                    java.util.ArrayList s_qp,
                                    java.util.ArrayList r_t)

    {
        // \lambda(q, (a,t)) = q^\prime if a has the property p and
        // s(q) + r(t) = s(q^\prime)
        if(hasProp(a, prop)){
            java.util.ArrayList setAddRes = new java.util.ArrayList();

            setAddRes.addAll(s_q);
            setAddRes.addAll(r_t);

            boolean contains = true;
            for(int r = 0; r < setAddRes.size(); r++)
                if(!s_qp.contains(setAddRes.get(r)))
                    return false;

            for(int r = 0; r < s_qp.size(); r++)
                if(!setAddRes.contains(s_qp.get(r)))
                    return false;

            return true;
        }
        return false;
    }


    public boolean isLoopEdge(sandmark.Algorithm alg1, sandmark.Algorithm a,
                              sandmark.program.Object t,
                                 sandmark.config.RequisiteProperty prop,
                                 java.util.ArrayList s_q,
                                 java.util.ArrayList r_t)

    {

        boolean hasProp = hasProp(a, prop);

        //if a=alg1 and t \in s(q)
        //or a has p and r(t) \subseteq s(q)
        //or a \neq alg1 and does not have p
        return (
                (a.equals(alg1) && s_q.contains(t)) ||
                (hasProp && s_q.containsAll(r_t)) ||
                ( (!a.equals(alg1)) && (!hasProp))
                );
    }

}



