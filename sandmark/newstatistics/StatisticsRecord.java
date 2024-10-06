package sandmark.newstatistics;

/**
 * This class contains statistical information about one class including
 * data about its fields, methods and the package in which it is located.
 *
 *       @author     Tapas R. Sahoo
 */
public class StatisticsRecord
{
    private boolean DEBUG = false;
    private boolean myDEBUG = false;

    private String myPackageName;
    private String myClassName;
    private int myNumberOfMethods;
    private int myNumNonBasicFields;
    private int myNumberStaticFields;
    private java.util.Vector myNonStaticFields;
    private sandmark.program.Method[] myMethods;

    private int myNumberOfMethodsAdded; // total number of methods in the class
    private int myNumberOfPublicMethods;
    private int myNumberOfProtectedMethods;
    private int myNumberOfPrivateMethods;
    private int myNumberOfInstanceMethods;
    private int myNumberOfClassMethods;

    private int myNumberOfMethodsInherited;
    private int myNumberOfMethodsInvoked;
    private int myNumberOfMethodsInScope;
    private int myNumberOfMethodsOverridden;

    private int myNumberOfApiCalls;
    private int myNumberOfConditionalStatements;

    private int myClassHierarchyLevel;
    private int myNumberOfSubClasses;

    private int myNumberOfScalars;
    private int myNumberOfVectors;
    private java.util.Vector myVectorDimensions = new java.util.Vector(10,1);

    private class StaticFieldData
    {
        String myType;
        int myCount;

        public StaticFieldData(String type)
        {
            myType = type;
            myCount = 1;
        }
    }

   /**
    *   Constructs a new StatisticsRecord
    */
    public StatisticsRecord()
    {
        myPackageName = "";
        myClassName = "";
        myNumberOfMethods = 0;
        myNumberOfPublicMethods = 0;
        myNumberOfInstanceMethods = 0;
        myNumNonBasicFields = 0;
        myNumberStaticFields = 0;
        myNonStaticFields = new java.util.Vector();
        myMethods = null;
        myNumberOfSubClasses = 0;

        myNumberOfMethodsInherited = 0;
        myNumberOfMethodsInvoked = 0;
        myNumberOfMethodsInScope = 0;
        myNumberOfMethodsOverridden = 0;

        myNumberOfConditionalStatements = 0;

        myNumberOfScalars = 0;
        myNumberOfVectors = 0;
    }


    /**
     *  Sets the class name associated with this record to the one provided.
     */
    public void setClassName(String name)
    {
        myClassName = name;
    }
    
    /**
     * Returns the name of this class.
     */
    public String getClassName()
    {
        return myClassName;
    }

    /**
     * Returns true if this object is the same as the one to which it is being compared.
     * @return true if this object has the same class name and package name as the one to which
     * it is being compared. False if either of these two tests if false.
     */
    public boolean equals(Object o)
    {
        return ((StatisticsRecord)o).getClassName().equals(myClassName)
            && ((StatisticsRecord)o).getPackageName().equals(myPackageName);
    }

    /**
     * Returns the name of the package to which this class belongs.
     */
    public String getPackageName()
    {
        return myPackageName;
    }

    /**
     * Sets the package name of this class to the specified argument.
     */
    public void setPackageName(String toSet)
    {
        myPackageName = toSet;
    }

    /**
     *  Returns the size of the given method in bytes
     *  @return the number of bytes that make up this method
     *  @param methodName the name of the method
     */

    public int getMethodSize(String methodName)
    {
        sandmark.program.Method m = getMethod(methodName);
        if(m ==null)
            return 0;
    org.apache.bcel.generic.InstructionList iList = m.getInstructionList();
    if(iList==null)
        return 0;
    org.apache.bcel.generic.Instruction ins[] = iList.getInstructions();
    if(ins==null)
        return 0;

    int iSize=0;
    for(int k=0; k<ins.length; k++)
        iSize += ins[k].getLength();

        return iSize;
    }


