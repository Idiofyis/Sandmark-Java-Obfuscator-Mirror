package sandmark.watermark.ct.embed;

//-----------------------------------------------------------
//-----------------------------------------------------------
/**
 * Adds extra formal arguments to selected method signatures
 * and calls.
 *
*/

public class AddParameters {
private static final boolean Debug=false;

sandmark.util.ConfigProperties props = null;
   sandmark.program.Application app = null;
String[][] storageCreators;
sandmark.util.MethodID[] methods;
java.util.HashSet methodSet;
String watermarkClassName;
sandmark.watermark.ct.trace.callforest.Node domMethodNode;
//-----------------------------------------------------------
//               Constructor and Main Routine
//-----------------------------------------------------------

/*
 * @param app the program to be watermarked.
 * @param storageCreators[][] an array of quadruples:
 *                            {methodName, returnType, localName, GLOBAL/FORMAL}
 *                            for example:
 *                            {"CreateStorage_sm$hash", "java.util.Hashtable", "sm$hash", "FORMAL"}
 * @param methods the list of methods to which we will add the new
 *                arguments in 'storageCreators'.
 */
public AddParameters (
   sandmark.program.Application app,
   sandmark.util.ConfigProperties props,
   String[][] storageCreators,
   sandmark.util.MethodID[] methods,
  sandmark.watermark.ct.trace.callforest.Node domNode){
   this.props = props;
   this.storageCreators = storageCreators;
   this.app = app;
   this.methods = methods;
   domMethodNode=domNode;
   watermarkClassName = props.getProperty("DWM_CT_Encode_ClassName");

}



/*************************************************************************/
/*************************************************************************/
/**
 * Compute the set of methods that will need to change.
 **/
java.util.HashSet changeSet;
void computeMethodsToChange() {
   changeSet = new java.util.HashSet();
   for(int m=0; m < methods.length; m++)
   {   changeSet.add(methods[m]);
	   if(Debug)
	   	System.out.println("@@@@@Adding old methodid= "+methods[m]);
   }

   //newly added ?? If one of the methods is called from a node outside the last callforest graph
   boolean change =true;
   while(change)
   { change = false;
   java.util.Iterator classes = app.classes();
      while(classes.hasNext()){
         sandmark.program.Class cls = (sandmark.program.Class) classes.next();

         if (cls.getName().equals(watermarkClassName))
   				continue;
            sandmark.program.Method[] methods = cls.getMethods();
             for(int j=0;j<methods.length;j++)
          {

             sandmark.program.Method mg = methods[j];

             if (mg.isNative() || mg.isAbstract()) continue;
			 if(mg.getName().equals("main")|| mg.getName().equals("main")) continue;


			    org.apache.bcel.generic.InstructionList il = mg.getInstructionList();
			    org.apache.bcel.generic.InstructionHandle[] ihs = il.getInstructionHandles();
			    for(int i=0; i < ihs.length; i++) {
			       org.apache.bcel.generic.InstructionHandle ih = ihs[i];
			       org.apache.bcel.generic.Instruction instr = ih.getInstruction();
			       if (instr instanceof org.apache.bcel.generic.InvokeInstruction) {
			           org.apache.bcel.generic.InvokeInstruction call =
			              (org.apache.bcel.generic.InvokeInstruction) instr;
			           String className  = call.getClassName(cls.getConstantPool());
					      String methodName = call.getName(cls.getConstantPool());
					      String methodSig  = call.getSignature(cls.getConstantPool());
					      if(methodName.equals("sm$mark"))
					      		continue;
					      sandmark.util.MethodID method =
      						new sandmark.util.MethodID(methodName, methodSig, className);
			           	 	if(changeSet.contains(method))
			           	 	{
								sandmark.util.MethodID newmethod =
      							new sandmark.util.MethodID(mg.getName(),mg.getSignature(),mg.getClassName());
								if((!changeSet.contains(newmethod)) &&
												(!domMethodNode.getMethod().equals(newmethod)))
								{changeSet.add(newmethod);
								  change =true;
                                                                  if (Debug)
	   								System.out.println("@@@@@@@@@@Adding new methodid="+newmethod);
								  break;
								}

							}
      				}
			   }
		}
	}

	}
    //end newly added

}


/**
 * Return true if the call instruction in the class ec
 * will need to be modified.
 * @param ec    a class that is being edited
 * @param call  a call instruction
 **/
boolean callShouldChange(
   sandmark.program.Class ec,
   org.apache.bcel.generic.InvokeInstruction call) {
   String className  = call.getClassName(ec.getConstantPool());
   String methodName = call.getName(ec.getConstantPool());
   String methodSig  = call.getSignature(ec.getConstantPool());
   if(methodName.equals("sm$mark"))
   		return false;
   sandmark.util.MethodID method =
      new sandmark.util.MethodID(methodName, methodSig, className);
      if(className.equals(watermarkClassName) && (methodName.indexOf("CreateStorage")>=0))
      		return false;

   return className.equals(watermarkClassName) || changeSet.contains(method);
   //return  changeSet.contains(method);
}

/**
 * Return true if the signature of the method mg
 * in the class ec will need to be modified.
 * @param ec    a class that is being edited
 * @param mg    a method
 **/
boolean signatureShouldChange(
   sandmark.program.Class ec,
   sandmark.program.Method mg) {
   String className  = ec.getName();
   String methodName = mg.getName();
   String methodSig  = mg.getSignature();
   sandmark.util.MethodID method =
      new sandmark.util.MethodID(methodName, methodSig, className);
   return changeSet.contains(method);
}

/*************************************************************************/
/**
 * This is the main entry point to this class.
 * For every class do
 * <OL>
 *   <LI> add extra formal parameters to the methods in 'methods'.
 *   <LI> add extra actual arguments to any calls to methods in
 *        methods.
 * </OL>
 **/
void add() throws java.io.IOException{
	computeMethodsToChange();

   java.util.Iterator classes = app.classes();
   while(classes.hasNext()){
      sandmark.program.Class cls = (sandmark.program.Class) classes.next();
      String className = cls.getName();
      if (!className.equals(watermarkClassName)) {

         sandmark.program.Method[] methods = cls.getMethods();
          for(int i=0;i<methods.length;i++)
       {

             sandmark.program.Method mg = methods[i];
             boolean changed1 = addStorageFormals(cls, mg);
             boolean changed2 = addStorageActuals(cls, mg);
             //System.out.println("M="+mg.getName());
             if (changed1 || changed2)
             {  if(Debug)
				 System.out.println("Changed"+changed1+changed2);
                mg.setMaxStack();
                mg.mark();
	         }

	 }
      }
   }


}


/*************************************************************************/
/*
 * For every method in 'methods', add formal parameters,
 * one per storage class.
 * @param ec    a class that is being edited
 * @param mg    a method being modified
 * <P>
 * For example,
 * <PRE>
 *   P(int) {
 *   }
 * could turn into
 *   P(int, java.util.Vector sm$vector, java.util.Hashtable sm$hash) {
 *   }
 * </PRE>
 */
boolean addStorageFormals(
   sandmark.program.Class ec,
   sandmark.program.Method mg) throws java.io.IOException{
   boolean changed = false;
   if (signatureShouldChange(ec,mg)) {
       for(int i=0; i<storageCreators.length; i++) {
          String returnType = storageCreators[i][1];
          String localName = storageCreators[i][2];
          boolean isGlobal = storageCreators[i][3].equals("GLOBAL");
          if (!isGlobal)
	      if (addStorageFormal(ec, mg, localName, returnType))
		  changed = true;
       }
   }
   return changed;
}

/**
 * Add formal 'name' of type 'type' to method eg in class ec.
 * @param ec    a class that is being edited
 * @param mg    a method being modified
 * @param name  name of the formal to add
 * @param type  type of the formal to add
 **/
boolean addStorageFormal (
   sandmark.program.Class ec,
   sandmark.program.Method mg,
   String name,
   String type) {
	String temp=mg.getName();
	if(Debug)
   		System.out.println("AddParameters: adding formal (" + name + " " + type + ") to " + mg.getName()+"A="+temp);

   if (mg.isNative() || mg.isAbstract()) return false;

   org.apache.bcel.generic.Type[] types = mg.getArgumentTypes();
   org.apache.bcel.generic.Type[] types1 = new org.apache.bcel.generic.Type[types.length+1];

   String[] names = mg.getArgumentNames();
   String[] names1 = new String[names.length+1];

   for(int i=0; i<names.length; i++) {
      types1[i] = types[i];
      names1[i] = names[i];
   }

   org.apache.bcel.generic.Type Type =
        sandmark.util.javagen.Java.typeToByteCode(type);

   types1[names.length] = Type;
   names1[names.length] = name;

   mg.setArgumentTypes(types1);
   mg.setArgumentNames(names1);

   org.apache.bcel.generic.LocalVariableGen lg =
      mg.addLocalVariable(name, Type, null, null);
   mg.setMaxLocals(mg.getMaxLocals()+1);
	mg.setName(temp);
	int localIndex = getLocalIndex(name,mg);
	moveLocal(mg,localIndex);
   return true;
}

void moveLocal(sandmark.program.Method mg,int localIndex)
{
   org.apache.bcel.generic.InstructionList il = mg.getInstructionList();
   org.apache.bcel.generic.InstructionHandle[] ihs = il.getInstructionHandles();
   int maxindex=0;
   for(int i=0; i < ihs.length; i++) {
      org.apache.bcel.generic.InstructionHandle ih = ihs[i];
      org.apache.bcel.generic.Instruction instr = ih.getInstruction();
      if (instr instanceof org.apache.bcel.generic.LocalVariableInstruction) {
  		org.apache.bcel.generic.LocalVariableInstruction lvinstr=
  				(org.apache.bcel.generic.LocalVariableInstruction)instr;
  			if(maxindex<lvinstr.getIndex())
  				maxindex=lvinstr.getIndex();

	}
   }
	//set all localvariableinstr with index as localIndex to maxindex+2

	for(int i=0; i < ihs.length; i++) {
	      org.apache.bcel.generic.InstructionHandle ih = ihs[i];
	      org.apache.bcel.generic.Instruction instr = ih.getInstruction();
	      if (instr instanceof org.apache.bcel.generic.LocalVariableInstruction) {
	  		org.apache.bcel.generic.LocalVariableInstruction lvinstr=
	  				(org.apache.bcel.generic.LocalVariableInstruction)instr;
	  			if(localIndex==lvinstr.getIndex())
	  				lvinstr.setIndex(maxindex+2);

		}
	}
	mg.setMaxLocals();


}

/*************************************************************************/
/*
 * For every method call in the program to one of the
 * methods that 'should change', add actual parameters,
 * one per storage class.
 * @param ec    a class that is being edited
 * @param mg    a method being modified
 * <P>
 * For example,
 * <PRE>
 *   P(int, java.util.Vector sm$vector, java.util.Hashtable sm$hash) {
 *     Q(5);
 *   }
 * </PRE>
 * should turn into
 * <PRE>
 *   P(int, java.util.Vector sm$vector, java.util.Hashtable sm$hash) {
 *     Q(5, sm$vector, sm$hash);
 *   }
 * </PRE>
 * At this point we should already have added the formals.
 */
boolean addStorageActuals (
   sandmark.program.Class ec,
   sandmark.program.Method mg) {
   if (mg.isNative() || mg.isAbstract()) return false;

   boolean changed = false;
   org.apache.bcel.generic.InstructionList il = mg.getInstructionList();
   org.apache.bcel.generic.InstructionHandle[] ihs = il.getInstructionHandles();
   for(int i=0; i < ihs.length; i++) {
      org.apache.bcel.generic.InstructionHandle ih = ihs[i];
      org.apache.bcel.generic.Instruction instr = ih.getInstruction();
      if (instr instanceof org.apache.bcel.generic.InvokeInstruction) {
          org.apache.bcel.generic.InvokeInstruction call =
             (org.apache.bcel.generic.InvokeInstruction) instr;
          if (editCall(ec, mg, il, ih, call))
	      changed = true;
      }
   }
   mg.mark();
   return changed;
}

int getLocalIndex(String localName,sandmark.program.Method mg)
{
		int i;
		int ct=0;
		if(!mg.isStatic())
			ct=1;
		String [] s=mg.getArgumentNames();
		org.apache.bcel.generic.Type [] oldArgTypes=mg.getArgumentTypes();
		for (i = 0; i < s.length; i++)
		{
			 if(s[i].equals(localName))
							break;
			  if(oldArgTypes[i].getSignature().equals("J")
					  || oldArgTypes[i].getSignature().equals("D"))
					 ct+=2;
			  else
					ct+=1;
		}
		int localIndex = findLocal(localName,mg);
		if(i < s.length)
			localIndex=ct;
		return localIndex;

}

int findLocal(String name,sandmark.program.Method method) {
    org.apache.bcel.generic.LocalVariableGen[] locals = method.getLocalVariables();
    for(int i=0; i<locals.length; i++)
	if (locals[i].getName().equals(name))
	    return locals[i].getIndex();
    return -1;
}


/*
 * For every storage class add 'push sm$class' before the
 * call instruction. Since the formals have already been
 * added we can look it up in the localvariable table.
 * @param ec    a class that is being edited
 * @param mg    a method being modified
 * @param il    the instruction list of the method
 * @param ih    the instruction handle of the call instruction to be edited
 * @param call  the call instruction to be edited.
 */

boolean editCall(
   sandmark.program.Class ec,
   sandmark.program.Method mg,
   org.apache.bcel.generic.InstructionList il,
   org.apache.bcel.generic.InstructionHandle ih,
   org.apache.bcel.generic.InvokeInstruction call) {
   if (!callShouldChange(ec, call)) return false;
	int i;
   boolean changed = false;
   for(int c=0; c<storageCreators.length; c++) {
      String returnType = storageCreators[c][1];
      String localName = storageCreators[c][2];
      boolean isGlobal = storageCreators[c][3].equals("GLOBAL");

      if (!isGlobal) {
		int localIndex = getLocalIndex(localName,mg);

			if(Debug)
        		System.out.println("EDITCALL:M="+mg+"I="+ih+"Lindex"+localIndex);
         if (localIndex >= 0) {
			if(Debug)
        	     System.out.println("AddParameters: adding 'ALOAD " + localName + "' to " + mg);
            org.apache.bcel.generic.ALOAD push = new org.apache.bcel.generic.ALOAD(localIndex);
            il.insert(ih,push);
          }
          else
           il.insert(ih,new org.apache.bcel.generic.ACONST_NULL());

            if (!call.getClassName(ec.getConstantPool()).equals(watermarkClassName)) {
               org.apache.bcel.generic.InvokeInstruction newCall =
               fixMethodSignature(ec, ih, returnType);
               ih.setInstruction(newCall);
            }
            else
            {
               if(Debug)
				System.out.println("Call Not Modified="+call);
			}

		    changed = true;


      }
   }
   return changed;
}

/*
 * Modify the call instruction by adding 'type' to the signature of
 * the method to call.
 * @param ec    a class that is being edited
 * @param call  the call instruction to be edited.
 * @param type  the type of the formal to be added.
 */
org.apache.bcel.generic.InvokeInstruction fixMethodSignature (
   sandmark.program.Class ec,
   org.apache.bcel.generic.InstructionHandle ih,
   String type) {

   org.apache.bcel.generic.InvokeInstruction call=
   		(org.apache.bcel.generic.InvokeInstruction)ih.getInstruction();
   String className = call.getClassName(ec.getConstantPool());
   String methodName = call.getName(ec.getConstantPool());

   org.apache.bcel.generic.Type[] types = call.getArgumentTypes(ec.getConstantPool());
   org.apache.bcel.generic.Type[] types1 = new org.apache.bcel.generic.Type[types.length+1];

   org.apache.bcel.generic.Type returnType = call.getReturnType(ec.getConstantPool());

   for(int i=0; i<types.length; i++)
      types1[i] = types[i];
   types1[types.length] = sandmark.util.javagen.Java.typeToByteCode(type);

   org.apache.bcel.generic.InstructionFactory factory =
       new org.apache.bcel.generic.InstructionFactory(ec.getConstantPool());

   short invokeKind = call.getOpcode();

   org.apache.bcel.generic.InvokeInstruction newCall =
      factory.createInvoke(className, methodName, returnType, types1, invokeKind);

   String callS = className + "." + methodName + ":" + call.getSignature(ec.getConstantPool());
   if(Debug)
   	System.out.println("AddParameters: changing call '" + callS + "' by adding formal " + type);
   return newCall;
}
}








