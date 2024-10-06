package sandmark.util.newgraph.codec;

/** This class represents a totally balanced binary sequence.
 *  A sequence a = [a_1, a_2, ...., a_2n is a TBBS if:<br>
 *  1) a contains n 0's and n 1's<br>
 *  2) for any i, 1 <= i <= 2n, it holds that:<br>
 *  |{j: 1 <= j <= i, a_i = 0}| >= |{j: 1 <= j <= i, a_i = 1}|.
 *  @author Zach Heidepriem
 *  @see sandmark.util.newgraph.codec.CatalanNumbers
 *  @see sandmark.util.newgraph.codec.PlantedPlaneCubicTree
*/   

public class TotallyBalancedBinarySequence {

    private boolean[] mSequence;
    private java.math.BigInteger mRank;
    private static boolean DEBUG = false;
        
    public TotallyBalancedBinarySequence(java.math.BigInteger bigInt){
        mRank = bigInt;
        int catalanNumber = CatalanNumbers.findMinimumNumber(bigInt);
        mSequence = new boolean[2*catalanNumber];
        if(DEBUG)
            System.out.println("Catalan Number is: " + catalanNumber);
        catalanUnrank(catalanNumber, bigInt);     
    }

    /**Take a sequence of booleans and calculate the corresponding bigInt
     */
    public TotallyBalancedBinarySequence(boolean[] sequence){
        mSequence = sequence;
        int catalanNumber = sequence.length / 2;
        catalanRank(catalanNumber, sequence);
    }

    /**@return the rank of this sequence in its Catalan family
     */
    public java.math.BigInteger getRank(){
        return mRank;
    }

    /**@return the Catalan family this TBBS belongs to.       
     */
    public int getCatalanNumber(){
        return this.size()/2;
    }
    
    /**@return true iff the number at position i in this TBBS is a 1
       @throws IllegalArgumentException if i > this.size()-1 or i < 0;
    */
    public boolean get(int i){
        if(i < 0 || i > this.size()-1)
            throw new IllegalArgumentException
                ("Cannot retrieve value at index " + i + " in the sequence.");
        return mSequence[i];
    }

    /**@return the Catalan sequence
     */
    public boolean[] getSequence(){
        return mSequence;
    }
    
    /**@return the size of this sequence
     */
    public int size(){
        return mSequence.length;
    }

    //Builds this sequence. (from  Kreher, Stinson "Combinatorial Algorithms", 1999)
    private void catalanUnrank(int n, java.math.BigInteger r){
        int y = 0;
        java.math.BigInteger lo = java.math.BigInteger.ZERO;
        for(int x = 1; x <= 2*n; x++){           
            java.math.BigInteger m = M(n, x, y+1);            
            java.math.BigInteger rhs = m.add(lo).subtract(java.math.BigInteger.ONE);                
            if(r.compareTo(rhs) <= 0){
                y++;
                mSequence[x-1] = false;
            }
            else{
                lo = lo.add(m);
                y--;
                mSequence[x-1] = true;
            }
        }
    }

    //Computes this rank. (from  Kreher, Stinson "Combinatorial Algorithms", 1999)
    private void catalanRank(int n, boolean[] sequence){       
        int y = 0;
        java.math.BigInteger lo = java.math.BigInteger.ZERO;
        for(int x = 1; x <= 2*n-1; x++){            
            if(!sequence[x-1])
                y++;
            else {
                lo = lo.add(M(n, x, y+1));
                y--;
            }
        }
        mRank = lo;
    }

    //taken from the same text...
    private static java.math.BigInteger M(int n, int x, int y){
        //2n-x
        int chooseFrom = 2*n-x;   
        //n - (x+y) / 2
        int numToChoose = n - (x+y) / 2;      
        java.math.BigInteger a = sandmark.util.Math.combinations(chooseFrom, numToChoose);
        //n - 1 - (x+y) / 2
        numToChoose--;              
        java.math.BigInteger b = sandmark.util.Math.combinations(chooseFrom, numToChoose);
        return a.subtract(b);           
    } 
    
    /**Displays the rank in parens followed by the sequence
     */
    public String toString(){
        String s = "(" + mRank + ") ";
        for(int i = 0; i < this.size(); i++)
            s += this.get(i) ? "1": "0";
        if(DEBUG)
            s += checkBalanced() ? " is balanced." : " is not balanced.";
        return s;       
    }
    /** For testing. The sequence should always be balanced, i.e. this method should
     *  always return true.
    */
    public boolean checkBalanced(){
        int sum = 0;
        for(int i = 0; i < this.size(); i++){
            sum += (this.get(i) ? -1: 1);
            if(sum < 0)
                return false;
        }
        return true;
    }

    //for testing
    public static void main(String[] args){
	if(args.length == 1) {
	    System.out.println(new TotallyBalancedBinarySequence
			       (new java.math.BigInteger(args[0])));
	    return;
	}

        //PASSES
        for(int i = 0; i < 100000; i++){
            java.math.BigInteger val = java.math.BigInteger.valueOf(i);
            TotallyBalancedBinarySequence tbbs = new TotallyBalancedBinarySequence(val);
            tbbs = new TotallyBalancedBinarySequence(tbbs.getSequence());
            if(tbbs.getRank().intValue() != i)
                System.out.println("ERROR AT: " + i);
        }
    }
}
