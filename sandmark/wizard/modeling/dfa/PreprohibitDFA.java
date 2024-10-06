package sandmark.wizard.modeling.dfa;

/**
 * Preprohibit.java
 *
 *
 * Created: Fri Sep 20 14:07:18 2002
 *
 * @author Kelly T Heffner
 */

public class PreprohibitDFA extends DFA implements IntermediateDFA
{

    public PreprohibitDFA(java.util.HashMap rangeMap, 
                          sandmark.Algorithm alg1, 
                          sandmark.config.RequisiteProperty prop,
                          sandmark.Algorithm [] allAlgs,
                          sandmark.program.Object[] allObjects)
    {
        super(rangeMap, alg1, prop, allAlgs, allObjects, true);
        //System.out.println("Number of states & edges before trim" + myStates.length + ", " + myEdges.length);
	this.dotInFile("preprohib_unreach.dot");
        trimUnreachable();
        //System.out.println("Number of states & edges after trim unreachable" + myStates.length + ", " + myEdges.length);
        trimDeadStates();
	//System.out.println("Number of states & edges after trim dead" + myStates.length + ", " + myEdges.length);

        this.dotInFile("preprohib.dot");

    }

    public boolean isNonLoopEdge(sandmark.Algorithm alg1, sandmark.Algorithm a,
                                 sandmark.program.Object t,
                                    sandmark.config.RequisiteProperty prop,
                                    java.util.ArrayList s_q,
                                    java.util.ArrayList s_qp,
                                    java.util.ArrayList r_t)

    {
        // \lambda(q, (a,t)) = q^\prime if a has property p and:
        // s(q) + r(t) = s(q^\prime)
        if(hasProp(a, prop)){

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
        return false;
    }


    public boolean isLoopEdge(sandmark.Algorithm alg1, sandmark.Algorithm a,
                              sandmark.program.Object t,
                                 sandmark.config.RequisiteProperty prop,
                                 java.util.ArrayList s_q,
                                 java.util.ArrayList r_t)
    {
        if(hasProp(a, prop)){
            //r(t) subset of s(q)
            for(int i = 0; i < r_t.size(); i++)
                if(!s_q.contains(r_t.get(i)))
                    return false;
            return true;
        }

        if(a.equals(alg1)){
            //r(t) intersect s(q) is empty
            for(int i = 0; i < r_t.size(); i++)
                if(s_q.contains(r_t.get(i)))
                    return false;
            return true;
        }
        else {//(!a.equals(alg1))
            return !s_q.contains(t);
        }

    }


} // Preprohibit

