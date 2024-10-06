package sandmark.obfuscate.methodmadness;


public class ParamReorder extends sandmark.obfuscate.MethodObfuscator
{
   /** Returns the URL at which you can find information about this obfuscator
    * */
   public java.lang.String getAlgURL()
   {
      return "sandmark/obfuscate/methodmadness/doc/helpparameterreorderer.html";
   }


   /** Returns an HTML description of this obfuscator */
   public java.lang.String getAlgHTML()
   {
      return  "<HTML> <BODY>" +
         "ParamReorder reorders the order of the arguments in a method. " +
         "If the method is implemented by sub/superclasses, those implementations " +
         "are also reordered" +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href=\"mailto:srini@cs.arizona.edu\">Srinivas Visvanathan</a>"+
         "</TR></TD>" +
         "</TABLE>" +
         "</BODY> </HTML>";
   }

   /** Returns a long description of this obfuscator */
   public java.lang.String getLongName()
   {
      return "Reorders the arguments of a method. If this method is implemented by super/subclasses," +
         "those implementations are also reordered";
   }

   /** Returns a short description of this obfuscator */
   public java.lang.String getShortName()
   {
      return "Reorder Parameters";
   }

   public java.lang.String getAuthor()
   {
      return "Srinivas Visvanathan";
   }

   public java.lang.String getAuthorEmail()
   {
      return "srini@cs.arizona.edu";
   }

   public java.lang.String getDescription()
   {
      return "Paramreorder rearranges the order of the arguments in a method";
   }

   public sandmark.config.ModificationProperty[] getMutations()
   {
      return new sandmark.config.ModificationProperty[] {
         sandmark.config.ModificationProperty.I_CHANGE_METHOD_SIGNATURES,
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE };
   }

   public sandmark.config.RequisiteProperty[] getPostprohibited(){
      return new sandmark.config.RequisiteProperty[]{
         new sandmark.config.AlgorithmProperty(this)
      };
   }

   public void apply(sandmark.program.Method mg) throws Exception
   {
      new sandmark.util.ParamReorder().apply(mg);
   }
}
