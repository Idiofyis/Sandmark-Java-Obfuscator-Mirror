package sandmark.program;

/**
   Encapsulates the obfuscation user configuration information for one
   application object.  This information includes the level of obfuscation
   that is desired for this object, whether the object is involved in
   threaded code or code that uses reflection, and other properties
   that affect what obfuscations should be run on the object.
   @author Steven Kobes
   @since SandMark 3.1
*/

public class UserObjectConstraints implements java.io.Serializable
{
    private static final boolean DEBUG = false;

    public boolean multithreaded = false;
    public boolean reflection = false;
    public float performanceCritical = 0;
    public float obfuscationLevel = 1;

    private java.util.HashMap myOffAlgorithms;

    public UserObjectConstraints() {
        myOffAlgorithms = new java.util.HashMap();
    }

    public UserObjectConstraints(UserObjectConstraints constraints) {
        copyFrom(constraints);
    }

    public void copyFrom(UserObjectConstraints constraints) {
        multithreaded = constraints.multithreaded;
        reflection = constraints.reflection;
        performanceCritical = constraints.performanceCritical;
        obfuscationLevel = constraints.obfuscationLevel;

        myOffAlgorithms = (java.util.HashMap)constraints.myOffAlgorithms.clone();
    }

    public String toString(){
        return multithreaded + ", " + reflection + ", " + performanceCritical + ", " +
            obfuscationLevel + ", " + myOffAlgorithms;
    }

    public boolean isAlgoOn(sandmark.Algorithm a) {
        return isAlgoOn(a.getShortName());
    }

    public boolean isAlgoOn(String algShortName) {
        return !myOffAlgorithms.containsKey(algShortName);
    }

    public void setAlgoOn(sandmark.Algorithm a, boolean isOn) {
        setAlgoOn(a.getShortName(),isOn);
    }

    public void setAlgoOn(String algShortName, boolean isOn) {
        if(isOn)
            myOffAlgorithms.remove(algShortName);
        else
            myOffAlgorithms.put(algShortName,algShortName);
    }

    /**
       Writes the user constraints for each part of the application to the given
       output stream.
    */
    public static void writeUserConstraints(java.io.OutputStream out,
                                            sandmark.program.Application app)
        throws java.io.IOException{
        //put every object with a user constraint into a hash table and
        //serialize the table
        java.util.HashMap constraints = new java.util.HashMap();


        collectUserConstraints(app, constraints);

        if(DEBUG)System.out.println(constraints);
        //if(constraints.size > 0){
        java.io.ObjectOutputStream oOut = new java.io.ObjectOutputStream(out);
        oOut.writeObject(constraints);
        oOut.flush();
        //}
    }

    private static void collectUserConstraints(sandmark.program.Object obj,
                                               java.util.HashMap constraintMap)
        throws java.io.IOException{
        if(obj.hasUserConstraints())
            constraintMap.put(obj.getCanonicalName(), obj.getUserConstraints());

        java.util.Iterator members = obj.members();
        while(members.hasNext()){
            sandmark.program.Object child = (sandmark.program.Object)members.next();
            collectUserConstraints(child, constraintMap);
        }
    }

    public static void readUserConstraints(java.io.InputStream in,
                                           sandmark.program.Application app)
        throws java.io.IOException, java.lang.ClassNotFoundException{
        java.io.ObjectInputStream oIn = new java.io.ObjectInputStream(in);
        java.util.HashMap constraints = (java.util.HashMap)oIn.readObject();

        if(DEBUG)System.out.println(constraints);

        assignUserConstraints(app, constraints);
    }

    private static void assignUserConstraints(sandmark.program.Object obj,
                                              java.util.HashMap constraintMap)
    {
        String canonicalName = obj.getCanonicalName();
        if(constraintMap.containsKey(canonicalName))
            obj.setUserConstraints((UserObjectConstraints)constraintMap.get(canonicalName));

        java.util.Iterator members = obj.members();
        while(members.hasNext()){
            sandmark.program.Object child = (sandmark.program.Object)members.next();
            assignUserConstraints(child, constraintMap);
        }
    }
}


