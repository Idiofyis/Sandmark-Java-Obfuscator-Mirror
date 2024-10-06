package sandmark.analysis.stacksimulator;

class UndefinedContext extends Context {
   private StackData[][] stack;
   private StackData[][] locals;

   UndefinedContext(Context c) {
      int stacksize = c.getStackSize();
      int localsize = c.getLocalVariableCount();

      stack = new StackData[stacksize][];
      locals = new StackData[localsize][];

      for (int i = 0; i < stacksize; i++) {
         StackData[] data = c.getStackAt(i);
         StackData[] newData = new StackData[data.length];
         for (int j = 0; j < data.length; j++)
            newData[j] = data[j].undefinedVersion();
         stack[i] = newData;
      }

      for (int i = 0; i < localsize; i++) {
         StackData[] data = c.getLocalVariableAt(i);
         if (data != null) {
            StackData[] newData = new StackData[data.length];
            for (int j = 0; j < data.length; j++)
               newData[j] = data[j].undefinedVersion();
            locals[i] = newData;
         }
         else
            locals[i] = null;
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
