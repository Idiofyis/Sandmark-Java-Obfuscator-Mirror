package sandmark.util;


/**
 * StaticSplit splits all the static and dynamic methods of a class into two
 * methods, a private static method that does all the work of the original 
 * method and a stub with the same interface as the original method that 
 * simply calls the private static one.
 *
 * @author Srinivas Visvanathan
 *
 */
public class StaticSplit
{

   /* perform the static split on class cls */
   public void apply(sandmark.program.Class cls)
   {
      java.util.Iterator it = cls.methods();
      while (it.hasNext()) {
         sandmark.program.Method meth = (sandmark.program.Method)it.next();
         if (meth.getName().equals("<init>") || meth.isAbstract()
             || meth.isNative() || meth.getName().equals("<clinit>") || 
             meth.getInstructionList()==null)
            continue;
         split(meth,cls);
      }
   }

   private void split(sandmark.program.Method meth, sandmark.program.Class cls)
   {
      //compute arg types of the new method
      org.apache.bcel.generic.Type oldAT[] = meth.getArgumentTypes();
      org.apache.bcel.generic.Type newAT[];
      if (meth.isStatic())
         newAT = oldAT;
      else {
         newAT = new org.apache.bcel.generic.Type[oldAT.length + 1];
         newAT[0] = org.apache.bcel.generic.Type.getType("L" + cls.toString() + ";");
         for (int jj = 0; jj < oldAT.length; jj++)
            newAT[jj + 1] = oldAT[jj];
      }

      meth.removeLineNumbers();
      meth.removeLocalVariables();
	
      //create a private, static copy of meth. meth.copy also makes lm a
      //member of cls
      sandmark.program.LocalMethod lm = meth.copy();
	
      //before changing the arg types, ensure no method with same name:sig
      //already exists; if it does, pick a new random name for lm
      String tmpName = lm.getName();
      String tmpSig = 
         org.apache.bcel.generic.Type.getMethodSignature
         (lm.getReturnType(),newAT);

      while (cls.getMethod(tmpName,tmpSig) != null)
         tmpName = "M" + (int)(1e9 * sandmark.util.Random.getRandom().nextDouble());

      lm.setName(tmpName);
      lm.setArgumentTypes(newAT);
      lm.setAccessFlags(org.apache.bcel.Constants.ACC_STATIC |
                        org.apache.bcel.Constants.ACC_PROTECTED);

      //rewrite meth as a stub that calls lm
      meth.removeExceptionHandlers();
      meth.removeAttributes();
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();
      org.apache.bcel.generic.InstructionFactory iF = 
         new org.apache.bcel.generic.InstructionFactory(meth.getCPG());
      //add code to simply push this and the args on the stack
      for (int jj = 0, idx = 0; jj < newAT.length; idx += newAT[jj].getSize(), jj++)
         il.append(iF.createLoad(newAT[jj],idx));
      //add an invokation to lm
      il.append(iF.createInvoke(cls.toString(),lm.getName(),
                                lm.getReturnType(),newAT,
                                org.apache.bcel.Constants.INVOKESTATIC));
      //add return
      il.append(iF.createReturn(lm.getReturnType()));
      meth.setInstructionList(il);
   }
}