    /**
     *  Returns true if this method throws or catches exceptions, otherwise it returns false.
     *  @return true if this method throws or catches exceptions, false otherwise.
     *  @param methodName the name of the method
     */
    public boolean throwsCatchesExc(String methodName)
    {
        sandmark.program.Method m = null;
        if( (m = getMethod(methodName) ) == null )
            return false;
		org.apache.bcel.generic.CodeExceptionGen ceg[] = 
			m.getExceptionHandlers();

		if(ceg==null) 
			return false;
		if(ceg.length>0) 
			return true;
		else
			return false;
    }

    /**
     *  Returns the number of non-static fields in this class.
     *  @return the number of non-static fields in this class.
     */
    public int getNumNonStaticFields()
    {
        int result = 0;

        for(int r = 0; r < myNonStaticFields.size(); r++){
            StaticFieldData curr = (StaticFieldData)myNonStaticFields.get(r);
            result += curr.myCount;
        }
        return result;
    }

    /**
     * Returns a <code>List</code> of all the Non-Static fields in the specified class.
     * @return a <code>List</code> representation of all non-static field types in the
     * specified  class
     */
    public java.util.List getNonStaticFields()
    {
        java.util.Vector result = new java.util.Vector();
        for(int i = 0; i < myNonStaticFields.size(); i++){
            StaticFieldData curr = (StaticFieldData)myNonStaticFields.get(i);
        if(DEBUG) System.out.println(" extracting NONSTATFIELD ### --> " + curr.myType);
            result.add(curr.myType);
            result.add(new Integer(curr.myCount));
        }
        return result;
    }


    /**
     *  Adds a non-static field object of specified type to the collection.
     *  @param type the type of non-static field to add.
     */
    public void addNonStaticField(String type)
    {
        for(int i = 0; i < myNonStaticFields.size(); i++){
            StaticFieldData curr = (StaticFieldData)myNonStaticFields.get(i);
            
            if(curr.myType != null && curr.myType.equals(type)) {
                curr.myCount++;
                return;
            }
        }
    	if(DEBUG) System.out.println(" adding NONSTATFIELD ### --> " + type);
        myNonStaticFields.add(new StaticFieldData(type));
    }

    /**
     *  Sets the number of static fields to the specified number.
     *  @param number the number of static fields to acknowledge.
     */
    public void setNumStaticFields(int number)
    {
        myNumberStaticFields = number;
    }

   /**
     *  Returns the number of static fields in this class.
     *  @return the number of static fields in this class
     */
    public int getNumStaticFields()
    {
        return myNumberStaticFields;
    }

   /**
     *  Sets the number fields with non-basic types to the specified number
     *  @param numNonBasic the number of fields of non-basic type
     */
    public void setNumNonBasicFields( int numNonBasic )
    {
        myNumNonBasicFields = numNonBasic;
    }

   /**
     *  Returns the number of non-basic fields that this class contains.
     *  @return the number of non-basic fields that this class contains.
     */
    public int getNumNonBasicFields()
    {
        return myNumNonBasicFields;
    }

   /**
     *  Returns the number of methods which this method contains
     *  @return the number of methods which this method contains
     */
    public int getNumberMethods()
    {
        return myMethods.length;
    }

   /**
     *  Sets the methods in this class to the specified array of MethodEditor objects.
     *  @param methods the array of MethodEditor objects to add.
     */
    public void setMethods(sandmark.program.Method[] methods)
    {
        myMethods = new sandmark.program.Method[methods.length];
        for(int k=0; k<methods.length; k++)
            myMethods[k] = methods[k];
    }

    // return the sandmark.program.Method object given a String methodName
    private sandmark.program.Method getMethod(String methodName)
    {
        if(methodName.length() > 10 && methodName.indexOf('(') != -1)
            if(methodName.substring(0, methodName.indexOf('(')).equals("Constructor"))
                methodName = "<init>";

        if(methodName.indexOf('(') != -1)
           methodName = methodName.substring(0, methodName.indexOf('('));

        for (int i = 0; i < myMethods.length; i++)
            if((myMethods[i].getName()).equals(methodName))
                return myMethods[i];

        if(myDEBUG) System.out.println(" In function. getMethod. did not found object ... \n");

        return null;
    }

