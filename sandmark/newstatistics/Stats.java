package sandmark.newstatistics;

/**
 *  This class is a collection of StatisticsRecords. It builds these
 *  records from an Application object input. It can find a record,
 *  access it and provide statistical data about the classes it records.
 *
 *       @author          Tapas R. Sahoo
 */

public class Stats
{
    private sandmark.program.Application myAppObject;

    private java.util.Vector myRecords;
    private java.util.Vector myPackages;

    private int totalNumberOfClasses = 0;
    private int numberOfAbstractClasses = 0;
    private int totalNumberOfMethods = 0;
    private int totalNumberOfPublicMethods = 0;
    private int avgNumberOfMethods = 0;

    private int totalNumberOfInstanceVariables = 0;
    private int avgNumberOfInstanceVariables = 0;

    private java.util.Vector numArrayDimensions = new java.util.Vector(10,1);

    private int numberOfMetrics=6;
    private int numberOfMethodMetrics=4;


    /*
    private boolean DEBUG = true;
    private boolean LOOP = true;
    */

    private boolean DEBUG = false;


    /**
     *  Constructs a new Statistics collection.
     *  @param appObject The Application which contains the classes to analyze.
     */
    public Stats (sandmark.program.Application appObject)
    {
        if(DEBUG)System.err.println("Constructing new Statistics...");
        myAppObject = appObject;

        if(DEBUG)
            sandmark.util.Log.message
                (0, " Extracting code statistics for application: " +
                 myAppObject.getName());

        myRecords = new java.util.Vector();
        myPackages = new java.util.Vector();

        java.util.Iterator itr = myAppObject.classes();
        sandmark.analysis.classhierarchy.ClassHierarchy chierarchy = myAppObject.getHierarchy();

        totalNumberOfClasses=0;

        /* run through every class in the Application Object */
        while(itr.hasNext()) {

            totalNumberOfClasses++;
            sandmark.program.Class myClassObj = (sandmark.program.Class)itr.next();
            org.apache.bcel.classfile.ConstantPool cp =
                (myClassObj.getConstantPool()).getConstantPool();
            String className = myClassObj.getName();
            if(DEBUG) System.out.println("fullClassName: " + className);

            java.util.Collection classes = new java.util.ArrayList(1);
            classes.add(className);

            /* Set up the className and the packageName for this StatisticsRecord object*/
            StatisticsRecord stat = new StatisticsRecord();
            String fullName = className;
            fullName = fullName.replace('.', '/');
            String name = parseClass(fullName);
            String packageName = parsePackage(fullName);

            if(DEBUG) {
                System.out.println(" fullName     -> "+ fullName);
                System.out.println(" className    -> "+ name);
                System.out.println(" packageName  -> "+ packageName);
            }
            stat.setClassName(name);

            if(!packageName.equals("") && packageName.lastIndexOf("/")==(packageName.length()-1))
                packageName = packageName.substring(0, packageName.length()-1);
            stat.setPackageName(packageName);

            if(!this.findPackage(packageName))
                myPackages.add(packageName);

            if(myClassObj.isAbstract())
                numberOfAbstractClasses++;

            /* get the class hierarchy information */
            if(chierarchy==null)
                if(DEBUG) System.out.println(" chierarchy is NULL ... ");
            else {
                try {
                    sandmark.program.Class tmp[] =
                        chierarchy.inheritanceChain
                        (chierarchy.lookup(className));
                    String inheritanceChain[] = new String[tmp.length];
                    for(int i = 0; i < tmp.length; i++)
                        inheritanceChain[i] = tmp[i].getName();

                    int hierarchyLevel = 0;
                    for(; hierarchyLevel< inheritanceChain.length; hierarchyLevel++)
                        if( inheritanceChain[hierarchyLevel].equals(className) )
                            break;
                    if(hierarchyLevel == inheritanceChain.length) {
                        System.out.println(" Error in reading inheritance depth ");
                        System.exit(0);
                    }
                    if(DEBUG) System.out.println(" hierarchy level for the class -> " + hierarchyLevel);
                    stat.setClassHierarchyLevel(hierarchyLevel);
                }catch(sandmark.analysis.classhierarchy.ClassHierarchyException exc) {
                    System.out.println(" Exception -> " + exc);
                    System.exit(1);
                }
            }

            /* get the number of inherited methods */
            int numberOfMethodsInherited = 0;

            sandmark.program.Class superclasses[] = myClassObj.getSuperClasses();
            if(superclasses!= null) {
                for(int k=0; k <superclasses.length; k++) {
                    String superclassname = superclasses[k].getName();
                    if(DEBUG) System.out.println(" superclassname : " + superclassname);
                    if(superclassname.startsWith("java.")||
                       superclassname.startsWith("javax."))
                        continue;

                    sandmark.program.Method[] supermethods = superclasses[k].getMethods();
                    for(int m=0; m<supermethods.length; m++) {
                        if(superclasses[k].isPublic() || superclasses[k].isProtected())
                            numberOfMethodsInherited++;
                    }
                }
            }
            stat.setNumberOfMethodsInherited(numberOfMethodsInherited);

            int publicmethodCount=0, privatemethodCount=0, protectedmethodCount=0, nonpublicmethodCount=0;
            int instancemethodCount = 0, classmethodCount=0;
            int totalNumberOfApiCalls = 0; // total api call in a class
            /* get the number of conditional statements */
            int numOfCondStats = 0;
            /* get number of method invokes */
            int apiCalls = 0;
            /* get number of scalars and vectors */
            int numArray = 0;
            int numNonArray = 0;
            /* get the number of overridden methods */
            int numberOfMethodsOverridden = 0;

            // print the inheritance chain for this class :
            if(DEBUG) {
                System.out.println(" Inheritance chain ... \n");
                String classchain[] = null;
                try {
                    sandmark.program.Class[] tmp =
                        chierarchy.inheritanceChain(chierarchy.lookup(className));
                    classchain = new String[tmp.length];
                    for(int i = 0; i < tmp.length; i++)
                        classchain[i] = tmp[i].getName();
                }catch(sandmark.analysis.classhierarchy.ClassHierarchyException ex) {
                    System.out.println(" Exception for i-chain : "+ ex);
                    System.exit(0);
                }
                if(classchain==null)
                    System.out.println(" Inheritance chain is null ");
                else {
                    for(int p=0; p<classchain.length; p++)
                        System.out.print("&"+classchain[p]);
                    System.out.println("");
                }
            }

            sandmark.program.Method[] methods = myClassObj.getMethods();
            sandmark.util.MethodID[] mids = null;
            try{
                mids = chierarchy.getMethods(chierarchy.lookup(className));
            }catch(sandmark.analysis.classhierarchy.ClassHierarchyException ex){
                System.out.println(" Exception while extracting MethodIDs : " + ex);
                System.exit(0);
            }

            int mlength = 0;
            if(methods != null){
                for(int m=0; m<methods.length; m++) {

                    if(mids==null) {
                        System.out.println(" Check code: (1)Error in extracting MethodIDs ");
                        System.exit(0);
                    }
                    if(methods.length!=mids.length) {
                        System.out.println(" Check code: (2)Error in extracting MethodIDs ");
                        System.exit(0);
                    }

                    if(DEBUG) {
                        System.out.println(" classname for mids[] -> " + mids[m].getClassName());
                        System.out.println(" methodname for mids[] -> " + mids[m].getName());
                    }
                    sandmark.util.MethodID smid[] = null;
                    String midclassname = (mids[m].getClassName()).replace('/', '.');
                    sandmark.util.MethodID tempmid =
                        new sandmark.util.MethodID( mids[m].getName(), mids[m].getSignature(), midclassname);
                    try {
                        smid = chierarchy.overrides(tempmid);
                    } catch(sandmark.analysis.classhierarchy.ClassHierarchyException ex) {
                        System.out.println(" Exception while extracting overridden methods : " + ex);
                        System.exit(0);
                    }

                    if(smid != null) {
                        if(smid.length > 1) { // since 1 count is for self overridding !
                            if(DEBUG) {
                                System.out.println(" is methodoverridden : "+smid.length);
                                for(int y=0; y<smid.length; y++)
                                    System.out.println(smid[y].getName()+":"+smid[y].getClassName());
                            }
                            numberOfMethodsOverridden++;
                        }
                    }


                    int maxlocals = methods[m].getMaxLocals();
                    org.apache.bcel.generic.LocalVariableGen lg[] = methods[m].getLocalVariables();
                    if(DEBUG) {
                        System.out.print(" maxlocals = " + maxlocals);
                        if(lg==null)
                            System.out.println("  : localvargens = 0 ");
                        else
                            System.out.println("  : localvargens = " +  lg.length);
                    }
                    if(lg!=null) {
                        for(int l=0; l<lg.length; l++) {
                            org.apache.bcel.generic.Type lvtype = lg[l].getType();
                            if(lvtype!=null) {
                                try{
                                    org.apache.bcel.generic.ArrayType atype =
                                        (org.apache.bcel.generic.ArrayType)lvtype;
                                    /* Get the dimensions of the array & store it in a vector;
                                     * the vector length goes in sync with the numArray */
                                    numArrayDimensions.addElement(new Integer(atype.getDimensions()));
                                    numArray++;
                                }catch(java.lang.ClassCastException ex) {
                                    numNonArray++;
                                }
                            }
                        }
                    }

                    if(methods[m].isPublic()) {
                        if(DEBUG) System.out.println(" is public ");
                        publicmethodCount++;
                    }
                    else {
                        if(methods[m].isProtected())
                            protectedmethodCount++;
                        if(methods[m].isPrivate())
                            privatemethodCount++;

                        if(DEBUG) System.out.println(" not public ");
                        nonpublicmethodCount++;
                    }

                    if(!methods[m].isStatic()) {
                        if(DEBUG) System.out.println(" is static");
                        instancemethodCount++;
                    }
                    else {
                        if(DEBUG) System.out.println(" is non-static ");
                        classmethodCount++;
                    }

                    org.apache.bcel.generic.InstructionList ilist = methods[m].getInstructionList();
                    org.apache.bcel.generic.Instruction ins[] = null;
                    if(ilist!= null)
                        ins = ilist.getInstructions();
                    if(ins != null) {
                        for(int r=0; r<ins.length; r++) {
                            if(DEBUG) System.out.println(" Instruction : " + ins[r].toString(cp));
                            if( (ins[r].toString()).startsWith("invoke"))
                                /* ensure its not a invoke to java.lang , or constructor */
                                if( ((ins[r].toString(cp)).indexOf("invokevirtual java") == -1) &&
                                            ((ins[r].toString(cp)).indexOf("<init>") == -1 ) ) {
                                    if(DEBUG) System.out.println(" is a apiCall ");
                                    apiCalls++;
                                }
                            if( (ins[r].toString()).startsWith("if"))
                                numOfCondStats++;
                       }
                    }
                    if(DEBUG) System.out.println("\n\n");
                }/* field info is updated later in the constructor; so not committed yet*/
                mlength = methods.length;
            }

            stat.setNumberOfMethodsOverridden(numberOfMethodsOverridden);
            stat.setNumberOfInstanceMethods(instancemethodCount);
            stat.setNumberOfClassMethods(classmethodCount);
            stat.setNumberOfMethodsAdded(mlength);
            totalNumberOfMethods += mlength;

            stat.setNumberOfApiCalls(apiCalls);
            totalNumberOfApiCalls += apiCalls;

            stat.setNumberOfMethodsInvoked(totalNumberOfApiCalls);
            stat.setNumberOfPublicMethods(publicmethodCount);
            stat.setNumberOfProtectedMethods(protectedmethodCount);
            stat.setNumberOfPrivateMethods(privatemethodCount);

            totalNumberOfPublicMethods+= publicmethodCount;

            stat.setNumberOfConditionalStatements(numOfCondStats);

            /* get number of inherited classes */
            if(chierarchy!=null) {
                try {
                    sandmark.program.Class tmp[] =
                        chierarchy.subClasses(chierarchy.lookup(className));
                    String subClasses[] = new String[tmp.length];
                    for(int i = 0; i < tmp.length; i++)
                        subClasses[i] = tmp[i].getName();

                    stat.setNumberOfSubClasses(subClasses.length-1);
                    /* NOTE: same class also included in the subclass list;
                     * so inorder to discard it, -1 */
                } catch(sandmark.analysis.classhierarchy.ClassHierarchyException exc) { // CC
                    System.out.println(" Exception --> " + exc);
                    System.exit(1);
                }
            }


            /* _____  Start Field statistic development ____ */

            sandmark.program.Field field[] = myClassObj.getFields();

            int statFields = 0, nonBasicFields = 0;
            if(field != null) {
                for(int n=0; n<field.length; n++) {

                    if( field[n].isStatic() )
                        statFields++;
                    else {
                        org.apache.bcel.generic.Type ftype = field[n].getType();
                        if(DEBUG) System.out.println(" NONSTATFIELDBY TYPE -> " + ftype.toString());
                        stat.addNonStaticField(ftype.toString());
                        if(!isBasicType(ftype))
                            nonBasicFields++;
                        try {
                            org.apache.bcel.generic.ArrayType atype =
                               (org.apache.bcel.generic.ArrayType)ftype;
                            numArray++;
                            /* get the dimensions of the array & store it in a vector;
                             * the vector length goes in sync with the numArray */
                            numArrayDimensions.addElement( new Integer(atype.getDimensions()));
                        }catch(java.lang.ClassCastException ex) {
                            numNonArray++;
                        }
                   }
                }
                totalNumberOfInstanceVariables += field.length;
            }
            stat.setNumberOfScalars(numNonArray);
            stat.setNumberOfVectors(numArray);
            stat.setVectorDimensions(numArrayDimensions);
            // numArrayDimensions.removeAllElements();

            stat.setNumNonBasicFields(nonBasicFields);
            stat.setNumStaticFields(statFields);

            /*  get the sandmark.program.Mmethod objects and store them in a record */

            stat.setMethods(methods);
            myRecords.add(stat);
        }

        avgNumberOfMethods = totalNumberOfMethods/totalNumberOfClasses;
        avgNumberOfInstanceVariables = totalNumberOfInstanceVariables/totalNumberOfClasses;

    } /* ________________  end of constructor ; stats evaluation  _____________ */


