package sandmark.watermark.gtw;

class GTWRecognizer implements java.util.Iterator {

   class MarkedMethod {
      String name;
      java.math.BigInteger value;
      int index;
      java.math.BigInteger marks[];
      MarkedMethod(String nm,java.math.BigInteger m,int n,java.math.BigInteger mks[]) {
         name = nm;
         value = m;
         index = n;
         marks = mks;
      }
   }

   public static boolean DEBUG = false;
   private MarkedMethod mMarkedMethods[];
   private boolean mHasNext;
   private boolean mDumpDot;
   private boolean useCRT;
   private sandmark.util.ConfigProperties configProperties;

   public GTWRecognizer(sandmark.program.Application app, 
                        sandmark.util.ConfigProperties props,
                        java.math.BigInteger key) 
      throws java.io.IOException {
      configProperties = props;

      useCRT = props.getProperty("Use CRT Splitter").equals("true");

      boolean dumpDot = props.getProperty("Dump Dot Graphs").equals("true");

      mDumpDot = dumpDot;

      java.util.ArrayList markedMethods = new java.util.ArrayList();
      for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
         sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();

         for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
            sandmark.program.Method method = (sandmark.program.Method)methodIt.next();

            if(method.getInstructionList() == null)
               continue;

            double markRatio=0.0;

            if (!useCRT){
               int markCounts[] = new int[2];
               
               sandmark.watermark.util.BasicBlockMarker bm =
                  new sandmark.watermark.util.MD5Marker(clazz,2,key);
               for(java.util.Iterator blockIt = 
                      method.getCFG(false).basicBlockIterator() ; 
                   blockIt.hasNext() ; ) {
                  sandmark.analysis.controlflowgraph.BasicBlock bb =
                     (sandmark.analysis.controlflowgraph.BasicBlock)blockIt.next();
                  for(java.util.Iterator markIt = bm.recognize(bb) ;
                      markIt.hasNext() ; ) {
                     java.math.BigInteger mark = (java.math.BigInteger)markIt.next();
                     if(mark.equals(java.math.BigInteger.ONE))
                        markCounts[1]++;
                     else
                        markCounts[0]++;
                  }
               }
               
               markRatio = markCounts[1] / 1.0 / (markCounts[0] + markCounts[1]);
               
               if(markRatio <= .4)
                  continue;
            }

            try {
               sandmark.analysis.controlflowgraph.MethodCFG cfg = 
                  method.getCFG(false);
               sandmark.util.newgraph.Graph gr = 
                  cfg.graph().removeUnreachable(cfg.source()).removeNode
                  (cfg.source()).removeNode(cfg.sink());
               if(mDumpDot)
                  sandmark.util.newgraph.Graphs.dotInFile
                     (gr,"graphs/rec.spg." + method.getName() + ".dot");
               sandmark.util.graph.graphview.GraphList.instance().add(gr, "rec.spg." + method.getName());
               sandmark.util.newgraph.codec.GraphCodec gc =
                  new sandmark.util.newgraph.codec.ReduciblePermutationGraph();
               java.math.BigInteger value = gc.decode(gr);
               if(DEBUG)
                  System.out.println("part value for " + method.getName() + ": " + value);            
                   
               java.math.BigInteger marks[] = null;

               if (useCRT){
                  marks = new java.math.BigInteger[]{
                     java.math.BigInteger.ONE
                  };
               }else{
                  marks = (.4 < markRatio && markRatio < .6) ?
                     new java.math.BigInteger[] {
                        java.math.BigInteger.ONE,
                        java.math.BigInteger.ZERO,
                     } : new java.math.BigInteger[] {
                        java.math.BigInteger.ONE,
                     };
               }

               if (DEBUG)
                  System.out.println(method + " has " + marks.length + " possibilities");
                   
               markedMethods.add
                  (new MarkedMethod(method.toString(),value,0,marks));
            } catch(sandmark.util.newgraph.codec.DecodeFailure e) {
               if (DEBUG)
                  System.out.println(method + " is not decodable");
            }
         }
      }

      mHasNext = markedMethods.size() != 0;

      if(mHasNext)
         mMarkedMethods = 
            (MarkedMethod [])markedMethods.toArray(new MarkedMethod[0]);
   }
   public boolean hasNext() {
      if(DEBUG)
         System.out.println("hasNext returning " + mHasNext);
      return mHasNext;
   }
   private boolean isLastMark() {
      for(int i = 0 ; i < mMarkedMethods.length ; i++)
         if(mMarkedMethods[i].index + 1 != mMarkedMethods[i].marks.length)
            return false;
      return true;
   }
   public Object next() {
      mHasNext = !isLastMark();
      java.util.ArrayList wmParts = new java.util.ArrayList();
      for(int i = 0 ; i < mMarkedMethods.length ; i++) {
         if(mMarkedMethods[i].marks[mMarkedMethods[i].index].equals
            (java.math.BigInteger.ONE)) {
            if(DEBUG) {
               //System.out.println(cfg.toDot());
               System.out.println(mMarkedMethods[i].name + " PART OF wm");
            }
            wmParts.add(mMarkedMethods[i].value);
         }
      }
      if(mHasNext)
         setNextPermutation();

      java.math.BigInteger mark;


      if (useCRT){
         try{
            javax.crypto.SecretKey w = (javax.crypto.SecretKey)
               new java.io.ObjectInputStream
               (new java.io.FileInputStream
                (configProperties.getProperty("Key File"))).readObject();
            mark = 
               new sandmark.util.splitint.SlowCRTSplitter(128,50,w).combine
               ((java.math.BigInteger[])wmParts.toArray
                (new java.math.BigInteger[0]));
         }catch(Exception ex){
            mark = java.math.BigInteger.ZERO;
         }
      }else{
         try {
            mark = (new sandmark.util.splitint.PartialSumSplitter()).combine
               ((java.math.BigInteger[])wmParts.toArray
                (new java.math.BigInteger[] {}));
         } catch(IllegalArgumentException e) {
            mark = java.math.BigInteger.ZERO;
         }
      }

      if(DEBUG)
         System.out.println("returning mark " + mark);
      return mark.toString();
   }
   private void setNextPermutation() {
      int i;
      boolean cont;
      for(i = 0,cont = true ; cont && i < mMarkedMethods.length ; i++) {
         mMarkedMethods[i].index = (mMarkedMethods[i].index + 1) % 
            mMarkedMethods[i].marks.length;
         if(mMarkedMethods[i].index != 0)
            cont = false;
      }
   }
   public void remove() {}
}

