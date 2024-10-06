package sandmark.util.primitivepromotion;



/**
 * This class provides routines for promoting primitive local variables in
 * methods to their corresponding wrapper type.
 *
 * @author Srinivas Visvanathan
*/

public class LocalPromoter
{
    private static String tName;				//name of wrapper type
    private static org.apache.bcel.generic.BasicType pType;	//primitve type
    
    private static String loadName;	//class name of primitve load/store
    private static String storeName;	//e.g. org.apache.bcel.generic.ISTORE

    private static String tValue;	//name of method which is used to get the
					//primitive value from a wrapper object

    /**
     * Used to promote local variables of type int in a method to
     * java.lang.Integer. It also affects local variables of type byte, char, 
     * short and boolean, since these are implemented using the int primitive
     * at the bytecode level.
     *
     * @param mg Method whose locals have to be promoted
     *
     */
    public static void iPromote(sandmark.program.Method mg)
    {
	if (mg.isAbstract() || mg.isNative())
	    return;
	
	tName = new String("java.lang.Integer");
	pType = org.apache.bcel.generic.Type.INT;
	loadName = "org.apache.bcel.generic.ILOAD";
	storeName = "org.apache.bcel.generic.ISTORE";
	tValue = new String("intValue");

	tPromote(mg);
    }

    
    /**
     * Used to promote local variables of type float in a method to
     * java.lang.Float. 
     *
     * @param mg Method whose locals have to be promoted
     *
     */
    public static void fPromote(sandmark.program.Method mg)
    {
	if (mg.isAbstract() || mg.isNative())
	    return;
	
	tName = new String("java.lang.Float");
	pType = org.apache.bcel.generic.Type.FLOAT;
	loadName = "org.apache.bcel.generic.FLOAD";
	storeName = "org.apache.bcel.generic.FSTORE";
	tValue = new String("floatValue");

	tPromote(mg);
    }
    
    
    
    /**
     * Used to promote local variables of type long in a method to
     * java.lang.Long. 
     *
     * @param mg Method whose locals have to be promoted
     *
     */
    public static void lPromote(sandmark.program.Method mg)
    {
	if (mg.isAbstract() || mg.isNative())
	    return;
	
	tName = new String("java.lang.Long");
	pType = org.apache.bcel.generic.Type.LONG;
	loadName = "org.apache.bcel.generic.LLOAD";
	storeName = "org.apache.bcel.generic.LSTORE";
	tValue = new String("longValue");

	tPromote(mg);
    }
    

    /**
     * Used to promote local variables of type double in a method to
     * java.lang.Double. 
     *
     * @param mg Method whose locals have to be promoted
     *
     */
    public static void dPromote(sandmark.program.Method mg)
    {
	if (mg.isAbstract() || mg.isNative())
	    return;
	
	tName = new String("java.lang.Double");
	pType = org.apache.bcel.generic.Type.DOUBLE;
	loadName = "org.apache.bcel.generic.DLOAD";
	storeName = "org.apache.bcel.generic.DSTORE";
	tValue = new String("doubleValue");

	tPromote(mg);
    }



    /* Main routne that manages the primitive promotion for all types. All the
     * previous public methods are wrappers for this method, to set stuff up for
     * the approapriate type */
    private static void tPromote(sandmark.program.Method mg)
    {
	org.apache.bcel.generic.InstructionList il = mg.getInstructionList();
	if (il == null) return;
	org.apache.bcel.generic.InstructionHandle ih = il.getStart();
	org.apache.bcel.generic.InstructionFactory iF = 
		new org.apache.bcel.generic.InstructionFactory(mg.getCPG());

	//wrap the input params of the type being promoted
	ih = wrapMethodParams(mg,iF,il,ih);

	//scan thru the method's instructions
	while (ih != null) {
	    org.apache.bcel.generic.Instruction instr = ih.getInstruction();

	    //if its a load for the type being promoted
	    if (instr.getClass().getName().equals(loadName)) 
		ih = fixLoad(iF,il,ih,(org.apache.bcel.generic.LoadInstruction)instr);
	    else
	    //if its a store for the type being promoted
	    if (instr.getClass().getName().equals(storeName))
		ih = fixStore(iF,il,ih,(org.apache.bcel.generic.StoreInstruction)instr);
	    else
	    //if its IINC and we're promoting int
	    if (instr instanceof org.apache.bcel.generic.IINC && 
		    pType == org.apache.bcel.generic.Type.INT)
		ih = fixIINC(iF,il,ih,(org.apache.bcel.generic.IINC)instr,mg.getCPG());
	    else
		ih = ih.getNext();
	}
    }


