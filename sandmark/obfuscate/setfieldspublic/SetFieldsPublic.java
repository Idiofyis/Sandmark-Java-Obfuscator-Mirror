package sandmark.obfuscate.setfieldspublic;


/**
 * The SetFieldsPublic obfuscator changes the field access modifiers
 * of all of the fields in a class.
 @author         Christian Collberg
 @version        1.0
 */

public class SetFieldsPublic extends sandmark.obfuscate.ClassObfuscator {
   static int AND_MASK = ~(org.apache.bcel.Constants.ACC_PRIVATE
         | org.apache.bcel.Constants.ACC_PROTECTED);
   static int OR_MASK = org.apache.bcel.Constants.ACC_PUBLIC;

   /**
    *  Constructor.
    */
   public SetFieldsPublic() {}

   public String getAuthor() {
      return "Christian Collberg";
   }

   public String getAuthorEmail() {
      return "collberg@cs.arizona.edu";
   }

   public String getDescription() {
      return "Make all the fields and methods in a class public";
   }

   public sandmark.config.ModificationProperty[] getMutations() {
      return null;
   }

   public sandmark.config.RequisiteProperty[] getPostprohibited() {
      return new sandmark.config.RequisiteProperty[] {
         new sandmark.config.AlgorithmProperty(this)
      };
   }

   public String getShortName() {
      return "Publicize Fields";
   }

   public String getLongName() {
      return "Make all the fields/methods in this class public";
   }

   public java.lang.String getAlgHTML() {
      return
            "<HTML><BODY>"
            + "SetFieldsPublic obfuscator changes the field access modifiers "
            + "of all of the fields in a class.\n" + "<TABLE>" + "<TR><TD>"
            + "Author: <a href =\"mailto:collberg@cs.arizona.edu\">Christian Collberg</a>\n"
            + "</TD></TR>" + "</TABLE>" + "</BODY></HTML>";
   }

   public java.lang.String getAlgURL() {
      return "sandmark/obfuscate/setfieldspublic/doc/help.html";
   }

   /*************************************************************************/
   
   /* Embedding                               */
   
   /*************************************************************************/


   public void apply(sandmark.program.Class cls) throws Exception {
      sandmark.program.Field[] fields = cls.getFields();

      for (int i = 0; i < fields.length; i++) {
         fields[i].setAccessFlags((fields[i].getAccessFlags() & AND_MASK)
               | OR_MASK);	    
      }
      cls.mark();
   }

}

