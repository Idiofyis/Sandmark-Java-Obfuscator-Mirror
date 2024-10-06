package sandmark.wizard.modeling.dfa;

/**
 * PostreqDFA.java
 *
 *
 * Created: Fri Sep 13 10:11:39 2002
 *
 * @author Kelly T Heffner
 */

public class PostreqDFA extends DFA implements IntermediateDFA
{

    public PostreqDFA(java.util.HashMap rangeMap, 
                      sandmark.Algorithm alg1, 
                      sandmark.config.RequisiteProperty prop,
                      sandmark.Algorithm [] allAlgs,
                      sandmark.program.Object[] allObjects)
    {
        super(rangeMap, alg1, prop, allAlgs, allObjects, false);
        myStartState.setAccept(true);
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
        // \lambda(q, (a,t)) = q^\prime if a = A and
        // s(q) + r(t) = s(q^\prime)
        if(a.equals(alg1)){

            java.util.ArrayList setAddRes = new java.util.ArrayList();
            setAddRes.addAll(s_q);
            setAddRes.addAll(r_t);

            boolean contains = true;
            for(int r = 0; r < setAddRes.size(); r++)
                if(!s_qp.contains(setAddRes.get(r)))
                    contains = false;
            for(int r = 0; r < s_qp.size(); r++)
                if(!setAddRes.contains(s_qp.get(r)))
                    contains = false;
            return contains;
        }

        if(hasProp(a, prop)){
            //s(q) - r(t) = s(q')
            java.util.ArrayList setAddRes = new java.util.ArrayList();
            setAddRes.addAll(s_q);
            setAddRes.removeAll(r_t);

            for(int i = 0; i < setAddRes.size(); i++)
                if(!s_qp.contains(setAddRes.get(i)))
                    return false;
            for(int i = 0; i < s_qp.size(); i++)
                if(!setAddRes.contains(s_qp.get(i)))
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
        if(a.equals(alg1)){
            //r(t) subset of s(q)
            for(int i = 0; i < r_t.size(); i++)
                if(!s_q.contains(r_t.get(i)))
                    return false;
            return true;
        }

        if(hasProp(a, prop)){
            //r(t) intersect s(q) is empty
            for(int i = 0; i < r_t.size(); i++)
                if(s_q.contains(r_t.get(i)))
                    return false;
            return true;
        }

        if(!a.equals(alg1)){
            return !hasProp(a, prop);
        }
        return false;
    }

} // PostreqDFA

