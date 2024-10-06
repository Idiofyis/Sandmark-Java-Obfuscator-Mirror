package sandmark.birthmark.fieldvalues;

public class CVFV extends sandmark.birthmark.StaticClassBirthmark{

   public static boolean DEBUG = false;

   public String getShortName(){
      return "CVFV";
   }

   public String getLongName(){
      return "Determines if two applications are similar using constant values in field variables";
   }

   public String getAlgHTML(){
      return "<HTML><BODY>" +
             "Constant Values in Field Variables birthmark" +
             "</BODY></HTML>";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return "Computes a birthmark based on the constant values in field" +
             " variables techniques in Design and Evaluation of Birthmarks" +
             " for Detecting Theft of Java Programs.";
   }

   public String getAlgURL(){
      return "sandmark/birthmark/fieldvalues/doc/help.html";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {};
      return properties;
   }

   public double calculate
      (sandmark.birthmark.StaticClassBirthMarkParameters params) 
      throws Exception{

      java.util.Iterator origFields = params.original.fields();
      java.util.Iterator suspectFields = params.suspect.fields();

      java.util.ArrayList origBirthmarks = new java.util.ArrayList();
      java.util.ArrayList suspectBirthmarks = new java.util.ArrayList();

      while(origFields.hasNext()){
         sandmark.program.Field of = (sandmark.program.Field)origFields.next();
         String init = of.getInitValue() == null ? "null" : of.getInitValue();
         CVFVPair pair = new CVFVPair(init, of.getSignature());
         origBirthmarks.add(pair);
      }

      while(suspectFields.hasNext()){
         sandmark.program.Field sf =
            (sandmark.program.Field)suspectFields.next();
         String init = sf.getInitValue() == null ? "null" : sf.getInitValue();
         CVFVPair pair = new CVFVPair(init, sf.getSignature());
         suspectBirthmarks.add(pair);
      }

      double maxLength = origBirthmarks.size() >= suspectBirthmarks.size() ?
         origBirthmarks.size() : suspectBirthmarks.size();
      double matchedPairs = 0;
      for(int i = 0; i < origBirthmarks.size(); i++){
         CVFVPair birthmark = (CVFVPair)origBirthmarks.get(i);
         if(suspectBirthmarks.contains(birthmark)){
            matchedPairs++;
            suspectBirthmarks.remove(birthmark);
         }
      }
      
      if(DEBUG){ 
         System.out.println("matchedPairs: " + matchedPairs);
         System.out.println("maxLength: " + maxLength);
      }
      double similarity = (matchedPairs / maxLength) * 100;

      //System.out.println("similarity: " + similarity);
      return similarity;
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
         CVFV cvfv = new CVFV();
         //System.out.println("similarity: " + cvfv.calculate(cls1, cls2));
      }catch(Exception e){
         e.printStackTrace();
         System.out.println("couldn't create app object");
      }
   }
}
