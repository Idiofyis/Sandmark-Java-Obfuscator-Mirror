/*
** File: RenameLocals.java
**
** @author Christian Collberg
**
*/
package sandmark.obfuscate.renameidentifiers;

public class RenameLocals extends sandmark.obfuscate.AppObfuscator {

   static final boolean DEBUG = false;

   /*
   ** Returns this obfuscator's short name.
   */
   public java.lang.String getShortName() {
      return "Rename Registers";
    }

   /*
   ** Returns this obfuscator's long name.
   */
   public java.lang.String getLongName() {
      return "Renames local variables to random identifiers.";
   }

    public String getAuthor()
    {
        return "Christian Collberg";
    }

    public String getAuthorEmail()
    {
        return "collberg@cs.arizona.edu";
    }

    public String getDescription()
    {
        return "RenameLocals renames local variables to random identifiers.";

    }

    public sandmark.config.ModificationProperty[] getMutations()
    {
        return null;
    }

   /*
   **  Get the HTML codes of the About page for Degrade
   */
   public java.lang.String getAlgHTML(){
      return
          "<HTML><BODY>" +
          "RenameLocals renames local variables to random identifiers.\n" +
          "<TABLE>" +
          "<TR><TD>" +
          "Authors: <A HREF = \"mailto:collberg@cs.arizona.edu\">Christian Collberg</A>\n" +
          "</TD></TR>" +
          "</TABLE>" +
          "</BODY></HTML>";
   }

