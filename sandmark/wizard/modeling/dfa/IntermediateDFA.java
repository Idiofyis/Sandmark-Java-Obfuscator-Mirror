package sandmark.wizard.modeling.dfa;

public interface IntermediateDFA
{
    public boolean isNonLoopEdge(sandmark.Algorithm alg1, sandmark.Algorithm a,
                                 sandmark.program.Object t,
                                 sandmark.config.RequisiteProperty prop,
                                 java.util.ArrayList s_q,
                                 java.util.ArrayList s_qp,
                                 java.util.ArrayList r_t);


    public boolean isLoopEdge(sandmark.Algorithm alg1, sandmark.Algorithm a,
                              sandmark.program.Object t,
                              sandmark.config.RequisiteProperty prop,
                              java.util.ArrayList s_q,
                              java.util.ArrayList r_t);


}

