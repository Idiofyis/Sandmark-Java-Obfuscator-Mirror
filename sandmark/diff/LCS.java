package sandmark.diff;

/** Uses dynamic programming to provide two static methods for 
 *  the Longest Common Subsequence operation.
 *  @author Zach Heidepriem
 */

public class LCS {
    static int[][] C;
    static char[][] B;
    static int m, n;
    
    /** Gets the length of the LCS of two sequences of objects, using 
     *  the <code>equals</code> method
     *  @param X the first sequence to perform LCS on
     *  @param Y the second sequence to perform LCS on
     *  @return the length of the lcs
     */    
    public static int getLength(java.lang.Object[] X, 
                                java.lang.Object[] Y){	
	m = X.length;
	n = Y.length;
	
	C = new int[m+1][n+1];
	B = new char[m+1][n+1];

	for(int i = 0; i <= m; i++)
	    C[i][0] = 0;
	for(int i = 0; i <= n; i++)
	    C[0][i] = 0;

	for(int i = 1; i <= m; i++){
	    for(int j = 1; j <= n; j++){
		if(X[i-1].equals(Y[j-1])){
		    C[i][j] = 1 + C[i-1][j-1];
		    B[i][j] = 'D';
		}
		else if(C[i-1][j] >= C[i][j-1]){
		    C[i][j] = C[i-1][j];
		    B[i][j] = 'U';
		}
		else {
		    C[i][j] = C[i][j-1];
		    B[i][j] = 'L';
		}	  
	    }
	}
	return C[m][n]; //This is the length of the LCS	  
    }    

    /** Get the LCS of two sequences using the <code>equals()</code> method
     *  @param a the first sequence to perform LCS on
     *  @param b the second sequence to perform LCS on
     *  @return 2 vectors, each containing Integer objects containing indices
     *  of elements in the lcs of the respective arrays.
     */
    public static java.util.Vector[] getSubsequence(java.lang.Object[] a,
                                                    java.lang.Object[] b){
        java.util.Vector v[] = new java.util.Vector[2];
	v[0] = new java.util.Vector();
	v[1] = new java.util.Vector();
	getLength(a,b);	
	int i = m; //The length of sequence X
	int j = n; //The length of sequence Y

        while( i > 0 && j > 0 ){
	    if( B[i][j]=='D'){	
		//System.out.println(X[i] + "=?" +  Y[j]);		
                v[0].add(new Integer(i-1));
		v[1].add(new Integer(j-1));		
                i--;
		j--;              
	    }
	    else if(B[i][j]=='U') i--;
	    else j--;	           
	}
	return v;
    }
    
    /** Get the LCS of two sequences
     *  @param a the first sequence to perform LCS on
     *  @param b the second sequence to perform LCS on
     *  @return 2 vectors, each containing Integer objects containing indices
     *  of elements in the lcs of the respective arrays.
     */
    public static java.util.Vector[] getSubsequence(int[] a, int[] b){
        Integer[] aobj = new Integer[a.length];
        for(int i = 0; i < a.length; i++)
            aobj[i] = new Integer(a[i]);
        Integer[] bobj = new Integer[b.length];
        for(int i = 0; i < b.length; i++)
            bobj[i] = new Integer(b[i]);
        return getSubsequence(aobj, bobj);
    }
    
    /** Gets the length of the LCS of two sequences of objects, using 
     *  the <code>equals</code> method
     *  @param a the first sequence to perform LCS on
     *  @param b the second sequence to perform LCS on
     *  @return the length of the lcs
     */
    public static int getLength(int[] a, int[] b){
        Integer[] aobj = new Integer[a.length];
        for(int i = 0; i < a.length; i++)
            aobj[i] = new Integer(a[i]);
        Integer[] bobj = new Integer[b.length];
        for(int i = 0; i < b.length; i++)
            bobj[i] = new Integer(b[i]);
        return getLength(aobj, bobj);
    }
}

