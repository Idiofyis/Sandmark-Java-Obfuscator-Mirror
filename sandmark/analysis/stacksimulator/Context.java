package sandmark.analysis.stacksimulator;

public abstract class Context {
   public StackTraceElement stack[];
   private static final boolean DEBUG = false;
   public Context() {
      if(DEBUG)
         try { throw new RuntimeException(); }
         catch(Exception e) { stack = e.getStackTrace(); }
   }
   Context replaceVariable(StackData[] data, int slot) {
      return maybeConsolidate(new ReplaceVarContext(this, slot, data));
   }

   Context replaceVariable(StackData data, int slot) {
      return replaceVariable(new StackData[] {data}, slot);
   }

   Context clearStack() {
      return maybeConsolidate(new ClearStackContext(this));
   }

   Context push(StackData[] data) {
      return pushAt(0, data);
   }

   Context push(StackData data) {
      return push(new StackData[] {data});
   }

   Context pushAt(int index, StackData[] data) {
      return maybeConsolidate(new PushContext(this, index, data));
   }

   Context pushAt(int index, StackData data) {
      return pushAt(index, new StackData[] {data});
   }

   Context pop() {
      return maybeConsolidate(new PopContext(this));
   }

   Context replaceStack(StackData[] data, int index) {
      return maybeConsolidate(new ReplaceStackContext(this, index, data));
   }

   Context initializeTop(org.apache.bcel.generic.InstructionHandle ih) {
      StackData tmp = getStackAt(0)[0];
      if (!(tmp instanceof UninitializedReferenceData))
         return this;

      UninitializedReferenceData urd =
         (UninitializedReferenceData)tmp;

      StackData rd = urd.initialize(ih);
      Context c = this;
      for (int i = 0,size = getStackSize(); i < size; i++) {
         StackData sd[] = getStackAt(i);
         StackData sd2[] = new StackData[sd.length];
         boolean replace = false;
         for (int j = 0; j < sd.length; j++)
            if (urd.equals(sd[j])) {
               replace = true;
               sd2[j] = rd;
            }
            else
               sd2[j] = sd[j];
         if (replace)
            c = c.replaceStack(sd2, i);
      }

      for (int i = 0,size = getLocalVariableCount(); i < size; i++) {
         StackData sd[] = getLocalVariableAt(i);
         if(sd == null)
            continue;
         StackData sd2[] = new StackData[sd.length];
         boolean replace = false;
         for (int j = 0; j < sd.length; j++)
            if (urd.equals(sd[j])) {
               replace = true;
               sd2[j] = rd;
            }
            else
               sd2[j] = sd[j];
         if (replace)
            c = c.replaceVariable(sd2, i);
      }

      return c;
   }

   public abstract int getStackSize();
   public abstract int getLocalVariableCount();
   public abstract StackData[] getStackAt(int index);
   public abstract StackData[] getLocalVariableAt(int index);
   public abstract int depth();

   public static Context maybeConsolidate(Context cx) {
      return cx.depth() > 16 ? cx.consolidate() : cx;
   }

   public Context consolidate() {
      Context cx = merge(this);
      if(cx.depth() != 0)
         throw new RuntimeException("consolidate didn't work");
      return cx;
   }

   // checks to see if 'this' is a subcontext of 'c'
   boolean isSubcontextOf(Context c) {
      if(c == null)
         return false;

      int size = getStackSize();

      //check to see if the stack is a subset of the other stack
      if(c.getStackSize() != size){
         throw new RuntimeException("Stack height should be the same at " +
                                    "the start of a basic block.");
      }

      for(int i = 0; i < size; i++) {
         StackData [] superset =
            c.getStackAt(i);
         StackData [] myset =
            getStackAt(i);

         for(int m = 0; m < myset.length; m++) {
            StackData temp = myset[m];
            boolean found = false;
            for(int s = 0; s < superset.length; s++){
               //maybe should add a "if contains undefined" found= true?

               if(temp.equals(superset[s])) {
                  found = true;
                  break;
               }
            }
            if(!found)
               return false;
         }
      }

      int lvcount = Math.max(getLocalVariableCount(), c.getLocalVariableCount());

      //check to see if the local variables are a subset of the other
      //local variables
      for(int i = 0; i < lvcount; i++){
         StackData [] superset =
            c.getLocalVariableAt(i);
         StackData [] myset =
            getLocalVariableAt(i);

         if(myset == null)
            continue;

         if(superset == null && myset != null)
            return false;

         for(int m = 0; m < myset.length; m++) {
            StackData temp = myset[m];
            boolean found = false;
            for(int s = 0; s < superset.length; s++){
               //maybe should add a "if contains undefined" found = true?
               if(temp.equals(superset[s])){
                  found = true;
                  break;
               }
            }
            if(!found)
               return false;
         }
      }

      return true;
   }

   Context merge(Context other) {
      if (other == null)
         return this;
      else
         return maybeConsolidate(new MergeContext(this, other));
   }

   Context undefinedVersion() {
      return maybeConsolidate(new UndefinedContext(this));
   }

   public String toString() {
      String rv = "";
      if(DEBUG) {
         rv += "Allocated at: \n";
         for(int i = 0 ; i < stack.length ; i++)
            rv += stack[i] + "\n";
      }
      rv += "Stack:\n";
      for(int i = 0 ; i < getStackSize() ; i++) {
         rv += i + "\n";
         StackData data[] = getStackAt(i);
         for(int j = 0 ; j < data.length ; j++)
            rv += data[j] + "\n";
      }
      rv += "Variables:\n";
      for(int i = 0 ; i < getLocalVariableCount() ; i++) {
         rv += i + "\n";
         StackData data[] = getLocalVariableAt(i);
         for(int j = 0 ; data != null && j < data.length ; j++)
            rv += data[j] + "\n";
      }
      return rv;
   }
}
