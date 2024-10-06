package sandmark.analysis.stacksimulator;

class MergeContext extends Context {
   private static final boolean DEBUG = false;
   private StackData[][] stack;
   private StackData[][] locals;

   MergeContext(Context c1, Context c2) {
      int size = c1.getStackSize();

      //merge the two stacks into one stack
      if(c2.getStackSize() != size)
         throw new RuntimeException("Cannot merge stacks of different height");

      stack = new StackData[size][];
      locals = new StackData[Math.max(c1.getLocalVariableCount(), 
                                      c2.getLocalVariableCount())][];
      for(int i = 0; i < size; i++) {
         StackData [] data1 = c1.getStackAt(i);
         StackData [] data2 = c2.getStackAt(i);

         java.util.ArrayList newData = new java.util.ArrayList();
         for(int d1 = 0; d1 < data1.length; d1++) {
            if(!newData.contains(data1[d1]))
               newData.add(data1[d1]);
         }
         for(int d2 = 0; d2 < data2.length; d2++) {
            if(!newData.contains(data2[d2]))
               newData.add(data2[d2]);
         }

         stack[i] = (StackData[])newData.toArray(new StackData[]{});
      }

      for(int i = 0; i < locals.length; i++) {
         StackData [] data1 = null;
         StackData [] data2 = null;

         data1 = c1.getLocalVariableAt(i);
         data2 = c2.getLocalVariableAt(i);

         if (data1 == null && data2 == null)
            locals[i] = null;
         else {
            java.util.ArrayList newData = new java.util.ArrayList();
            if(data1 != null)
               for(int d1 = 0; d1 < data1.length; d1++){
                  if(!newData.contains(data1[d1]))
                     newData.add(data1[d1]);
               }
            if(data2 != null)
               for(int d2 = 0; d2 < data2.length; d2++){
                  if(!newData.contains(data2[d2]))
                     newData.add(data2[d2]);
               }
		
            locals[i] = 
               (StackData[])newData.toArray(new StackData[]{});
            if(locals[i].length == 0)
               locals[i] = null;
         }
      }
   }

   public int getStackSize() {
      return stack.length;
   }

   public int getLocalVariableCount() {
      return locals.length;
   }

   public StackData[] getStackAt(int index) {
      return stack[index];
   }

   public StackData[] getLocalVariableAt(int index) {
      return locals[index];
   }

   public int depth() { return 0; }
}
