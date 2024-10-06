package sandmark.analysis.controlflowgraph;

public interface CallGenerator {
    void addPhantomCall(MethodCFG srcMethod,Edge srcEdge,MethodCFG destMethod);
}