    /**
     *  Retruns the names of all of the methods in the class specified
     *  @return String array representing all of the method names in the class specified
     */    
    public String[] getNames()
    {
        String[] result = new String[this.getNumberMethods()];
    if(result==null)
        return null;

        if(DEBUG) {
           System.out.println(" packageName --> " + myPackageName );
           System.out.println(" className --> " + myClassName );
           System.out.println(" myMethods.length >> " + myMethods.length);
           System.out.println(" this.getNumberMethods() >> " + this.getNumberMethods());
        }
        
        for(int i = 0; i < result.length; i++) {
            sandmark.program.Method mg = myMethods[i];

            String temp = new String(mg.getName());

            //org.apache.bcel.generic.Type type = mg.getReturnType();
            String sig = mg.getSignature();
            
            if(DEBUG) System.out.println(" temp -----------> " + temp);
            
            //To make entries more readable, this section
            //will allow modification from BLOAT(now BCEL!) returns to
            //a more readable format.
            if(temp.equals("<init>"))
                temp = "Constructor";
            
            //temp += parseType(type);
            temp += parseType(sig); // not  required to 'parse' anymore for bcel format output
            result[i] = temp;
        }
        return result;
    }

   /**
     *  Returns a <code>List</code> of the bytecodes in the specied method.
     *  @return a <code>List</code> of the bytecodes in the specied method.
     *  @param methodName the name of the method from which to draw the bytecodes.
     */
    public java.util.List getMethodByteCodes(String methodName)
    {
        if(DEBUG) System.out.println(" methodName --> " + methodName);
        sandmark.program.Method mg = null;
        if( (mg = getMethod(methodName)) == null ) {
            if(myDEBUG) System.out.println(" method object returned is NULL ");
            return null;
        }

        java.util.List codelist = new java.util.Vector();

        org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
        if(instrlist==null) {
            if(myDEBUG) System.out.println(" instructionlist returned is NULL ");
            return null;
        }
        org.apache.bcel.generic.Instruction ins[] = instrlist.getInstructions();
        if(ins==null) {
            if(myDEBUG) System.out.println(" ins[]  returned is NULL ");
            return null;
        }

        for(int k=0; k<ins.length; k++)
            codelist.add(ins[k]);

		if(myDEBUG) System.out.println(" codelist size = "+codelist.size());
        return codelist;
    }


    public int getNumberOfConditionalStatementsInMethod(String methodName)
    {
       sandmark.program.Method mg = null;
       if( (mg = this.getMethod(methodName)) == null )
          return 0;

       int count = 0;
       org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
       if(instrlist==null)
       return 0;
       org.apache.bcel.generic.Instruction ins[] = instrlist.getInstructions();
       if(ins==null)
       return 0;

       /* go through all of the Instructions in the byte code of this method. */
       for(int k=0; k<ins.length; k++)
           if( (ins[k].toString()).startsWith("if") || (ins[k].toString()).startsWith("goto") ) 
               count++;

       return count;
    }


    public String getMethodByteCodeUsage(String methodName)
    {
        java.util.List aList = this.getMethodByteCodes(methodName);
        sandmark.util.InstructionTree iTree = new sandmark.util.InstructionTree();
        java.util.Iterator itr = aList.iterator();

        while(itr.hasNext()){
            org.apache.bcel.generic.Instruction instr =
                (org.apache.bcel.generic.Instruction)itr.next();

            String str = instr.toString();
        	if(DEBUG) System.out.println(" str1 -> " + str);
        	int id = str.indexOf('[');
        	if(id==-1) {
				id = str.indexOf(' ');
				if(id==-1)
        			id = str.length();
			}
            String x = str.substring(0, id);
            // short x = instr.getOpcode();
            iTree.add(x);
        }

        return iTree.toString();
    }


