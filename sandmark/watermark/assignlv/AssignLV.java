package sandmark.watermark.assignlv;

/**
 * This algorithm ..... 
 */

public class AssignLV  
   extends sandmark.watermark.StaticWatermarker {

   private int bitsEmbedded;
   private boolean DEBUG = false;
    
    
    /**
     *  Returns this watermarker's short name.
     */
   public String getShortName() {
      return "Qu/Potkonjak";
   }
    
   /**
     *  Returns this watermarker's long name.
   */
   public String getLongName() {
	   return "Embeds a watermark in the local variable assignments of an application";
   }

    /*
     *  Get the HTML codes of the About page.
     */
   public java.lang.String getAlgHTML(){
	   return 
           "<HTML><BODY>\n" +
           "AssignLV is a watermarking algorithm that embeds the watermark in the local variable assignment by adding constraints to the interference graphs.\n" +
           "<table>\n" +
	    "<TR><TD>\n" +
	    "   Author: <a href=\"mailto:mylesg@cs.arizona.edu\">Ginger Myles</a>\n" +
	    "</TR></TD>\n" +
           "</table>\n" +
           "</BODY></HTML>\n";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return "AssignLV is a watermarking algorithm that embeds the watermark in the local variable assignment by adding constraints to the interference graphs.";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {
         sandmark.config.ModificationProperty.I_MODIFY_METHOD_CODE, 
         sandmark.config.ModificationProperty.I_ADD_LOCAL_VARIABLES};
      return properties;
   }

   public sandmark.config.RequisiteProperty[] getPostprohibited(){
      sandmark.config.RequisiteProperty[] properties = {
         sandmark.config.ModificationProperty.I_MODIFY_METHOD_CODE,
         sandmark.config.ModificationProperty.I_REMOVE_METHODS,
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
         sandmark.config.ModificationProperty.I_CHANGE_LOCAL_VARIABLES,
         sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE,
      };
      return properties;
   }

    /*
     *  Get the URL of the Help page
     */
   public java.lang.String getAlgURL(){
	   return "sandmark/watermark/assignlv/doc/help.html";
   }

/*************************************************************************/
/*                               Embedding                               */
/*************************************************************************/


/* Embed a watermark value into the program. The props argument
 * holds at least the following properties:
 *  <UL>
 *     <LI> Watermark: The watermark value to be embedded.
 *     <LI> Input File: The name of the file to be watermarked.
 *     <LI> Output File: The name of the jar file to be constructed.
 *  </UL>
 */
public void embed(sandmark.watermark.StaticEmbedParameters params)
   throws sandmark.watermark.WatermarkingException {
 
   String key = params.key; 
   String watermark = params.watermark;

   //Convert the watermark to a binary and add the length at the begining
   ToBinary binaryObject = new ToBinary(watermark, true);
   String markToEmbed = binaryObject.getBinary();
   int wmLength = markToEmbed.length();

   //convert the watermark to a binary without adding the length at the begining
   ToBinary bo = new ToBinary(watermark, false);
   String justWM = bo.getBinary();

   if(!params.app.classes().hasNext()){
      throw new sandmark.watermark.WatermarkingException(
      "There must be at least one class to watermark.");
   }

   sandmark.util.Log.message(0, "Watermarking using " + this.getShortName());

   //iterate over the classes putting the class name and method into an arrayList.
   java.util.ArrayList methodList = new java.util.ArrayList();
   for(java.util.Iterator classes = params.app.classes() ; classes.hasNext() ; ) {
      sandmark.program.Class clazz = (sandmark.program.Class)classes.next();
      if(clazz.isInterface()  || clazz.isAbstract())
         continue;
      for(java.util.Iterator methods = clazz.methods() ; methods.hasNext() ; ) {
         sandmark.program.Method method = (sandmark.program.Method)methods.next();
         if(method.getInstructionList() == null)
            continue;
         methodList.add(method);
      }
   }
      
   //sort the list of methods based upon their signature and method length
   MethodCompare mc = new MethodCompare();
   java.util.Collections.sort(methodList, mc);
   
   if(DEBUG)
      System.out.println(methodList);


   //seed a pseudorandom number generator with the key
   long seed;
   if(key == null || key.equals("")){
      seed = 42;
   }else{
      java.math.BigInteger bigIntKey = sandmark.util.StringInt.encode(key);
      seed = bigIntKey.longValue();
   }
   java.util.Random generator = sandmark.util.Random.getRandom(); 
   generator.setSeed(seed);

   //randomly choose methods embedding the watermark
   int methodsLeft = methodList.size();
   String workingWM = markToEmbed;
   while(methodsLeft != 0){
      int slot = Math.abs(generator.nextInt()) % methodList.size();
      sandmark.program.Method method = (sandmark.program.Method)methodList.get(slot);
      if(method == null)
         continue;
      methodList.set(slot,null);
         methodsLeft--;

            //sandmark.program.Method[] methods = origClass.getMethods();
            if(DEBUG)
               System.out.println("method: " + method);

            //generate the interference graph information
            sandmark.analysis.interference.InterferenceGraph ig = method.getIFG();
            
            //do the register allocation
            new sandmark.analysis.controlflowgraph.RegisterAllocator(ig).allocate(true);

            //get the interference graph with the new register allocation
            java.util.ArrayList igNodes = new java.util.ArrayList();
	    for(java.util.Iterator it = ig.nodes() ; it.hasNext() ; )
		igNodes.add(it.next());

            java.util.Collections.sort(igNodes);
            //System.out.println(igNodes);
            
            //add edges to the interference graph based upon the watermark
            workingWM = IGModifier(ig, igNodes, workingWM, justWM,method.getName());

            //do the register allocation on the new interference graph
            //sandmark.analysis.controlflowgraph.RegisterAllocator.DEBUG = true;
            new sandmark.analysis.controlflowgraph.RegisterAllocator(ig).allocate(false);
            //sandmark.analysis.controlflowgraph.RegisterAllocator.DEBUG = false;
      
  
            //igNodes = ig.getVars();

            //java.util.Collections.sort(igNodes);

            method.mark(); 
            method.setMaxLocals();
          
   } 
   //check if we embedded the whole watermark
   if(bitsEmbedded < wmLength){
     throw new sandmark.watermark.WatermarkingException(
        "This watermark is too long for this application.");
     //sandmark.util.Log.message(0, "Embedding failed: unable to embed the entire watermark.");
   }//else{
      //sandmark.util.Log.message(0, "Watermarking using " + this.getShortName() + 
      //"is done");
   //}
}

private String IGModifier(sandmark.analysis.interference.InterferenceGraph ig,
			  java.util.ArrayList igNodes, String workingWM, String justWM,String methodName){
    java.util.HashSet hasFakeEdge = new java.util.HashSet();
   //sort the nodes
   java.util.Collections.sort(igNodes);
   //System.out.println("orig: " + igNodes);
   String wm = "";
   //if(DEBUG) 
   String printwm = "";

   java.util.ArrayList tripleList = new java.util.ArrayList();

   //for each node find the two closest nodes that are not connected
   //add an interference based upon the next bit in the watermark
   int numNodes = igNodes.size();
   for(int i = 0; i < numNodes; i++){
      sandmark.analysis.defuse.DUWeb v = 
        (sandmark.analysis.defuse.DUWeb)igNodes.get(i);

      //check if it interferes with any nodes already in the tripleList
      boolean vInterfere = false;
      for(int k = 0; k < tripleList.size(); k++){
         //boolean interfere = false;
         sandmark.analysis.defuse.DUWeb possible = 
	     (sandmark.analysis.defuse.DUWeb)tripleList.get(k);
         if(ig.hasEdge(v,possible)){
            vInterfere = true;
         }
      }


      if(!(hasFakeEdge.contains(v)) && !vInterfere){
         
         sandmark.analysis.defuse.DUWeb v1 = null;
         sandmark.analysis.defuse.DUWeb v2 = null;

         int j = (i+1) % numNodes;
         boolean foundFirst = false;
         boolean foundSecond = false;

         while(!foundFirst && j!=i){
            v1 = (sandmark.analysis.defuse.DUWeb)igNodes.get(j);

            //check if it interferes with any nodes already in the tripleList
            boolean v1Interfere = false;
            for(int k = 0; k < tripleList.size(); k++){
               sandmark.analysis.defuse.DUWeb possible = 
		   (sandmark.analysis.defuse.DUWeb)tripleList.get(k);
               if(ig.hasEdge(v1,possible)){
                  v1Interfere = true;
               }
            }

            if((!ig.hasEdge(v,v1)) && (v.getIndex() == v1.getIndex()) && (!hasFakeEdge.contains(v1)) && !v1Interfere){
               foundFirst = true;
            }
            j = (j+1) % numNodes;
         }//end while

         while(!foundSecond && j!=i){
            v2 = (sandmark.analysis.defuse.DUWeb)igNodes.get(j);

            //check if it interferes with any nodes already in the tripleList
            boolean v2Interfere = false;
            for(int k = 0; k < tripleList.size(); k++){
               sandmark.analysis.defuse.DUWeb possible = 
		   (sandmark.analysis.defuse.DUWeb)tripleList.get(k);
               if(ig.hasEdge(v2,possible)){
                  v2Interfere = true;
               }
            }


            if(!ig.hasEdge(v,v2) && (v.getIndex() == v2.getIndex()) && (!hasFakeEdge.contains(v2)) && !v2Interfere){
               foundSecond = true;
            }
            j = (j+1) % numNodes;
         }//end while

         if(foundFirst && foundSecond){
            //System.out.println("found a triple: " + v + "\n" + v1 + "\n" + v2);
            tripleList.add(v);
            tripleList.add(v1);
            tripleList.add(v2);
            
            if(workingWM.length() == 0){
               workingWM += justWM;
            }
            char bit = workingWM.charAt(0);
            if(bit == '0'){
		hasFakeEdge.add(v);
		hasFakeEdge.add(v1);
		ig.addEdge(v,v1);
		ig.addEdge(v1,v);
               wm += "0";
               printwm += "0";
            }else{
		hasFakeEdge.add(v);
		hasFakeEdge.add(v2);
		ig.addEdge(v,v2);
		ig.addEdge(v2,v);
               wm += "1";
               printwm += "1";
            }
            workingWM = workingWM.substring(1);
            bitsEmbedded++;
         }//end if
      }//end if
   }//end for
   if(DEBUG && printwm != null && printwm.length() != 0)
      System.out.println("embedded: " + printwm + " in " + methodName);
   return workingWM;
   
}

/*************************************************************************/
/*                              Recognition                              */
/*************************************************************************/

/* An iterator which generates the watermarks
 * found in the program.
 */
   class Recognizer implements java.util.Iterator {
      java.util.Vector result = new java.util.Vector();
      int current = 0;

      public Recognizer(sandmark.watermark.StaticRecognizeParameters params) {
         generate(params);
      }

      public void generate(sandmark.watermark.StaticRecognizeParameters params) {
         java.util.Iterator classes = params.app.classes();         

         //iterate over the classes putting the class name and method into an 
         //arrayList.
         java.util.ArrayList methodList = new java.util.ArrayList();
         while(classes.hasNext()){
            sandmark.program.Class origClass = (sandmark.program.Class)classes.next();
            if(origClass.isInterface()  || origClass.isAbstract())
               continue;

            sandmark.program.Method[] methods = origClass.getMethods();
            for(int i=0; i < methods.length; i++){
               if(methods[i].getInstructionList() != null)
                  methodList.add(methods[i]);
            }
         }
      
         //sort the list of methods based upon their signature and method length
         MethodCompare mc = new MethodCompare();
         java.util.Collections.sort(methodList, mc);

         if(DEBUG)
            System.out.println(methodList);


         //seed a pseudorandom number generator with the key
         long seed;
         if(params.key == null || params.key.equals("")){
            seed = 42;
         }else{
            java.math.BigInteger bigIntKey = 
                sandmark.util.StringInt.encode(params.key);
            seed = bigIntKey.longValue();
         }
         java.util.Random generator = sandmark.util.Random.getRandom(); //new java.util.Random(seed);        
	 generator.setSeed(seed);
 
         //iterate over the methods in bundleArray in the same order as they 
         //where embedded
         int methodsLeft = methodList.size();
         String workingWM = "";
         while(methodsLeft != 0){
            int slot = Math.abs(generator.nextInt()) % methodList.size();
            sandmark.program.Method method = 
               (sandmark.program.Method)methodList.get(slot);
            if(method == null)
               continue;
            methodList.set(slot,null);
            methodsLeft--;
               
                  //generate the interference information
                  sandmark.analysis.interference.InterferenceGraph ig = method.getIFG();

                  //get interference graph nodes
                  java.util.Hashtable indices = new java.util.Hashtable();
		  for(java.util.Iterator it = ig.nodes() ; it.hasNext() ; ) {
                     sandmark.analysis.defuse.DUWeb web = 
                        (sandmark.analysis.defuse.DUWeb)it.next();
                     indices.put(web,new Integer(web.getIndex()));
                  }
               
                  //do the register allocation to obtain the original allocation
                  new sandmark.analysis.controlflowgraph.RegisterAllocator(ig).allocate(true);
                  //method.mark();

                  //get new interference graph nodes
                  java.util.ArrayList origColoring = new java.util.ArrayList();
		  for(java.util.Iterator it = ig.nodes() ; it.hasNext() ; )
		      origColoring.add(it.next());

                  //sort the list
                  java.util.Collections.sort(origColoring);
                  //System.out.println(origColoring);

                  //compare the coloring to obtain the watermark
                  workingWM += compare(ig,origColoring, indices,method.getName());
         }//end iteration over methods

               //If binary watermark does not have length at least 16 search for
               //the field that contains the remaining portion of the watermark
               //System.out.println("********The complete binary watermark obtained from the methods" + workingWM);

               //changed all 16's to 8's
               if(workingWM.length() < 8){
                  result.add("null");
                  
               }else{ 
                  //take the first 8 to determine how long the watermark should be
                  String binaryLength = workingWM.substring(0, 8);
                  workingWM = workingWM.substring(8);
                  FromBinary binaryObject = new FromBinary(binaryLength, true);
                  String sbl = binaryObject.getString();
                  Integer intObject = new Integer(sbl);
                  int wmLength = intObject.intValue();

                  //if the binary watermark is not that length search for the field
                  //that contains the remaining protion of the watermark
                  if(wmLength > workingWM.length()){
                     result.add("null");
                  }else if(wmLength <= workingWM.length()){
                     while(workingWM.length() >= wmLength){
                        String wm = workingWM.substring(0, wmLength);
                        workingWM = workingWM.substring(wmLength);
                        
                        FromBinary bo = new FromBinary(wm, false);
                        String watermark = bo.getString();
                        result.add(watermark);
                     }
                  }
                  

               }
      } //end generate()

      private String compare(sandmark.analysis.interference.InterferenceGraph ig,
			     java.util.ArrayList origColoring, 
			     java.util.Hashtable markedIndices,
                             String methodName){
         String wm = "";
         java.util.ArrayList tripleList = new java.util.ArrayList();
	 java.util.HashSet hasFakeEdge = new java.util.HashSet();

         //System.out.println("orig: " + origColoring);

         int origLength = origColoring.size();
         for(int i = 0; i < origLength; i++){
            sandmark.analysis.defuse.DUWeb v = 
              (sandmark.analysis.defuse.DUWeb)origColoring.get(i);
            int w = ((Integer)markedIndices.get(v)).intValue();

            //check if it interferes with any nodes already in the tripleList
            boolean vInterfere = false;
            for(int k = 0; k < tripleList.size(); k++){
               sandmark.analysis.defuse.DUWeb possible = 
		   (sandmark.analysis.defuse.DUWeb)tripleList.get(k);
               if(ig.hasEdge(v,possible)){
                  vInterfere = true;
               }
            }

            if(!(hasFakeEdge.contains(v)) && !vInterfere){

               sandmark.analysis.defuse.DUWeb v1 = null;
               sandmark.analysis.defuse.DUWeb v2 = null;
               int w1 = -1;
               int w2 = -1;

               int j = (i+1) % origLength;
               boolean foundFirst = false;
               boolean foundSecond = false;

               while(!foundFirst && j!=i){
                  v1 = (sandmark.analysis.defuse.DUWeb)origColoring.get(j);

                  //check if it interferes with any nodes already in the tripleList
                  boolean v1Interfere = false;
                  for(int k = 0; k < tripleList.size(); k++){
		      sandmark.analysis.defuse.DUWeb possible = 
			  (sandmark.analysis.defuse.DUWeb)tripleList.get(k);
                     if(ig.hasEdge(v1,possible)){
                        v1Interfere = true;
                     }
                  }



                  if(!ig.hasEdge(v,v1) && (v.getIndex() == v1.getIndex()) && (!hasFakeEdge.contains(v1)) && !v1Interfere){
                     foundFirst = true;
                     w1 = ((Integer)markedIndices.get(v1)).intValue();
                  }
                  j = (j+1) % origLength;
               }//end while

               while(!foundSecond && j!=i){
                  v2 = (sandmark.analysis.defuse.DUWeb)origColoring.get(j);

                  //check if it interferes with any nodes already in the tripleList
                  boolean v2Interfere = false;
                  for(int k = 0; k < tripleList.size(); k++){
                     sandmark.analysis.defuse.DUWeb possible = 
			 (sandmark.analysis.defuse.DUWeb)tripleList.get(k);
                     if(ig.hasEdge(v2,possible)){
                        v2Interfere = true;
                     }
                  }



                  if(!ig.hasEdge(v,v2) && (v.getIndex() == v2.getIndex()) && (!hasFakeEdge.contains(v2)) && !v2Interfere){
                     foundSecond = true;
                     w2 = ((Integer)markedIndices.get(v2)).intValue();
                  }
                  j = (j+1) % origLength;
               }//end while

               if(foundFirst && foundSecond){
                  //System.out.println("found a triple: " + v + "\n" + v1 + "\n" + v2);
                  //System.out.println("with corresponding nodes " + w + " " + w1 + " " + w2);
                  tripleList.add(v);
                  tripleList.add(v1);
                  tripleList.add(v2);
                  
                  if(w != w1){
                     wm += "0";
		     ig.addEdge(v,v1);
		     ig.addEdge(v1,v);
		     hasFakeEdge.add(v);
		     hasFakeEdge.add(v1);
                  }else{
                     wm += "1";
		     ig.addEdge(v,v2);
		     ig.addEdge(v2,v);
		     hasFakeEdge.add(v);
		     hasFakeEdge.add(v2);
                  }
               }//end if
            }//end if
         }//end for
         if(DEBUG && wm != null && wm.length() != 0)
            System.out.println("found: " + wm + " in " + methodName);
         return wm;
      }

     
      public boolean hasNext() {
         return current < result.size();
      }

      public java.lang.Object next() {
         return result.get(current++);
      }

      public void remove() {}
   }


/* Return an iterator which generates the watermarks
 * found in the program. The props argument
 * holds at least the following properties:
 *  <UL>
 *     <LI> Input File: The name of the file to be watermarked.
 *  </UL>
 */
   public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
     throws sandmark.watermark.WatermarkingException {
         return new Recognizer(params);
   }


} // class AssignLV

