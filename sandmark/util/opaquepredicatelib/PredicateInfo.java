package sandmark.util.opaquepredicatelib;

public class PredicateInfo {
    private final int mSupportedValues;
    private final int mPredType;
    PredicateInfo(int predType,int supportedValues) 
    { mPredType = predType ; mSupportedValues = supportedValues; }
    public int getSupportedValues() { return mSupportedValues; }
    public int getType() { return mPredType; }
}