    /**
     *  Displays most of the code statistics in the class
     */
    public void findAllRecords()
    {
        for(int i = 0; i < myRecords.size(); i++) {
            StatisticsRecord stat = (StatisticsRecord)myRecords.get(i);
            System.out.println("\n packagename = " + stat.getPackageName());
            System.out.println("classname = " + stat.getClassName());

            System.out.println("number of methods = " + stat.getNumberMethods());
            System.out.println("public methods = " + stat.getNumberOfPublicMethods());
            System.out.println("instance methods = " + stat.getNumberOfInstanceMethods());

            System.out.println("nonBasicFields = " + stat.getNumNonBasicFields());
            System.out.println("static fields = " + stat.getNumStaticFields());
            System.out.println("scalars = " + stat.getNumberOfScalars());
            System.out.println("vectors = " + stat.getNumberOfVectors());

            System.out.println("cond. statements = " + stat.getNumberOfConditionalStatements());

            System.out.println("class hierarchy level = " + stat.getClassHierarchyLevel());
            System.out.println("subclasses = " + stat.getNumberOfSubClasses());

            System.out.println("methods inherited = " + stat.getNumberOfMethodsInherited());
            System.out.println("methods overridden = " + stat.getNumberOfMethodsOverridden());
            System.out.println("methods invoked = " + stat.getNumberOfMethodsInvoked());
            System.out.println("methods in scope = " + stat.getNumberOfMethodsInScope());
        }
        return;
    }

