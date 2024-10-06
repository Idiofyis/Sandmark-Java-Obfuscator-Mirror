/***********************************************************
*  FalseRefactoring Algorithm  :                           *
************************************************************
*  Idea Suggested by :                                     *
*  ==================                                      *
*  Dr.Christian Collberg                                   *
*  collberg@cs.arizona.edu                                 *
*                                                          *
*  Date : 4 May 2002                                       *
*  -----------------                                       *
*  Ashok Purushotham       &&      RathnaPrabhu            *
*  ashok@cs.arizona.edu            prabhu@cs.arizona.edu   *
************************************************************/
package sandmark.obfuscate.classsplitter;

public class FalseRefactor  extends sandmark.obfuscate.AppObfuscator {    

    public void apply(sandmark.program.Application app) throws Exception {
	java.util.Iterator classes = app.classes();
        sandmark.program.Class class1,class2;
        while (classes.hasNext() && (class1 = (sandmark.program.Class)classes.next()) != null &&
               classes.hasNext() && (class2 = (sandmark.program.Class)classes.next()) != null) {
            String baseClassName = class1.getName() + "SMBase";
            if(app.getClass(baseClassName) == null) {
                System.out.println("class " + baseClassName + "already exists");
                continue;
            }

            baseClassName = class1.getName() + "SMBase";

            sandmark.program.Class baseClass =
                doRefactoring(class1,class2,baseClassName);
        }
    }
    public sandmark.program.Class doRefactoring
        (sandmark.program.Class class1,sandmark.program.Class class2,
         String baseClassName) {   	    
        sandmark.program.Field class1Fields[] = class1.getFields();
        sandmark.program.Field class2Fields[] = class2.getFields();
        if(class1Fields.length == 0 || class2Fields.length == 0 ) {
            System.out.println(" Cant refactor classes");
            return null;
        }

	String class1SuperName = class1.getSuperclassName();
	String class2SuperName = class2.getSuperclassName();
	boolean addInterface = !(class1SuperName.equals("java/lang/Object") && 
                                 class2SuperName.equals("java/lang/Object"));

        int newClassAccess = 
            org.apache.bcel.Constants.ACC_SUPER |
            org.apache.bcel.Constants.ACC_PUBLIC;
        if(addInterface)
            newClassAccess |= org.apache.bcel.Constants.ACC_INTERFACE;

        sandmark.program.Class newClass =
            new sandmark.program.LocalClass(class1.getApplication(),baseClassName,"java.lang.object",
                                            "bogus.java",newClassAccess,null);
        newClass.addEmptyConstructor(org.apache.bcel.Constants.ACC_PUBLIC);
	    
        java.util.Hashtable class2FieldsMatched = new java.util.Hashtable();
        for(int i = 0 ; i < class1Fields.length ; i++) {
            for(int j = 0 ; j < class2Fields.length ; j++){
                if(class1Fields[i].getType().equals(class2Fields[j].getType()) && 
                   !class1Fields[i].isPrivate() && !class2Fields[j].isPrivate() &&
                   class2FieldsMatched.get(class2Fields[j]) == null){
                    class2FieldsMatched.put(class2Fields[j],class2Fields[j]);
                    sandmark.program.Field field =
                        new sandmark.program.LocalField
                        (newClass,org.apache.bcel.Constants.ACC_PUBLIC,
                         class1Fields[i].getType(),class1Fields[i].getName());
                    int class2FieldIndex = class2Fields[j].getNameIndex();
                    class1Fields[i].delete();
                    class2Fields[j].delete();
                    if(addInterface) {
                        class1.addInterface(baseClassName);
                        class2.addInterface(baseClassName);
                    } else {
                        class1.setSuperclassName(baseClassName);
                        class2.setSuperclassName(baseClassName);
                    }
                    class2.getConstantPool().setConstant
                        (class2FieldIndex,
                         new org.apache.bcel.classfile.ConstantUtf8(field.getName()));
                }
            }
        }
        return newClass;
    }
    
    public String getShortName() {
	return "FalseRefactor";
    }
    
    public String getLongName() {
	return "False Refactor";
    }

    public java.lang.String getAlgHTML(){
	return 
	    "<HTML><BODY>" +
	    "FalseRefactor is an application obfuscator. It is performed on " +
	    "two classes C1 and C2 that have no common behavior. If both " +
	    "classes have instance variables of the same type, these can be " +
	    "moved into the new parent class C3. C3's methods can be buggy " +
	    "versions of some of the methods from C1 and C2 .\n" +
	    "<TABLE>" +
	    "<TR><TD>" +
	    "Author: <a href =\"mailto:ashok@cs.arizona.edu\">Ashok Purushotham</a> and " +
	    "<a href = \"mailto:prabhu@cs.arizona.edu\">RathnaPrabhu</a>\n" +
	    "</TD></TR>" +
	    "</TABLE>" +
	    "</BODY></HTML>";
    }

    public java.lang.String getAlgURL() {
	return "sandmark/obfuscate/classsplitter/doc/help_falserefactor.html";
    }

    public String getAuthor() {
	return "Ashok P. Ramasamy Venkatraj & Rathnaprabhu Rajendran";
    }

    public String getAuthorEmail() {
	return "ashok@cs.arizona.edu prabhu@cs.arizona.edu";
    }

    public String getDescription() {
	return "False Refactor merges two classes C1 and C2 by adding a " +
            "bogus parent class C3. If both classes have instance " + 
            "variables of the same type, these can be moved into C3.";
    }

    public sandmark.config.ModificationProperty [] getMutations() {
        return null;
    }

}


