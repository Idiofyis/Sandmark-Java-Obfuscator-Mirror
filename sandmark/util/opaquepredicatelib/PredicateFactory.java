package sandmark.util.opaquepredicatelib;

public class PredicateFactory {
    private static final String predicateInfoMethodName =
	"getInfo";
    private Class mPredicateGeneratorClass;
    private java.lang.reflect.Constructor cons;
    private PredicateInfo mInfo;
    PredicateFactory(Class predicateGenerator) {
	mPredicateGeneratorClass = predicateGenerator;
    }
    public OpaquePredicateGenerator createInstance() {
	try {
	    return (OpaquePredicateGenerator)
		mPredicateGeneratorClass.newInstance();
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new Error();
	}
    }
    public PredicateInfo getPredicateInfo() {
	if(mInfo == null) {
	    java.lang.reflect.Method infoMethod;
	    try { 
		infoMethod =
		    mPredicateGeneratorClass.getMethod
		    (predicateInfoMethodName,
		     new Class[0]);
		mInfo = 
		    (PredicateInfo)infoMethod.invoke(null,new Object[0]);
	    } catch(Exception e) {
		e.printStackTrace();
		throw new Error("can't call getInfo on " + 
				mPredicateGeneratorClass.getName());
	    }
	}
	return mInfo;
    }
}
