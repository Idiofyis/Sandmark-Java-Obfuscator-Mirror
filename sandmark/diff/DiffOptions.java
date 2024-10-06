package sandmark.diff;

/** A class for storing and accessing diff options.
 *  @author Zach Heidepriem
 */

public class DiffOptions {

    private double filter;
    private boolean filterNames,filterBodies;    
    private int ignoreLimit;
    private int objectCompare;
    public static final int COMPARE_ALL_PAIRS = 0;
    public static final int COMPARE_BY_NAME = 1;

    /** Create a new set of DiffOptions with default values:
     *  objectCompare = true
     *  ignoreLimit = 10
     *  filter = .5
     *  filterNames = false
     *  filterBodies = true
    */
    public DiffOptions(){
        objectCompare = COMPARE_BY_NAME;
        ignoreLimit = 10;        
        filter = .5;
        filterNames = false;
        filterBodies = true;
    }
    
    /** @return the comparison method, either COMPARE_ALL_PAIRS
     *  or COMPARE_BY_NAME
     */
    public int getObjectCompare(){
        return objectCompare;
    }

    public void setObjectCompare(int comparisonMethod){
        objectCompare = comparisonMethod;
    }
    /** @return a value between 0-1 that determines which pairs of object
     *  are returned by an algorithm.
     */
    public double getFilter(){
        return filter;
    }

    public void setFilter(double f){
        filter = f;
    }

    /** @return true if objects with the same name should be filtered out
     */
    public boolean getFilterNames(){
        return filterNames;
    }

    public void setFilterNames(boolean b){
        filterNames = b;
    }

    /** @return true if objects with the same data should be filtered out
     */
    public boolean getFilterBodies(){
        return filterBodies;
    }

    public void setFilterBodies(boolean b){
        filterBodies = b;
    }
    
    /** @return the minimium size of method bodies that should be diffed
     */
    public int getIgnoreLimit(){
        return ignoreLimit;
    }
    
    public void setIgnoreLimit(int i){
        ignoreLimit = i;
    }

    /** @return a String representation of these options
     */
    public String toString(){
        String c = "";
        if(objectCompare == COMPARE_ALL_PAIRS)
            c = "all pairs";
        else if(objectCompare == COMPARE_BY_NAME)
            c = "by name";
        
        return "\nFilter: " + getFilter() +
            "\nFilter identical object names: " + getFilterNames() +
            "\nFilter identical object bodies; " + getFilterBodies() +
            "\nIgnore methods with less than " + getIgnoreLimit() +
            "instructions" +
            "\nCompare " + c;
    }
}