    /* Adds code at the start of mg that wraps primitive locals of the type
     * being promoted. Returns the handle to the first instruction of mg that
     * follows this wrapper code */
    private static org.apache.bcel.generic.InstructionHandle
	wrapMethodParams(   sandmark.program.Method mg,
			    org.apache.bcel.generic.InstructionFactory iF,
			    org.apache.bcel.generic.InstructionList il,
			    org.apache.bcel.generic.InstructionHandle ih
			)
    {
    	int idx = mg.isStatic() ? 0 : 1;
	org.apache.bcel.generic.Type at[] = mg.getArgumentTypes();
	java.util.List indices = new java.util.ArrayList();

	//collect indexes of params that have to be wrapped in the list indices
	for (int jj = 0; jj < at.length; jj++)
	{
	    switch (at[jj].getType()) {
		case org.apache.bcel.Constants.T_INT:
		case org.apache.bcel.Constants.T_BOOLEAN:
		case org.apache.bcel.Constants.T_BYTE:
		case org.apache.bcel.Constants.T_CHAR:
		case org.apache.bcel.Constants.T_SHORT:
		    //if we're promoting ints then remember this index
		    if (pType == org.apache.bcel.generic.Type.INT)
			indices.add(new java.lang.Integer(idx));
		    idx++;
		    break;
		case org.apache.bcel.Constants.T_FLOAT:
		    //if we're promoting floats then remember this index
		    if (pType == org.apache.bcel.generic.Type.FLOAT)
			indices.add(new java.lang.Integer(idx));
		    idx++;
		    break;
		case org.apache.bcel.Constants.T_LONG:
		    //if we're promoting long then remember this index
		    if (pType == org.apache.bcel.generic.Type.LONG)
			indices.add(new java.lang.Integer(idx));
		    idx += 2;
		    break;
		case org.apache.bcel.Constants.T_DOUBLE:
		    //if we're promoting doubles then remember this index
		    if (pType == org.apache.bcel.generic.Type.DOUBLE)
			indices.add(new java.lang.Integer(idx));
		    idx += 2;
		    break;
		default:
		    //array type, reference type etc.
		    idx++;
	    };
	}
			

	//generate instructions to wrap each of the local params of the type
	//being promoted
	boolean insertedOnce = false;
	for (int jj = 0; jj < indices.size(); jj++) {
	    idx = ((java.lang.Integer)indices.get(jj)).intValue();
	    
	    //new java.lang.T
	    if (!insertedOnce) {
		ih = il.insert(ih,iF.createNew(tName));
		insertedOnce = true;
	    }
	    else {
		ih = il.append(ih,iF.createNew(tName));
	    }
	    //dup
	    ih = il.append(ih,org.apache.bcel.generic.InstructionConstants.DUP);
	    //tload idx
	    ih = il.append(ih,iF.createLoad(pType,idx));
	    //invokespecial T(t)V
	    ih = il.append(ih,iF.createInvoke(tName,"<init>",
			org.apache.bcel.generic.Type.VOID,
			new org.apache.bcel.generic.Type[] { pType },
			org.apache.bcel.Constants.INVOKESPECIAL));
	    //astore idx
	    ih = il.append(ih,iF.createStore(
		    new org.apache.bcel.generic.ObjectType(tName),idx));
	}
	
	//return handle to first instruction in the original list
	return insertedOnce ? ih.getNext() : ih;
    }
    
    /* Replaces "TLOAD x" with the sequence:
     *	ALOAD x
     *  INVOKEVIRTUAL T.tvalue()t
     *  This method returns the handle to the instruction following the original
     *  "TLOAD x"
     */
    private static org.apache.bcel.generic.InstructionHandle
	fixLoad(    org.apache.bcel.generic.InstructionFactory iF,
		    org.apache.bcel.generic.InstructionList il,
		    org.apache.bcel.generic.InstructionHandle ih,
		    org.apache.bcel.generic.LoadInstruction instr
		)
    {
	int idx = instr.getIndex();
	org.apache.bcel.generic.InstructionHandle oldh = ih;

	//aload <idx> - load object ref of wrapper type tName
	ih = il.append(ih,iF.createLoad(
		    new org.apache.bcel.generic.ObjectType(tName),idx));
	
	//redirect branches targetting the iload to now target the aload
	if (oldh.hasTargeters()) {
	    org.apache.bcel.generic.InstructionTargeter it[] = oldh.getTargeters();
	    for (int jj = 0; jj < it.length; jj++)
		it[jj].updateTarget(oldh,ih);
	}

	//delete the tload instruction
	try {
	    il.delete(oldh);
	} catch (org.apache.bcel.generic.TargetLostException e) {
	    throw new RuntimeException(e.toString());
	}

	//invokevirtual T.tValue()t
	ih = il.append(ih,iF.createInvoke(tName,tValue,pType,
			    new org.apache.bcel.generic.Type[] {},
			    org.apache.bcel.Constants.INVOKEVIRTUAL));

	return ih.getNext();
    }



