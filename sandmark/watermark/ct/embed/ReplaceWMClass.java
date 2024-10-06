package sandmark.watermark.ct.embed;
/* It is used to replace the class found in DWM_CT_Encode_ClassName(Watermark class)
** with the class found in Node Class (This class is set by findReplaceClass()).
** It is also used to automatically calculate the class Node Class that closely
** represents the Watermark class
*/

public class ReplaceWMClass {
  private static final boolean Debug=true;
  sandmark.util.ConfigProperties props = null;
  sandmark.program.Application app = null;
/**
 * @param app the program to be watermarked
 * @param props         global property list
 */

  public ReplaceWMClass (
    sandmark.program.Application app,
    sandmark.util.ConfigProperties props)
  {
   	this.props = props;
   	this.app = app;
  }


/* It replaces the class found in DWM_CT_Encode_ClassName(Watermark class)
** with the class found in Node Class (This class is set by findReplaceClass())
** The steps involved are
** 1. Copy the Constant Pool table from old class to new class
** 2. Copy all the methods from old class to new class
** 3. Copy the storage allocator fields if any from old class to new class
** 4. Change the Constantpool of all class that reference the old class to refer
**      to the new class
** 5. Deletes the old class
*/
public void doReplace()
{
  String watermarkClassName=props.getProperty("DWM_CT_Encode_ClassName");
  String newwmclassname=props.getProperty("Node Class");

  if( (!newwmclassname.equals(watermarkClassName) ))
  {
	   sandmark.program.Class newwmclass=app.getClass(newwmclassname);
	   sandmark.program.Class cls=app.getClass(watermarkClassName);
	   org.apache.bcel.generic.ConstantPoolGen newcpg=newwmclass.getConstantPool();
	   org.apache.bcel.generic.ConstantPoolGen cpg=cls.getConstantPool();
	   org.apache.bcel.generic.CodeExceptionGen cegen[];


	  for(int i=1;i<cpg.getSize();i++)
	  {
	 	newcpg.addConstant(cpg.getConstant(i),cpg);
	  }

	   sandmark.program.Method[] methods = cls.getMethods();
	  for(int i=0;i<methods.length;i++)
	  {  if(!methods[i].getName().equals("<init>") && !methods[i].getName().equals("<clinit>") )
		 {
	       org.apache.bcel.generic.InstructionList nlist=
	       new org.apache.bcel.generic.InstructionList(methods[i].getInstructionList().getByteCode());
	       nlist.replaceConstantPool(cpg,newcpg);
	       sandmark.program.Method tempmeth= new sandmark.program.LocalMethod(newwmclass,methods[i].getAccessFlags(),
									methods[i].getReturnType(),methods[i].getArgumentTypes(),
									methods[i].getArgumentNames(),
									methods[i].getName(),
									nlist);

			cegen=methods[i].getExceptionHandlers();
			for(int j=0;j<cegen.length;j++)
			 tempmeth.addExceptionHandler(cegen[j].getStartPC(),cegen[j].getEndPC(),
						cegen[j].getHandlerPC(),cegen[j].getCatchType());

		 }
	   }

	//move fields of type storage allocator if the storage type is global
	sandmark.program.Field f[]=cls.getFields();
	org.apache.bcel.generic.ObjectType objtype= new org.apache.bcel.generic.ObjectType(cls.getName());
	for(int i=0;i<f.length;i++)
	{	
	    if(!f[i].getType().equals(objtype) && (newwmclass.getField(f[i].getName(),f[i].getType().getSignature())==null))
		new sandmark.program.LocalField(newwmclass,f[i].getAccessFlags(),f[i].getType(),f[i].getName());

	}
	//end

	replaceCallstoWatermarkMethods();
	cls.delete();

  }
}

/*
** It change the Constantpool entry of all class that reference the old watermark class to refer
** to the new watermark class
*/
void replaceCallstoWatermarkMethods()
{	  String watermarkClassName=props.getProperty("DWM_CT_Encode_ClassName");
      String newwmclassname=props.getProperty("Node Class");
	  java.util.Iterator classes = app.classes();
	  while(classes.hasNext()){
		 sandmark.program.Class cls = (sandmark.program.Class) classes.next();
		 String className = cls.getName();
		 if(className.equals(watermarkClassName))
		 	continue;
		 int index=cls.getConstantPool().lookupClass(watermarkClassName);
		 if(index>=0)
		 {
	       int newindex=cls.getConstantPool().addClass(newwmclassname);
	       cls.getConstantPool().setConstant(index,cls.getConstantPool().getConstant(newindex));
	     }


	 }
}

private static java.util.Set getAcceptableTypes(sandmark.program.Class clazz) {
   java.util.HashSet types = new java.util.HashSet();
   java.util.HashSet visited = new java.util.HashSet();
   java.util.Stack stack = new java.util.Stack();
   stack.add(clazz);
   while(!stack.empty()) {
      sandmark.program.Class cls = (sandmark.program.Class)stack.pop();
      if(visited.contains(cls))
	 continue;
      visited.add(cls);
      types.add(cls.getType());
      stack.add(cls.getSuperClass());
      sandmark.program.Class interfaces[] = cls.getInterfaces();
      for(int i = 0 ; i < interfaces.length ; i++)
	 stack.add(interfaces[i]);
   }
   return types;
}

private static boolean constructorsOK(sandmark.program.Class cls) {
   while(!cls.getType().equals(org.apache.bcel.generic.Type.OBJECT)) {
      sandmark.program.Method defaultCons = cls.getMethod("<init>","()V");
      if(defaultCons == null)
	 return false;
      org.apache.bcel.generic.InstructionList il = 
	 defaultCons.getInstructionList();
      if(il.getLength() != 3)
	 return false;
      if(!(il.getStart().getInstruction() 
	   instanceof org.apache.bcel.generic.ALOAD))
	 return false;
      org.apache.bcel.generic.ALOAD a = 
	 (org.apache.bcel.generic.ALOAD)il.getStart().getInstruction();
      if(a.getIndex() != 0)
	 return false;
      if(!(il.getStart().getNext().getInstruction()
	   instanceof org.apache.bcel.generic.INVOKESPECIAL))
	 return false;
      org.apache.bcel.generic.INVOKESPECIAL is =
	 (org.apache.bcel.generic.INVOKESPECIAL)
	 il.getStart().getNext().getInstruction();
      if(!is.getMethodName(defaultCons.getConstantPool()).equals("<init>") ||
	 !is.getSignature(defaultCons.getConstantPool()).equals("()V"))
	 return false;
      if(!(il.getStart().getNext().getNext().getInstruction()
	   instanceof org.apache.bcel.generic.RETURN))
	 return false;
      cls = cls.getSuperClass();
   }
   return true;
}

/*
 * It determines the class that closely resembles the Watermark Class.
 * It then sets the "Node Class" & "DWM_CT_Encode_AvailableEdges" properties
 * based on the class selected and the fields in it.
 */
public java.util.Hashtable findReplaceClass() throws Exception
{	sandmark.program.Class curr;
	String treeClassName=null;
	int maxct=0;
	int maxnewcalls=-1;
	int temp=0;
	java.util.Hashtable numnewcalls=calcNewCalls();
	Integer ncalls;
	int components = 2;
    String watermarkClassName=props.getProperty("DWM_CT_Encode_ClassName");
	String newwmclassname=props.getProperty("Node Class");

  if((newwmclassname.equals(watermarkClassName)))
  {
	java.util.Iterator it = app.classes();
	while(it.hasNext())
	{
		curr=(sandmark.program.Class)it.next();
		java.util.Set acceptableTypes = getAcceptableTypes(curr);
		if(curr.isAbstract() || curr.isInterface() || 
		   !constructorsOK(curr))
			continue;
		int ct=0;
		sandmark.program.Field f[]=curr.getFields();
		if(treeClassName==null)
			treeClassName=curr.getName();

		for(int i=0;i<f.length;i++)
		{
			if(f[i].isStatic()||f[i].isPrivate()||f[i].isProtected())
				continue;

			if(acceptableTypes.contains(f[i].getType()))
				ct++;

		}
		// Choose the class with most number of new calls pointing to it .
		if(ct>=maxct)
		{
				ncalls=(Integer)numnewcalls.get(curr.getType());
				if(ncalls!=null)
					temp=ncalls.intValue();
				else
					temp=0;

				if(temp>maxnewcalls||ct>maxct)
				{  maxnewcalls=temp;
				   maxct=ct;
				   maxct=Math.min(maxct,components);
				   treeClassName=curr.getName();
				}


		}



	}
	if(Debug)
		System.out.println("Class Selected="+treeClassName);
   }
	else
	treeClassName=newwmclassname;
	// Add new fields to the chosen class if numfields less than no of numcomponents

	curr=app.getClass(treeClassName);
	if(curr==null)
		throw new Exception("The class "+treeClassName+" set in property Node Class is not found");
	java.util.Set acceptableTypes = getAcceptableTypes(curr);

	for(int i=maxct;i<components;i++)
	{
		 //create the bogus field
		  String newFieldName="newwmfield$"+i;
		  int field_access_flags = org.apache.bcel.Constants.ACC_PUBLIC;
		  sandmark.program.LocalField fg = new sandmark.program.LocalField(curr,
		   field_access_flags, new org.apache.bcel.generic.ObjectType(curr.getName()), newFieldName);
		  if(Debug)
		  	System.out.println("Field add:"+fg.getName()+" "+ fg.getParent());
		  fg.mark();
		  int fieldNameIndex = curr.getConstantPool().addFieldref(curr.getName(), newFieldName,
			 fg.getSignature());

	}

	//create the fieldlist for DWM_CT_Encode_AvailableEdges
	String fieldlist="";
	sandmark.program.Field f[]=curr.getFields();
	int ct=0;
	java.util.Hashtable requireCast = new java.util.Hashtable();
	if (Debug) System.out.println(acceptableTypes);
	for(int i=0;i<f.length && ct<components;i++)
	{
		if(f[i].isStatic()||f[i].isPrivate()||f[i].isProtected())
			continue;

		if (Debug) System.out.println(f[i].getType());
		if(acceptableTypes.contains(f[i].getType())) {	
		   if(!f[i].getType().equals(curr.getType()))
		      requireCast.put(f[i].getName(),f[i].getType());
		   fieldlist+=f[i].getName()+":";
		   ct++;
		}
	}
	fieldlist=fieldlist.substring(0,fieldlist.length()-1);
	curr.mark();
	if(Debug)
		System.out.println("Fieldlist="+fieldlist);
	props.setProperty("Node Class",treeClassName);
	props.setProperty("DWM_CT_Encode_AvailableEdges",fieldlist);
	return requireCast;
}

/**
 *  Creates a hashtable containing the number of new calls for each class
 *  in the collection. It is used to break the tie when many classes are eligible
 * to be watermark class
 */
java.util.Hashtable calcNewCalls()
{
java.util.Hashtable numnewcalls=new java.util.Hashtable();
sandmark.program.Class s;
java.util.Iterator it = app.classes();
Integer ncalls;
int temp;
while(it.hasNext())
{
	s=(sandmark.program.Class)it.next();
	sandmark.program.Method m[]=s.getMethods();
	for(int j=0;j<m.length;j++)
	{
		org.apache.bcel.generic.InstructionList instrList = m[j].getInstructionList();
		if (instrList == null)
				continue;
		for (org.apache.bcel.generic.InstructionHandle ihandle = instrList.getStart();
					 ihandle != null;
					 ihandle = ihandle.getNext())
		{

			org.apache.bcel.generic.Instruction instr = ihandle.getInstruction();
			if (!(instr instanceof org.apache.bcel.generic.NEW))
								continue;

			ncalls=	(Integer)numnewcalls.get(
				((org.apache.bcel.generic.NEW)instr).getLoadClassType(s.getConstantPool()));
			if(ncalls!=null)
					temp=ncalls.intValue();
			else
					temp=0;
			temp++;

			numnewcalls.put(
				((org.apache.bcel.generic.NEW)instr).getLoadClassType(s.getConstantPool()),new Integer(temp));

		}
	}


}
return numnewcalls;

}
}
