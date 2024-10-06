package sandmark.obfuscate.ArrayObfuscation;

/**
  * IntArraySplitter, as the name suggests, does Data Obfuscation by splitting
  * an integer array in the original program into two arrays, and based on some
  * encoding method, the elements are put in either of the two arrays. The current
  * implementation supports the following form of encoding: Even elements are put in
  * one array and odd in another.
  *
  * @author Ashok Venkatraj (ashok@cs.arizona.edu)
  *
  */

public class IntArraySplitter extends  sandmark.obfuscate.AppObfuscator
{

    private boolean DEBUG = false;
    private sandmark.program.Class classgen;
    private sandmark.program.Method[] methods1;
    private sandmark.program.Method methodgen;
 
    private String classname2;

    org.apache.bcel.generic.Instruction[] in=null ;
    org.apache.bcel.generic.InstructionHandle[] to_del =null;
   
    int arraycount=0;
    int returnhandle=-1;
    int no_of_locals=0;
    int len_of_array=0;
    int arr1_lvindex=-1;
    int arr2_lvindex=-1;
    int arr_assign_start=-1;
    int arr_assign_end=-1;
    int lvindex=-1;
    int poscount=0;
    int delcount=0;
    byte arraytype;
    int increase=0; 

    String cur_mtd = "";
    String cur_sig = "";

    /** Gets all the BCEL bytecode editing class objects up and running
     * @param cg1
     */

    public int initialize(sandmark.program.Class cg1)
    {
	if ( cg1.isInterface() || cg1.isAbstract())
	    return -1;
	methods1 = cg1.getMethods();
	return 1;
    }

    /** Obtain the length of an array ,whose length is constant ,from bytecode
     * @param i Offset of the newarray instruction
     * @param l Instruction array for this method .
     */

    public int getLength(int i,org.apache.bcel.generic.Instruction[] l)
    {
    	org.apache.bcel.generic.Instruction[] local_in=l;
	if(local_in[i-1] instanceof org.apache.bcel.generic.BIPUSH)
	{
	    org.apache.bcel.generic.BIPUSH bipush =(org.apache.bcel.generic.BIPUSH)local_in[i-1];
	    Integer integ = (Integer)bipush.getValue();
	    return integ.intValue();	    
	}
	else if(local_in[i-1]instanceof org.apache.bcel.generic.SIPUSH)
	{
	    org.apache.bcel.generic.SIPUSH sipush =(org.apache.bcel.generic.SIPUSH)local_in[i-1];
	    Integer integ = (Integer)sipush.getValue();
	    return integ.intValue();
	}
	else if (local_in[i-1]instanceof org.apache.bcel.generic.ICONST)
	{
	    org.apache.bcel.generic.ICONST iconst =(org.apache.bcel.generic.ICONST)local_in[i-1];
	    Integer integ = (Integer)iconst.getValue();
	    return integ.intValue();
	}
	return -1;
    }


    /** If the array 's length will be known only at runtime , use this method to add two new 
     *	integer arrays and delete the old array
     * @param type The type of the array
     * @param xastores_offset
     * @param lens_offset The offset in bytecode where you can find the instruction that 
     *  specifies the length of the array
     * @param local_il Instruction List Array
     * @param local_pos Instruction Position Array
     * @param cpg1 ConstantPoolGen
     */

