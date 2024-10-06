package sandmark.birthmark.methodcalls;

public class SMC extends sandmark.birthmark.StaticClassBirthmark{

   public static final boolean DEBUG = false;
   public SMC(){}

   public String getShortName(){
      return "SMC";
   }

   public String getLongName(){
      return "Determines if two applications are similar using the sequence of"
             + " method calls from well-known classes";
   }

   public String getAlgHTML(){
      return "<HTML><BODY>" +
             "Sequence of Method Calls birthmark" +
             "</BODY></HTML>";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return "Computes a birthmark based on the sequence of method calls" +
             " techniques in Design and Evaluation of Birthmarks" +
             " for Detecting Theft of Java Programs.";
   }

   public String getAlgURL(){
      return "sandmark/birthmark/methodcalls/doc/help.html";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {};
      return properties;
   }

   public double calculate
      (sandmark.birthmark.StaticClassBirthMarkParameters params) 
      throws Exception{

      sandmark.program.Method[] origMethods = params.original.getMethods();
      java.util.Arrays.sort(origMethods, new MethodComparator());
      sandmark.program.Method[] suspectMethods = params.suspect.getMethods();
      java.util.Arrays.sort(suspectMethods, new MethodComparator());

      if(DEBUG)System.out.println("original");
      java.util.ArrayList origBirthmarks = findValidMethodCalls(origMethods);
      if(DEBUG)System.out.println("suspect");
      java.util.ArrayList suspectBirthmarks = findValidMethodCalls(suspectMethods);

      double maxLength = origBirthmarks.size() >= suspectBirthmarks.size() ?
         origBirthmarks.size() : suspectBirthmarks.size();
      int minLength = origBirthmarks.size() <= suspectBirthmarks.size() ?
         origBirthmarks.size() : suspectBirthmarks.size();

      double matchedPairs = 0;
      for(int i = 0; i < minLength; i++){
      //for(int i = 0; i < origBirthmarks.size(); i++){
         SMCTriple birthmark = (SMCTriple)origBirthmarks.get(i);
         if(((SMCTriple)suspectBirthmarks.get(i)).equals(birthmark)){
            matchedPairs++;
         }
      }
      
      if(DEBUG){
         System.out.println("matchedPairs: " +  matchedPairs);
         System.out.println("maxLength: " + maxLength);
      }
      double similarity = (matchedPairs / maxLength) * 100;          
      
      return similarity;

   }

   private java.util.ArrayList findValidMethodCalls(
      sandmark.program.Method[] methods){

      java.util.ArrayList birthmarks = new java.util.ArrayList();

      for(int i = 0; i < methods.length; i++){
         sandmark.program.Method m = methods[i];
         org.apache.bcel.generic.ConstantPoolGen cpg = m.getCPG();
         org.apache.bcel.generic.InstructionList il = m.getInstructionList();
         if(il == null)
            break;
         org.apache.bcel.generic.Instruction[] insts = il.getInstructions();
         for(int j = 0; j < insts.length; j++){
            org.apache.bcel.generic.Instruction inst = insts[j];
            if(inst instanceof org.apache.bcel.generic.InvokeInstruction){
               org.apache.bcel.generic.InvokeInstruction ii = 
                  (org.apache.bcel.generic.InvokeInstruction)inst;
               String methodName = ii.getMethodName(cpg);
               String className = ii.getClassName(cpg);
               String signature = ii.getSignature(cpg);
               //if the method call is in our set of well-known classes
               //then we want to add it to the birthmarks
               if(sandmark.birthmark.util.KnownClassesManager.isKnownClass(className)){
                  SMCTriple t = new SMCTriple(methodName, className, signature);
                  if(DEBUG)System.out.println("found birthmark: " + className);
                  birthmarks.add(t);
               }
            }
         }
      }

      return birthmarks;
   }

   public static void main(String[] argv){

      String original = argv[0];
      String origClass = argv[1];
      String suspect = argv[2];
      String suspectClass = argv[3];

      sandmark.program.Application app1;
      sandmark.program.Application app2;

      try{
         app1 = new sandmark.program.Application(original);
         app2 = new sandmark.program.Application(suspect);
         sandmark.program.Class cls1 = app1.getClass(origClass);
         sandmark.program.Class cls2 = app2.getClass(suspectClass);
         SMC smc = new SMC();
         //System.out.println("similarity: " + smc.calculate(cls1, cls2));
      }catch(Exception e){
         e.printStackTrace();
         System.out.println("couldn't create app object");
      }
   }

}
