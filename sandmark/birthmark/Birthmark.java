package sandmark.birthmark;


public class Birthmark{
   java.util.Properties props;
   sandmark.program.Application app1;
   sandmark.program.Application app2;
   java.util.Hashtable ht = null;

   private static String[] allBirthmarkNames;
   private static java.util.Hashtable shortNameToClassName =
      new java.util.Hashtable();

   public static sandmark.birthmark.DynamicBirthmark getDynamicBirthmarkByName(
      String name){
      return (sandmark.birthmark.DynamicBirthmark)getBirthmarkInstance(name);
   }

   public static sandmark.birthmark.DynamicBirthmark getDynamicBirthmarkByShortName(
      String name){
      return (sandmark.birthmark.DynamicBirthmark)getBirthmarkInstance(
         (String)shortNameToClassName.get(name));
   }

   private static java.util.Hashtable classNameToInstance =
      new java.util.Hashtable();
   private static Object getBirthmarkInstance(String className){
      Object o;
      if((o = classNameToInstance.get(className)) != null)
         return o;
      try{
         Class c = Class.forName(className);
         o = c.newInstance();
         classNameToInstance.put(className,o);
      }catch(Throwable t) {
         t.printStackTrace();
         return null;
      }
      return o;
   }

   public static String[] getAllBirthmarkNames(){
      return allBirthmarkNames;
   }

   static{
      allBirthmarkNames =
        (String[])sandmark.util.classloading.ClassFinder.getClassesWithAncestor(
        sandmark.util.classloading.IClassFinder.DYN_BIRTHMARK).toArray(
        new String[] {});
   }

   public void calculate(){

   }

   private static sandmark.util.ConfigProperties sConfigProps;
   public static sandmark.util.ConfigProperties getProperties(){
      return sandmark.Console.getConfigProperties();
   }

   /**
     *  Get the HTML codes of the About page for Birthmark
        @return HTML code for the about page
     */
   public static java.lang.String getAboutHTML(){
      return
            "<HTML><BODY>" +
            "<CENTER><B>List of Birthmarks</B></CENTER>" +
            "</BODY></HTML>";
   }

    /**
     *  Get the URL of the Help page for Obfuscate
        @return url for the help page
     */
   public static java.lang.String getHelpURL(){
      return "sandmark/birthmark/doc/birthmark.html";
   }

   /*
     *  Describe what obfuscation is.
     */
   public static java.lang.String getOverview(){
	   return "";
   }

/*
   public static void runBirthmark(sandmark.program.Application app1,
                                   sandmark.program.Application app2,
                                   java.util.Properties props,
                                   sandmark.Algorithm alg) throws Exception{
      try{
         if(alg instanceof sandmark.birthmark.DynamicBirthmark){
            ((sandmark.birthmark.DynamicBirthmark)alg).calculate(app1, app2,
props);
         }else if(alg instanceof sandmark.birthmark.StaticClassBirthmark){
            ((sandmark.birthmark.StaticClassBirthmark)alg).calculate(app1, app2,
props);
         }
         sandmark.util.Log.message(0, "Done calculating birthmark percentage");
      }catch(BirthmarkException e){
         e.printStackTrace();
         sandmark.util.Log.message(0, "Birthmark calculation failed");
      }
   }
*/
}