    /*
     * Returns a hashtable associating instruction with a value indicating how often its called
     */
    public java.util.Hashtable getByteCodeUsage ( String methodName )
    {
        java.util.Hashtable usage = new java.util.Hashtable();
        java.util.List aList = this.getMethodByteCodes(methodName);

        if(aList==null) {
            if(myDEBUG) System.out.println(" aList should not be null !! maybe could not obtain method object ! ");
            if(myDEBUG) System.out.println(" This will unfortunately throw nullpointer exception ! \n");
	    	return null;
        }
            
        java.util.Iterator itr = aList.iterator();

        while(itr.hasNext()){
            org.apache.bcel.generic.Instruction instr =
                (org.apache.bcel.generic.Instruction)itr.next();

            String str = instr.toString();
            if(myDEBUG) System.out.println(" str2 -> " + str);
        	int id = str.indexOf('[');
        	if(id==-1) {
				id = str.indexOf(' ');
				if(id==-1)
            		id = str.length();
			}
        	String x = str.substring(0, id);
            if(myDEBUG) System.out.println(" x -> " + x);

            if(DEBUG) System.out.println( "Pushing: *"+x+"* "+usage.containsKey(x) );
                
            if ( usage.containsKey ( x ) ) {
                Integer count = (Integer)usage.get(x);
                usage.put( x, new Integer ( count.intValue() + 1 ) );
            }
            else
                usage.put( x, new Integer ( 1 ) );
        }
        return usage;
    }


   /**
     *  Returns the number of non-static fields in this class that are of the specified type.
     *  @return the number of non-static fields in this class that are of the specified type.
     *  @param type type to count.
     */
    public int getNumNonStatFieldsByType(String type)
    {
        for(int y = 0; y < myNonStaticFields.size(); y++){
            StaticFieldData curr = (StaticFieldData)myNonStaticFields.get(y);
            if(curr.myType.equals(type))
                return curr.myCount;
        }
        return -1;
    }

    
    public boolean isOfTypeBranch(org.apache.bcel.generic.Instruction instr)
    {
        if( instr instanceof org.apache.bcel.generic.IF_ICMPGT  ||
            instr instanceof org.apache.bcel.generic.IF_ICMPLT  ||
            instr instanceof org.apache.bcel.generic.IF_ICMPGE  ||
            instr instanceof org.apache.bcel.generic.IF_ICMPLE  ||
            instr instanceof org.apache.bcel.generic.IF_ICMPEQ  ||
            instr instanceof org.apache.bcel.generic.IF_ICMPNE  ||
            instr instanceof org.apache.bcel.generic.IFGT ||
            instr instanceof org.apache.bcel.generic.IFLT ||
            instr instanceof org.apache.bcel.generic.IFGE ||
            instr instanceof org.apache.bcel.generic.IFLE ||
            instr instanceof org.apache.bcel.generic.IFEQ ||
            instr instanceof org.apache.bcel.generic.IFNE ||
            instr instanceof org.apache.bcel.generic.GOTO )
             return true;

        return false;
    }

   
   /**
     *  Returns true if there is a forward branch instruction in the method specified.
     *  @return true if there is a forward branch, otherwise false.
     *  @param methodName the name of the method to inspect.
     */
    public boolean hasForwardBranch(String methodName)
    {
        sandmark.program.Method mg = null;
        if( (mg = getMethod(methodName)) == null )
            return false;

        org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
    if(instrlist==null)
        return false;

        instrlist.setPositions();

        org.apache.bcel.generic.InstructionHandle ihs[] = instrlist.getInstructionHandles();
    if(ihs==null)
            return false;
        org.apache.bcel.generic.InstructionHandle targetHandle = null;

        for(int k=0; k<ihs.length; k++) {
            org.apache.bcel.generic.Instruction ins = ihs[k].getInstruction();

            if( isOfTypeBranch(ins) ) {
                targetHandle = ((org.apache.bcel.generic.BranchHandle)ihs[k]).getTarget();
                int targetposition = targetHandle.getPosition();
                if( ihs[k].getPosition() < targetposition ) 
                    return true;
            }
        }
        return false;
    }


