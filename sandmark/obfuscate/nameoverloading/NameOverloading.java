package sandmark.obfuscate.nameoverloading;

/**
   Name obfuscation based on Paul Tyma's patented algorithm.
   @author Andrew Lenards with revision by Kelly Heffner and a big rewrite by Andrew Huntwork
   <p>lenards@cs.arizona.edu and kheffner@cs.arizona.edu and ash@cs.arizona.edu
*/

/*
 * Obfuscation - Layout Transformation
 *
 * The algorithm below is way more complicated than necessary,
 * so i removed a bunch of steps, but it's still basically equivalent to 
 * this algorithm. -- ASH
 *
 * name: MethodOverloadingObfuscator
 *
 * input := jar containing class files
 * 
 * Algorithm:
 *   graph <- collectMethodNode(input)
 *      # foreach class file, create a MethodNode
 *      # representing the class name it came from,
 *      # the name of the method, and the signuture
 *      # of the method.
 *      #    <for all methods in the class>
 *      # [important] if the method is not related
 *      #             to the Java 2 SDK, add it to
 *      #             the set representing the
 *      #             "graph"
 *   connectGraph(graph)
 *      # determine which nodes should be connected
 *      # and created edges between them.
 *      # def: given a MethodNode n1 and a
 *      #      MethodNode n2, they are connected
 *      #      iff they have the same signature.
 *   colorGraph()
 *      # assign names to the nodes, a node cannot
 *      # share the same name with any node that
 *      # it is connected to.
 *   createLookup()
 *      # defining a mapping of old name to new name
 *   commitNameChanges()
 *      # traverse the methods of each classfile,
 *      # use the lookup to determine the new name
 *      # of a given method, encode the name, set
 *      # the index in the constant pool, mark the
 *      # index as changed, and dump the classfile
 *   save changes to jar
 *      # each classfile had its changed dumped into
 *      # it, save the jar to make all changes final
 *   end
 */



public class NameOverloading extends sandmark.obfuscate.AppObfuscator
{
    boolean DEBUG = false;

    public String getShortName() {
       return "Overload Names";
    }
    public String getLongName() {
        return "Identifier renaming using Paul Tyma's algorithm";
    }
    public java.lang.String getAlgHTML() {
        return
            "<HTML><BODY>" +
            "NameOverloading is an application obfuscator that renames " +
            "program identifiers by using Paul Tyma's algorithm.\n" +
            "<TABLE>" +
            "<TR><TD>" +
"Author: <a href =\"mailto:lenards@cs.arizona.edu\">Andrew Lenards</a>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }
    public java.lang.String getAlgURL() {
        return "sandmark/obfuscate/nameoverloading/doc/help.html";
    }
    public java.lang.String getAuthor() {
	return "Andrew Lenards";
    }
    public java.lang.String getAuthorEmail() {
        return "lenards@cs.arizona.edu";
    }
    public java.lang.String getDescription() {
        return
            "NameOverloading obfuscates an application by renaming " +
            "program identifiers. It uses Paul Tyma's algorithm.\n";
    }
    public sandmark.config.ModificationProperty[] getMutations() {
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_CHANGE_METHOD_NAMES
        };
    }

    public void apply(sandmark.program.Application app) throws Exception {
        sandmark.analysis.classhierarchy.ClassHierarchy ch = new sandmark.analysis.classhierarchy.ClassHierarchy(app); 
        java.util.Hashtable sigToMethodList = collectSignatureGroups(app,ch);
        java.util.Hashtable methodToNewName = createMethodRenameMap(app,sigToMethodList);
        sandmark.program.util.Renamer.renameMethods(methodToNewName,ch);

	java.util.Hashtable fieldToNewName = createFieldRenameMap(app);
	sandmark.program.util.Renamer.renameFields(fieldToNewName,ch);
    }

