package sandmark.watermark.objectwm;

/*  This class implements all the vector frequency update and control operations 
 */

public class VectorUpdateCtrl
{
    private int vecfreq[];
    private int mark[];
    private int zeroFreqFlag[];
    private int vectUpdatesRem;
    private int substSearch[];
 
    /*  Constructor 
     */
    public VectorUpdateCtrl(java.util.Vector wmVector)
    {
        vecfreq = new int[wmVector.size()];
        zeroFreqFlag = new int[wmVector.size()];
        mark = new int[wmVector.size()];
        substSearch = new int[wmVector.size()];
  
        for(int id=0; id<wmVector.size(); id++){
            vecfreq[id] = ((Integer)wmVector.elementAt(id)).intValue();
  
            if (vecfreq[id] != 0)
   	        zeroFreqFlag[id]=0;
            else
  	        zeroFreqFlag[id]=1;
  
            mark[id] = 0;
            substSearch[id] = 1;
        }
        vectUpdatesRem = wmVector.size();
    }
 
    public void setSubstSearch(int vIndex)
    {
        substSearch[vIndex] = 1;
    }
 
    public void unsetSubstSearch(int vIndex)
    {
        substSearch[vIndex] = 0;
    }
 
    public int getSubstSearch(int vIndex)
    {
        return(substSearch[vIndex]);
    }
 
    public int getElementAt(int vIndex)
    {
        return vecfreq[vIndex];
    }
 
    public void setFreqCounterToZero(int vIndex)
    {
        vecfreq[vIndex] = 0;
        if( vecfreq[vIndex] == 0 )
            mark[vIndex] = 1;
        return;
    }
 
    public boolean zerofreqState(int vIndex)
    {
        if(zeroFreqFlag[vIndex]>0){
            if(zeroFreqFlag[vIndex]!=2){
                zeroFreqFlag[vIndex] = 2;
                 vectUpdatesRem--;
            }
            return true;
        } /* zeroFreqFlag = 2   =>   update over & noted down;
                          = 1   =>   initially the vector index was zero;*/
        return false;
    }
 
    public boolean markState(int vIndex)
    {
        if((vecfreq[vIndex]==0) && (mark[vIndex]>0)){
            if(mark[vIndex]!=2){
                mark[vIndex] = 2;
                vectUpdatesRem--;
            }
            return true;
        } /* this vector element update completed */
 
       return false;
    }
 
    public void updateFrequencyCounter(int vIndex)
    {
        vecfreq[vIndex]--;
        if(vecfreq[vIndex]==0)
            mark[vIndex]=1;
        return;
    }
 
    public boolean allUpdatesDone()
    {
        if(vectUpdatesRem<=0)
            return true;
        return false;
    }
 
    public void updateFrequencyCounterInThreshold(int vIndex, int updateVal)
    {
        vecfreq[vIndex] -= updateVal;
        if(vecfreq[vIndex]<=0){
            vecfreq[vIndex]=0;
 	    if(mark[vIndex]==0)
 	        mark[vIndex] = 1;
        }
        return;
    }
 
    public void displayVectorFreq(String headerMessage)
    {
        System.out.println(headerMessage);
        for (int i=0;i<vecfreq.length;i++)
            System.out.print("  " + vecfreq[i]);
        System.out.println();
        return;
    }
}