    /**
     *  Returns 'true' if the parameter is a basic type, else returns false.
     */
    private boolean isBasicType(org.apache.bcel.generic.Type type)
    {
       if( (type ==  org.apache.bcel.generic.Type.BOOLEAN) ||
           (type ==  org.apache.bcel.generic.Type.BYTE) ||
           (type ==  org.apache.bcel.generic.Type.CHAR) ||
           (type ==  org.apache.bcel.generic.Type.DOUBLE) ||
           (type ==  org.apache.bcel.generic.Type.FLOAT) ||
           (type ==  org.apache.bcel.generic.Type.INT) ||
           (type ==  org.apache.bcel.generic.Type.LONG) ||
           (type ==  org.apache.bcel.generic.Type.SHORT) ||
           (type ==  org.apache.bcel.generic.Type.STRING) )
           return true;
       return false;
    }

    /**
     *  This method derives the package name from the full-name.
     *  For example: given 'sandmark.newstatistics.Stats', this method
     *  would return * 'sandmark/newstatistics'
     */
    static String parsePackage(String fullName)
    {
        String result = "";
        java.util.StringTokenizer st = new java.util.StringTokenizer(fullName, "/");
        while(st.hasMoreTokens())
        {
            if(st.countTokens() > 1)
                result += st.nextToken() + "/";
            else
                break;
        }
        return result;
    }

