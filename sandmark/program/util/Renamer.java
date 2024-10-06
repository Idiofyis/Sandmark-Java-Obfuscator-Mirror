//-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
package sandmark.program.util;

/**
 * Provides a set of operations for moving and renaming program objects.
 * These are heavyweight methods that operate on the whole application,
 * altering all references.
 *
 * <P><STRONG>The methods in this class have been specified
 * but not yet implemented.</STRONG>
 * The interface is based on various suggestions, rounded out for
 * completeness; it's not clear which of these we really need, and 
 * they're not trivial.
 *
 */

public class Renamer {
   private static final boolean DEBUG = false;

   

   private Renamer() {}         // no public constructor; instances not useful

   /**
    * Renames a class to a new name and updates all references to the
    * class.  Changing the package denotation of a class will move
    * it to a new package.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param c the class to rename
    * @param newname the fully qualified new name (ex java.lang.Object)
    */
   public static void rename(sandmark.program.Class c, String newname) {
      throw new java.lang.UnsupportedOperationException("unimplemented");

   }

   /**
    * Renames a method and updates all references to the method.  Note
    * that this does not do analysis of dynamic dispatch.  References
    * to a method are determined by matching classname/methodname/signature.
    * To ensure change to a method called via dynamic dispatch, you must
    * rename <i>all</i> all of the overrides of the method.  For help
    * with this see {@link sandmark.analysis.classhierarchy.ClassHierarchy
    * sandmark.analysis.classhierarchy.ClassHierarchy}.
    *
    * @param m the method to rename
    * @param newname the new name for the method
    */
    public static void rename(sandmark.program.Method m, String newname) {
        rename(m,newname,null);
    }

    public static void rename(sandmark.program.Method m, String newname, 
                              sandmark.analysis.classhierarchy.ClassHierarchy ch) {
        java.util.Hashtable ht = new java.util.Hashtable();
        ht.put(m,newname);
        renameMethods(ht,ch);
   }



   /**
    * Renames a field and updates all references to the field.  This method
    * currently does not ensure that a hard-wired reference to the field
    * will be updated accordingly.
    *
    * @param f the field to rename
    * @param newname the new name for this field
    */
   public static void rename(sandmark.program.Field f, String newname) {
      rename(f,newname,null);
   }

   public static void rename
      (sandmark.program.Field f,String newname,
       sandmark.analysis.classhierarchy.ClassHierarchy ch) {
       java.util.Hashtable ht = new java.util.Hashtable();
       ht.put(f,newname);
       renameFields(ht,ch);
   }

   /**
    * Changes all members of a package to a new name, including subpackages.
    * If the new package already exists, the contents of the packages will
    * be merged - <i>if a conflict arises, an exception will be raised?</i>.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param a the application containing the package
    * @param old the old package name
    * @param newName the new package name
    */
   public static void renamePackage(
         sandmark.program.Application a, String old, String newName) {
      throw new java.lang.UnsupportedOperationException("unimplemented");
   }



   /**
    * Renames several classes and updates all references to the classes.
    * This method does the same thing as
    * {@link sandmark.program.util.Renamer#rename(
    *    sandmark.program.Class, String)
    *    rename(Class, String)}
    * in batch.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param map A mapping of (sandmark.program.Class, String) pairs
    */
   public static void renameClasses(java.util.Map map) {
      throw new java.lang.UnsupportedOperationException("unimplemented");
   }



   /**
    * Renames several methods and updates all references to the methods.
    * This method does the same thing as
    * {@link sandmark.program.util.Renamer#rename(
    *    sandmark.program.Method, String)
    *    rename(Method, String)}
    * in batch.
    *
    * @param map A mapping of (sandmark.program.Method, String) pairs
    */
   public static void renameMethods(java.util.Map map) {
       renameMethods(map,null);
   }
    