    public int addDynaNewArr(byte type,int xastores_offset,int lens_offset,int newarrays_offset,
			     org.apache.bcel.generic.InstructionList local_il,int[]local_pos,
			     org.apache.bcel.generic.ConstantPoolGen  cpg1)
    {
	int local_incr=0;
	arr1_lvindex =no_of_locals++;
	arr2_lvindex =no_of_locals++;

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert istore before " + local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	int whichvar=no_of_locals++;
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ISTORE(whichvar) );
	local_incr+= (new org.apache.bcel.generic.ISTORE(no_of_locals++)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert iload before " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ILOAD(whichvar) );
	local_incr+= ( new org.apache.bcel.generic.ILOAD(whichvar)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert iconst_2 before " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ICONST(2) );
	local_incr+= (new org.apache.bcel.generic.ICONST(2)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert idiv before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.IDIV() );
	local_incr+= ( new org.apache.bcel.generic.IDIV()).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert iload before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ILOAD(whichvar) );
	local_incr+= ( new org.apache.bcel.generic.ILOAD(whichvar)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert iconst_2 before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ICONST(2) );
	local_incr+= ( new org.apache.bcel.generic.ICONST(2)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert iREM before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.IREM() );
	local_incr+= ( new org.apache.bcel.generic.IREM()).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert iadd before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.IADD() );
	local_incr+= (new org.apache.bcel.generic.IADD()).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert newarray before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.NEWARRAY(type));
	local_incr+= ( new org.apache.bcel.generic.NEWARRAY(type)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert astore before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ASTORE(arr1_lvindex) );
	local_incr+= ( new org.apache.bcel.generic.ASTORE(arr1_lvindex)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert iload before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ILOAD(whichvar) );
	local_incr+= (new org.apache.bcel.generic.ILOAD(whichvar)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert iconst_2 before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ICONST(2) );
	local_incr+= ( new org.apache.bcel.generic.ICONST(2)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert idiv before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.IDIV() );
	local_incr+= ( new org.apache.bcel.generic.IDIV()).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert newarray before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.NEWARRAY(type) );
	local_incr+= ( new org.apache.bcel.generic.NEWARRAY(type)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("NEW ARRAY: insert astore before: " +local_il.findHandle
			       (local_pos[newarrays_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[newarrays_offset]+local_incr),
			new org.apache.bcel.generic.ASTORE(arr2_lvindex) );
	local_incr+= ( new org.apache.bcel.generic.ASTORE(arr2_lvindex)).getLength();

	return local_incr;
    }

    /** Add two new integer arrays and delete the old array
     * @param type The type of the array
     * @param xastores_offset
     * @param lens_offset The offset in bytecode where you can find the instruction 
     * that specifies  the length of the array
     * @param local_il Instruction List Array
     * @param local_pos Instruction Position Array
     * @param cpg1 ConstantPoolGen
     */

    public int addNewArrays(byte type,int xastores_offset,int lens_offset,
			    org.apache.bcel.generic.InstructionList local_il,int[] local_pos,
			    org.apache.bcel.generic.ConstantPoolGen  cpg1)
    {
        int flag=0;
	int cp_index,odd=-1;
	int local_incr=0;
	cp_index=cpg1.addInteger(len_of_array/2);
	if(len_of_array%2==1)
	{
	  odd=cpg1.addInteger((len_of_array/2)+1);
	  flag=1;
	}
	arr1_lvindex =no_of_locals++;
	arr2_lvindex =no_of_locals++;

	if(flag==0)
	{
	    local_il.setPositions();
	    if(DEBUG)
		System.out.println("addNewArrays: insert ldc before " +local_il.findHandle
				   (local_pos[lens_offset]+local_incr).toString());
	    local_il.insert(local_il.findHandle(local_pos[lens_offset]+local_incr),
			    new org.apache.bcel.generic.LDC(cp_index) );
	    local_incr+= ( new org.apache.bcel.generic.LDC(cp_index)).getLength();
	}
	else
	{
	    local_il.setPositions();
	    if(DEBUG)
		System.out.println("addNewArrays: insert ldc before " +local_il.findHandle
				   (local_pos[lens_offset]+local_incr).toString());
	    local_il.insert(local_il.findHandle(local_pos[lens_offset]+local_incr),
			    new org.apache.bcel.generic.LDC(odd));
	    local_incr+= ( new org.apache.bcel.generic.LDC(odd)).getLength();
	}

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("addNewArrays:insert newarray before: " + local_il.findHandle
			       (local_pos[lens_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[lens_offset]+local_incr),
			new org.apache.bcel.generic.NEWARRAY(type) );
	local_incr+= ( new org.apache.bcel.generic.NEWARRAY(type)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("addNewArrays:insert astore before: " +local_il.findHandle
			       (local_pos[lens_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[lens_offset]+local_incr),
			new org.apache.bcel.generic.ASTORE(arr1_lvindex) );
	local_incr+= ( new org.apache.bcel.generic.ASTORE(arr1_lvindex)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("addNewArrays:insert ldc before: " +local_il.findHandle
			       (local_pos[lens_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[lens_offset]+local_incr),
			new org.apache.bcel.generic.LDC(cp_index) );
	local_incr+= ( new org.apache.bcel.generic.LDC(cp_index)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("addNewArrays:insert newarray before: " +local_il.findHandle
			       (local_pos[lens_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[lens_offset]+local_incr),
			new org.apache.bcel.generic.NEWARRAY(type) );
	local_incr+= ( new org.apache.bcel.generic.NEWARRAY(type)).getLength();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println("addNewArrays:insert astore before: " +local_il.findHandle
			       (local_pos[lens_offset]+local_incr).toString());
	local_il.insert(local_il.findHandle(local_pos[lens_offset]+local_incr),
			new org.apache.bcel.generic.ASTORE(arr2_lvindex) );
	local_incr+= ( new org.apache.bcel.generic.ASTORE(arr2_lvindex)).getLength();

	return local_incr;

    }


    public int setIndex(int aload,org.apache.bcel.generic.InstructionList local_il,
			int []local_pos,
			org.apache.bcel.generic.InstructionHandle ih)
    {

	int local_incr=0;
	local_il.setPositions();
	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert aload_2ndarray before: " +
			       local_il.findHandle(aload).toString());
	ih=local_il.insert(local_il.findHandle(aload),
			   new org.apache.bcel.generic.ALOAD(arr2_lvindex));
	local_il.setPositions();
	local_incr+= ( new org.apache.bcel.generic.ALOAD(arr2_lvindex)).getLength();

	int len=-100;
	try
        {
	    local_il.setPositions();
	    
	    if(DEBUG)
		System.out.println(" ARR WRITE:: TRying to redirect all branches that had  " +				   
				   local_il.findHandle(aload+local_incr).toString() +" to " +
				   local_il.findHandle(ih.getPosition()).toString());
	    local_il.redirectBranches(local_il.findHandle(aload+local_incr),
				      local_il.findHandle(ih.getPosition()));
		
	    if(DEBUG)
		System.out.println(" ARR WRITE:: delete aload_orig: " +
				   local_il.findHandle(aload+local_incr).toString());
		
	    len= local_il.findHandle(aload+local_incr).getInstruction().getLength();
	    local_il.delete(local_il.findHandle(aload+local_incr));
	    local_incr -= len;
	    local_il.setPositions();
	    
	}catch(Exception e6)
	{
	    local_incr -= len;
	    local_il.setPositions();
	    ///e6.printStackTrace();
	}

	return local_incr;

    }

    public int deleteIlist(int sip,int eip,
			   org.apache.bcel.generic.InstructionList local_il,
			   int[] local_pos)
    {
	int ii=0,len=0;
	int local_incr=0;
	for(int k=sip;k<=eip;k+=ii)
	{
	    try
	    {
		local_il.setPositions();
		if(DEBUG)
		    System.out.println("ARR WRITE: deleting orig index list : " +
				       local_il.findHandle(k+local_incr).toString());
		len= local_il.findHandle(k+local_incr).getInstruction().getLength();
		local_il.delete(local_il.findHandle(k+local_incr));
		local_incr -= len;
		local_il.setPositions();
	    }catch(Exception e6)
	    {
		local_incr -= len;
		local_il.setPositions();
		///e6.printStackTrace();
	    }
	    ii=len;	   
	}

	return local_incr;
    }


    public int insert_iby_2(org.apache.bcel.generic.InstructionList local_il,int []local_pos,
			    int index_end_ip,int incr_tillnow)
    {
    	int local_incr=0;

	local_il.setPositions();
	if(DEBUG)
	    System.out.println(" ARR WRITE:: append idiv after: " +
			       local_il.findHandle(index_end_ip+incr_tillnow).toString());
	local_il.append(local_il.findHandle(index_end_ip+incr_tillnow),
			new org.apache.bcel.generic.IDIV());
	local_incr+= ( new org.apache.bcel.generic.IDIV()).getLength();

	local_il.setPositions();
	if(DEBUG)
	System.out.println(" ARR WRITE:: append iconst2 after: " +
			   local_il.findHandle(index_end_ip+incr_tillnow).toString());
	local_il.append(local_il.findHandle(index_end_ip+incr_tillnow),
			new org.apache.bcel.generic.ICONST(2));
	local_incr+= ( new org.apache.bcel.generic.ICONST(2)).getLength();
	local_il.setPositions();
	
	return local_incr;
    }

     public int finish_insert(org.apache.bcel.generic.InstructionList local_il,
			      int []local_pos,
			      int aloads_offset, int Ilistlen, int Vlistlen, 
			      org.apache.bcel.generic.InstructionList Ilist,
			      org.apache.bcel.generic.InstructionList Vlist,
			      int cur_xastore_ip)
     {
    	int local_incr=0;
	org.apache.bcel.generic.InstructionHandle ttemp=null;
	org.apache.bcel.generic.InstructionList Ilistcopy = Ilist.copy();

	local_il.setPositions();
	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert index list before: " +
			       local_il.findHandle(aloads_offset).toString());
	ttemp=local_il.insert(local_il.findHandle(aloads_offset),Ilist);
	local_il.setPositions();

	local_incr+= Ilistlen;
	local_il.setPositions();


	if(DEBUG)
	    System.out.println(" ARR WRITE:: Redirect all branches that had  " +
			       local_il.findHandle(aloads_offset+local_incr).toString() +  
			       " to " + local_il.findHandle(ttemp.getPosition()).toString());
	local_il.redirectBranches(local_il.findHandle(aloads_offset+local_incr),
				  local_il.findHandle(ttemp.getPosition()));


	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert istore before: " +
			       local_il.findHandle(aloads_offset+local_incr).toString());
	int temp_var = no_of_locals++;
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.ISTORE(temp_var));
	local_incr+= (new org.apache.bcel.generic.ISTORE(temp_var)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert iLOAD before: " +
			       local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.ILOAD(temp_var));
	local_incr+= (new org.apache.bcel.generic.ILOAD(temp_var)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert iconst_2 before: " +
			       local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.ICONST(2));
	local_incr+= (new org.apache.bcel.generic.ICONST(2)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert irem before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.IREM());
	local_incr+= (new org.apache.bcel.generic.IREM()).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert ifne before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
		new org.apache.bcel.generic.IFNE
			(local_il.findHandle(aloads_offset+local_incr)));
	local_incr+=(new org.apache.bcel.generic.IFNE
		     (local_il.findHandle(aloads_offset+local_incr))).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert aload_1st array before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.ALOAD(arr1_lvindex));
	local_incr+= (new org.apache.bcel.generic.ALOAD(arr1_lvindex)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert iLOAD before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.ILOAD(temp_var));
	local_incr+= (new org.apache.bcel.generic.ILOAD(temp_var)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert iconst_2 before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.ICONST(2));
	local_incr+= (new org.apache.bcel.generic.ICONST(2)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert idiv before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.IDIV());
	local_incr+= (new org.apache.bcel.generic.IDIV()).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert value list before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),Vlist);
	local_incr+= Vlistlen;
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert iastore before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+local_incr),
			new org.apache.bcel.generic.IASTORE());
	local_incr+= (new org.apache.bcel.generic.IASTORE()).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert goto before: " +
			   local_il.findHandle(aloads_offset+local_incr).toString());
	int oldincr=local_incr;

	if(DEBUG)
	    System.out.println(" ARR WRITE:: Target of goto: " +
			       local_il.findHandle(cur_xastore_ip+local_incr).toString());
	local_il.insert(local_il.findHandle(aloads_offset+oldincr),
			new org.apache.bcel.generic.GOTO
			(local_il.findHandle(cur_xastore_ip+local_incr)));
	local_incr+= (new org.apache.bcel.generic.GOTO
		      (local_il.findHandle(cur_xastore_ip+local_incr) ) ).getLength();
	local_il.setPositions();

	int incr_tillnow = local_incr;

	if(DEBUG)
	    System.out.println(" ARR WRITE:: insert idiv before: " +
			       local_il.findHandle(aloads_offset+incr_tillnow).toString());
	local_il.append(local_il.findHandle(aloads_offset+incr_tillnow),
			new org.apache.bcel.generic.IDIV());
	local_incr+= (new org.apache.bcel.generic.IDIV()).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: append iconst_2 after: " +
			       local_il.findHandle(aloads_offset+incr_tillnow).toString());
	local_il.append(local_il.findHandle(aloads_offset+incr_tillnow),
			new org.apache.bcel.generic.ICONST(2));
	local_incr+= (new org.apache.bcel.generic.ICONST(2)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR WRITE:: append iLOAD after: " +
			       local_il.findHandle(aloads_offset+incr_tillnow).toString());
	local_il.append(local_il.findHandle(aloads_offset+incr_tillnow),
			new org.apache.bcel.generic.ILOAD(temp_var));
	local_incr+= (new org.apache.bcel.generic.ILOAD(temp_var)).getLength();
	local_il.setPositions();

	return local_incr;
    }
    

    public int editingReads(org.apache.bcel.generic.InstructionList local_il,
     			int []local_pos, int aloadsoffset,int ialoadsoffset)
    {
    	int local_incr=0;

	int len=0;
	try{
	    local_il.setPositions();
	    if(DEBUG)
		System.out.println(" ARR READS:: deleting original aload : " +
				   local_il.findHandle(local_pos[aloadsoffset]).toString());
	    len= local_il.findHandle(local_pos[aloadsoffset]).getInstruction().getLength();
	    local_il.delete(local_il.findHandle(local_pos[aloadsoffset]));
	    local_incr -= len;
	    local_il.setPositions();
	}catch(Exception e6)
	{
	    local_incr -= len;
	    local_il.setPositions();
	    ///e6.printStackTrace();
	}

	if(DEBUG)
	    System.out.println(" ARR READ:: insert istore before: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	int temp_var = no_of_locals++;
	local_il.insert(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.ISTORE(temp_var));
	len=(new org.apache.bcel.generic.ISTORE(temp_var)).getLength();
	local_incr+= len;
	local_il.setPositions();


	if(DEBUG)
	    System.out.println(" ARR READ:: append iaload after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.IALOAD());
	int newincr =local_incr+ (new org.apache.bcel.generic.IALOAD()).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append idiv after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.IDIV());
	newincr += (new org.apache.bcel.generic.IDIV()).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append iconst2 after: " +
			   local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.ICONST(2));
	newincr += (new org.apache.bcel.generic.ICONST(2)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append iLOAD after: " +
			   local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.ILOAD(temp_var));
	newincr+= (new org.apache.bcel.generic.ILOAD(temp_var)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append ALOAD after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	org.apache.bcel.generic.InstructionHandle ihtemp=
		local_il.append(local_il.findHandle
				(local_pos[ialoadsoffset]+local_incr),
				new org.apache.bcel.generic.ALOAD(arr2_lvindex));
	local_il.setPositions();
	int temp=ihtemp.getPosition();
	newincr+= (new org.apache.bcel.generic.ALOAD(arr2_lvindex)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append goto after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	int oldincr=local_incr;
	if(DEBUG)
	    System.out.println(" ARR READ:: Target of goto stmt is : " +
			       local_il.findHandle(local_pos[ialoadsoffset+1]+ newincr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.GOTO
			(local_il.findHandle(local_pos[ialoadsoffset+1]+ newincr)));
	int gotolen=(new org.apache.bcel.generic.GOTO
		     (local_il.findHandle(local_pos[ialoadsoffset+1]+ newincr) ) ).getLength();
	newincr+=gotolen;
	temp+= gotolen;
	local_il.setPositions();
	

	if(DEBUG)
	    System.out.println(" ARR READ:: append iaload after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.IALOAD());
	newincr += (new org.apache.bcel.generic.IALOAD()).getLength();
	temp += (new org.apache.bcel.generic.IALOAD()).getLength();
	local_il.setPositions();
	
	if(DEBUG)
	    System.out.println(" ARR READ:: append idiv after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.IDIV());
	newincr += (new org.apache.bcel.generic.IDIV()).getLength();
	temp += (new org.apache.bcel.generic.IDIV()).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append iconst2 after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.ICONST(2));
	newincr += (new org.apache.bcel.generic.ICONST(2)).getLength();
	temp += (new org.apache.bcel.generic.ICONST(2)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append iLOAD after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.ILOAD(temp_var));
	newincr+= (new org.apache.bcel.generic.ILOAD(temp_var)).getLength();
	temp+= (new org.apache.bcel.generic.ILOAD(temp_var)).getLength();
	local_il.setPositions();


	if(DEBUG)
	    System.out.println(" ARR READ:: append ALOAD after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.ALOAD(arr1_lvindex));
	newincr+= (new org.apache.bcel.generic.ALOAD(arr1_lvindex)).getLength();
	temp+= (new org.apache.bcel.generic.ALOAD(arr1_lvindex)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append  ifne after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
		new org.apache.bcel.generic.IFNE(local_il.findHandle(temp)));
	newincr+= (new org.apache.bcel.generic.IFNE(local_il.findHandle(temp) ) ).getLength();
	local_il.setPositions();


	if(DEBUG)
	    System.out.println(" ARR READ:: append irem after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.IREM());
	newincr += (new org.apache.bcel.generic.IREM()).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append iconst2 after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.ICONST(2));
	newincr += (new org.apache.bcel.generic.ICONST(2)).getLength();
	local_il.setPositions();

	if(DEBUG)
	    System.out.println(" ARR READ:: append iLOAD after: " +
			       local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	local_il.append(local_il.findHandle(local_pos[ialoadsoffset]+local_incr),
			new org.apache.bcel.generic.ILOAD(temp_var));
	newincr+= (new org.apache.bcel.generic.ILOAD(temp_var)).getLength();
	local_il.setPositions();

	try
	{
	    local_il.setPositions();
	    if(DEBUG)
		System.out.println(" ARR READS:: deleting iaload : " +
				   local_il.findHandle(local_pos[ialoadsoffset]+local_incr).toString());
	    len= local_il.findHandle(local_pos[ialoadsoffset]+local_incr).getInstruction().getLength();
	    local_il.delete(local_il.findHandle(local_pos[ialoadsoffset]+local_incr));
	    newincr-=len;
	    local_incr -= len;
	    local_il.setPositions();
	}catch(Exception e6)
	{
	    local_incr -= len;
	    newincr-=len;
	    local_il.setPositions();
	    ///e6.printStackTrace();
	}
	return newincr;

    }

    public void adjustArrLengths()
    {
        int total_incr=0,aload_ip=-1000;
	org.apache.bcel.generic.ConstantPoolGen cpg1 = classgen.getConstantPool();
	String classname1 = classgen.getFileName();

	sandmark.program.Method[] localm= classgen.getMethods();

    	sandmark.program.Method localmg =classgen.getMethod(cur_mtd,cur_sig);
	
        org.apache.bcel.generic.InstructionList local_il= localmg.getInstructionList();
	org.apache.bcel.generic.Instruction[] local_in=local_il.getInstructions();
	int[] local_pos=local_il.getInstructionPositions();

	org.apache.bcel.generic.InstructionHandle ihr=null;

	if(DEBUG)
	{
	    System.out.println("ARR LENGTH: LVINDEX   :"+lvindex);
	    System.out.println("ARR LENGTH: Method Getcode printout");
	    System.out.println(localmg.getCode().toString());
	}

	org.apache.bcel.classfile.CodeException [] coe = 
	    classgen.getMethod(cur_mtd,cur_sig).getCode().getExceptionTable();
	    ///	    classgen.getMethods()[cur_mtd].getCode().getExceptionTable();
	org.apache.bcel.classfile.CodeException [] coecopy
	    = new org.apache.bcel.classfile.CodeException[coe.length];
	for (int coeindex=0;coeindex < coe.length;coeindex++)
	{
	    coecopy[coeindex]= coe[coeindex].copy();
	}

  	for(int i=0;i<local_in.length;i++)
	{
	    if(DEBUG)
		System.out.println(" ARRLENGTHS:  Reading .. " +local_in[i].toString());
	    org.apache.bcel.generic.ALOAD aload =null;

	    if(local_in[i] instanceof org.apache.bcel.generic.ALOAD &&
	       ( ( (org.apache.bcel.generic.ALOAD)local_in[i] ).getIndex()==lvindex)  &&
	       local_in[i+1] instanceof org.apache.bcel.generic.ARRAYLENGTH )
		{
		    aload_ip=local_pos[i];
		    total_incr+=editlength(local_il,local_pos,i);
		    local_il.setPositions();
		    
		    adjustExceptions(coe,coecopy,aload_ip,total_incr,total_incr);
		    classgen.getMethod(cur_mtd,cur_sig).getCode().setExceptionTable(coe);
		    coe =classgen.getMethod(cur_mtd,cur_sig).getCode().getExceptionTable();
		    
		    for (int coeindex=0;coeindex < coe.length;coeindex++)
				coecopy[coeindex]= coe[coeindex].copy();
		
		    local_in=local_il.getInstructions();
		    local_pos=local_il.getInstructionPositions();
		    
		}
	}
 	localmg.setInstructionList(local_il);
	localmg.setMaxLocals(no_of_locals);
	localmg.setMaxStack();

	localmg.stripAttributes(true);


    }
    public int editlength(org.apache.bcel.generic.InstructionList local_il,
     			int []local_pos, int aloads_offset)
    {
      int local_incr=0;
      if(DEBUG)
	System.out.println(" ARR LENGTH:: insert aload_2ndarray before: " +
			   local_il.findHandle(local_pos[aloads_offset]+local_incr).toString());
      local_il.insert(local_il.findHandle(local_pos[aloads_offset]+local_incr),
		      new org.apache.bcel.generic.ALOAD(arr2_lvindex));
      local_il.setPositions();
      local_incr+= ( new org.apache.bcel.generic.ALOAD(arr2_lvindex)).getLength();
      

      if(DEBUG)
	System.out.println(" ARR LENGTH:: insert arrayLENGTH before: " +
			   local_il.findHandle(local_pos[aloads_offset]+local_incr).toString());
      local_il.insert(local_il.findHandle(local_pos[aloads_offset]+local_incr),
		      new org.apache.bcel.generic.ARRAYLENGTH());
      local_il.setPositions();
      local_incr+= ( new org.apache.bcel.generic.ARRAYLENGTH() ).getLength();
      
      if(DEBUG)
	 System.out.println(" ARR LENGTH:: insert aload_1st array before: " +
			   local_il.findHandle(local_pos[aloads_offset]+local_incr).toString());
      local_il.insert(local_il.findHandle(local_pos[aloads_offset]+local_incr),
		      new org.apache.bcel.generic.ALOAD(arr1_lvindex));
      local_incr+= (new org.apache.bcel.generic.ALOAD(arr1_lvindex)).getLength();
      local_il.setPositions();
      
      if(DEBUG)
	  System.out.println(" ARR LENGTH:: insert arrayLENGTH before: " +
			     local_il.findHandle(local_pos[aloads_offset]+local_incr).toString());
      local_il.insert(local_il.findHandle(local_pos[aloads_offset]+local_incr),
		      new org.apache.bcel.generic.ARRAYLENGTH());
      local_il.setPositions();
      local_incr+= ( new org.apache.bcel.generic.ARRAYLENGTH() ).getLength();
      
      if(DEBUG)
	  System.out.println(" ARR LENGTH:: insert iadd before: " +
			     local_il.findHandle(local_pos[aloads_offset]+local_incr).toString());
      local_il.insert(local_il.findHandle(local_pos[aloads_offset]+local_incr),
		      new org.apache.bcel.generic.IADD());
      local_il.setPositions();
      local_incr+= ( new org.apache.bcel.generic.IADD() ).getLength();
      
      int len=0;
	try
	{
	    local_il.setPositions();
	    if(DEBUG)
		System.out.println(" ARR length:: deleting original aload : " 
				   +local_il.findHandle(local_pos[aloads_offset]+local_incr).toString());
	    len= local_il.findHandle(local_pos[aloads_offset]+local_incr).getInstruction().getLength();
	    local_il.delete(local_il.findHandle(local_pos[aloads_offset]+local_incr));
	    local_incr -= len;
	    local_il.setPositions();
	}catch(Exception e6)
	{
	    local_incr -= len;
	    local_il.setPositions();
	    e6.printStackTrace();
	}

	try{
	    local_il.setPositions();
	    if(DEBUG)
		System.out.println(" ARR length:: deleting original arraylength : " +
				   local_il.findHandle(local_pos[aloads_offset+1]+local_incr).toString());
	    len= local_il.findHandle(local_pos[aloads_offset+1]+local_incr).getInstruction().getLength();
	    local_il.delete(local_il.findHandle(local_pos[aloads_offset+1]+local_incr));
	    local_incr -= len;
	    local_il.setPositions();
	}catch(Exception e6)
	{
	    local_incr -= len;
	    local_il.setPositions();
	    e6.printStackTrace();
	}
	return local_incr;
    }

    public void adjustArrReads()
    {

        org.apache.bcel.generic.ConstantPoolGen  cpg1 = classgen.getConstantPool();
	String classname1 = classgen.getFileName();

	sandmark.program.Method[] localm= classgen.getMethods();

    	sandmark.program.Method localmg = classgen.getMethod(cur_mtd,cur_sig);

        org.apache.bcel.generic.InstructionList local_il= localmg.getInstructionList();
	org.apache.bcel.generic.Instruction[] local_in=local_il.getInstructions();
	int[] local_pos=local_il.getInstructionPositions();

	org.apache.bcel.generic.InstructionHandle ihr=null;

	if(DEBUG)
	{
	    System.out.println("ARR READS: LVINDEX               :"+lvindex);
	    System.out.println("ARR READS: Method Getcode printout");
	    System.out.println(localmg.getCode().toString());
	}

	int decl_ip=-1;
	int count=0,finalflag=0;
	int total_incr=0,aload_ip=0;
	org.apache.bcel.classfile.CodeException [] coe =
	    classgen.getMethod(cur_mtd,cur_sig).getCode().getExceptionTable();
	org.apache.bcel.classfile.CodeException [] coecopy
			= new org.apache.bcel.classfile.CodeException[coe.length];
	for (int coeindex=0;coeindex < coe.length;coeindex++)
	{
	    coecopy[coeindex]= coe[coeindex].copy();
	}

	for(int i=0;i<local_in.length;i++)
	{
	    if(DEBUG)
		System.out.println(" ARR READS:  Reading .. " +local_in[i].toString());
	    org.apache.bcel.generic.ALOAD aload =null;
	    
	    if(local_in[i] instanceof org.apache.bcel.generic.ALOAD &&
	       ( ((org.apache.bcel.generic.ALOAD)local_in[i]).getIndex()==lvindex) )
	    {
		count++;
		
		for(int j=i+1;j<local_in.length;j++)
		{
		    if(DEBUG)
			System.out.println(" Reading .. " +local_in[j].toString());

		     // modify for xastore
		    if(local_in[j] instanceof org.apache.bcel.generic.IALOAD)
		    {
			count--;
			if(count==0) // found iastore for an aload i.e array write
			{
			    aload_ip=local_pos[i];
			    org.apache.bcel.generic.InstructionHandle ih1=
				local_il.findHandle(local_pos[i]);
			    org.apache.bcel.generic.InstructionHandle ih2=
				local_il.findHandle(local_pos[j]);
			    
			    total_incr+=editingReads(local_il,local_pos,i,j);
			    
			    finalflag=1;
			    break;
			}
			else
			    continue;
			
		    }
		}

		if(finalflag==1)
		{
		    finalflag=0;
		    local_il.setPositions();
		    
		    adjustExceptions(coe,coecopy,aload_ip,total_incr,total_incr);
		    classgen.getMethod(cur_mtd,cur_sig).getCode().setExceptionTable(coe);
		    coe =classgen.getMethod(cur_mtd,cur_sig).getCode().getExceptionTable();
		    
		    for (int coeindex=0;coeindex < coe.length;coeindex++)
			coecopy[coeindex]= coe[coeindex].copy();
	  	        
		    local_in=local_il.getInstructions();
		    local_pos=local_il.getInstructionPositions();
		    
		}

	    }

	}
	
 	localmg.setInstructionList(local_il);
	localmg.setMaxLocals(no_of_locals);
	localmg.setMaxStack();

	localmg.stripAttributes(true);

    }

    public void adjustArrWrites()
    {
    	String classname1 = classgen.getFileName();
        org.apache.bcel.generic.ConstantPoolGen  cpg1 = classgen.getConstantPool();  /*/**/
	sandmark.program.Method[] localm= classgen.getMethods();
    	sandmark.program.Method local_mg =classgen.getMethod(cur_mtd,cur_sig);
	if(DEBUG)
	    System.out.println ( local_mg.getCode().toString());
	

	org.apache.bcel.generic.InstructionList local_il= local_mg.getInstructionList();
	org.apache.bcel.generic.Instruction[] local_in=local_il.getInstructions();
	int[] local_pos=local_il.getInstructionPositions();

	org.apache.bcel.generic.InstructionList Ilist=
	    new org.apache.bcel.generic.InstructionList();
	org.apache.bcel.generic.InstructionList Vlist=null;

	org.apache.bcel.generic.InstructionHandle wr_start=null ,
	    wr_end=null,index_end=null;
	org.apache.bcel.generic.InstructionHandle[] value=null;
	org.apache.bcel.generic.InstructionHandle dummy_ih=null;

	int index_st_ip=-1,index_end_ip=-1,incr=0,Ilistlen=-1,Vlistlen=-1,finalflag=0;
	int count=0; // this is to keep track of arrays ,using arrays as index
	int ip_end =-1; // ip in bytecode after the array write
	int total_incr=0,aload_ip=0;
	org.apache.bcel.classfile.CodeException [] coe =
	    classgen.getMethod(cur_mtd,cur_sig).getCode().getExceptionTable();
	org.apache.bcel.classfile.CodeException [] coecopy =
	    new org.apache.bcel.classfile.CodeException[coe.length];
	for (int coeindex=0;coeindex < coe.length;coeindex++)
	    coecopy[coeindex]= coe[coeindex].copy();
	

	if(DEBUG)
	{
	    System.out.println("LVINDEX : "+lvindex);
	    System.out.println("Method getCode printout");
	    System.out.println(local_mg.getCode().toString());
	    System.out.println("Instrlist printout");
	    org.apache.bcel.generic.InstructionHandle[] tt2 = 
		local_il.getInstructionHandles();
	    for(int z=0;z<tt2.length;z++)
		System.out.println(tt2[z].toString());		
	}


	for(int i=0;i<local_in.length;i++)
	{
	    if(DEBUG)
		System.out.println(" arr writes: Reading .. " +local_in[i].toString());

	    org.apache.bcel.generic.ALOAD aload =null;
	    

	    if(local_in[i] instanceof org.apache.bcel.generic.IASTORE)
	    {
		wr_end=local_il.findHandle(local_pos[i]);
		if(DEBUG)
		    System.out.println(" Before giving call to arr " + wr_end.toString());
	
		org.apache.bcel.generic.InstructionHandle arr_name=
		    arr.getNameofArray(wr_end,local_mg);
		if(arr_name.getInstruction() instanceof org.apache.bcel.generic.ALOAD &&
		   ( ((org.apache.bcel.generic.ALOAD)(arr_name.getInstruction())).getIndex()==lvindex))
		{
		    wr_start=arr_name;

		    int aloads_ip=wr_start.getPosition();
		    index_end=arr.getIndexInstructions(wr_end,local_mg); // ih is got
		    index_end_ip=index_end.getPosition();
		    index_st_ip=wr_start.getPosition() + wr_start.getInstruction().getLength();
		    
		    if(DEBUG)
			System.out.println(" aloadsipo "  + aloads_ip +" end ip = " +index_end_ip);
		    
		    int toadd=  local_pos[i] + (new org.apache.bcel.generic.IASTORE()).getLength();
		    Ilistlen=index_end_ip-index_st_ip+(local_il.findHandle(index_end_ip)).getInstruction().getLength();
		    
		    Ilist=createIndexIlist(index_st_ip,index_end_ip,local_il,local_pos);
		    value=arr.getValueInstructions(wr_start,wr_end,local_mg);
		    
		    if(DEBUG)
			System.out.println(" AFTER CALLING SS VALUE [0] : " +value[0].toString() +
					   " and VALUE[1] : " +    value[1].toString());
		    
		    Vlistlen=value[1].getPosition() + value[1].getInstruction().getLength() -
			(  value[0].getPosition() + value[0].getInstruction().getLength() );

		    if(DEBUG)
			System.out.println(" Value list len = " +Vlistlen + "Indexlstlen =" +Ilistlen);

		    Vlist= createValueIlist(value,local_il,local_pos);
		    
		    int in_t_now=setIndex(aloads_ip,local_il,local_pos,dummy_ih);
		    total_incr+=in_t_now;
		    local_il.setPositions();
		    int fun2=deleteIlist(index_st_ip+in_t_now,index_end_ip+in_t_now,local_il,local_pos);
		    total_incr+=fun2;

		    
		    int tempe=finish_insert(local_il,local_pos,aloads_ip,Ilistlen,Vlistlen,Ilist,Vlist,toadd+fun2+in_t_now);
		    total_incr+=tempe;
		    if(DEBUG)
		    {
			System.out.println("********************************************************");
			org.apache.bcel.generic.InstructionHandle[] tt1 = local_il.getInstructionHandles();
			for(int z=0;z<tt1.length;z++)
			    	System.out.println(tt1[z].toString());
			System.out.println("********************************************************");
		    }
		    finalflag =1;
		    
		}  // end of if iastore
		//} // eo for loop
		
		if(finalflag==1)
		{
		    finalflag=0;
		    local_il.setPositions();
		    adjustExceptions(coe,coecopy,aload_ip,total_incr,total_incr);
		    classgen.getMethod(cur_mtd,cur_sig).getCode().setExceptionTable(coe);
		    coe =classgen.getMethod(cur_mtd,cur_sig).getCode().getExceptionTable();
		    
		    for (int coeindex=0;coeindex < coe.length;coeindex++)
	  		coecopy[coeindex]= coe[coeindex].copy();
	  	        

		    local_in=local_il.getInstructions();
		    local_pos=local_il.getInstructionPositions();
		    
		}
	    }  //eo if
	} //eo for  loop
	

	local_il.setPositions();
 	local_mg.setInstructionList(local_il);
	local_mg.setMaxLocals(no_of_locals);
	local_mg.setMaxStack();
	local_mg.stripAttributes(true);
	if(DEBUG)
	{
	    System.out.println("ARR WRITES ::Instrlist printout AFTER ");
	    org.apache.bcel.generic.InstructionHandle[] tt2 = local_il.getInstructionHandles();
	    for(int z=0;z<tt2.length;z++)
		System.out.println(tt2[z].toString());
		
	}
	///classgen.setConstantPool(cpg1);

    }

    public org.apache.bcel.generic.InstructionList createIndexIlist(int sip,int eip,
    	org.apache.bcel.generic.InstructionList local_il,int[] local_pos)
    {
	org.apache.bcel.generic.InstructionList temp = 
	    new org.apache.bcel.generic.InstructionList();
	int ii=0;
	for(int k=sip;k<=eip;k+=ii)
	{
	    if(DEBUG)
		System.out.println(" Appending " + local_il.findHandle(k).toString());
	    temp.append(local_il.findHandle(k).getInstruction());
	    ii=local_il.findHandle(k).getInstruction().getLength();
	}

	return temp;
    }

    public org.apache.bcel.generic.InstructionList createValueIlist
	(org.apache.bcel.generic.InstructionHandle[] val,
    	org.apache.bcel.generic.InstructionList local_il,int[] local_pos)
    {
	org.apache.bcel.generic.InstructionList temp = 
	    new org.apache.bcel.generic.InstructionList();
	int ii=0;
	int sip= val[0].getPosition() + val[0].getInstruction().getLength();
	int eip= val[1].getPosition() ;

	if(DEBUG)
	    System.out.println(" Going to append to value list  " + sip + "  " +eip);

	for(int k=sip;k<=eip;k+=ii)
	{
	    if(DEBUG)
		System.out.println(" Appending " + local_il.findHandle(k).toString());
	    temp.append(local_il.findHandle(k).getInstruction());
	    ii=local_il.findHandle(k).getInstruction().getLength();
	}

	return temp;
    }

    public void split(int dup,int xstore,int flag,byte arrtype)
    {
    	int start_bc=-1,end_bc=-1;
	int start_ip=-1,end_ip=-1;
	int incr=0,ret=-1;
	int local_incr=0;
	int flag1=-1;
	int arrlen_offset=-100;
	int oldlens_offset=-1;
	int oldastore_ip=0,oldnarr_ip=0,oldlen_ip=0;
	org.apache.bcel.classfile.CodeException [] coe =
	    classgen.getMethod(cur_mtd,cur_sig).getCode().getExceptionTable();
	org.apache.bcel.classfile.CodeException [] coecopy
	    = new org.apache.bcel.classfile.CodeException[coe.length];
	for (int coeindex=0;coeindex < coe.length;coeindex++)
	    coecopy[coeindex]= coe[coeindex].copy();

    	if(flag==0)
	{
	  org.apache.bcel.generic.ConstantPoolGen cpg1 = classgen.getConstantPool();  /*/**/
	  String classname1 = classgen.getFileName();
	  sandmark.program.Method local_mg = classgen.getMethod(cur_mtd,cur_sig);
	  org.apache.bcel.generic.InstructionList local_il= local_mg.getInstructionList();
	  org.apache.bcel.generic.Instruction[] local_in=local_il.getInstructions();
	  int [] local_pos =local_il.getInstructionPositions();

    	  for(int k=0;k<local_in.length;k++)
  	  {
	         org.apache.bcel.generic.NEWARRAY newarray=null;
		 if(((local_in[k] instanceof org.apache.bcel.generic.NEWARRAY) 
		    && (local_in[k+1] instanceof org.apache.bcel.generic.DUP))||
		    ( local_in[k] instanceof org.apache.bcel.generic.NEWARRAY &&
		      local_in[k+1] instanceof org.apache.bcel.generic.ASTORE))
		 {
			for(int x=k+1;x<local_in.length;x++)
			{
			    org.apache.bcel.generic.ASTORE astore=null;
			    if(DEBUG)
				System.out.println(" Split: " + local_in[x].toString());
			    if((local_in[x]instanceof org.apache.bcel.generic.ASTORE)
			       && ( ((org.apache.bcel.generic.ASTORE)local_in[x]).getIndex()
				    !=lvindex) )
			    {
				break;
			    }
			    else if ((local_in[x]instanceof org.apache.bcel.generic.ASTORE)
				     && ( ((org.apache.bcel.generic.ASTORE)local_in[x]).getIndex()
					  ==lvindex) )
			    {
				start_bc=local_pos[k+1];   // dups
				start_ip=k+1;
				end_bc=local_pos[x];
				arrlen_offset=k-1;
				end_ip=x;
				flag1=1;
				break;
			    }
			    else
				continue;
		  	}
		 }
		 else
		     continue;

		 if(flag1==1)
		 	break;

           }
	   oldlens_offset=local_pos[arrlen_offset];
	   ret=addNewArrays(arrtype,end_ip,arrlen_offset,local_il,local_pos,cpg1);
	   local_il.setPositions();
	   adjustExceptions(coe,coecopy,oldlens_offset,ret,ret);
	   local_mg.setInstructionList(local_il);
	   local_mg.setMaxLocals(no_of_locals);
	   local_mg.setMaxStack();
	   local_mg.stripAttributes(true);
	}
	else
	{
	  org.apache.bcel.generic.ConstantPoolGen cpg1 = classgen.getConstantPool();  /*/**/
	  String classname1 = classgen.getFileName();
	  sandmark.program.Method local_mg = classgen.getMethod(cur_mtd,cur_sig);
	  org.apache.bcel.generic.InstructionList local_il= local_mg.getInstructionList();
	  org.apache.bcel.generic.Instruction[] local_in=local_il.getInstructions();
	  int [] local_pos =local_il.getInstructionPositions();

	  for(int k=0;k<local_in.length;k++)
  	  {
	     org.apache.bcel.generic.NEWARRAY newarray=null;
	     if((local_in[k] instanceof org.apache.bcel.generic.NEWARRAY) 
		&& (local_in[k+1] instanceof org.apache.bcel.generic.ASTORE)
		&& ((org.apache.bcel.generic.ASTORE)local_in[k+1]).getIndex()==lvindex)
	     {
		 arrlen_offset=k-1;
		 oldlens_offset=local_pos[arrlen_offset];
		 if(getLength(k,local_in)==-1 )
		     ret=addDynaNewArr(arrtype,k+1,k-1,k,local_il,local_pos,cpg1);
		 else
		     ret=addNewArrays(arrtype,k+1,k-1,local_il,local_pos,cpg1);
	     }
	     else if(local_in[k] instanceof org.apache.bcel.generic.NEWARRAY && 
		     local_in[k+1] instanceof org.apache.bcel.generic.DUP)
	     {
		 for(int cnt=k+2;cnt<local_in.length;cnt++)
		 {
		     if(local_in[cnt] instanceof org.apache.bcel.generic.ASTORE &&
			((org.apache.bcel.generic.ASTORE)local_in[cnt]).getIndex()==lvindex)
		     {
			 arrlen_offset=k-1;
			 oldlens_offset=local_pos[arrlen_offset];
			 if(getLength(k,local_in)==-1 )
			     ret=addDynaNewArr(arrtype,cnt,k-1,k,local_il,local_pos,cpg1);
			 else
			     ret=addNewArrays(arrtype,cnt,k-1,local_il,local_pos,cpg1);
		     } 
		 }
		    
	     }
	     
          }
	  local_il.setPositions();
	  adjustExceptions(coe,coecopy,oldlens_offset,ret,ret);
	  local_mg.setInstructionList(local_il);
	  local_mg.setMaxLocals(no_of_locals);
	  local_mg.setMaxStack();
	  local_mg.stripAttributes(true);
	}

	sandmark.program.Method[] localm= classgen.getMethods();
	String classname1 = classgen.getFileName();
	org.apache.bcel.generic.ConstantPoolGen  cpg1=classgen.getConstantPool();
    	sandmark.program.Method local_mg =classgen.getMethod(cur_mtd,cur_sig);

	if(DEBUG)
	{
	    System.out.println (" CHEK: in split ");
	    System.out.println ( local_mg.getCode().toString());
	}

	org.apache.bcel.generic.InstructionList local_il= local_mg.getInstructionList();
    	org.apache.bcel.generic.Instruction[] local_in=local_il.getInstructions();
    	int[] local_pos=local_il.getInstructionPositions();
	coe =classgen.getMethod(cur_mtd,cur_sig).getCode().getExceptionTable();
	for (int coeindex=0;coeindex < coe.length;coeindex++)
		coecopy[coeindex]= coe[coeindex].copy();
	
	flag1=-1;


    	if(flag==1)
	{
	  int locallen=0;
	  for(int k=0;k<local_in.length;k++)
  	  {
	      org.apache.bcel.generic.NEWARRAY newarray=null;
	      if((local_in[k] instanceof org.apache.bcel.generic.NEWARRAY) 
		 && (local_in[k+1] instanceof org.apache.bcel.generic.DUP))
	      {
		  for(int x=k+1;x<local_in.length;x++)
		  {
		      org.apache.bcel.generic.ASTORE astore=null;

		      if((local_in[x]instanceof org.apache.bcel.generic.ASTORE)
			 && ( ((org.apache.bcel.generic.ASTORE)local_in[x]).getIndex()
			      !=lvindex) )
		      {
			  break;
		      }
		      else if ((local_in[x]instanceof org.apache.bcel.generic.ASTORE)
			       && ( ((org.apache.bcel.generic.ASTORE)local_in[x]).getIndex()
				    ==lvindex) )
		      {
			  oldlen_ip=local_pos[k-1];
			  start_bc=local_pos[k+1];   // dups
			  start_ip=k+1;
			  end_bc=local_pos[x];
			  end_ip=x;
			  flag1=1;
			  break;
		      }
		      else
			  continue;
		  }
	      }
	      else
		  continue;

	      if(flag1==1)
		  break;

           }

	  boolean firstarray=true;
	  for(int i=start_ip;i<=end_ip;i++)
	  {
	      if(DEBUG)
		  System.out.println("Reading ... "+local_in[i].toString());
	      
	      int arrayindex;
	      int xastore_ip=-1,xastore_bc=-1;
	      if(local_in[i] instanceof org.apache.bcel.generic.DUP)
	      {
		  arrayindex= getLength(i+2,local_in);
		  
		  if(DEBUG)
		      System.out.println(" Current Array Index:" +arrayindex + " Lvindex:"+lvindex);

		  for(int j=i+1;j<=end_ip;j++)
		  {
		      if(!(local_in[j] instanceof org.apache.bcel.generic.IASTORE))
			  continue;
		      
		      xastore_bc=local_pos[j];
		      xastore_ip=j;
		      break;
		  }
		  //delete dup and arrindex
		  try
		  {

		      local_il.setPositions();
		      
		      if(DEBUG)
		      {
			  System.out.println(" GOING TO DEL : " +
					     local_il.findHandle(local_pos[i]+incr).toString());
		      }

		      int len= local_il.findHandle(local_pos[i]+incr).getInstruction().getLength();
		      local_il.delete(local_il.findHandle(local_pos[i]+incr));
		      incr-=len;
		      local_il.setPositions();
		      
		      if(DEBUG)
			  System.out.println( " LeN: "+incr);
		      
		      local_il.setPositions();
		      
		      if(DEBUG)
		      {
			  System.out.println(" GOING TO DEL : " +
					     local_il.findHandle(local_pos[i+1]+incr).toString());
		      }

		      len= local_il.findHandle(local_pos[i+1]+incr).getInstruction().getLength();
		      local_il.delete(local_il.findHandle(local_pos[i+1]+incr));
		      local_il.setPositions();
		      incr-=len;

		      if(DEBUG)
			  System.out.println( " LeN: "+incr);
		      
		  }catch(Exception e)
		  {
		      e.printStackTrace();
		  }

		   if(firstarray==true)
		   {
		       firstarray=false;
		       local_il.setPositions();
		       if(DEBUG)
			   System.out.println("Insert aload before: " +
					  local_il.findHandle(local_pos[i+2]+incr).toString());
		       local_il.insert(local_il.findHandle(local_pos[i+2]+incr),
				       new org.apache.bcel.generic.ALOAD(arr1_lvindex) );
		       incr+= ( new org.apache.bcel.generic.ALOAD(arr1_lvindex)).getLength();
		       local_il.setPositions();
		       
		       if(DEBUG)
			   System.out.println("Insert iconst before: " +
					      local_il.findHandle(local_pos[i+2]+incr).toString());
			local_il.insert(local_il.findHandle(local_pos[i+2]+incr),
					new org.apache.bcel.generic.ICONST(arrayindex/2) );
			incr+= ( new org.apache.bcel.generic.ICONST(arrayindex/2)).getLength();
			local_il.setPositions();
		   }
		   else
		   {
			local_il.setPositions();
			if(DEBUG)
			    System.out.println(" odd Insert aload before: " +
					       local_il.findHandle(local_pos[i+2]+incr).toString());
			local_il.insert(local_il.findHandle(local_pos[i+2]+incr),
					new org.apache.bcel.generic.ALOAD(arr2_lvindex) );
			incr+= ( new org.apache.bcel.generic.ALOAD(arr2_lvindex)).getLength();

			local_il.setPositions();
			if(DEBUG)
			System.out.println(" odd INsert iconst before: " +
					   local_il.findHandle(local_pos[i+2]+incr).toString());
			local_il.insert(local_il.findHandle(local_pos[i+2]+incr),
					new org.apache.bcel.generic.ICONST(arrayindex/2) );
			incr+= ( new org.apache.bcel.generic.ICONST(arrayindex/2)).getLength();
			local_il.setPositions();
			firstarray=true;
		   }
		  }   // if (dup)
	      }    //for

	      try
	      {
		  if(DEBUG)
		      System.out.println("DEL: " +local_il.findHandle(local_pos[end_ip]+incr).toString());
		  int len= local_il.findHandle(local_pos[end_ip]+incr).getInstruction().getLength();
		  locallen=len;
		  local_il.delete(local_il.findHandle(local_pos[end_ip]+incr));
		  local_il.setPositions();
		  incr-=len;

	      }catch(Exception e)
	      {
		  incr-=locallen;
		  if(DEBUG)
		      System.out.println( " LeN: in catch "+incr);
		  ///e.printStackTrace();
	      }

	      try
	      {
		  if(DEBUG)
		      System.out.println("DEL: " +local_il.findHandle(local_pos[start_ip-1]).toString());
		  int len= local_il.findHandle(local_pos[start_ip-1]).getInstruction().getLength();
		  locallen=len;
		  local_il.delete(local_il.findHandle(local_pos[start_ip-1]));
		  local_il.setPositions();
		  incr-=len;
	      }catch(Exception e)
	      {
		  incr-=locallen;
		  
		  if(DEBUG)
		      System.out.println( " LeN: in catch "+incr);
		  ///e.printStackTrace();
	      }

	      try
	      {
		  if(DEBUG)
		      System.out.println("DEL: " +local_il.findHandle(local_pos[start_ip-2]).toString());
		  int len= local_il.findHandle(local_pos[start_ip-2]).getInstruction().getLength();
		  locallen=len;
		  local_il.delete(local_il.findHandle(local_pos[start_ip-2]));
		  local_il.setPositions();
		  incr-=len;
	      }catch(Exception e)
	      {
		  incr-=locallen;
		  if(DEBUG)
		      System.out.println( " LeN: in catch "+incr);
		  ///e.printStackTrace();
	      }
	      
	      if(DEBUG)
	      {
		  System.out.println(" Changed bytecode posn : Start :: "+start_bc+" End:: "+end_bc);
		  System.out.println(" Changed instr posn : Start :: "+start_ip+" End:: "+end_ip);
		  System.out.println( " CHEKING INCR after everything: "+incr);

		  org.apache.bcel.generic.InstructionHandle[] tt2 = local_il.getInstructionHandles();
		  for(int z=0;z<tt2.length;z++)
		  	System.out.println(tt2[z].toString());
	      }

	      adjustExceptions(coe,coecopy,oldlen_ip,incr,incr);
	  }
	  else
	  {

	      for(int k=0;k<local_in.length;k++)
	      {

		  int del_len=0;
		  org.apache.bcel.generic.NEWARRAY newarray=null;
		  if((local_in[k] instanceof org.apache.bcel.generic.NEWARRAY) && 
		     (local_in[k+1] instanceof org.apache.bcel.generic.ASTORE)
		     && ((org.apache.bcel.generic.ASTORE)local_in[k+1]).getIndex()==lvindex)
		 {
		     oldastore_ip=local_pos[k+1];
		     oldnarr_ip=local_pos[k];
		     oldlen_ip=local_pos[k-1];
		     if(DEBUG)
			 System.out.println("astore :"+ oldastore_ip +" newarr:"+oldnarr_ip + " len:"+oldlen_ip);

		     int locallen=0;
		     
		     try
                     {
			 local_il.setPositions();
			 if(DEBUG)
			     System.out.println("DEL: " +local_il.findHandle(local_pos[k+1]).toString());
			 int len= local_il.findHandle(local_pos[k+1]).getInstruction().getLength();
			 locallen=len;
			 del_len-=len;
			 local_il.delete(local_il.findHandle(local_pos[k+1]));
			 incr-=len;

		     }catch(Exception e)
		     {
			 incr-=locallen;
			 e.printStackTrace();
		     }


        	   try
                   {
		       local_il.setPositions();
		       if(DEBUG)
			   System.out.println("DEL: " +local_il.findHandle(local_pos[k]).toString());
		       int len= local_il.findHandle(local_pos[k]).getInstruction().getLength();
		       locallen=len;
		       del_len-=len;
		       local_il.delete(local_il.findHandle(local_pos[k]));
		       incr-=len;
		   }catch(Exception e)
		   {
		       incr-=locallen;
		       e.printStackTrace();
		   }             /// dont forget to do iii lar to incr-=locallen for all these cases too
		   
		   // Newly added code
		     try
                     {
			 local_il.setPositions();
			 if(DEBUG)
			     System.out.println("DEL: " +local_il.findHandle(local_pos[k-1]).toString());
			 int len= local_il.findHandle(local_pos[k-1]).getInstruction().getLength();
			 locallen=len;
			 del_len-=len;
			 local_il.delete(local_il.findHandle(local_pos[k-1]));
			 incr-=len;

		     }catch(Exception e)
		     {
			 incr-=locallen;
			 e.printStackTrace();
		     }
		     // end of newly added code

		   adjustExceptions(coe,coecopy,oldlen_ip,del_len,del_len);
		   break;
		 } // if
	      } // for
	      
	  } // else
	  
	  local_il.setPositions();
	  local_mg.setInstructionList(local_il);
	  local_mg.setMaxLocals(no_of_locals);
	  local_mg.setMaxStack();
	  local_mg.stripAttributes(true);

    }

    /**
      * The main function that coordinates the calls to all other functions 
      * which actually execute the algorithm
      */
    public void arr_creat_split()
    {
	org.apache.bcel.generic.Instruction[] local_in =null;
	org.apache.bcel.generic.InstructionList local_il=null;
	org.apache.bcel.generic.InstructionHandle temp_ih =null;
	int pos[],astart_bc=-1,aend_bc=-1,astart_ip=-1,aend_ip=-1;
	org.apache.bcel.generic.ConstantPoolGen  cpg1=classgen.getConstantPool();
	String classname1 = classgen.getFileName();

    	for(int i=0;i<methods1.length;i++)
	{
	    methodgen= methods1[i];

	    if(methodgen.getName().equals("<init>") || 
	       methodgen.getName().equals("<clinit>") || methodgen.isFinal())
		continue;
	    if(DEBUG)
		System.out.println(" Applying array splitting for "+ methodgen.getClassName()
				   + ":"+methodgen.getName());
	    
	    cur_mtd= methodgen.getName();
	    cur_sig = methodgen.getSignature();

	    no_of_locals = methodgen.getMaxLocals();
	    local_il = methodgen.getInstructionList();
	    pos = local_il.getInstructionPositions();	    
	    local_in = local_il.getInstructions();
	    to_del = local_il.getInstructionHandles();

	   

	    for(int k=0;k<local_in.length;k++)
	    {
		
		org.apache.bcel.generic.NEWARRAY newarray=null;
		if(!(local_in[k] instanceof org.apache.bcel.generic.NEWARRAY))
		    continue;
	
		len_of_array=arraycount=increase=0;
		
		newarray= (org.apache.bcel.generic.NEWARRAY)local_in[k];
		arraytype=newarray.getTypecode();
		if(arraytype != org.apache.bcel.Constants.T_INT){
		    if(DEBUG)
			System.out.println("Array is not integer array");
		    continue;
		}

		astart_bc=pos[k];
		astart_ip=k;
		
		for(int x=k+1;x<local_in.length;x++)
		{
		    org.apache.bcel.generic.ASTORE astore=null;
		    if(!(local_in[x]instanceof org.apache.bcel.generic.ASTORE))		        
			continue;
			
		    astore =(org.apache.bcel.generic.ASTORE)local_in[x];
		    lvindex= astore.getIndex();
		    
		    aend_bc=pos[x];
		    aend_ip=x;
		    
		    if(DEBUG)
		    {
			System.out.println(" LOCAL VARIABLE INDEX:" + lvindex +
					   " Array Len: "+len_of_array);
			System.out.println(" ByteCode Positions Start : " + 
					   astart_bc +" Array End: "+aend_bc);
		    }
		    break;
		}
		
	
		if(local_in[k+1] instanceof org.apache.bcel.generic.DUP)
		{
		    boolean res=testifok(local_in,pos,local_il,cpg1,methodgen);
		    if(DEBUG)
			System.out.println(" Can the array be modified ? " +res);
		    if(res==true)
		    {
			len_of_array=getLength(k,local_in);
			if(len_of_array==-1)
			{
			    System.out.println("Unable to apply Array Splitter for this array ");
			    break;
			}
			int dupstart_ip=k+1;
			split(dupstart_ip,aend_ip,1,arraytype);
			adjustArrWrites();
			adjustArrReads();
			adjustArrLengths();
		    }
		}
		else if(local_in[k+1] instanceof org.apache.bcel.generic.ASTORE)
		{
		    boolean res=testifok(local_in,pos,local_il,cpg1,methodgen);
		    if(DEBUG)
			System.out.println(" Can the array be modified ? " +res);
		    if(res==true)
		    {
			len_of_array=getLength(k,local_in);
			if(len_of_array==-1)
			{
			    System.out.println("Unable to apply Array Splitter for this array ");
			    break;
			}
			if(DEBUG)
			    System.out.println("LVIndex: " + lvindex);
			split(0,k+1,0,arraytype);
			adjustArrWrites();
			adjustArrReads();
			adjustArrLengths();
		    }
		}
		
		break;
	    }

	    if(DEBUG)
	    {
		System.out.println(" AFTER ALL MODIFications ");
		System.out.println(classgen.getMethod(cur_mtd,cur_sig).getCode().toString());
	    }
	}

    }

    /** The future work will include the integration of Array Merging with Array Splitting.
     * The reason I am saying this is that this function won't be needed then ..
     * @param local_in Instruction Array
     * @param local_pos Instruction Position Array
     * @param local_il Instruction List Array (editing purposes)
     * @param cpg1 org.apache.bcel.generic.ConstantPoolGen
     * @param local_mg sandmark.program.Method 
    */
    public boolean testifok(org.apache.bcel.generic.Instruction [] local_in,
			    int [] local_pos,
			    org.apache.bcel.generic.InstructionList local_il,
			    org.apache.bcel.generic.ConstantPoolGen cpg1,
			    sandmark.program.Method local_mg)
    {
    	boolean b=true;
	sandmark.analysis.stacksimulator.StackSimulator ss = local_mg.getStack();

    	for(int i=0;i<local_in.length;i++)
	{
	    if(DEBUG)
		System.out.println(" Testing : " + local_in[i].toString());
	    if(local_in[i] instanceof org.apache.bcel.generic.INVOKESTATIC )
	    {
		org.apache.bcel.generic.InvokeInstruction is=
		    (org.apache.bcel.generic.InvokeInstruction)local_in[i];
		org.apache.bcel.generic.Type[] argtypes =is.getArgumentTypes(cpg1);
		 int no_of_args=argtypes.length;
		 org.apache.bcel.generic.InstructionHandle ih=local_il.findHandle(local_pos[i]);
		 b= arr.chekifarrayinstaticmtd(ih,lvindex,no_of_args,ss);
		 if(b==false)
		     return false;
		 else if(b==true && i!=(local_in.length-1))
		     continue;
		 else if(b==true && i==(local_in.length-1))
		     return true;
	    }
	    else if (local_in[i] instanceof org.apache.bcel.generic.INVOKEVIRTUAL)
	    {
		org.apache.bcel.generic.InvokeInstruction is=
		    (org.apache.bcel.generic.InvokeInstruction)local_in[i];
		org.apache.bcel.generic.Type[] argtypes =is.getArgumentTypes(cpg1);
		int no_of_args=argtypes.length;
		org.apache.bcel.generic.InstructionHandle ih=local_il.findHandle(local_pos[i]);
		b= arr.chekifarrayinobjectsmtd(ih,lvindex,no_of_args,ss);
		if(b==false)
		    return false;
		else if(b==true && i!=(local_in.length-1))
		    continue;
		else if(b==true && i==(local_in.length-1))
		    return true;
	    }
	    else if (local_in[i] instanceof org.apache.bcel.generic.ARETURN)
	    {
		org.apache.bcel.generic.ReturnInstruction is=
		    (org.apache.bcel.generic.ReturnInstruction)local_in[i];
		org.apache.bcel.generic.InstructionHandle ih=local_il.findHandle(local_pos[i]);
		b= arr.chekifarrayinreturn(ih,lvindex,ss);
		if(b==false)
		    return false;
		else if(b==true && i!=(local_in.length-1))
		    continue;
		else if(b==true && i==(local_in.length-1))
		    return true;
	    }
	}
	return b;
    }


   /**
     * Calls the other methods to obfuscate the class file using the
     * Array-Spltting algorithm
     * @param app Application Object
     */

    public void apply(sandmark.program.Application app) throws Exception
    {
        java.util.Iterator classes = app.classes();
        while(classes.hasNext())
        {
	    classgen = (sandmark.program.Class)classes.next();
	    String classname=classgen.getFileName();
	    if(DEBUG)
		System.out.println("ClasName: "+ classname);
	    int canproceed =initialize(classgen);
	    if(canproceed ==-1)
	 	continue;
	    else
	    {
		arr_creat_split();
	    }
	}
    }

    /**
      * Constructor
      */

    public IntArraySplitter() {}

    public sandmark.config.ModificationProperty[] getMutations()
    {
    	sandmark.config.ModificationProperty[] temp=null;
	return temp;
    }

    public String getShortName()
    {
	return "Integer Array Splitter";
    }

    public String getDescription()
    {
	return "Array Splitter splits an array into two and adjusts read"+
	    ",write and other references made by the old array" ;
    }

    public String getAuthor()
    {
	return "Ashok Venkatraj";
    }

    public String getAuthorEmail()
    {
	return "ashok@cs.arizona.edu";
    }

    public String getLongName() {
	return "Split an array in a class into two arrays and changes the references";
    }

    public java.lang.String getAlgHTML()
    {
	return
	    "<HTML><BODY>" +
	    "IntArraySplitter is a class obfuscator which splits an array" +
       " in a class into two arrays and changes the references." +
	    "<TABLE>" +
	    "<TR><TD>" +
	    "Author: <a href =\"mailto:ashok@cs.arizona.edu\">Ashok Venkatraj</a>\n" +
	    "</TD></TR>" +
	    "</TABLE>" +
	    "</BODY></HTML>";
    }

    public java.lang.String getAlgURL(){
	return "sandmark/obfuscate/ArrayObfuscation/doc/help.html";
    }

    /* This function is called whenever code is added inside in a try-catch block. Modifies the
     * startPC,endPC and handlerPC of the exception table to reflect the changes to the code.
     *
     * @param coe ExceptionTable Object
     * @param coecopy A deep copy of the exception table of the unmodified code.
     * @param cur_pos Position of the bytecode instruction that is modified in the unmodifed code.
     * @param count Number of new instructions added to method in place of current instruction.
     * @param addcount A cumulative count of the instructions added to the method.
     */
    private void adjustExceptions(org.apache.bcel.classfile.CodeException []coe,
				  org.apache.bcel.classfile.CodeException []coecopy,
				  int cur_pos,int count,int addcount)
    {
	int epc;
	int spc;
	int hpc;

	for (int i=0;i<coe.length;i++)
	{
	    epc=coecopy[i].getEndPC();
	    spc = coecopy[i].getStartPC();
	    hpc = coecopy[i].getHandlerPC();

	    if (cur_pos >= spc && cur_pos <= epc)
	    {

		coe[i].setEndPC(epc+addcount);
		coe[i].setHandlerPC(hpc+addcount);

	    }
	    else if (cur_pos <= epc) 
	    {
		coe[i].setStartPC(spc+addcount);
		coe[i].setEndPC(epc+addcount);
		coe[i].setHandlerPC(hpc+addcount);
	    }
	    else
	    {
		for (int j=i+1;j<coe.length;j++)
		{
		    int epc1=coecopy[j].getEndPC();
		    int spc1 = coecopy[j].getStartPC();
		    int hpc1 = coecopy[j].getHandlerPC();
		    if ( spc1 == spc && epc1 == epc && cur_pos <= hpc1)
		    {
			coe[j].setHandlerPC(hpc1+addcount);
		    }

		}
	    }

	}
	
	if(DEBUG)
	{
	    System.out.println( " EXCEPTION TABLE:");
	    for (int i=0;i<coe.length;i++)
	    {
		System.out.println(coe[i].getStartPC() 
				   +"  "+ coe[i].getEndPC()
				   +"  "+coe[i].getHandlerPC());
	    }
	}
    }

}

