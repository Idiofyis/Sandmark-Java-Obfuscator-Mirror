package sandmark.gui;

public interface AlgorithmPanel {
    ConfigPropertyPanel getCPP();
    void setAlgorithm(sandmark.Algorithm alg);
    sandmark.Algorithm getCurrentAlgorithm();
    sandmark.program.Application getApplication() throws Exception;
}

