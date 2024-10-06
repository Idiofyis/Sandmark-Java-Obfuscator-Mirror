package sandmark.obfuscate.blockmarker;

public class BlockMarker extends sandmark.obfuscate.AppObfuscator {
   private final static double MARK_PROBABILITY = .001;
   public String getAuthor(){
      return "Edward Carter";
   }

   public String getAuthorEmail(){
      return "ecarter@cs.arizona.edu";
   }

   public String getDescription(){
      return "Use a BasicBlockMarker to mark basic blocks randomly. " +
         "This is a useful against some watermarking algorithms.";
   }

   public sandmark.config.ModificationProperty [] getMutations() {
      return null;
   }


   public String getShortName() {
      return "Block Marker";
   }

   public String getLongName() {
      return "Basic Block Marker Obfuscation";
   }

   public java.lang.String getAlgHTML(){
      return
         "<HTML><BODY>" +
         "The BlockMarker obfuscation randomly marks all basic blocks in " +
         "the program with either 0 or 1.\n" +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:ecarter@cs.arizona.edu\">Edward Carter</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/blockmarker/doc/help.html";
   }

   private java.util.Random r;
   private int markCount;

   public BlockMarker() {
       r = sandmark.util.Random.getRandom(); //new java.util.Random();
      markCount = 0;
   }

   private java.math.BigInteger nextValue() {
      if (r.nextBoolean())
         return java.math.BigInteger.ONE;
      else
         return java.math.BigInteger.ZERO;
   }

   private void markMethod(sandmark.program.LocalMethod method,
                           sandmark.watermark.util.BasicBlockMarker marker,
                           boolean isLastMethod) {
      org.apache.bcel.generic.CodeExceptionGen ehs[] =
         method.getExceptionHandlers();
      org.apache.bcel.generic.InstructionHandle startPCs[] =
         new org.apache.bcel.generic.InstructionHandle[ehs.length];
      org.apache.bcel.generic.InstructionHandle endPCs[] =
         new org.apache.bcel.generic.InstructionHandle[ehs.length];
      org.apache.bcel.generic.InstructionHandle handlerPCs[] =
         new org.apache.bcel.generic.InstructionHandle[ehs.length];
      for (int j = 0; j < ehs.length; j++) {
         startPCs[j] = ehs[j].getStartPC();
         endPCs[j] = ehs[j].getEndPC();
         handlerPCs[j] = ehs[j].getHandlerPC();
      }

      java.util.Iterator i = method.getCFG(false).basicBlockIterator();
      while (i.hasNext()) {
         sandmark.analysis.controlflowgraph.BasicBlock b =
            (sandmark.analysis.controlflowgraph.BasicBlock)i.next();
         if(r.nextDouble() < MARK_PROBABILITY
            || (isLastMethod && !i.hasNext() && markCount == 0)) {
            marker.embed(b, nextValue());
            markCount++;
         }
      }

      method.getInstructionList().setPositions();

      for (int j = 0; j < ehs.length; j++) {
         ehs[j].setStartPC(startPCs[j]);
         ehs[j].setEndPC(endPCs[j]);
         ehs[j].setHandlerPC(handlerPCs[j]);
      }

    }

    private void markClass(sandmark.program.Class clazz,
                           sandmark.watermark.util.BasicBlockMarker marker,
                           boolean isLastClass) {
       java.util.Iterator methodIt = clazz.methods();
       while (methodIt.hasNext()) {
          java.lang.Object method = methodIt.next();
          try {
             if (method instanceof sandmark.program.LocalMethod)
                markMethod((sandmark.program.LocalMethod)method,
                           marker, isLastClass && !methodIt.hasNext());
          }
          catch (sandmark.analysis.controlflowgraph.EmptyMethodException e) {
             // we just don't mark empty methods
          }
       }
    }


   /**
    * <STRONG> Not yet implemented;
    * needs to be written and documented. </STRONG>
    */
   public void apply(sandmark.program.Application app) throws Exception {
      sandmark.program.Class markerClass = app.getMain();

      for(java.util.Iterator classIt = app.classes() ;
          markerClass == null && classIt.hasNext() ; ) {
         sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
         if(!clazz.isInterface())
            markerClass = clazz;
      }
      sandmark.watermark.util.BasicBlockMarker marker =
         new sandmark.watermark.util.MD5Marker(markerClass);

      for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
         sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
         if(clazz != markerClass)
            markClass(clazz, marker, false);
      }
      markClass(markerClass, marker, true);

      //System.out.println("marked " + markCount + " blocks");
   }

}

