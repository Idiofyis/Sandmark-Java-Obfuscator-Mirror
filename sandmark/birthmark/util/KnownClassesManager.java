package sandmark.birthmark.util;

public class KnownClassesManager{

   public static final String[] KNOWNCLASSES = new String[]{
      "java", "javax", "com.netscape", "com.apple", 
      "org.apache", "org.omg", "org.xml", "org.w3c", 
      "org.ietf", "junit", "org.gnu", "pnuts", 
      "com.sun", "org.eclipse", "sun", 
   };

   public static boolean isKnownClass(String className){
      if(className == null)
         return false;

      className = className.replace('/', '.');

      for(int i=0; i < KNOWNCLASSES.length; i++){
         if(className.startsWith(KNOWNCLASSES[i])) //Package prefix
            return true;
         else if(className.endsWith(KNOWNCLASSES[i])) //Package suffix
            return true;
         else if(className.equals(KNOWNCLASSES[i])) //Class name
            return true;
      }
      return false;
   }
}