   /** 
     *  Returns true if there is a backward branch instruction in the method specified.
     *  @return true if there is a backward branch, otherwise false.
     *  @param methodName the name of the method to inspect.
     */
    public boolean hasBackwardBranch(String methodName)
    {
        sandmark.program.Method mg = null;
        if( (mg = getMethod(methodName)) == null )
            return false;

        org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
    if(instrlist==null)
        return false;
        instrlist.setPositions();

        org.apache.bcel.generic.InstructionHandle ihs[] = instrlist.getInstructionHandles();
    if(ihs==null)
            return false;
        org.apache.bcel.generic.InstructionHandle targetHandle = null;

        for(int k=0; k<ihs.length; k++) {
            org.apache.bcel.generic.Instruction ins = ihs[k].getInstruction();

            if( isOfTypeBranch(ins) ) {
                targetHandle = ((org.apache.bcel.generic.BranchHandle)ihs[k]).getTarget();
                int targetposition = targetHandle.getPosition();
                if( ihs[k].getPosition() < targetposition ) 
                    return true;
            }
        }
        return false;
    }



    /**
     * Returns true if the specified method calls other static methods, otherwise false
     * @return true if the specified method calls other static methods, otherwise false
     * @param methodName the name of the method to investigate
     */
    public boolean callsStaticMethods(String methodName)
    {
        sandmark.program.Method mg = null;
        if( (mg = getMethod(methodName)) == null )
            return false;
        
        org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
    if(instrlist==null)
        return false;
        org.apache.bcel.generic.InstructionHandle ihs[] = instrlist.getInstructionHandles();
    if(ihs==null)
            return false;

        for(int k=0; k<ihs.length; k++) {
            org.apache.bcel.generic.Instruction ins = ihs[k].getInstruction();
            if( (ins.toString()).indexOf("invokestatic") != -1 )
                    return true;
        }

        return false;
    }

    
    /**
     * Returns true if the specified method calls other dynamic  methods, otherwise false
     * @return true if the specified method calls other static methods, otherwise false
     * @param methodName the name of the method to investigate
     */
    public boolean callsDynamicMethods(String methodName)
    {
        sandmark.program.Method mg = null;
        if( (mg = getMethod(methodName)) == null )
            return false;
        
        org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
    if(instrlist==null)
        return false;
        org.apache.bcel.generic.InstructionHandle ihs[] = instrlist.getInstructionHandles();
    if(ihs==null)
            return false;

        for(int k=0; k<ihs.length; k++) {
            org.apache.bcel.generic.Instruction ins = ihs[k].getInstruction();
            if( (ins.toString()).indexOf("invokevirtual") != -1  )
                    return true;
        }

        return false;
    }


    //This method is used to build up the 'type' part of the method
    //signature. It converts things like '[[I' to 'int[][]' and
    //'Ljava/lang/Integer' to 'Integer'.
    private String parseType(String sig) // org.apache.bcel.generic.Type type)
    {
        org.apache.bcel.generic.Type typeArray[] = 
            org.apache.bcel.generic.Type.getArgumentTypes(sig);

        String result = "(";
        int array = 0;

        //for every parameter argument to the method...
        for(int i = 0; i < typeArray.length; i++)
        {
            String param = typeArray[i].toString();
            int n = 0;

        if(DEBUG) System.out.println(" pparam = " + param);
        param = param.replace('.', '/');

            //gets the dimensions of the array, if any.
            while(param.charAt(n) == '['){
                array++;
                n++;
            }

            param = param.substring(n, param.length());

            //This tree goes through the 8 primitive types in Java
            if(param.equals("B"))
                result += "byte";
            else if(param.equals("C"))
                result += "char";
            else if(param.equals("D"))
                result += "double";
            else if(param.equals("F"))
                result += "float";
            else if(param.equals("I"))
                result += "int";
            else if(param.equals("J"))
                result += "long";
            else if(param.equals("S"))
                result += "short";
            else if(param.equals("Z"))
                result += "boolean";
            else if(param.indexOf('/') == -1){
                //if it is not a primitive, it must be a class
                //This case checks for classes without package
                result += param.substring(0, param.length()); //  - 1);
            }
            else{
                //This class parses out the package. For instance:
                //converts: Ljava/lang/String to String
                java.util.StringTokenizer st = new java.util.StringTokenizer(param, "/");
                String name = "";

                while(st.hasMoreTokens())
                    name = st.nextToken();

                result += name.substring(0, name.length()); //  - 1);
            }

            //this part tacks on the dimensions of array to the end.
            while(array > 0){
                result += "[]";
                array--;
            }
            if(i < typeArray.length - 1)
                result += ",";
        }
        return result + ")";
    }


