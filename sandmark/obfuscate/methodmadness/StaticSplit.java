package sandmark.obfuscate.methodmadness;

/*
 *  Splits each nonstatic method of the class into a nonstatic method
 *  with the same name and signature as before, and a static method
 *  that contains the method body of the original method.
 *
 *  <p>
 *  Used by Method2RMadness obfuscator.
 *
 *  @author Kelly Heffner (<a href="mailto:kheffner@cs.arizona.edu">kheffner@cs.arizona.edu</a>)
 *  @version 1.0, May 8, 2002
 */
public class StaticSplit extends sandmark.obfuscate.ClassObfuscator
{

   public void apply(sandmark.program.Class cls) throws java.io.IOException
   {
      new sandmark.util.StaticSplit().apply(cls);
   }


   /*  Returns the URL at which you can find information about this obfuscator. */
   public java.lang.String getAlgURL()
   {
      return "sandmark/obfuscate/methodmadness/doc/helpstaticsplit.html";
   }


   /*  Returns an HTML description of this obfuscator. */
   public java.lang.String getAlgHTML() 
   {
      return
         "<HTML><BODY>" +
         "Static Method Bodies splits all of the nonstatic methods " +
         "into a static helper method and a nonstatic stub that calls it.\n" +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:stepp\">Martin Stepp</a> and " +
         "<a href = \"mailto:kheffner@cs.arizona.edu\">Kelly Heffner</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }


   public java.lang.String getAuthor()
   {
      return "Kelly Heffner";
   }

   public java.lang.String getAuthorEmail()
   {
      return "kheffner@cs.arizona.edu";
   }

   public java.lang.String getDescription()
   {
      return "Static Method Bodies splits all of the nonstatic methods " +
         "into a static helper method and a nonstatic stub that calls it.";
   }

   public sandmark.config.ModificationProperty [] getMutations()
   {
      return new sandmark.config.ModificationProperty[] {
         sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE,
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
         sandmark.config.ModificationProperty.I_ADD_METHODS
      };
   }

   public sandmark.config.RequisiteProperty [] getPostsuggestions()
   {
      return new sandmark.config.RequisiteProperty[] {
         sandmark.config.ModificationProperty.I_CHANGE_METHOD_SIGNATURES,
         sandmark.config.ModificationProperty.I_CHANGE_METHOD_NAMES
      };
   }

   public sandmark.config.RequisiteProperty [] getPostprohibited()
   {
      return new sandmark.config.RequisiteProperty[] {
         new sandmark.config.AlgorithmProperty(this)
      };
   }

   /*  Returns a long description of this obfuscator's name. */
   public java.lang.String getLongName()
   {
      return "Nonstatic methods to static methods and nonstatic stubs";
   }

   /*  Returns a short description of this obfuscator's name. */
   public java.lang.String getShortName()
   {
      return "Static Method Bodies";
   }
}