    /**
     *  This method derives the class name from the full-name.
     *  For example: given 'sandmark.newstatistics.Stats', this method
     *  would return 'Stats'
     */
    static String parseClass(String fullName)
    {
        String result = "";
        java.util.StringTokenizer st = new java.util.StringTokenizer(fullName, "/");
        while(st.hasMoreTokens())
        {
            if(st.countTokens() == 1)
                result += st.nextToken();
            else
                st.nextToken();
        }
        return result;
    }


    /**
     *  Internal method which returns a reference to a StatisticsRecord
     *  in the collection with given name.
     */
    private StatisticsRecord findClassRecord(String className)
    {
        for(int i = 0; i < myRecords.size(); i++)
            if(((StatisticsRecord)myRecords.get(i)).getClassName().equals(className))
                return (StatisticsRecord)myRecords.get(i);
        return null;
    }



    /**
     *  Improvement to the above method;
     *  the above method does not support multiple classes of same name in different
     *  packages; subsequently giving 'method' record not found error.
     */
    private StatisticsRecord findRecord(String packageName, String className)
    {
        for(int i = 0; i < myRecords.size(); i++)
            if(((StatisticsRecord)myRecords.get(i)).getClassName().equals(className) &&
               ((StatisticsRecord)myRecords.get(i)).getPackageName().equals(packageName))
                return (StatisticsRecord)myRecords.get(i);
        return null;
    }

    public java.util.List getByteCode()
    {
        java.util.List result = new java.util.Vector();

        for(int i = 0; i < myPackages.size(); i++) {
            String packageName = (String)myPackages.get(i);
            result.add(this.getByteCodeByPackage(packageName));
        }
        return result;
    }

    public java.util.List getByteCodeByPackage(String packageName)
    {
        java.util.List result = new java.util.Vector();

        java.util.List packages = this.getListOfClassesByPackageName(packageName);
        for(int i = 0; i < packages.size(); i++) {
            String className = (String)packages.get(i);
            result.add(this.getByteCodeByClassName(packageName, className));
        }
        return result;
    }

