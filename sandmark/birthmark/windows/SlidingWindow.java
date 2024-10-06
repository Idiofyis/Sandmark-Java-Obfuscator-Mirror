package sandmark.birthmark.windows;

public class SlidingWindow extends sandmark.birthmark.StaticClassBirthmark{

   public static boolean DEBUG = false;

   public SlidingWindow(){}

   public String getShortName(){
      return "Sliding Window";
   }

   public String getLongName(){
      return "Determines if two applications are similar using a sliding window" +
             " to find sequences of opcodes.";
   }

   public String getAlgHTML(){
      return "<HTML><BODY>" +
             "Sliding Window birthmark" +
             "</BODY></HTML>";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return "Computes a birthmark based on a sliding window over the class";
   }

   public String getAlgURL(){
      return "sandmark/birthmark/windows/doc/help.html";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {};
      return properties;
   }

   private sandmark.util.ConfigProperties mConfigProps;
   public sandmark.util.ConfigProperties getConfigProperties(){
      if(mConfigProps == null){
         String props[][] = new String[][]{
            {"Window Size",
             "5", "Size of the sliding window.",
             "5", "S", "SB",
            },
         };
         mConfigProps = new sandmark.util.ConfigProperties(props, null);
      }
      return mConfigProps;
   }

   public double calculate
      (sandmark.birthmark.StaticClassBirthMarkParameters params) 
      throws Exception{

      Integer iw = new Integer(getConfigProperties().getProperty("Window Size"));
      java.util.TreeSet origBirthmarks = computeBirthmark(params.original,
            iw.intValue());
      java.util.TreeSet suspectBirthmarks = computeBirthmark(params.suspect,
            iw.intValue());

      
      double length = origBirthmarks.size();
      int otherlength = suspectBirthmarks.size();
      double matchedPairs = 0;

      int size = origBirthmarks.size() <= suspectBirthmarks.size() ?
         origBirthmarks.size() : suspectBirthmarks.size();

      java.util.Iterator obms = origBirthmarks.iterator();
      for(int i = 0; i < size; i++){
         String o = (String)obms.next();
         if(suspectBirthmarks.contains(o)){
            suspectBirthmarks.remove(o);
            matchedPairs++;
         }
      }
            
      if(DEBUG){ 
         System.out.println("matchedPairs: " + matchedPairs);
         System.out.println("orig length: " + length);
         System.out.println("suspect length: " + otherlength);
      }
      double similarity = 0;
      if(length > 0)
         similarity = (matchedPairs / length) * 100;

      return similarity;
   }

   private java.util.TreeSet computeBirthmark(sandmark.program.Class cls,
                                              int windowSize){

      java.util.TreeSet bms = new java.util.TreeSet();

      WindowState ws = new WindowState(windowSize, null);

      sandmark.program.Method[] methods = cls.getMethods();
      for(int i = 0; i < methods.length; i++){
         sandmark.program.Method m = methods[i];
         if(m.getInstructionList() == null)
            continue;
         m.removeNOPs();
         sandmark.analysis.stacksimulator.StackSimulator ss =
            m.getStack();
         for(org.apache.bcel.generic.InstructionHandle ih =
            m.getInstructionList().getStart(); ih != null ; ih =
            ih.getNext()){
            ws.collect(ih, ss.getInstructionContext(ih).getStackSize() == 0);
         }
         ws.clear();
      }
      
      int size = windowSize-1;
      java.util.Iterator windowsIter = ws.getWindows();
      while(windowsIter.hasNext()){
         String w = (windowsIter.next()).toString();
         if(DEBUG)System.out.println(w);
         if(w.matches("(\\d*\\s){" + size + "}\\d*"))
            bms.add(w);
      }

      return bms;
   }

   public static void main(String[] argv){

      String original = argv[0];
      String origClass = argv[1];
      String suspect = argv[2];
      String suspectClass = argv[3];
      String windowSize = argv[4];

      sandmark.program.Application app1;
      sandmark.program.Application app2;

      try{
         app1 = new sandmark.program.Application(original);
         app2 = new sandmark.program.Application(suspect);
         sandmark.program.Class cls1 = app1.getClass(origClass);
         sandmark.program.Class cls2 = app2.getClass(suspectClass);
         SlidingWindow sw = new SlidingWindow();
         //System.out.println("similarity: " + sw.calculate(cls1, cls2));
      }catch(Exception e){
         e.printStackTrace();
         System.out.println("couldn't create app object");
      }
   }
}