    public static void renameMethods
        (java.util.Map map,sandmark.analysis.classhierarchy.ClassHierarchy ch) {
        java.util.Hashtable midToNewName = new java.util.Hashtable();
        java.util.Set apps = new java.util.HashSet();
        for(java.util.Iterator methodIt = map.keySet().iterator() ; methodIt.hasNext() ; ) {
            sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
            String newName = (String)map.get(method);
            midToNewName.put(new sandmark.util.MethodID(method),newName);
            apps.add(method.getApplication());
        }
        for(java.util.Iterator appIt = apps.iterator() ; appIt.hasNext() ; ) {
            sandmark.program.Application app = 
                (sandmark.program.Application)appIt.next();
            for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
                sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
                if(DEBUG)
                    System.out.println("renaming in " + clazz.getName());
                for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
                    sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
                    if(DEBUG)
                        System.out.println("renaming in " + method.getName());
                    if(method.getInstructionList() == null)
                        continue;
                    org.apache.bcel.generic.InstructionHandle ihs[] = 
                        method.getInstructionList().getInstructionHandles();
                    org.apache.bcel.generic.ConstantPoolGen cpg = method.getConstantPool();
                    for(int i = 0 ; i < ihs.length ; i++) {
                        if(!(ihs[i].getInstruction() instanceof org.apache.bcel.generic.InvokeInstruction))
                            continue;
                        org.apache.bcel.generic.InvokeInstruction inv =
                            (org.apache.bcel.generic.InvokeInstruction)ihs[i].getInstruction();
                        if(DEBUG)
                            System.out.println("about to rename " + inv.getName(cpg));
                        sandmark.util.MethodID mid = new sandmark.util.MethodID
                            (inv.getName(cpg),inv.getSignature(cpg),inv.getClassName(cpg));
                        if(DEBUG)
                            System.out.print(mid + " resolves to ");
                        if(ch != null)
                            try { 
                                sandmark.program.Method resolved = ch.resolveMethodReference(mid,clazz); 
                                if(resolved == null)
                                    resolved = ch.resolveInterfaceMethodReference(mid,clazz);
                                mid = resolved == null ? null : new sandmark.util.MethodID(resolved);
                            }catch(sandmark.analysis.classhierarchy.ClassHierarchyException e) {}
                        if(DEBUG)
                            System.out.println(mid);
                        if(mid == null || !midToNewName.containsKey(mid)) {
                            if(DEBUG)
                                System.out.println("not renaming " + mid);
                            continue;
                        }
                        String newName = (String)midToNewName.get(mid);
                        int newCPIndex;
                        if(inv instanceof org.apache.bcel.generic.INVOKEINTERFACE)
                            newCPIndex = cpg.addInterfaceMethodref
                                (mid.getClassName(),newName,mid.getSignature());
                        else
                            newCPIndex = cpg.addMethodref
                                (mid.getClassName(),newName,mid.getSignature());
                        inv.setIndex(newCPIndex);
                        if(DEBUG)
                            System.out.println("now called " + inv.getName(cpg));
                    }
                    method.mark();
                }
            }
        }
        for(java.util.Iterator methods = map.keySet().iterator() ; 
            methods.hasNext() ; ) {
            sandmark.program.Method method = 
                (sandmark.program.Method)methods.next();
            String name = (String)map.get(method);
            method.setName(name);
        }
    }

   /**
    * Renames several fields and updates all references to the fields.
    * This method does the same thing as
    * {@link sandmark.program.util.Renamer#rename(
    *    sandmark.program.Field, String)
    *    rename(Field, String)}
    * in batch.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param map A mapping of (sandmark.program.Field, String) pairs
    */
   public static void renameFields(java.util.Map map) {
      renameFields(map,null);
   }
   
    public static void renameFields
        (java.util.Map map,sandmark.analysis.classhierarchy.ClassHierarchy ch) {
        java.util.Hashtable fidToNewName = new java.util.Hashtable();
        java.util.Set apps = new java.util.HashSet();
        for(java.util.Iterator fieldIt = map.keySet().iterator() ; fieldIt.hasNext() ; ) {
            sandmark.program.Field field = (sandmark.program.Field)fieldIt.next();
            String newName = (String)map.get(field);
            fidToNewName.put(new sandmark.util.FieldID(field),newName);
            apps.add(field.getApplication());
        }
        for(java.util.Iterator appIt = apps.iterator() ; appIt.hasNext() ; ) {
            sandmark.program.Application app = 
                (sandmark.program.Application)appIt.next();
            for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
                sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
                if(DEBUG)
                    System.out.println("renaming in " + clazz.getName());
                for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
                    sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
                    if(DEBUG)
                        System.out.println("renaming in " + method.getName());
                    if(method.getInstructionList() == null)
                        continue;
                    org.apache.bcel.generic.InstructionHandle ihs[] = 
                        method.getInstructionList().getInstructionHandles();
                    org.apache.bcel.generic.ConstantPoolGen cpg = method.getConstantPool();
                    for(int i = 0 ; i < ihs.length ; i++) {
                        if(!(ihs[i].getInstruction() instanceof org.apache.bcel.generic.FieldInstruction))
                            continue;
                        org.apache.bcel.generic.FieldInstruction fi =
                            (org.apache.bcel.generic.FieldInstruction)ihs[i].getInstruction();
                        if(DEBUG)
                            System.out.println("about to rename " + fi.getName(cpg));
                        sandmark.util.FieldID fid = new sandmark.util.FieldID
                            (fi.getName(cpg),fi.getSignature(cpg),fi.getClassName(cpg));
                        if(DEBUG)
                            System.out.print(fid + " resolves to ");
                        if(ch != null)
                            try { 
                                sandmark.program.Field resolved = ch.resolveFieldReference(fid,clazz); 
                                fid = resolved == null ? null : new sandmark.util.FieldID(resolved);
                            }catch(sandmark.analysis.classhierarchy.ClassHierarchyException e) {}
                        if(DEBUG)
                            System.out.println(fid);
                        if(fid == null || !fidToNewName.containsKey(fid)) {
                            if(DEBUG)
                                System.out.println("not renaming " + fid);
                            continue;
                        }
                        String name = (String)fidToNewName.get(fid);
                        int newCPIndex = cpg.addFieldref
                            (fid.getClassName(),name,fid.getSignature());
                        fi.setIndex(newCPIndex);
                        if(DEBUG)
                            System.out.println("now called " + fi.getName(cpg));
                    }
                    method.mark();
                }
            }
        }
        for(java.util.Iterator fields = map.keySet().iterator() ; 
            fields.hasNext() ; ) {
            sandmark.program.Field f = (sandmark.program.Field)fields.next();
            String name = (String)map.get(f);
            f.setName(name);
        }
    }



   /**
    * Changes all members of multiple packages to new names,
    * including subpackages.
    * This method does the same thing as
    * {@link sandmark.program.util.Renamer#renamePackage(
    *    sandmark.program.Application, String, String)
    *    renamePackage(Application, String, String)}
    * in batch.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param a the application containing the package
    * @param map a mapping of (String, String) pairs
    */
   public static void renamePackages(
         sandmark.program.Application a, java.util.Map map) {
      throw new java.lang.UnsupportedOperationException("unimplemented");
   }



   /**
    * Moves a method from one class to another and updates all references
    * to that method in the application.
    *
    * @param m the method to move
    * @param newClass the destination for the method
    */
   public static void move(sandmark.program.Method m, 
                           sandmark.program.Class newClass) {
       
   }



   /**
    * Moves several methods to another class and updates all references
    * to each method in the application.  This method does the same
    * thing as {@link sandmark.program.util.Renamer#move(
    *    sandmark.program.Method,
    *    sandmark.program.Class) move(Method, Class)}
    * in batch.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param map a mapping of (Method, Class) pairs
    */
   public static void moveMethods(java.util.Map map) {
      throw new java.lang.UnsupportedOperationException("unimplemented");
   }

   public static void setConstantPoolName(sandmark.program.Class clazz) {
      
   }
}