    /**
     *  Returns the number of the method in specified class, or -1 if class does not exist.
     */
    public int getNumMethods(String packageName, String className)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumberMethods();
    }

    /**
     *  Returns the names of all of the methods in the class specified
     */
    public String[] getNames(String packageName, String className)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return null;
        return stat.getNames();
    }


    /**
     *  Returns the classname at the specified index
     *  @return the name of the class at <i>index</i>
     *  @param index the index at which to get the classname
     */
    public String getClassNameAt(int index)
    {
        String result = "";
        if(index < this.getNumClasses())
            result = ((StatisticsRecord)myRecords.get(index)).getClassName();
        return result;
    }

    /**
     *  Returns the number of classes in this application object
     */
    public int getNumClasses()
    {
        return myRecords.size();
    }

    /**
     *  Given a specific index, this method returns the packageName at that index
     *  @return the name of the package at <i>index</i>
     *  @param index the index at which to get the packageName
     */
    public String getPackageNameAt(int index)
    {
        String result = "";
        if(index < this.getNumClasses())
            result = ((StatisticsRecord)myRecords.get(index)).getPackageName();
        return result;
    }

    public String getPackageName(String className)
    {
       StatisticsRecord stat = null;

       if( (stat=findClassRecord(className)) == null)
          return null;

       return stat.getPackageName();
    }

    /**
     * Returns the number of packages in this application object
     */
    public int getNumPackages()
    {
        return myPackages.size();
    }

    /**
     *  Returns a list of all of the classNames are in the specified package.
     *  @return a list of all of the classNames are in the specified package.
     *  @param packageName the name of the package from which to get classNames.
     */
    public java.util.List getListOfClassesByPackageName(String packageName)
    {
        java.util.Vector result = new java.util.Vector();
        for(int i = 0; i < myRecords.size(); i++)
            if(packageName.equals(this.getPackageNameAt(i)))
                result.add(this.getClassNameAt(i));
        return result;
    }

    public java.util.Vector getAllClassNames()
    {
       java.util.Vector classNames = new java.util.Vector(10,2);
       java.util.List packages = this.getPackageNames();
       for(int k=0; k<packages.size(); k++) {
          String packageName = (String)packages.get(k);
          java.util.List classes = getListOfClassesByPackageName(packageName);
          for(int m=0; m<classes.size(); m++)
             classNames.addElement(packageName + ":" + (String)classes.get(m));
       }

       return classNames;
    }

    public java.util.Vector getAllMethodNames()
    {
       java.util.Vector methodNames = new java.util.Vector(10,2);
       java.util.List packages = this.getPackageNames();
       for(int k=0; k<packages.size(); k++) {
          String packageName = (String)packages.get(k);
          java.util.List classes = getListOfClassesByPackageName(packageName);
          for(int m=0; m<classes.size(); m++) {
             String className = (String)classes.get(m);
             String methods[] = this.getNames(packageName, className);
             for(int j=0; j<methods.length; j++)
                methodNames.addElement(packageName + ":" + className + ":" + methods[j]);
          }
       }

       return methodNames;
    }

    /**
     * Returns the number of classes in the specified package.
     */
    public int getNumClassesInPackage(String packageName)
    {
        int result = 0;

        for(int i = 0; i < myRecords.size(); i++)
            if(packageName.equals(this.getPackageNameAt(i)))
                result++;
        return result;
    }

    /**
     * Returns a list of all of the packages in this Application object,
     * discounting duplicates.
     */
    public java.util.List getPackageNames()
    {
        java.util.Collections.sort(myPackages);
        return myPackages;
    }

    /**
     *  Checks to see if the named package is in this collection.
     *  @return true if package exists in this collection, false if it does not.
     *  @param packageToFind the name of the package to look for in this collection.
     */
    public boolean findPackage(String packageToFind)
    {
        for(int i = 0; i < myPackages.size(); i++)
            if(((String)myPackages.get(i)).equals(packageToFind))
                return true;
        return false;
    }

    public int getPackageIndex(String packageToFind)
    {
       for(int i = 0; i < myPackages.size(); i++)
          if(((String)myPackages.get(i)).equals(packageToFind))
             return(i);
       return(-1);
    }

    public java.util.List getByteCodeByClassName(String packageName, String className)
    {
        java.util.List result = new java.util.Vector();

        String[] methodNames = this.getNames(packageName, className);

        for(int i = 0; i < methodNames.length; i++) {
            String methodName = methodNames[i];
            result.add(getByteCode(packageName, className, methodName));
        }
        return result;
    }

    /**
     *  Returns the bytecode of a specified method.
     *  @return the <code>List</code> representation of the bytecode.
     *  @param className the name of the class.
     *  @param methodName the name of the method from which to get the bytecode.
     */
    public java.util.List getByteCode(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;
        java.util.List result = null;

        if((stat = findRecord(packageName, className)) == null)
            return null;
        if((result = stat.getMethodByteCodes(methodName)) == null )
            return null;
        return result;
    }

    public String getMethodByteCodeUsage(String packageName, String className, String methodName) {
        StatisticsRecord stat = null;
        String result = null;

        if((stat = findRecord(packageName, className)) == null)
            return null;
        if((result = stat.getMethodByteCodeUsage(methodName)) == null )
            return null;
        return result;
    }


    public java.util.Hashtable getByteCodeUsage(String packageName, String className, String methodName) {
        StatisticsRecord stat = null;
        java.util.Hashtable result = null;

        if((stat = findRecord(packageName, className)) == null) {
            return null;
    }
        if((result = stat.getByteCodeUsage(methodName)) == null ) {
            return null;
    }

        return result;
    }

    /**
     *  Returns the size of the method in bytes, or -1 if the class or method does not exist
     *  @return the number of bytes in the method, or -1 if the class or method does not exist
     *  @param className the name of the class
     *  @param methodName the name of the method from which to get the size.
     */
    public int getMethodSizeInBytes(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getMethodSize(methodName);
    }

    /**
     *  Returns true if specified method throws or catches exceptions, or false if it does not.
     *  @return true if specified method throws or catches exceptions, or false if it does not.
     *  @param className the name of the class
     *  @param methodName the name of the method to investigate
     */
    public boolean throwsCatchesExceptions(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return false;
        return stat.throwsCatchesExc(methodName);
    }

    public int getNumNonStaticFields(String packageName, String className)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumNonStaticFields();
    }

    /**
     *  Returns the number of fields in the specified class which are
     *  static, or -1 if the class does not exist
     */
    public int getNumberOfStaticFields(String packageName, String className)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumStaticFields();
    }

    /**
     *  Returns the number of fields, by type in the specified class which are
     *  non-static, or -1 if the class does not exist
     */
    public int getNumNonStaticFieldsByType(String packageName, String className, String type)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumNonStatFieldsByType(type);
    }

    /**
     *  Returns the number of fields that are not basic in the specified class,
     *  or -1 if the class does not exist
     */
    public int getNumFieldsNonBasicTypes(String packageName, String className)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumNonBasicFields();
    }

    /**
     *  Returns true if specified method uses forward branches, or false if it does not.
     */
    public boolean hasForwardBranches(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return false;
        return stat.hasForwardBranch(methodName);
    }

    /**
     *  Returns true if specified method uses backward branches, or false if it does not.
     */
    public boolean hasBackwardBranches(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return false;
        return stat.hasBackwardBranch(methodName);
    }

    /**
     *  Returns a <code>List</code> of all the Non-Static fields in the specified class.
     */
    public java.util.List getNonStaticFields(String packageName, String className)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return null;
        return stat.getNonStaticFields();
    }

    /**
     *  Returns true if specified method calls other static methods, or false if it does not.
     */
    public boolean callsStaticMethods(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return false;

        return stat.callsStaticMethods(methodName);
    }

    /**
     * Returns true if specified method calls other dynamic methods, or false if it does not.
     */
    public boolean callsDynamicMethods(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return false;

        return stat.callsDynamicMethods(methodName);
    }



    /* _____  new stat implementation starts here  ____ */

    public int getNumberOfOpcodesInMethod( String packageName, String className, String methodName, String opcode)
    {
        if( findRecord(packageName, className) == null)
            return -1;

        String methodNames[] = this.getNames(packageName, className);
        if(methodNames==null) {
            System.out.println("Method " + methodName + " does not exist in the specified class");
            return -1;
        }

        java.util.Hashtable opcodeHash = new java.util.Hashtable();
        for(int k=0; k<methodNames.length; k++) {

            methodNames[k] = methodNames[k].substring(0, methodNames[k].indexOf('(') );
            if( methodNames[k].equals("Constructor") )
                methodNames[k] = "<init>";

            if( methodNames[k].equals(methodName) ) {
                opcodeHash = this.getByteCodeUsage(packageName, className, methodNames[k]);
                if( opcodeHash.get(opcode) != null )
                    return ((Integer)opcodeHash.get(opcode)).intValue();
                else {
                    return 0;
        }
            }
        }

        System.out.println("Method " + methodName + " does not exist in the specified class");
        return (-1);
    }

    public int getNumberOfOpcodesInClass(String packageName, String className, String opcode)
    {
        if(findRecord(packageName, className) == null)
            return -1;

        int opcodeCount = 0;
        String methodNames[] = this.getNames(packageName, className);
        if(methodNames==null)
            return -1;
        java.util.Hashtable opcodeHash = new java.util.Hashtable();
        for(int k=0; k<methodNames.length; k++) {
            opcodeHash = this.getByteCodeUsage(packageName, className, methodNames[k]);
        if(opcodeHash.get(opcode)==null)
            continue;
            opcodeCount += ((Integer)opcodeHash.get(opcode)).intValue();
        }

        return opcodeCount;
    }

    public int getNumberOfOpcodesInPackage(String packageName, String opcode)
    {
        java.util.List  classNames = this.getListOfClassesByPackageName(packageName);
        int opcodeCount = 0;
        for(int k=0; k<classNames.size(); k++)
            opcodeCount +=
                this.getNumberOfOpcodesInClass(packageName, (String)classNames.get(k), opcode);

        return opcodeCount;
    }


    public sandmark.program.Method getMethodEditor(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return null;

        return stat.getMethodEditor(methodName);
    }



    /**
     *  ________________ modules for 'method size' __________________
     */

    public int getNumberOfStatmentsInMethod(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumberOfStatementsInMethod(methodName);
    }

    public int getNumberOfMessageSends(String packageName, String className, String methodName)
    {
       if(findRecord(packageName, className) == null)
            return -1;
       // TBD: ... calls the 'getNumberOfMessageSends' in 'StatisticsRecord' class
       return -1;
    }


    /**
     *  ________________ modules for 'class size' __________________
     */

    public int getNumberOfPublicMethods(String packageName, String className)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return -1;

        return stat.getNumberOfPublicMethods();
    }

    public int getNumberOfPrivateMethods(String packageName, String className)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return -1;

        return stat.getNumberOfPrivateMethods();
    }

    public int getNumberOfProtectedMethods(String packageName, String className)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return -1;

        return stat.getNumberOfProtectedMethods();
    }

    public int getNumberOfInstanceMethods(String packageName, String className)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return -1;

        return stat.getNumberOfInstanceMethods();
    }

    /**
     *  this method returns the average number of instance methods per class
     *  in this application object
     */
    public int getAvgNumberOfInstanceMethods()
    {
        return avgNumberOfMethods;
    }

    public int getNumberOfInstanceVariables(String packageName, String className)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return -1;

        return stat.getNumberOfInstanceVariables();
    }

    /**
     *  this method returns the average number of instance variables per class
     *  in this application object
     */
    public int getAvgNumberOfInstanceVariables()
    {
        return avgNumberOfInstanceVariables;
    }

    /**
     *  this method calculates the number of class methods (ie. static methods) in a class
     */
    public int getNumberOfClassMethods(String packageName, String className)
    {
        /* to be implemented in the StatiticsRecord file , currently it returns 'true' */
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return -1;

        return stat.getNumberOfClassMethods();
    }


    /**
     *  _______________ modules for 'method Internals' _____________
     */

    /** To Be Implemented ...
    public int getCFGdepth(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null) {
            System.out.println(" no Records found for class: " + className);
            return -1;
        }

        return stat.getCFGdepth(methodName);
    }
    */


    public int getNumberOfApiCalls(String packageName, String className)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null) {
            System.out.println(" no Records found for class: " + className);
            return -1;
        }

        return stat.getNumberOfApiCalls();
    }

    /**
     *  Returns the number of loops in the method. Here we are only taking care of "proper regions",
     *  so, we just extract them based on knowledge of backedges. Note that there can be loops
     *  forming improper regions( ones find in unstructured languages); that has not been implemented
     *  over here yet
     */
    public int getNumberOfloops(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;

        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumberOfloops(methodName);
    }


    public String[] getNamesOfMethodsInvoked(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return null;

        return stat.getNamesOfMethodsInvoked(methodName);
    }

    public int getNumberOfMethodParams(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null) {
            System.out.println(" no Records found for class: " + className);
            return -1;
        }

        return stat.getNumberOfMethodParams(methodName);
    }

    public int getNumberOfCondStatsInMethod(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
           return -1;
        return stat.getNumberOfConditionalStatementsInMethod(methodName);
    }

    /**
     *  returns the number of non-array locals in the method
     */
    public int getNumberOfScalarLocals(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumberOfScalarLocals(methodName);
    }

    /**
     *  returns the number of array locals in the method
     */
    public int getNumberOfVectorLocals(String packageName, String className, String methodName)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return -1;
        return stat.getNumberOfVectorLocals(methodName);
    }

    /**
     *  Returns the 'number of dimensions' of all the vectors(non-array locals) in the method
     */
    public int[] getMethodVectorDimensions(String packageName,
                                    String className, String methodName)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
            return null;
        return stat.getMethodVectorDimensions(methodName);
    }


    /**
     *  _______________ modules for 'class internals' _____________
     */


    public int getNumberOfConditionalStatements(String packageName, String className)
    {
        StatisticsRecord stat = null;
        if((stat = findRecord(packageName, className)) == null)
           return -1;
        return stat.getNumberOfConditionalStatements();
    }

    public int getAverageNumberOfMethodParams(String packageName, String className)
    {
        if(findRecord(packageName, className) == null)
            return -1;

        String methodnames[] = getNames(packageName, className);
        if(methodnames==null)
            return 0;

        int mParams = 0;
        for(int i=0; i<methodnames.length; i++)
            mParams+= getNumberOfMethodParams(packageName, className, methodnames[i]);

        return mParams/methodnames.length;
    }

    public int getNumberOfScalars(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;

       return stat.getNumberOfScalars();
    }

    public int getNumberOfVectors(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;

       return stat.getNumberOfVectors();
    }

    public java.util.Vector getVectorDimensions(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return null;

       return stat.getVectorDimensions();
    }


    /**
     *  _______________ modules for 'class externals' _____________
     */

    public int getNumberOfMethodsInvoked(String packageName, String className)
    {

       // use API --> getNumberOfApiCalls() in constructor;
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;
       return stat.getNumberOfMethodsInvoked();
    }

    private int getNumberOfInheritedMethods(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;
       return stat.getNumberOfMethodsInherited();
    }

    public int getNumberOfMethodsInScope(String packageName, String className)
    {
       // use API --> getNumberOfPublicMethods() ;
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;
       int currentClassMethods = stat.getNumberOfPublicMethods();

       int inheritedMethods = getNumberOfInheritedMethods(packageName, className);
       // ie. protected methods from the ancestors that are 'public'ly extended .. >>>>TBD:

       return totalNumberOfPublicMethods+inheritedMethods-currentClassMethods;
    }


    /**
     *  _______________ modules for 'class inheritance' _____________
     */

    public int getClassHierarchyLevel(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;
       return stat.getClassHierarchyLevel();
    }

    public int getNumberOfSubClasses(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;
       return stat.getNumberOfSubClasses();
    }

    public int getNumberOfAbstractClasses()
    {
       return numberOfAbstractClasses;
    }

    /* this method finds the number of methods in the class that are inherited from
       more than one class */
    public int getNumberOfmultipleInheritance(String packageName, String className)
    {
       // TBD: not supported in java!
       return 0;
    }


    /**
     *  _______________ modules for 'method inheritance' _____________
     */

    public int getNumberOfMethodsInherited(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;
       return stat.getNumberOfMethodsInherited();
    }

    public int getNumberOfMethodsAdded(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;
       return stat.getNumberOfMethodsAdded();
    }

    public int getNumberOfMethodsOverridden(String packageName, String className)
    {
       StatisticsRecord stat = null;
       if((stat = findRecord(packageName, className)) == null)
          return -1;
       return stat.getNumberOfMethodsOverridden();
    }


    /**
     *  ___________________  Metrics specific modules ____________
     */

    public sandmark.program.Application getApplicationObject()
    {
        return myAppObject;
    }

    public int getNumberOfMetrics()
    {
        return numberOfMetrics;
    }

    public int getNumberOfMethodMetrics()
    {
        return numberOfMethodMetrics;
    }


    public void setNumberOfMetrics(int numMetrics)
    {
        numberOfMetrics=numMetrics;
    }

   /**
      Returns the Application level Metric names.
      @return an array of fully qualified class names of Application level
      metrics
   */
   public static String[] getMetricNames(){
      return getMetricNames(sandmark.util.classloading.IClassFinder.APP_METRIC);
   }

   public static String[] getMethodMetricNames(){
      return getMetricNames(sandmark.util.classloading.IClassFinder.METHOD_METRIC);
   }

   public static String[] getClassMetricNames(){
      return getMetricNames(sandmark.util.classloading.IClassFinder.CLASS_METRIC);
   }

   private static String[] getMetricNames(int classFinderID){
      java.util.Collection appMetrics =
         sandmark.util.classloading.ClassFinder.getClassesWithAncestor
         (classFinderID);
      return (String[])(appMetrics.toArray(new String[appMetrics.size()]));
   }


    private static sandmark.metric.Metric[] getMetrics(int metricType){
        java.util.Collection metrics =
            sandmark.util.classloading.ClassFinder.getClassesWithAncestor
            (metricType);

        java.util.ArrayList metricInstances = new java.util.ArrayList();

        java.util.Iterator itr = metrics.iterator();
        while(itr.hasNext()){
            String className = (String)itr.next();
            try {
                Class c = Class.forName(className);
                java.lang.reflect.Method getInstance =
                    c.getMethod("getInstance", new Class[0]);
                metricInstances.add(getInstance.invoke(null, new Object[0]));
            } catch(Exception e) {
               throw new Error();
            }
        }

        sandmark.metric.Metric obj[] =
            new sandmark.metric.Metric[metricInstances.size()];
        for(int i = 0; i < obj.length; i++){
            obj[i] = (sandmark.metric.Metric)(metricInstances.get(i));
        }
        return obj;
    }
    /**
     *  Returns the Application level Metric objects
     */
    public static sandmark.metric.Metric[] getApplicationMetrics(){
        return getMetrics(sandmark.util.classloading.IClassFinder.APP_METRIC);
    }

    /**
     *  returns the Class level metric objects
     */
    public static sandmark.metric.Metric[] getClassMetrics(){
        return getMetrics(sandmark.util.classloading.IClassFinder.CLASS_METRIC);
    }

    /**
     *  returns the Method level metric objects
     */
    public static sandmark.metric.Metric[] getMethodMetrics(){
        return getMetrics(sandmark.util.classloading.IClassFinder.METHOD_METRIC);
    }
}

