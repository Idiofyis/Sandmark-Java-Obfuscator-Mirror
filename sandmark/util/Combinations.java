package sandmark.util;

public class Combinations implements java.util.Iterator {
   private int last[];
   private int n;
   boolean hasNext = true;
   public Combinations(int n,int k) {
      this.n = n;
      last = new int[k];
      for(int i = 0 ; i < k - 1 ; i++)
	 last[i] = i;
      last[k - 1] = k - 2;
      hasNext();
   }
   public boolean hasNext() {
      if(last.length == 0 || n == 0 || n < last.length)
	 return (hasNext = false);

      if(last[last.length - 1] != n - 1)
	 return (hasNext = true);
      for(int i = 0 ; i < last.length - 1; i++)
	 if(last[i] != last[i + 1] - 1)
	    return (hasNext = true);

      return (hasNext = false);
   }
   public Object next() {
      if(!hasNext)
         throw new Error("must check hasNext before calling next");
      
      int pivot;
      for(pivot = last.length - 1 ; 
	  last[pivot] >= n - last.length + pivot ; pivot--)
	 ;
      last[pivot]++;
      for(int i = pivot + 1 ; i < last.length ; i++)
	 last[i] = last[pivot] + i - pivot;
      return last.clone();
   }
   public void remove() { throw new UnsupportedOperationException(); }

   public static void main(String argv[]) {
      for(int i =  1 ; i < 10 ; i++) {
	 for(int j = 1 ; j <= i ; j++) {
	    System.out.print(i + " c " + j + ": ");
	    int count = 0;
	    for(java.util.Iterator c = new Combinations(i,j) ; c.hasNext() ; count++) {
	       int comb[] = (int [])c.next();
	       System.out.print("[");
	       for(int k = 0 ; k < comb.length ; k++)
		  System.out.print(comb[k] + ",");
	       System.out.print("], ");
	    }
	    System.out.println(" (" + count + ") ");
	 }
	 System.out.println();
      }
   }
}