    /**** new StatisticRecord implementation .... ****/

    // used, since getMethod is private !
    public sandmark.program.Method getMethodEditor(String methodName)
    {
       return getMethod(methodName);

    }


    /* ______________ 'method size' implementation __________ */
 

    public int getNumberOfStatementsInMethod(String methodName)
    {
        sandmark.program.Method mg = null;
        if( (mg = getMethod(methodName)) == null )
            return -1;

        org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
    if(instrlist==null)
        return 0;
        org.apache.bcel.generic.Instruction instr[] = instrlist.getInstructions();
        if(instr==null)
            return 0;
        else
            return instr.length;
    }


    public int getNumberOfMessageSends(String methodName)
    {
       // TBD:
       return -1;
    }



    /* ________________ 'class size' implementation _____________ */

    public void setNumberOfMethodsAdded(int numMethods)
    {
       myNumberOfMethodsAdded = numMethods;
       return;
    }  
    public int getNumberOfMethodsAdded()
    {
       return myNumberOfMethodsAdded;
    }

    public void setNumberOfPublicMethods(int numMethods)
    {
       myNumberOfPublicMethods = numMethods;
       return;
    }  
    public int getNumberOfPublicMethods()
    {
       return myNumberOfPublicMethods;
    }

    public void setNumberOfPrivateMethods(int numMethods)
    {
       myNumberOfPrivateMethods = numMethods;
       return;
    }  
    public int getNumberOfPrivateMethods()
    {
       return myNumberOfPrivateMethods;
    }

    public void setNumberOfProtectedMethods(int numMethods)
    {
       myNumberOfProtectedMethods = numMethods;
       return;
    }  
    public int getNumberOfProtectedMethods()
    {
       return myNumberOfProtectedMethods;
    }

    public void setNumberOfInstanceMethods(int numMethods)
    {
       myNumberOfInstanceMethods = numMethods;
       return;
    }
    public int getNumberOfInstanceMethods()
    {
       return myNumberOfInstanceMethods;
    }
 
    public int getNumberOfInstanceVariables()
    {
       int numNonStaticfield = this. getNumNonStaticFields();
       return numNonStaticfield + myNumberStaticFields; 
    }

    public void setNumberOfClassMethods(int numMethods)
    {
       myNumberOfClassMethods = numMethods;
       return;
    }
    public int getNumberOfClassMethods()
    {
      return myNumberOfClassMethods;
    }



    /* ______________ 'method internals' implementation __________ */

	/** This function evaluates the CFG depth based on the maximum depth of loop nesting in 
	 *  the CFG. ... To Be Implemented ... 
	 *
    public int getCFGdepth(String methodName)
    {
        sandmark.program.Method meth = this.getMethod(methodName);
        if(meth==null) {
            System.out.println(" getNumberOfloops : methodObject is null ");
            return -1;
        }
        if(meth.getInstructionList()==null) {
            if(DEBUG) System.out.println(" getCFGdepth: no instructions in this method. returning numloops=0  ");
            return 0;
        }

	    sandmark.analysis.controlflowgraph.MethodCFG mcfg = 
	        new sandmark.analysis.controlflowgraph.MethodCFG(meth);

	    java.util.ArrayList backedgelist = mcfg.getBackEdges();
	    // slot 0->1, 2->3 etc denote the back edges 

		int finalDepth = 0;
        for(int k=0; k<backedgelist.size()-1; k++) {

			int currDepth=1;
			org.apache.bcel.generic.InstructionHandle tailIH = 
				(org.apache.bcel.generic.InstructionHandle)backedgelist.get(k));
			org.apache.bcel.generic.InstructionHandle headIH = 
				(org.apache.bcel.generic.InstructionHandle)backedgelist.get(k+1));

			// get all the other back edges and check for dom/postdom condition 
  	    }
    }
	*/