   /*
   **  Get the URL of the Help page for Degrade
   */
   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/renameidentifiers/doc/RenameLocals.html";
   }

   /*
   ** All the work for the RenameLocals obfuscation
   */
   public void apply(sandmark.program.Application app) throws Exception {
      java.util.Iterator classes = app.classes();
      while(classes.hasNext()) {
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();
         String className = cls.getName();
         rename(cls, className);
      }
   }

   /*
    * FOR all classes DO rename local variables.
    */
   void rename(
      sandmark.program.Class cls,
      //org.apache.bcel.generic.ClassGen cg,
      String className) {
      //org.apache.bcel.classfile.Method[] methods=cg.getMethods();
      sandmark.program.Method[] methods = cls.getMethods();
      org.apache.bcel.generic.ConstantPoolGen cpg = cls.getConstantPool(); //cg.getConstantPool();

      for(int i=0;i<methods.length;i++) {
         sandmark.program.Method origMeth = methods[i];
         //org.apache.bcel.classfile.Method origMeth = methods[i];
         //org.apache.bcel.generic.MethodGen mg=
         //   new org.apache.bcel.generic.MethodGen(origMeth,className,cpg);
         //rename(mg);
         rename(origMeth);
         //org.apache.bcel.classfile.Method newMeth = mg.getMethod();
         //cg.replaceMethod(origMeth,newMeth);
         cls.mark();
      }
   }

   /*
    * FOR all methods DO rename local variables.
    * We don't rename "this" because it seems to screw up
    * SourceAgain. Maybe we should?
    */
   void rename(
       sandmark.program.Method mg){
       //org.apache.bcel.generic.MethodGen mg) {
       org.apache.bcel.generic.LocalVariableGen[] lvgs =
           mg.getLocalVariables();
       java.util.Hashtable localMap = initMap();
       java.util.Hashtable localCount = new java.util.Hashtable();
       for (int j = 0; j < lvgs.length; j++) {
          org.apache.bcel.generic.LocalVariableGen local = lvgs[j];
          if (mg.isStatic() || (j>0)) {
             String oldName = local.getName();
             int slot = local.getIndex();
             org.apache.bcel.generic.Type type = local.getType();
             String newName = findNewName(oldName, type, localMap, localCount);
             local.setName(newName);
             if (DEBUG)
                 System.out.println("CHANGING " + oldName + 
                                " (slot " + slot + ")" +
                                " TO " + newName + 
                                " IN " + mg.getClassName() + "." + mg.getName());
             //    mg.addLocalVariable(newName, type, slot, local.getStart(), local.getEnd());
          }
       }
       mg.mark();
   }

   /*
    * Return a new name for a given local variable.
    */
   String findNewName(
      String oldName, 
      org.apache.bcel.generic.Type type, 
      java.util.Hashtable localMap,
      java.util.Hashtable localCount) {
      String newName = oldName;
      java.util.LinkedList L = (java.util.LinkedList)localMap.get(type);
      String n = DEFAULT;
      if (L != null)
         n = (String)L.getFirst();
      if (n.endsWith("#")) {
         newName = n.substring(0,n.length()-1);
         java.lang.Integer k = (java.lang.Integer) localCount.get(newName);
         if (k==null) 
            k = new java.lang.Integer(0);
         k = new java.lang.Integer(k.intValue()+1);
         localCount.put(newName,k);
         newName += k.toString();
      } else {
         newName = n;
         L.removeFirst();
      }
      return newName;
   }

   void map(
      org.apache.bcel.generic.Type t, 
      String[] names, 
      java.util.Hashtable localMap) {
      java.util.LinkedList L = new java.util.LinkedList();
      for(int i=0; i<names.length; i++) {
         L.addLast(names[i]);
      }
      localMap.put(t,L);
   }

   static final String DEFAULT = "Z#";
   java.util.Hashtable initMap () {
      java.util.Hashtable localMap = new java.util.Hashtable();
      map(org.apache.bcel.generic.Type.INT, 
          new String[]{"i","j","k","m","n","o","p","i#"}, 
          localMap);
      map(org.apache.bcel.generic.Type.BOOLEAN,  
          new String[]{"b","c","b#"}, 
          localMap);
      map(org.apache.bcel.generic.Type.BYTE,  
          new String[]{"by#"}, 
          localMap);
      map(org.apache.bcel.generic.Type.CHAR,  
          new String[]{"c","c#"}, 
          localMap);
      map(org.apache.bcel.generic.Type.DOUBLE,  
          new String[]{"d","d#"}, 
          localMap);
      map(org.apache.bcel.generic.Type.FLOAT,  
          new String[]{"f","g","h","f#"}, 
          localMap);
      map(org.apache.bcel.generic.Type.LONG,  
          new String[]{"l#"}, 
          localMap);
      map(org.apache.bcel.generic.Type.SHORT,  
          new String[]{"s#"}, 
          localMap);
      map(org.apache.bcel.generic.Type.STRING,  
          new String[]{"S","T","U","V","W","X","S#"}, 
          localMap);
      map(new org.apache.bcel.generic.ObjectType("java.lang.Integer"), 
          new String[]{"I","J","K","M","N","I#"}, 
          localMap);
      map(new org.apache.bcel.generic.ObjectType("java.lang.Long"), 
          new String[]{"L#"}, 
          localMap);
      map(new org.apache.bcel.generic.ObjectType("java.lang.Float"), 
          new String[]{"F#"}, 
          localMap);
      map(new org.apache.bcel.generic.ObjectType("java.lang.Double"), 
          new String[]{"D#"}, 
          localMap);
      return localMap;
   }

    public sandmark.config.RequisiteProperty[] getPostprohibited()  {
        return new sandmark.config.RequisiteProperty[]{
            new sandmark.config.AlgorithmProperty(this)
        };
    }

    public static void main(String [] args) {
        if (args.length < 1){
            System.out.println("Usage: java -classpath ../../../:../../../../smextern3/BCEL.jar sandmark.obfuscate.renameidentifiers.RenameLocals JAR_FILE");
            System.exit(1);
        }

        try {
            sandmark.program.Application app =
               new sandmark.program.Application(args[0]);
            sandmark.obfuscate.renameidentifiers.RenameLocals reorderer = 
                new sandmark.obfuscate.renameidentifiers.RenameLocals();
            reorderer.apply(app);

            app.save("NAME_OBF_HARDCODE.jar");
        }
        catch (java.lang.Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }


    }
}

