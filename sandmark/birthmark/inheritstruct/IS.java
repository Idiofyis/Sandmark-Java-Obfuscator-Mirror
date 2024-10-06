package sandmark.birthmark.inheritstruct;

public class IS extends sandmark.birthmark.StaticClassBirthmark{

   public static final boolean DEBUG = false;
   public IS(){}

   public String getShortName(){
      return "IS";
   }

   public String getLongName(){
      return "Determines if two applications are similar using the inheritance"
             + " structure consisting of only well-known classes";
   }

   public String getAlgHTML(){
      return "<HTML><BODY>" +
             "Inheritance Structure birthmark" +
             "</BODY></HTML>";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return "Computes a birthmark based on the inheritance structure" +
             " technique in Design and Evaluation of Birthmarks" +
             " for Detecting Theft of Java Programs.";
   }

   public String getAlgURL(){
      return "sandmark/birthmark/inheritstruct/doc/help.html";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {};
      return properties;
   }

   public double calculate
      (sandmark.birthmark.StaticClassBirthMarkParameters params) 
      throws Exception{

      if(DEBUG)System.out.println("original");
      java.util.ArrayList origBirthmarks = getBirthmarks(params.original);
      if(DEBUG)System.out.println("suspect");
      java.util.ArrayList suspectBirthmarks = getBirthmarks(params.suspect);

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
         System.out.println("matchedPairs: " + matchedPairs);
         System.out.println("maxLength: " + maxLength);
      }
      double similarity = (matchedPairs / maxLength) * 100;          
      
      return similarity;

   }

   private java.util.ArrayList getBirthmarks(sandmark.program.Class cls){
      java.util.ArrayList birthmarks = new java.util.ArrayList();

      sandmark.program.Class[] superClasses = cls.getSuperClasses();
      for(int i=0; i < superClasses.length; i++){
         String className = superClasses[i].getName();
         if(sandmark.birthmark.util.KnownClassesManager.isKnownClass(className))
            birthmarks.add(className);
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
         IS is = new IS();
         //System.out.println("similarity: " + is.calculate(cls1, cls2));
      }catch(Exception e){
         e.printStackTrace();
         System.out.println("couldn't create app object");
      }
   }

}
