package sandmark.watermark.dm;

/**
 * Author: Zachary Heidepriem
 */

public class DM  
    extends sandmark.watermark.StaticWatermarker {

   private static boolean DEBUG = false; 
   private static final String MAGIC_START = "4", MAGIC_END = "1";  
   
   private sandmark.util.ConfigProperties mConfigProps;
   public sandmark.util.ConfigProperties getConfigProperties() {
      if(mConfigProps == null) {
         String args[][] = new String[][] {
               {"Original File","","Path to un-watermarked file",
                null,"J","SR",},
         };
         mConfigProps = new sandmark.util.ConfigProperties(args,null);
      }
      return mConfigProps;
   }

   /**
   *  Returns this watermarker's short name.
   */
   public String getShortName() {
      return "Davidson/Myhrvold";
   }

   /**
   *  Returns this watermarker's long name.
   */
   public String getLongName() {
      return "Embed a unique signature via block reordering.";
   }

   /*
   *  Get the HTML codes of the About page.
   */
   public java.lang.String getAlgHTML(){
      return 
         "<HTML><BODY>" +
         getDescription() +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href=\"mailto:" + getAuthorEmail() + "\">" + 
             getAuthor() + "</a>" +
         "</TR></TD>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public String getAuthor(){
      return "Zachary Heidepriem";
   }

   public String getAuthorEmail(){
      return "zachary@cs.arizona.edu";
   }

   public String getDescription(){
      return "The DM algorithm generates a unique signature and embeds it " +
             "into the executable via a reordering of basic blocks.";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = 
        {sandmark.config.ModificationProperty.I_REORDER_INSTRUCTIONS,
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE};
      return properties;
   }

   /*
   *  Get the URL of the Help page
   */
   public java.lang.String getAlgURL(){
      return "sandmark/watermark/dm/doc/help.html";
   }    
    
   /* Generate and embed a signature value into the program. The props argument
    * holds a watermark and possibly a key
    *  <UL>
    *     <LI> Watermark: The watermark to encode.
    *     <LI> Key: The key with which to recover the watermark
    *  </UL>
    */   
     
   public void embed(sandmark.watermark.StaticEmbedParameters params)
         throws sandmark.watermark.WatermarkingException {       
      
      java.util.Vector watermark = getPermutation(params.watermark);
      MethodChooser mc = new MethodChooser(params.app, params.key, watermark.size());
      sandmark.program.Method oldMethod = mc.getMethod();

      if(oldMethod != null)         
           watermark(oldMethod, watermark);              
      else
         throw new sandmark.watermark.WatermarkingException(
         "WM too long for this program (not enough basic blocks).");
   } //embed
   
   /*  Gets a permutation for the watermark contained in props 
    */
    static public long vertexCount(java.math.BigInteger value) {
	java.math.BigInteger nfact = java.math.BigInteger.ONE;
	long n = 1;
	do {
	    n++;
	    java.math.BigInteger N = java.math.BigInteger.valueOf(n);
	    nfact = nfact.multiply(N);
	} while (nfact.compareTo(value)<=0);
	return n;
    }

    static void swap (java.util.Vector V, long i, long j) {
	java.lang.Object tmp = V.get((int)i);
	V.set((int)i, V.get((int)j));
	V.set((int)j, tmp);
    }

    static java.util.Vector createVector(long length) {
	java.util.Vector per = new java.util.Vector();
	per.ensureCapacity((int)length);
	per.setSize((int)length);
	return per;
    }

    // encode perIndex into the corresponding permutation
    // PRE2: 0 <= perIndex < factorial(perLength)
    // POST1: perIndex = 0
    static public java.util.Vector index2perm(
					      long perLength, java.math.BigInteger perIndex)  {

	java.util.Vector per = createVector(perLength);
	for(long i=0; i<perLength; i++) 
	    per.set((int)i, new java.lang.Long(i));

	for(long r=2; r<=perLength; r++) {
	    java.math.BigInteger R = java.math.BigInteger.valueOf(r);
	    java.math.BigInteger[] DR = perIndex.divideAndRemainder(R);
	    java.math.BigInteger D = DR[0];
	    java.math.BigInteger S = DR[1];
	    long s = S.longValue();
	    perIndex = D;           // we're reducing perIndex to 0
	    swap(per, r-1, s);
	}

	if (perIndex.compareTo(java.math.BigInteger.ZERO)!= 0)
	    System.out.println("Postcondition 1 of index2perm() is violated! " +
			       "perIndex=" + perIndex.toString());

	return per;
    }

    // decode a permutation per, returning its corresponding perIndex
    // PRE2: per[0..perLength-1] is a permutation of 0..perLength-1
    // POST1: 0 <= perIndex < factorial(n)
    // POST2: per[i] = i forall i in 0..perLength-1
    static public java.math.BigInteger perm2index(
						  long perLength, java.util.Vector per)  {
	java.math.BigInteger perIndex = java.math.BigInteger.ZERO;

	for(long r=perLength; r>=2; r--) {
	    // search for s: per[s] == r-1
	    long s = 0;
	    for(long i=0; i<r; i++) {
		s = i;
		long S = ((java.lang.Long) per.get((int)i)).longValue();
		if (S == (r-1)) 
		    break;
	    }
	    swap(per, r-1, s);
	    java.math.BigInteger R = java.math.BigInteger.valueOf(r);
	    java.math.BigInteger X = java.math.BigInteger.valueOf(s);
	    perIndex = (perIndex.multiply(R)).add(X);
	}

	return perIndex;
    }

   private static java.util.Vector getPermutation(String wmstring){
      wmstring = MAGIC_START + wmstring + MAGIC_END;
      java.math.BigInteger wmInt = sandmark.util.StringInt.encode(wmstring);
      int perm_size = (int)vertexCount(wmInt);
      java.util.Vector watermark = index2perm(perm_size, wmInt);
      
      if(DEBUG) {
          System.out.println("The StringInt is: " + wmInt);
          System.out.print("The permutatation is: " + watermark);
      }
      return watermark;
   } 
   
   /*  Gets a string for the permutation in perm 
    */
   private static String getString(java.util.Vector perm ){
      java.math.BigInteger aBigInt = perm2index(perm.size(), perm);         
      String s = sandmark.util.StringInt.decode(aBigInt);          
      return s;
   }    

   private void watermark(
      sandmark.program.Method method, 
      java.util.Vector watermark){          
                   
      String className = method.getEnclosingClass().getName();
      sandmark.analysis.controlflowgraph.MethodCFG cfg = 
        new  sandmark.analysis.controlflowgraph.MethodCFG(method);       

      if(DEBUG) {
          System.out.println("Embedding in " + 
                             method + " of " + className);              
          System.out.println((method.getExceptionTable() == null) + "" +
                             method.getExceptions().length +
                             method.getExceptionHandlers().length);
      }
      java.util.Vector table = makeTable(cfg);                   
      sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
        reorder(cfg, watermark);                                               
      
      if(DEBUG){
          System.out.println("Original:");
          //cfg.printCFG();
          System.out.println("Reordered:");
          //for(int i=0;i<blocks.length;i++) System.out.println(blocks[i]);
          //System.out.println(watermark);
      }
      Relinker rl = new Relinker(blocks,table,watermark);
      org.apache.bcel.generic.InstructionList allInstrs = rl.relink();
      buildMethod(method, allInstrs);       
   }
    
    //Returns a vector that contains instruction handles of 
    //first instructions of every block
    //the last entry in the table is the first instruction in the method
   private java.util.Vector makeTable(
        sandmark.analysis.controlflowgraph.MethodCFG cfg ){      
      java.util.Vector v = new java.util.Vector();
      java.util.Iterator it = 
          sandmark.diff.methoddiff.DMDiffAlgorithm.getBlocksInOrder(cfg).
          iterator();                       
      sandmark.analysis.controlflowgraph.BasicBlock pre = 
            (sandmark.analysis.controlflowgraph.BasicBlock)it.next();       
      org.apache.bcel.generic.InstructionHandle startLink = 
        (org.apache.bcel.generic.InstructionHandle)(pre.getInstList().get(0));
      sandmark.analysis.controlflowgraph.BasicBlock post;                  
      while(it.hasNext()){                    
         post = (sandmark.analysis.controlflowgraph.BasicBlock)it.next();
         v.add((post.getInstList().get(0)));          
         pre = post;
      }           
      v.add(startLink); //last element in v is link to first      
      return v;
   }    
   
    private void buildMethod(
       sandmark.program.Method mg,
       org.apache.bcel.generic.InstructionList allInstrs) {
       
      allInstrs.setPositions(); 
      mg.setInstructionList(allInstrs);  
      mg.removeLocalVariables();
      mg.removeLineNumbers();
   }

    private boolean unique
        (int n,sandmark.analysis.controlflowgraph.BasicBlock blocks[]){
        
        for(int i = 0; i < blocks.length; i++)
            if(i != n &&
               new sandmark.diff.methoddiff.ComparableBlock(blocks[n]).
               compareTo(
               new sandmark.diff.methoddiff.ComparableBlock(blocks[i])) == 0)
		return false;
	return true;
    }
   
   
   private sandmark.analysis.controlflowgraph.BasicBlock[] reorder(
                        sandmark.analysis.controlflowgraph.MethodCFG cfg,
                        java.util.Vector watermark){            
      //Reorder the blocks in the cfg to match watermark
      //The new watermark accounts for non-unique blocks 
      java.util.Vector newWatermark = new java.util.Vector();
      java.util.ArrayList list = sandmark.diff.methoddiff.DMDiffAlgorithm.
          getBlocksInOrder(cfg);
      sandmark.analysis.controlflowgraph.BasicBlock[] orig_blocks = 
         new sandmark.analysis.controlflowgraph.BasicBlock[list.size()];  
      sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
         new sandmark.analysis.controlflowgraph.BasicBlock[list.size()];  
      for(int i = 0; i < list.size(); i++)
          orig_blocks[i] = (sandmark.analysis.controlflowgraph.BasicBlock)
              list.get(i);  
      
      //ctr is the current index of blocks
      //wm_index is the index in the original wm
      int ctr = 0, wm_index = 0; 

      //skip to first unique block      
      while(ctr < orig_blocks.length && !unique(ctr, orig_blocks)){
	  blocks[ctr] = orig_blocks[ctr];
          newWatermark.add(new Long(ctr++));
      }

      //if(DEBUG) System.out.println(ctr);
      while(ctr < blocks.length){
          if(wm_index < watermark.size()){
              //n is the current value in the old watermark
              int n = ((Long)watermark.get(wm_index++)).intValue();
              //Get the "nth" unique block, 
              //and all the non-unique blocks that follow it
              //ctr2 is an index in the old blocks
              int ctr2 = 0, index = -1;              
              while(index < n)
                  if(unique(ctr2++,orig_blocks))
                      index++;
              java.util.Vector v = new java.util.Vector();
              v.add(orig_blocks[ctr2-1]);
              /*if(DEBUG)
                  System.out.println("The " + n + "th unique block is: " +
                  orig_blocks[ctr2-1]);*/
              while(ctr2 < orig_blocks.length && !unique(ctr2,orig_blocks)){
                  v.add(orig_blocks[ctr2]);                 
                  ctr2++;
              }
              for(int i = 0; i < v.size(); i++){
                  blocks[ctr++] = 
                      (sandmark.analysis.controlflowgraph.BasicBlock)v.get(i);
                  newWatermark.add(new Long(ctr2-v.size()+i));    
              }            
          }
          else {//have reordered the first watermark* unique blocks
              //so just copy the old blocks over
              blocks[ctr] = orig_blocks[ctr]; 
              newWatermark.add(new Long(ctr++));              
          }
      }     
      watermark.clear();
      watermark.addAll(newWatermark);
      return blocks;
      /*
      while(it.hasNext()){          
         b = (sandmark.analysis.controlflowgraph.BasicBlock)it.next();
	 //b must be unique
         if(ctr < watermark.size()){	    	    
            int relativeIndex = watermark.indexOf(new Long(ctr));
            int absoluteIndex = getAbsoluteIndex(relativeIndex, blocks, cfg);
  
            blocks[absoluteIndex] = b;
	    b = (sandmark.analysis.controlflowgraph.BasicBlock)it.next();
	    while(notUnique(b)){           
                blocks[absoluteIndex++] = b;
                b = (sandmark.analysis.controlflowgraph.BasicBlock)it.next();
            }
	 }
         else
            blocks[ctr] = b; //rest of blocks are in same order
         ctr++;            
      }             
      return blocks;*/
   }
   
   public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params) throws 
        sandmark.watermark.WatermarkingException {
      sandmark.program.Application originalApp;
      try{
         java.io.File origPath = (java.io.File)
         	getConfigProperties().getValue("Original File");
         if(origPath == null || !origPath.exists()) {
            String watermarked_jar = params.app.getMostRecentPath().toString();
            String original_jar = watermarked_jar.substring
            	(0,watermarked_jar.indexOf("_wm")) +
                  watermarked_jar.substring
                  (watermarked_jar.indexOf("_wm")+3, 
                        watermarked_jar.length());
            if(DEBUG) 
               System.out.println("Using " + original_jar + 
               " as source jar.");
            origPath = new java.io.File(original_jar);
         }
         originalApp = new sandmark.program.Application(origPath);
      }catch(Exception e){
         throw new sandmark.watermark.WatermarkingException
         ("This is a non-blind algorithm. The watermarked file must " +
               "have the form '{fileName}_wm.jar' where the source file " +
         "is in the same directory and named '{fileName}.jar'");
      }
      return new BlockRecognizer(originalApp,params.app).iterator();
   }
   
   /* A vector to store the watermarks
    * found in the program.
    */
   private class BlockRecognizer extends java.util.Vector {
  
      public BlockRecognizer(sandmark.program.Application orig_app,
                             sandmark.program.Application wm_app) { 
         //Put all the methods into a vector         
         sandmark.program.Class[] wm_classes = wm_app.getClasses();
         sandmark.program.Class[] orig_classes = orig_app.getClasses();
         java.util.Vector wm_methods = new java.util.Vector();
         for(int i = 0; i < wm_classes.length; i++)
             for(int j = 0; j < wm_classes[i].getMethods().length; j++)
                 wm_methods.add(wm_classes[i].getMethods()[j]);
         //for both apps
         java.util.Vector orig_methods = new java.util.Vector();
         for(int i = 0; i < orig_classes.length; i++)
             for(int j = 0; j < orig_classes[i].getMethods().length; j++)
                 orig_methods.add(orig_classes[i].getMethods()[j]);

         sandmark.analysis.controlflowgraph.MethodCFG wm_cfg = null;     
         sandmark.analysis.controlflowgraph.MethodCFG orig_cfg = null;
         //For all pairs of methods, build CFGs and try to get watermarks
         for(int i = 0; i < orig_methods.size();i++){
             //if(DEBUG) System.out.println(i + "/" + orig_methods.size());
             sandmark.program.Method om = 
                 (sandmark.program.Method)orig_methods.get(i);
             for(int j = 0; j < wm_methods.size(); j++){
                 sandmark.program.Method wmm = 
                     (sandmark.program.Method)wm_methods.get(j);
                 try{
                     wm_cfg = 
                         new sandmark.analysis.controlflowgraph.MethodCFG(wmm);
                     orig_cfg = 
                         new sandmark.analysis.controlflowgraph.MethodCFG(om);
                 }catch(sandmark.analysis.controlflowgraph.
                        EmptyMethodException eme){
                     continue;
                 }
                 if(DEBUG)
                     System.out.println("Examining " + om.getName() + 
                                        " and " + wmm.getName());
                 getWatermarks(wm_cfg, orig_cfg);
             }
         }                     
         if(DEBUG)
             for(int i = 0; i < size(); i++)
                 System.out.println("Watermark #" + i + ": " + 
                                    get(i));
         wm_app.close();
         orig_app.close();      
      }   

      private void getWatermarks
          (sandmark.analysis.controlflowgraph.MethodCFG wm_cfg,
           sandmark.analysis.controlflowgraph.MethodCFG orig_cfg) {   
         
         if(DEBUG) { 
             //wm_cfg.printCFG();
             //System.out.println("\n\noriginal: "); 
             //orig_cfg.printCFG(); 
         }           
         //Put all the orig blocks into a list
         java.util.Vector ordering = new java.util.Vector();
         java.util.ArrayList list = sandmark.diff.methoddiff.DMDiffAlgorithm.
             getBlocksInOrder(orig_cfg);
         sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
               new sandmark.analysis.controlflowgraph.BasicBlock[list.size()];
         for(int i = 0; i < list.size(); i++)
             blocks[i] = 
                 (sandmark.analysis.controlflowgraph.BasicBlock)list.get(i);
    
         //Find locations of corresponding blocks in wm cfg
         if(DEBUG)
             System.out.println("Getting ordering...");
         for(int i = 0; i < blocks.length; i++)
             if(unique(i, blocks)){
                 java.util.Iterator it = wm_cfg.basicBlockIterator();
                 ordering.add(getLocations(blocks[i],it));
             }
        
         if(ordering.contains(null))
             return; //couldn't match all blocks
         if(DEBUG)
             System.out.println("Generating all possibles...");
         java.util.Vector allOrderings = getPossibleOrderings(ordering);
	 if(DEBUG){
             System.out.println("Ordering: " + ordering);
             System.out.println("All Orderings: " + allOrderings);
         }
         for(int i = 0; i < allOrderings.size(); i ++){
             ordering = (java.util.Vector)allOrderings.get(i);             
             //Since we skipped some non-unique blocks, the
             //indices arent consecutive. So we have to reduce.
             // e.g. [2,5,3] becomes [1,3,2]
             if(DEBUG)
                 System.out.println("Reducing and converting " + i + "/" + 
                                    allOrderings.size() + "...");
             reduce(ordering);                             
             //Renumber the orderings and add perms for trailing numbers
             //An ordering like [2,1,3] could really be [2,1] so
             //put both into a new vector
             java.util.Vector perms = getPerms(ordering);
             //Decode all the permutations. If decoded string
             //has magic number, we can add it to 'this'
             for(int j = 0; j < perms.size(); j++){
                 String s = getString((java.util.Vector)perms.get(j));
                 if(s.startsWith(MAGIC_START) && s.endsWith(MAGIC_END)){
                     if(DEBUG) System.out.println("The watermark is: " + s);
                     String extractedWatermark = 
                         s.substring(MAGIC_START.length(),
                                     s.length()-MAGIC_END.length());
                     if(!contains(extractedWatermark))
                         add(extractedWatermark);                 
                 }
             }        
         }      
      }      

       //Reverse the process of reordering
       //e.g. if the indices in wm cfg is [1,2,0],
       //the encoded permutation was [2,0,1]
      private java.util.Vector getPerms(java.util.Vector ordering){
         java.util.Vector perms = new java.util.Vector();        
         java.util.Vector tmp = new java.util.Vector();
         for(int j=0; j<ordering.size(); j++)
             tmp.add(new Long(0));
         for(int j=0; j<ordering.size(); j++){
             Long k = (Long)ordering.get(j);
             tmp.setElementAt(new Long(j), k.intValue());
         }
         perms.add(tmp);
               
         //Perms 2 are the extra perms we need for
         //consecutive trailing numbers
         java.util.Vector perms2 = new java.util.Vector();      
         for(int i = 0; i<perms.size(); i++){               
            boolean flag = true;
            java.util.Vector v = (java.util.Vector)perms.get(i);          
            for(int j = v.size()-1; flag && j > 0; j--){             
               if(((Long)v.elementAt(j)).intValue() == j)              
                  perms2.add(new java.util.Vector(v.subList(0,j)));
               else flag = false;
            }          
         }
         perms.addAll(perms2);
         if(DEBUG){             
            System.out.println("Perms: ");
            for(int i = 0; i<perms.size(); i++)
               System.out.println(perms.get(i));          
         }
         return perms;
      }

      private void reduce(java.util.Vector ordering){
         Object[ ] sorted = ordering.toArray();
         java.util.Arrays.sort(sorted);
         java.util.Vector tmp = new java.util.Vector();
         for(int i = 0; i < sorted.length; i++)
            tmp.add(sorted[i]);         
         for(int j=0; j<ordering.size(); j++){
             Long k = (Long)ordering.get(j);
             int idx = tmp.indexOf(k);                  
             ordering.setElementAt(new Long(idx), j);                  
         }               
         if(DEBUG)
            System.out.println("Reduced vector: " + ordering);
      }

       // v is a vector of Long vectors.
       // returns all unique Long vectors w st w[i] is a member of v[i]
       private java.util.Vector getPossibleOrderings(java.util.Vector v ){
           java.util.Vector result = new java.util.Vector();            
           //base case
           if (v.size() == 0)          
               return new java.util.Vector();         
           else{
               java.util.Vector list = (java.util.Vector)v.get(0);       
               for(int j = 0; j < list.size(); j++){                        
                   Long curr = (Long)list.get(j);
                   java.util.Vector rest = (java.util.Vector)v.clone();    
                   rest.remove(0);                        
                   java.util.Vector restOfOrderings = 
                       getPossibleOrderings(rest);
                   int res = restOfOrderings.size();
                   if(res==0){
                       res++;
                       java.util.Vector vec1 = new java.util.Vector();
                       java.util.Vector vec2 = new java.util.Vector();
                       vec1.add(vec2);
                       restOfOrderings = vec1;
                   }

                   for(int k = 0; k < res; k++){                               
                       java.util.Vector tmp = new java.util.Vector();      
                       tmp.add(curr);
                       java.util.Vector perm = (java.util.Vector)
                           restOfOrderings.get(k);
                       if(!perm.contains(curr)){
                           tmp.addAll(perm); 
                           result.add(tmp);                  
                       }        
                   }            
               }                     
           }      
           return result;       
           }


       //Given a basic block, find all occurences of it in cfg
       //using the compareBlocks method. If it does not occur,
       //or occurs more than once and is not a goto block, return null
      private java.util.Vector getLocations
          (sandmark.analysis.controlflowgraph.BasicBlock block,
           java.util.Iterator it) {         

         int result = 0;
         java.util.Vector v = new java.util.Vector();         
         while (it.hasNext()) {         
             sandmark.analysis.controlflowgraph.BasicBlock next =
                 (sandmark.analysis.controlflowgraph.BasicBlock)it.next();
             if(compareBlocks(block,next) &&
                next.getIH() != null)
                v.add(new Long(next.getIH().getPosition()));
            result++;         
         }
         if(v.size() == 0)
             return null;
         //There should only be 1 location in wm cfg for 
         //the given block, unless the block is a GOTO block
         if(v.size() > 1)
             if(block.getInstList().size() != 1 ||
                !(((org.apache.bcel.generic.InstructionHandle)
                   block.getInstList().get(0)).getInstruction() instanceof
                  org.apache.bcel.generic.GOTO))
                 return null;
         return v;
      }

      private boolean compareInstList(java.util.ArrayList alist, 
                                      java.util.ArrayList blist){
         if(alist.size() == blist.size()){
            for(int i = 0; i < alist.size(); i++){
               org.apache.bcel.generic.InstructionHandle aih =
                 ((org.apache.bcel.generic.InstructionHandle)alist.get(i));
               org.apache.bcel.generic.InstructionHandle bih =
                 ((org.apache.bcel.generic.InstructionHandle)blist.get(i));
               if(aih.getInstruction().getOpcode() != 
                  bih.getInstruction().getOpcode())
                   return false;
            }      
            return true;
         }
         else return false;
      }

      //a is an original block, b is wm
      private boolean compareBlocks(
                   sandmark.analysis.controlflowgraph.BasicBlock ablock,
                   sandmark.analysis.controlflowgraph.BasicBlock bblock){
         java.util.ArrayList a = (java.util.ArrayList)
             ablock.getInstList().clone();
         java.util.ArrayList b = (java.util.ArrayList)
             bblock.getInstList().clone();
         if(a.size() > 0){
            if(((org.apache.bcel.generic.InstructionHandle)
               a.get(a.size()-1)).getInstruction().getOpcode() == 
               org.apache.bcel.Constants.GOTO)
               a.remove(a.size()-1);
         }
         else
             return false; //ignoring empty blocks
            
         if(b.size() > 0){
            if(((org.apache.bcel.generic.InstructionHandle)
               b.get(b.size()-1)).getInstruction().getOpcode() == 
               org.apache.bcel.Constants.GOTO)
               b.remove(b.size()-1);
         }
         else
             return false; //ignoring empty blocks
         if (!compareInstList(a, b))
            return false;          
         return true;
      }        

   } //class BlockRecognizer
   
   private class Relinker{

       //public final static boolean DEBUG = false;
      sandmark.analysis.controlflowgraph.BasicBlock[] blocks;
      java.util.Vector table;
      java.util.Vector watermark;

      public Relinker(sandmark.analysis.controlflowgraph.BasicBlock[] blocks, 
                   java.util.Vector table, 
                   java.util.Vector watermark){
         this.blocks = blocks;
         this.table = table;
         this.watermark = watermark;              
      }

      public org.apache.bcel.generic.InstructionList relink(){

         org.apache.bcel.generic.InstructionList il = 
            new org.apache.bcel.generic.InstructionList(); 
         java.util.Vector newTable = new java.util.Vector();
         org.apache.bcel.generic.InstructionHandle startlink = null; 

         for(int i = 0; i < blocks.length; i++){              
            org.apache.bcel.generic.InstructionHandle ih = null;  
            sandmark.analysis.controlflowgraph.BasicBlock b = blocks[i];
            java.util.ArrayList instrs = b.getInstList(); 

            org.apache.bcel.generic.InstructionHandle firstIH = 
              (org.apache.bcel.generic.InstructionHandle)instrs.get(0);

            //deal with the first instruction
            if(instrs.size() > 1){
               if(firstIH instanceof org.apache.bcel.generic.BranchHandle)
                  ih = il.append(((org.apache.bcel.generic.BranchHandle)
                    firstIH).getInstruction());                   
               else
                  ih = il.append(firstIH.getInstruction()); 
               newTable.add(new IHPair(firstIH, ih));
            }           
            //deal with middle instructions
            for(int j = 1; j < instrs.size()-1; j++)
               il.append(((org.apache.bcel.generic.InstructionHandle)
                 instrs.get(j)).getInstruction());

            //now deal with last instruction
            org.apache.bcel.generic.InstructionHandle oldih =
             (org.apache.bcel.generic.InstructionHandle)
               instrs.get(instrs.size()-1);    
            org.apache.bcel.generic.InstructionHandle newih;
            org.apache.bcel.generic.Instruction inst = oldih.getInstruction();

            int idx = i;
            if (i < watermark.size())
               idx = ((Long)(watermark.get(i))).intValue();      
            if(idx == 0 && ih != null) //first instruction, set startLink
               startlink = ih;
            boolean fixFallThrough = true;
            if(i < watermark.size()-1 && (idx+1) == 
                ((Long)(watermark.get(i+1))).intValue() 
                || idx == blocks.length-1
                || i >= watermark.size())
               fixFallThrough = false;           

            if (inst instanceof org.apache.bcel.generic.BranchInstruction){
               //need to change existing branch              
               newih = 
                 il.append((org.apache.bcel.generic.BranchInstruction)inst);
               if(fixFallThrough){
                  org.apache.bcel.generic.GOTO newInst = 
                     new org.apache.bcel.generic.GOTO
                      ((org.apache.bcel.generic.InstructionHandle)
                       table.get(idx));
                  il.append(newInst);                              
               }
            }
            else{ //some non-branching instruction                 
               newih = il.append(inst);
               //insert new branch instruction (to fix fall throughs)
               if(fixFallThrough){
                   org.apache.bcel.generic.GOTO newInst = 
                      new org.apache.bcel.generic.GOTO
                      ((org.apache.bcel.generic.InstructionHandle)
                       table.get(idx));
                   il.append(newInst);              
               }
            }          
            newTable.add(new IHPair(oldih, newih));              
            //first instruction, set startLink
            if(instrs.size() <= 1 && idx == 0) 
                startlink = newih;
         }                      
         /*if(DEBUG) 
            for(int i=0; i < newTable.size(); i++) 
            System.out.println(newTable.get(i));*/
         org.apache.bcel.generic.InstructionList tmp = 
           new  org.apache.bcel.generic.InstructionList(
               new org.apache.bcel.generic.GOTO(startlink));
         tmp.append(il);
         il = tmp;
         setTargets(il, newTable, watermark);           
         return il;
      } //method Relink 

      private void setTargets(org.apache.bcel.generic.InstructionList il,
                           java.util.Vector t, java.util.Vector watermark){

        for( int i = 0; i < t.size(); i++){
           IHPair pair = (IHPair)t.get(i);
           il.redirectBranches(pair.getA(), pair.getB()); 
           /*if (DEBUG) System.out.println("replacing " + pair.getA() + 
             " with " + pair.getB());*/
        }
      }

      private class IHPair{
         org.apache.bcel.generic.InstructionHandle a;
         org.apache.bcel.generic.InstructionHandle b;

         public IHPair(org.apache.bcel.generic.InstructionHandle a,
                                 org.apache.bcel.generic.InstructionHandle b){

            this.a = a;
            this.b = b;
         }

         public String toString(){
            return "[" + a.toString() + " | " + b.toString() + "]";
         }

         public org.apache.bcel.generic.InstructionHandle getA(){
            return a;         
         }

         public org.apache.bcel.generic.InstructionHandle getB(){
            return b;         
         }
      } //IHPair

   } //class Relinker

   private class MethodChooser {

      //private org.apache.bcel.classfile.Method method;    
      //private org.apache.bcel.classfile.JavaClass jc;
      private sandmark.program.Method method;
      private static final String DEFAULT_KEY = "some_key";
       //public static final boolean DEBUG = false;

      public MethodChooser(sandmark.program.Application app,String key,int minSize){
         try{ 
            java.util.Iterator classes = app.classes();       
            if (!classes.hasNext())
               throw new sandmark.watermark.WatermarkingException(
                        "There must be at least one class to watermark.");

            int numPossibles = numPossibles(app, minSize);
            if(numPossibles > 0){
               int method2mark = getMethod2Mark(key, numPossibles);      
               setMethod(minSize, method2mark, app);
            }  
         }catch(Exception e){e.printStackTrace(); }
      }  

      public sandmark.program.Method getMethod(){      
         return method;      
      }

       private void setMethod(int n, int method2mark,
          sandmark.program.Application app) {

          java.util.ArrayList goodMethods = new java.util.ArrayList();
          for(java.util.Iterator classes = app.classes() ; 
              classes.hasNext() ; ) {
             sandmark.program.Class clazz = 
                (sandmark.program.Class)classes.next();
             for(java.util.Iterator methods = clazz.methods() ; 
                 methods.hasNext() ; ) {
                sandmark.program.Method method = 
                   (sandmark.program.Method)methods.next();
                int size = uniqueBlockCount(method.getCFG());
                if(size >= n && method.getExceptionTable() == null &&
                   method.getExceptionHandlers().length == 0)
                   goodMethods.add(method);
             }
          }

          if(goodMethods.size() != 0)
             method = (sandmark.program.Method)goodMethods.get
                (method2mark % goodMethods.size());
      }

       private int uniqueBlockCount
           (sandmark.analysis.controlflowgraph.MethodCFG cfg){
	   java.util.ArrayList list = sandmark.diff.methoddiff.
               DMDiffAlgorithm.getBlocksInOrder(cfg);
           sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
               new sandmark.analysis.controlflowgraph.BasicBlock[list.size()];
           for(int i = 0; i < list.size(); i++)
               blocks[i] = 
                   (sandmark.analysis.controlflowgraph.BasicBlock)list.get(i);
           int ctr = 0;
           for(int i = 0; i < blocks.length; i++)
               if(unique(i,blocks))
                   ctr++;
           return ctr;
       }                

      private int numPossibles(sandmark.program.Application app, int n) {      
         int sum = 0;
         int max = 0;
         java.util.Iterator classes = app.classes();
         while(classes.hasNext()){  
            sandmark.program.Class cls = 
                (sandmark.program.Class)classes.next();       
            String className = cls.getName();   
            sandmark.program.Method[] methods = cls.getMethods();      

            for(int i = 0; i < methods.length; i++){
               sandmark.program.Method mg = methods[i];
               sandmark.analysis.controlflowgraph.MethodCFG cfg = null;
               try{cfg = new  sandmark.analysis.controlflowgraph.MethodCFG(mg);
               }catch(sandmark.analysis.controlflowgraph.
                      EmptyMethodException eme) { 
                   if(DEBUG)
                       System.out.println("Unable to create cfg for method " + 
                                          methods[i] + " in class " + className);
                   //return 0;
                   continue;
               }   
               //int size = cfg.size()-2; //source and sink, i think?
               int size = uniqueBlockCount(cfg);
               
               //if(DEBUG) System.out.println(size);
	       if( size >= n && 
                   methods[i].getExceptionTable() == null &&
                   methods[i].getExceptionHandlers().length == 0){
                  sum++;   
                  if(size > max)
                      max = size;  
               }    
            }
         }             
         if(DEBUG) {
            System.out.println(sum + " methods with at least " +
                               n + " unique blocks.");
            System.out.println("The largest cfg has: " + max +
                               "unique blocks.");
         }            
         return sum;
      }

      private int getMethod2Mark(String keystring,int n){ 
         if(keystring.equals(""))
            keystring = DEFAULT_KEY;
         java.math.BigInteger keyInt = 
             sandmark.util.StringInt.encode(keystring);       
	 java.util.Random r = sandmark.util.Random.getRandom(); 
	 r.setSeed(keyInt.intValue());          
         return Math.abs(r.nextInt() % n);        
      }    
   } //MethodChooser  

    public static void main(String[] args){
        //String in = "../smtest3/tests/hard/fft/test_wm.jar";
        if(args.length < 1)
            args = new String[]{"TTT_wm.jar"};
        try{            
            sandmark.program.Application app =
                new sandmark.program.Application(args[0]);
            new DM().recognize
                (sandmark.watermark.StaticWatermarker.getRecognizeParams(app));       
        }catch(Exception e){ e.printStackTrace(); }
    }
} //DM