    public int getNumberOfloops(String methodName)
    {
       int numloopsInMethod = 0;
       sandmark.program.Method mg = this.getMethod(methodName);
        
       if(mg == null) {
          System.out.println(" getNumberOfloops : methodObject is null ");
          return -1;
       }
       if(mg.getInstructionList()==null) {
          if(DEBUG) System.out.println(" getNumberOfloops: no instructions in this method. returning numloops=0  ");
          return numloopsInMethod;
       }

       if(DEBUG) System.out.println(" method name = "+mg.getName()+" : "+ "class name = "+mg.getClassName());

       /* extract the back-edges and the loops in the method CFG */
       sandmark.analysis.controlflowgraph.MethodCFG mcfg =
           new sandmark.analysis.controlflowgraph.MethodCFG(mg, false);

       java.util.ArrayList backedgelist = mcfg.getBackedges();
       if(backedgelist==null)
           numloopsInMethod=0;
       else {
           if((backedgelist.size()%2)!=0) {
               System.out.println(" Error in evaluating back edges in method; check sandmark.analysis.controlflowgraph.MethodCFG code ... \n");
               numloopsInMethod=-1;
           }
           else 
               numloopsInMethod=backedgelist.size()/2;
       }

       return numloopsInMethod;
    }

    public int getNumberOfVectorLocals(String methodName)
    {
       sandmark.program.Method mg = this.getMethod(methodName);
       if(mg == null) {
          System.out.println(" vector : methodEditor null ");
          return -1;
       }

       int numVectors = 0;
       for(int index=0; index< mg.getMaxLocals(); index++) {
          org.apache.bcel.generic.LocalVariableGen lg[] = mg.getLocalVariables();
          if(DEBUG) {
             System.out.print(" maxlocals = " + mg.getMaxLocals());
             if(lg==null)
                 System.out.println("  : localvargens = 0 ");
             else
                 System.out.println("  : localvargens = " +  lg.length);
          }

          if(lg!=null) {
             for(int l=0; l<lg.length; l++) {
                 org.apache.bcel.generic.Type lvtype = lg[l].getType();

                 if(lvtype!=null) {
                     org.apache.bcel.generic.ArrayType atype =
                         (org.apache.bcel.generic.ArrayType)lvtype;
                     numVectors++;
                 }
             }
          }
       }
       return numVectors++;
    }


    public int[] getMethodVectorDimensions(String methodName)
    {
       int numVectors = this.getNumberOfVectorLocals(methodName);
       sandmark.program.Method mg = this.getMethod(methodName);

       int dimensions[] = new int[numVectors];

       org.apache.bcel.generic.LocalVariableGen lg[] = mg.getLocalVariables();
       int numVecs = 0;
       for(int index=0; index< mg.getMaxLocals(); index++) {
          org.apache.bcel.generic.Type lvtype = lg[index].getType();

          if( lvtype != null) {
              org.apache.bcel.generic.ArrayType atype =
                      (org.apache.bcel.generic.ArrayType)lvtype;

              dimensions[numVecs++] = atype.getDimensions();
          }
       }
       return dimensions;
    }


    public int getNumberOfScalarLocals(String methodName)
    {
       sandmark.program.Method mg = this.getMethod(methodName);

       if(mg == null) {
          System.out.println(" scalar: methodEditor null ");
          return -1;
       }

       org.apache.bcel.generic.LocalVariableGen lg[] = mg.getLocalVariables();

       int numScalars = 0;
       for(int index=0; index< mg.getMaxLocals(); index++) {
          org.apache.bcel.generic.Type lvtype = lg[index].getType();
          if( this.isScalarType(lvtype) )
             numScalars++;
       }
       return numScalars;
    }


    private boolean isScalarType(org.apache.bcel.generic.Type type)
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

    public void setNumberOfApiCalls(int numberOfApiCalls)
    {
       myNumberOfApiCalls = numberOfApiCalls;
    }


    public int getNumberOfApiCalls()
    {
       return myNumberOfApiCalls;
    }
 