    private java.util.Hashtable collectSignatureGroups(sandmark.program.Application app,
                                                       sandmark.analysis.classhierarchy.ClassHierarchy ch) 
        throws sandmark.analysis.classhierarchy.ClassHierarchyException {
        java.util.Hashtable sigToMethodList = new java.util.Hashtable();
        for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
            sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
            for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
                sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
                java.util.ArrayList methodsWithSig = (java.util.ArrayList)sigToMethodList.get
                    (getSignatureString(method));
                if(methodsWithSig == null) {
                    methodsWithSig = new java.util.ArrayList();
                    sigToMethodList.put(getSignatureString(method),methodsWithSig);
		    if(DEBUG)
			System.out.println("putting " + 
					   method + 
					   " in new group for sig " + 
					   getSignatureString(method));
                }
                sandmark.util.MethodID mid = new sandmark.util.MethodID(method);
                if(!ch.isSpecialMethod(mid) && !ch.overridesLibraryMethod(mid) &&
		   !method.isNative()) {
                    methodsWithSig.add(method);
		    if(DEBUG)
			System.out.println("added " + mid + " to group");
                } else {
		    if(DEBUG)
			System.out.println(mid + " special: " + ch.isSpecialMethod(mid) + " ; java: " + 
					   ch.overridesLibraryMethod(mid));
                }
            }
        }
        
        return sigToMethodList;
    }

    private java.util.Hashtable createFieldRenameMap
	(sandmark.program.Application app) {
        java.util.HashSet existingFields = new java.util.HashSet();

        for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
           sandmark.program.Class clazz = 
              (sandmark.program.Class)classes.next();
           for(java.util.Iterator fields = clazz.fields() ; 
               fields.hasNext() ; ) {
              sandmark.program.Field field = 
                 (sandmark.program.Field)fields.next();
              existingFields.add(new FieldNameAndSig(field));
           }
        }

	int suffix = 0;
	java.util.Hashtable fieldToNewName = new java.util.Hashtable();
        for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
           sandmark.program.Class clazz = 
              (sandmark.program.Class)classes.next();
           for(java.util.Iterator fields = clazz.fields() ; 
               fields.hasNext() ; ) {
              sandmark.program.Field field = 
                 (sandmark.program.Field)fields.next();
	      String newName;
	      do newName = "__f" + suffix++;
	      while(existingFields.contains
		    (new FieldNameAndSig(newName,field.getSignature())));
	      if(DEBUG)
		  System.out.println("changing field " + field + " to " + newName);
	      fieldToNewName.put(field,newName);
	   }
	}

	return fieldToNewName;
    }

    private java.util.Hashtable createMethodRenameMap
	(sandmark.program.Application app,java.util.Hashtable sigToMethodList) {
        java.util.Hashtable methodToNewName = new java.util.Hashtable();
	java.util.Hashtable mNASToNewName = new java.util.Hashtable();
        java.util.HashSet existingMethods = new java.util.HashSet();

        for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
           sandmark.program.Class clazz = 
              (sandmark.program.Class)classes.next();
           for(java.util.Iterator methods = clazz.methods() ; 
               methods.hasNext() ; ) {
              sandmark.program.Method method = 
                 (sandmark.program.Method)methods.next();
              existingMethods.add(new MethodNameAndSig(method));
           }
        }


        for(java.util.Iterator methodLists = sigToMethodList.values().iterator() ; methodLists.hasNext() ; ) {
	    int suffix = 0;
            java.util.ArrayList methods = (java.util.ArrayList)methodLists.next();
            for(java.util.Iterator methodIt = methods.iterator() ; methodIt.hasNext() ; ) {
                sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
                String newName;
		MethodNameAndSig mnas = new MethodNameAndSig(method);
		if((newName = (String)mNASToNewName.get(mnas)) == null) {
                   do newName = "__m" + suffix++;
                   while(existingMethods.contains
			 (new MethodNameAndSig(newName,getSignatureString(method))));
		    mNASToNewName.put(mnas,newName);
		    if(DEBUG)
			System.out.println("name for group " + mnas + " is " + newName);
		} else
		    if(DEBUG)
			System.out.println("using equivalence class name " + newName + " for " + method);
		if(DEBUG)
		    System.out.println("changing " + method.getName() + " to " + newName);
                methodToNewName.put(method,newName);
            }
        }
        return methodToNewName;
    }

   private String getSignatureString(sandmark.program.Method method) {
      String sig = "";
      org.apache.bcel.generic.Type args[] = method.getArgumentTypes();
      for(int i = 0 ; i < args.length ; i++)
	 sig += args[i].getSignature();
      return sig;
   }

    public static void main(String[] andy) throws Exception
    {
        NameOverloading no =
            new NameOverloading();

        if(andy.length > 0 && andy[0].equals("-h"))
        {
            System.out.println("\nparameters");
            System.out.println("\t-f <class file> ex: -f T1.class T2.class T3.class");
            System.out.println("\t-j <jar file>" );
            System.out.println("\t-h ... help" );
            System.out.println( );
        }
        else if(andy.length > 1 && andy[0].equals("-f"))
        {
            try {
                no.apply(new sandmark.program.Application());
            } catch(java.io.IOException ioe) {
                System.out.println("***error - file obfuscation not attempted***");
            }
        }
        else if(andy.length > 1 && andy[0].equals("-j"))
        {
            try {
                no.apply(new sandmark.program.Application(andy[1]));
            } catch(java.io.IOException ioe) {
                System.out.println("***error - file obfuscation not attemtped***");
                ioe.printStackTrace();
            }
        }
        else
            System.out.println("Invalid Parameters");
    }

    class MethodNameAndSig {
       String name;
       String signature;
	MethodNameAndSig(sandmark.program.Method method) {
           name = method.getName();
           signature = getSignatureString(method);
	}
       MethodNameAndSig(String name,String signature) {
          this.name = name;
          this.signature = signature;
       }
	public boolean equals(Object o) {
	    if(!(o instanceof MethodNameAndSig))
		return false;
	    MethodNameAndSig otherMNAS = (MethodNameAndSig)o;

	    return name.equals(otherMNAS.name) &&
		signature.equals(otherMNAS.signature);
	}
	public int hashCode() {
           return name.hashCode() + signature.hashCode();
	}
	public String toString() {
           return name + signature;
	}
    }

    class FieldNameAndSig {
       String name;
       String signature;
       FieldNameAndSig(sandmark.program.Field field) {
	  name = field.getName();
	  signature = field.getSignature();
       }
       FieldNameAndSig(String name,String signature) {
          this.name = name;
          this.signature = signature;
       }
	public boolean equals(Object o) {
	    if(!(o instanceof FieldNameAndSig))
		return false;
	    FieldNameAndSig otherFNAS = (FieldNameAndSig)o;

	    return name.equals(otherFNAS.name) &&
		signature.equals(otherFNAS.signature);
	}
	public int hashCode() {
           return name.hashCode() + signature.hashCode();
	}
	public String toString() {
           return name + signature;
	}
    }
}

/**
 * guilty party: andy lenards
 *      project: SandMark
 *        topic: Robust Source Code Obfuscation
 *      advisor: Dr. Christian Collberg
 *     location: University of Arizona, Computer Science Dept.
 *         date: Nov. 11, 2001
 *        class: NameOverloading.java
 *          use: Method Name Obfuscation, will be used to create
 *                               interference graphs for method renaming.
 *
 */