    /* Replaces "TSTORE x" with the sequence:
     *  NEW java.lang.T
     *  DUP_X1/DUP_X2 (X1 for int/float, X2 for long/double)
     *  DUP_X1/DUP_X2 (X1 for int/float, X2 for long/double)
     *  POP
     *  INVOKESPECIAL T.<init>(t)V
     *  ASTORE x
     *  This method returns the handle to the instruction following the original
     *  "TSTORE x"
     */
    private static org.apache.bcel.generic.InstructionHandle
	fixStore(    org.apache.bcel.generic.InstructionFactory iF,
		    org.apache.bcel.generic.InstructionList il,
		    org.apache.bcel.generic.InstructionHandle ih,
		    org.apache.bcel.generic.StoreInstruction instr
		)
    {
	int idx = instr.getIndex();
	org.apache.bcel.generic.InstructionHandle oldh = ih;

	//new java.lang.T
	ih = il.append(ih,iF.createNew(tName));

	//redirect branches targetting the tstore to now target the new
	if (oldh.hasTargeters()) {
	    org.apache.bcel.generic.InstructionTargeter it[] = oldh.getTargeters();
	    for (int jj = 0; jj < it.length; jj++)
		it[jj].updateTarget(oldh,ih);
	}

	//delete the tstore instruction
	try {
	    il.delete(oldh);
	} catch (org.apache.bcel.generic.TargetLostException e) {
	    throw new RuntimeException(e.toString());
	}

	//2 * dup_x1 for int/float, 2 * dup_x2 for long/double
	if (pType.getType() == org.apache.bcel.Constants.T_LONG ||
		pType.getType() == org.apache.bcel.Constants.T_DOUBLE) {
	    ih = il.append(ih,org.apache.bcel.generic.InstructionConstants.DUP_X2);
	    ih = il.append(ih,org.apache.bcel.generic.InstructionConstants.DUP_X2);
	}
	else {
	    ih = il.append(ih,org.apache.bcel.generic.InstructionConstants.DUP_X1);
	    ih = il.append(ih,org.apache.bcel.generic.InstructionConstants.DUP_X1);
	}
	//POP
	ih = il.append(ih,org.apache.bcel.generic.InstructionConstants.POP);
	//invokespecial T(t)V
	ih = il.append(ih,iF.createInvoke(tName,"<init>",
		    org.apache.bcel.generic.Type.VOID,
		    new org.apache.bcel.generic.Type[] { pType },
		    org.apache.bcel.Constants.INVOKESPECIAL));
	//astore idx
	ih = il.append(ih,iF.createStore(
	    new org.apache.bcel.generic.ObjectType(tName),idx));

	return ih.getNext();
    }


    /* Replaces "IINC x i" with the sequence 
     *	ILOAD x
     *	ICONST i
     *	IADD
     *	ISTORE x
     * The method returns the handle to the new "ILOAD x" instruction, so that
     * when the main loop continues scanning, the ILOAD and ISTORE will be
     * processed */
    private static org.apache.bcel.generic.InstructionHandle
	fixIINC(    org.apache.bcel.generic.InstructionFactory iF,
		    org.apache.bcel.generic.InstructionList il,
		    org.apache.bcel.generic.InstructionHandle ih,
		    org.apache.bcel.generic.IINC instr,
		    org.apache.bcel.generic.ConstantPoolGen cp
	        )
    {
	int incr = instr.getIncrement();
	int idx = instr.getIndex();
	org.apache.bcel.generic.InstructionHandle oldh = ih;

	//iload idx
	ih = il.append(ih,iF.createLoad(pType,idx));
	org.apache.bcel.generic.InstructionHandle reth = ih; //remember handle to iload

	//redirect branches targetting the iload to now target the aload
	if (oldh.hasTargeters()) {
	    org.apache.bcel.generic.InstructionTargeter it[] = oldh.getTargeters();
	    for (int jj = 0; jj < it.length; jj++)
		it[jj].updateTarget(oldh,ih);
	}

	//delete the iinc instruction
	try {
	    il.delete(oldh);
	} catch (org.apache.bcel.generic.TargetLostException e) {
	    throw new RuntimeException(e.toString());
	}

	//iconst incr
	ih = il.append(ih,new org.apache.bcel.generic.PUSH(cp,incr));
	//iadd
	ih = il.append(ih,org.apache.bcel.generic.InstructionConstants.IADD);
	//istore idx
	ih = il.append(ih,iF.createStore(pType,idx));

	return reth;	//return handle to iload, next instruction to be processed
			//will be handled in the main loop as an ILOAD
    }
}