    public String[] getNamesOfMethodsInvoked(String methodName)
    {
        java.util.Vector mnames = new java.util.Vector(10,2);
        String tempName;
                                     
        sandmark.program.Method mg = this.getMethod(methodName);
        if(mg == null)
          return null;

        org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
    if(instrlist==null)
        return null;
        org.apache.bcel.generic.InstructionHandle ihs[] = instrlist.getInstructionHandles();
    if(ihs==null)
        return null;

        for(int k=0; k<ihs.length; k++) {
            org.apache.bcel.generic.Instruction ins = ihs[k].getInstruction();
            String code = ins.toString();
            if( code.startsWith("invoke") ) {
             
                int sIndex = code.indexOf("L");
                int eIndex =  code.indexOf("(");

                /* check added for print etc. calls */
                tempName  = code.substring(sIndex+1, eIndex-1);

                if( tempName.startsWith("java") &&
                        !tempName.equals("java/lang/Object;.<init>"))
                    continue;
             
                if(tempName.equals("java/lang/Object;.<init>"))
                    tempName = myClassName + ";.<init>";

                mnames.addElement(tempName);
            }
        }

        String methodNames[] = new String[mnames.size()];
        for(int k=0; k<mnames.size(); k++)
            methodNames[k] = (String)mnames.elementAt(k);

        return methodNames;
    }


    public int getNumberOfMethodParams(String methodName)
    {
        sandmark.program.Method mg = this.getMethod(methodName);
        if(mg == null)
            return -1;

        org.apache.bcel.generic.Type type[] = mg.getArgumentTypes();
        if(type == null)
            return 0;

        return type.length;
    }

    


    /* ________________ 'class internal' implementation __________ */

    public void setNumberOfConditionalStatements(int numOfCondStats)
    {
       myNumberOfConditionalStatements = numOfCondStats;
    }
    public int getNumberOfConditionalStatements()
    {
       return myNumberOfConditionalStatements;
    }

    public void setNumberOfScalars(int numScalars)
    {
       myNumberOfScalars = numScalars;
    }
    public int getNumberOfScalars()
    {
       return myNumberOfScalars;
    }

    public void setNumberOfVectors(int numVectors)
    {
       myNumberOfVectors = numVectors;
    }
    public int getNumberOfVectors()
    {
       return myNumberOfVectors;
    }

    public void setVectorDimensions(java.util.Vector numArrayDimensions)
    {

       for(int i=0; i<numArrayDimensions.size(); i++)
          myVectorDimensions.addElement( numArrayDimensions.elementAt(i));
    }
    public java.util.Vector getVectorDimensions()
    {
       return myVectorDimensions;
    }




    /* ____________  'class external' implementation _____________ */

    public void setNumberOfMethodsInvoked(int numberOfmethodsInvoked)
    {
       myNumberOfMethodsInvoked = numberOfmethodsInvoked;
    }
    public int getNumberOfMethodsInvoked()
    {
       return myNumberOfMethodsInvoked;
    }

    public void setNumberOfMethodsInScope(int numberOfmethodsInScope)
    {
       myNumberOfMethodsInScope = numberOfmethodsInScope;
    }
    public int getNumberOfMethodsInScope()
    {
       return myNumberOfMethodsInScope;
    }





    /* ______________ 'class hierarchy' implementation __________ */
    
    public void setClassHierarchyLevel(int hierarchyLevel)
    {
       myClassHierarchyLevel = hierarchyLevel;
    }
    public int getClassHierarchyLevel()
    {
       return myClassHierarchyLevel;
    }

    public void setNumberOfSubClasses(int numberOfSubClasses)
    {
       myNumberOfSubClasses = numberOfSubClasses;
    }
    public int getNumberOfSubClasses()
    {
       return myNumberOfSubClasses;
    }




    /* ______________ 'method hierarchy' implementation ________ */

    public void setNumberOfMethodsInherited(int numberOfMethodsInherited)
    {
       myNumberOfMethodsInherited = numberOfMethodsInherited;
    }
    public int getNumberOfMethodsInherited()
    {
       return myNumberOfMethodsInherited;
    }

    public void setNumberOfMethodsOverridden(int numberOfMethodsOverridden)
    {
       myNumberOfMethodsOverridden = numberOfMethodsOverridden;
    }
    public int getNumberOfMethodsOverridden()
    {
       return myNumberOfMethodsOverridden;
    }
}

