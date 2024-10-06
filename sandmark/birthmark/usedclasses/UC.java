package sandmark.birthmark.usedclasses;

public class UC extends sandmark.birthmark.StaticClassBirthmark{

   public static final boolean DEBUG = false;
   public UC(){}

   public String getShortName(){
      return "UC";
   }

   public String getLongName(){
      return "Determines if two applications are similar using an alphabetized"
             + " list of used well-known classes";
   }

   public String getAlgHTML(){
      return "<HTML><BODY>" +
             "Used Classes birthmark" +
             "</BODY></HTML>";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return "Computes a birthmark based on the used classes" +
             " techniques in Design and Evaluation of Birthmarks" +
             " for Detecting Theft of Java Programs.";
   }

   public String getAlgURL(){
      return "sandmark/birthmark/usedclasses/doc/help.html";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {};
      return properties;
   }

   public double calculate
      (sandmark.birthmark.StaticClassBirthMarkParameters params) 
      throws Exception{

      if(DEBUG)System.out.println("original");
      java.util.TreeSet origBirthmarks = getBirthmarks(params.original);
      if(DEBUG)System.out.println("suspect");
      java.util.TreeSet suspectBirthmarks = getBirthmarks(params.suspect);

      double maxLength = origBirthmarks.size() >= suspectBirthmarks.size() ?
         origBirthmarks.size() : suspectBirthmarks.size();
      double matchedPairs = 0;
      java.util.Iterator origIter = origBirthmarks.iterator();
      java.util.Iterator suspectIter = suspectBirthmarks.iterator();
      while(origIter.hasNext() && suspectIter.hasNext()){
         String origMark = (String)origIter.next();
         //System.out.println("orig: " + origMark);
         String suspectMark = (String)suspectIter.next();
         //System.out.println("suspect: " + suspectMark);
         if(origMark.equals(suspectMark))
            matchedPairs++;
      }
      
      if(DEBUG){
         System.out.println("mactchedPairs: " + matchedPairs);
         System.out.println("maxLength: " + maxLength);
      }
      double similarity = (matchedPairs / maxLength) * 100;          
      
      return similarity;

   }

   private java.util.TreeSet getBirthmarks(sandmark.program.Class cls){
      java.util.TreeSet birthmarks = new java.util.TreeSet();

      getCPEntries(birthmarks, cls);
      getMethodSigEntries(birthmarks, cls);
      getFieldEntries(birthmarks, cls);

      return birthmarks;
   }

   private void getCPEntries(java.util.TreeSet birthmarks,
                             sandmark.program.Class cls){
      org.apache.bcel.generic.ConstantPoolGen cpg = cls.getConstantPool();
      org.apache.bcel.classfile.ConstantPool cp = cpg.getConstantPool();
      org.apache.bcel.classfile.Constant[] constants = cp.getConstantPool();

      for(int i=0; i < constants.length; i++){
         if(constants[i] != null && constants[i].getTag() == 
            org.apache.bcel.Constants.CONSTANT_Class){
            String className =
               ((org.apache.bcel.classfile.ConstantClass)constants[i]).getBytes(cp);
            if(sandmark.birthmark.util.KnownClassesManager.isKnownClass(className)){
               className = className.replace('/', '.');
               birthmarks.add(className);
            }
         }
      }
      
   }

   private void getMethodSigEntries(java.util.TreeSet birthmarks,
                                    sandmark.program.Class cls){
      sandmark.program.Method[] methods = cls.getMethods();
      for(int i=0; i < methods.length; i++){
         sandmark.program.Method m = methods[i];
         //method return type
         org.apache.bcel.generic.Type returnType = m.getReturnType();
         String className = getClassName(returnType);
         if(sandmark.birthmark.util.KnownClassesManager.isKnownClass(className)){
            className = className.replace('/', '.');
            birthmarks.add(className);
         }

         //method argument types
         org.apache.bcel.generic.Type[] argTypes = m.getArgumentTypes();
         for(int j=0; j < argTypes.length; j++){
            className = getClassName(argTypes[j]);
            if(sandmark.birthmark.util.KnownClassesManager.isKnownClass(className)){
               className = className.replace('/', '.');
               birthmarks.add(className);
            }
         }
      }
   }

   private void getFieldEntries(java.util.TreeSet birthmarks,
                                sandmark.program.Class cls){
      sandmark.program.Field[] fields = cls.getFields();
      for(int i=0; i < fields.length; i++){
         org.apache.bcel.generic.Type type = fields[i].getType();
         String className = getClassName(type);
         if(sandmark.birthmark.util.KnownClassesManager.isKnownClass(className)){
            className = className.replace('/', '.');
            birthmarks.add(className);
         }
      }
   }

   private String getClassName(org.apache.bcel.generic.Type type){
      if(type instanceof org.apache.bcel.generic.ObjectType){
         return ((org.apache.bcel.generic.ObjectType)type).getClassName();
      }
      else if(type instanceof org.apache.bcel.generic.ArrayType){
         return getClassName(((org.apache.bcel.generic.ArrayType)type).getBasicType());
      }

      return null;
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
         UC uc = new UC();
         //System.out.println("similarity: " + uc.calculate(cls1, cls2));
      }catch(Exception e){
         e.printStackTrace();
         System.out.println("couldn't create app object");
      }
   }

}
