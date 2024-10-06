
package sandmark.util.opaquepredicatelib;

/**
 * The OpaqueManager class encapsulates the various opaque predicate libraries.
 * This forms an interface between the user and the detailed low level
 * implementation of the various opaque predicates such as 'algebraic' predicates,
 * 'heap/alias analysis' predicates, 'thread contention' predicates, etc.
 *  Each of these predicates are implemented in there own different classes
 *  extending from a base class.
 *  @author Ashok Purushotham Ramasamy Venkatraj (ashok@cs.arizona.edu)
 *          Tapas R. Sahoo(tapas@cs.arizona.edu)
 */

public class OpaqueManager {
    private static boolean DEBUG = false;
    public static final int PV_TRUE = 1;
    public static final int PV_FALSE = 2;
    public static final int PV_UNKNOWN = 4;

    public static final int PT_ALGEBRAIC = 1;
    public static final int PT_THREAD = 2;
    public static final int PT_INT_OP = 3;
    public static final int PT_OBJECT_OP = 4;
    public static final int PT_STRING_OP = 5;
    public static final int PT_DATA_STRUCTURE_OP = 6;

    private static PredicateFactory [] mPredicates;

    private OpaqueManager() {}

    public static PredicateFactory [] getPredicates() {
	loadPredicates();
	return (PredicateFactory [])mPredicates.clone();
    }
    private static void loadPredicates() {
	if(mPredicates != null)
	    return;

	java.util.Collection predicateNames =
	    sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	    (sandmark.util.classloading.IClassFinder.PREDICATE_GENERATOR);
	java.util.ArrayList classes = new java.util.ArrayList();
	for(java.util.Iterator it = predicateNames.iterator() ; 
	    it.hasNext() ; ) {
	    String className = (String)it.next();
	    try {
		classes.add(new PredicateFactory(Class.forName(className)));
	    } catch(Exception e) {}
	}
	mPredicates = 
	    (PredicateFactory [])classes.toArray(new PredicateFactory[0]);
    }
    public static PredicateFactory [] getPredicatesByType(int type) {
	loadPredicates();
	return getPredicatesByType(type,mPredicates);
    }
    public static PredicateFactory [] getPredicatesByType
	(int type,PredicateFactory [] predicates) {
	java.util.ArrayList preds = new java.util.ArrayList();
	for(int i = 0 ; i < predicates.length ; i++) {
	    PredicateInfo info = predicates[i].getPredicateInfo();
	    if(info.getType() == type)
		preds.add(predicates[i]);
	}
	return (PredicateFactory [])preds.toArray(new PredicateFactory[0]);
    }
    public static PredicateFactory [] getPredicatesByValue(int value) {
	loadPredicates();
	return getPredicatesByValue(value,mPredicates);
    }
    public static PredicateFactory [] getPredicatesByValue
	(int value,PredicateFactory [] predicates) {
	java.util.ArrayList preds = new java.util.ArrayList();
	for(int i = 0 ; i < predicates.length ; i++) {
	    PredicateInfo info = predicates[i].getPredicateInfo();
	    if((info.getSupportedValues() & value) != 0)
		preds.add(predicates[i]);
	}
	return (PredicateFactory [])preds.toArray(new PredicateFactory[0]);
    }
}

