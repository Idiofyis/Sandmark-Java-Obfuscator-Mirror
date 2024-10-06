package sandmark.watermark.objectwm;

/* This class contains all the configuration information required
 * by the watermarking algorithm
 */

public class Config
{
    private int myNumberOfEmbeddingOptions;
    private int myMaxCodeInstructions;
    private String myWatermarkValue;
    private double myRecognitionThreshold;
    private int myMethodEmbedThreshold;
    private int myMethodCopyLowerThreshold;
    private int myMethodCopyUpperThreshold;
 
    private int myMaxMethodOverloads;
    private int myMaxSelectTry;
    private int myBranchNullifyAbortThreshold;
 
    private int myEmbedEffortCount;
 
    public java.util.Vector origVector = new java.util.Vector(10,2);
 
    /*  Constructor
     */
    public Config()
    {
        myNumberOfEmbeddingOptions = 2;
        /* number of new code embedding options, leaving aside substitution*/
  
        myMaxCodeInstructions = 20;
        /* generally used during taking number of substitution Instructions */
  
        myRecognitionThreshold = 90.0;
        /* correlation threshold for recognizer to output watermark " marked" */
  
        myMethodEmbedThreshold = 3;
        /* threshold for new instruction embed */
  
        myMethodCopyLowerThreshold = 0;
        /* lower threshold for method length to overload it */
  
        myMethodCopyUpperThreshold = 200;
        /* upper threshold for method length to overload it */
  
        myMaxMethodOverloads = 5;
        /* used to reduce the execution time */
  
        myMaxSelectTry = 20;
        /* max number of random selects while picking a method/class */
  
        myBranchNullifyAbortThreshold = 10;
        /* max number of branch nullify embed point selct aborts before we try 
         * to direct the branch to the next instruction */
  
        myEmbedEffortCount = 200;
    }
 
    public int getBranchNullifyAbortThreshold()
    {
        return myBranchNullifyAbortThreshold;
    }
 
    public int getMaxTry()
    {
        return myMaxSelectTry;
    }
 
    public int getMaxMethodOverloads()
    {
        return myMaxMethodOverloads;
    }
 
    public int getMethodEmbedThreshold()
    {
        return myMethodEmbedThreshold;
    }
 
    public int getMethodCopyLowerThreshold()
    {
        return myMethodCopyLowerThreshold;
    }
 
    public int getMethodCopyUpperThreshold()
    {
        return myMethodCopyUpperThreshold;
    }
 
    public double getRecognitionThreshold()
    {
        return myRecognitionThreshold;
    }
 
    public void setWatermarkValue(String watermark)
    {
        myWatermarkValue = watermark;
    }
  
    public String getWatermarkValue()
    {
        return myWatermarkValue;
    }
 
    public int getNumberOfEmbeddingOptions()
    {
        return myNumberOfEmbeddingOptions;
    }
 
    public int getMaxCodeInstructions()
    {
        return myMaxCodeInstructions;
    }
 
    public int getEmbedEffortCount()
    {
        return myEmbedEffortCount;
    }
}

