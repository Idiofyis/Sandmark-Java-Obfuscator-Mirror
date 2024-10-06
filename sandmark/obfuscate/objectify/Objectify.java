package sandmark.obfuscate.objectify;

public class Objectify extends sandmark.obfuscate.ClassObfuscator {
   public void apply(sandmark.program.Class clazz) {
      sandmark.program.Application app = clazz.getApplication();
      sandmark.analysis.classhierarchy.ClassHierarchy ch =
         app.getHierarchy();

      for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();

         for(java.util.Iterator methods = cls.methods(); methods.hasNext(); ) {
            sandmark.program.Method method =
               (sandmark.program.Method)methods.next();
            if(method.getInstructionList() == null)
               continue;

            org.apache.bcel.generic.InstructionFactory factory =
               new org.apache.bcel.generic.InstructionFactory
               (method.getConstantPool());

            for(org.apache.bcel.generic.InstructionHandle ih =
                   method.getInstructionList().getStart() ; ih != null ;
                ih = ih.getNext()) {

               if(!(ih.getInstruction() instanceof 
                    org.apache.bcel.generic.FieldInstruction))
                  continue;

               org.apache.bcel.generic.FieldInstruction fi =
                  (org.apache.bcel.generic.FieldInstruction)
                  ih.getInstruction();
               sandmark.util.FieldID fid =
                  new sandmark.util.FieldID
                  (fi.getFieldName(method.getConstantPool()),
                   fi.getFieldType(method.getConstantPool()).getSignature(),
                   fi.getClassName(method.getConstantPool()));
               sandmark.program.Field resolved = null;
               try {
                  resolved = ch.resolveFieldReference(fid,cls);
                  if(resolved == null)
                     continue;
               } catch(sandmark.analysis.classhierarchy.ClassHierarchyException e) {
                  continue;
               }
               if(resolved.getInitValue() != null)
                  continue;
               if(resolved.getEnclosingClass() != clazz ||
                  !(resolved.getType() instanceof
                    org.apache.bcel.generic.ReferenceType))
                  continue;

               org.apache.bcel.generic.ReferenceType origType =
                  (org.apache.bcel.generic.ReferenceType)
                  resolved.getType();
               ih.setInstruction
                  (factory.createFieldAccess
                   (fid.getClassName(),fid.getName(),
                    org.apache.bcel.generic.Type.OBJECT,
                    fi.getOpcode()));
               if(fi instanceof org.apache.bcel.generic.GETFIELD ||
                  fi instanceof org.apache.bcel.generic.GETSTATIC)
                  method.getInstructionList().append
                     (ih,factory.createCheckCast(origType));
            }
            method.mark();
         }
      }
      for(java.util.Iterator fields = clazz.fields() ; fields.hasNext() ; ) {
         sandmark.program.Field field = 
            (sandmark.program.Field)fields.next();
         if(!(field.getType() instanceof 
              org.apache.bcel.generic.ReferenceType))
            continue;
         if(field.getInitValue() != null)
            continue;
         field.setType(org.apache.bcel.generic.Type.OBJECT);
      }
   }

   public String getShortName() { return "Objectify"; }
   public String getLongName() { return "Objectify"; }
   public String getAlgHTML() { return 
                                   "<HTML><BODY>" +
                                   "Objectify is a class level obfuscator replaces all fields with reference types with " +
                                   "fields of type Object" +
                                   "<TABLE>" +
                                   "<TR><TD>" +
                                   "Author: <a href=\"mailto:ash@cs.arizona.edu\">Andrew Huntwork</a>\n" +
                                   "</TR></TD>" +
                                   "</TABLE>" +
                                   "</BODY></HTML>"; }
   public String getAlgURL() { return "sandmark/obfuscate/objectify/doc/help.html"; }
   public String getAuthor() { return "Andrew Huntwork"; }
   public String getAuthorEmail() { return "ash@cs.arizona.edu"; }
   public String getDescription() { 
      return "Objectify replaces all fields with reference types with " +
         "fields of type Object"; 
   }
   public sandmark.config.ModificationProperty [] getMutations() {
      return null;
   }
}
