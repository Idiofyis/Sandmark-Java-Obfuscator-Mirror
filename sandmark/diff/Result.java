package sandmark.diff;

/** A Diff result contains two objects with the associated similarity of 
 *  between the two objects.
 *  @author Zach Heidepriem
 */
public class Result implements Comparable {
    sandmark.program.Object object1, object2;
    double similarity;

    /** @param a the first object in this Result
     *  @param b the second object in this Result
     *  @param sim the similarity between a and b
     */ 
    public Result(sandmark.program.Object a,
                  sandmark.program.Object b,
		  double sim){
	object1 = a;
	object2 = b;
	similarity = sim;
    }
    
    public sandmark.program.Object getObject1(){
	return object1;
    }

    public sandmark.program.Object getObject2(){
	return object2;
    }
    
    public double getSimilarity(){
	return similarity;
    }    
    
    /** The natural ordering of Results is descending by similarity.
     */
    public int compareTo(Object o){
	if( ((Result)o).getSimilarity() < this.getSimilarity())
	    return -1;
	else if( ((Result)o).getSimilarity() > this.getSimilarity())
	    return 1;
	else return 0;
    }    
}

