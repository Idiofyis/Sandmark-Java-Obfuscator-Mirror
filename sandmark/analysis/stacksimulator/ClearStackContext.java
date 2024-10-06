package sandmark.analysis.stacksimulator;

class ClearStackContext extends Context {
   private Context c;

   ClearStackContext(Context c) {
      this.c = c;
   }

   public int getStackSize() {
      return 0;
   }

   public int getLocalVariableCount() {
      return c.getLocalVariableCount();
   }

   public StackData[] getStackAt(int index) {
      throw new IndexOutOfBoundsException();
   }

   public StackData[] getLocalVariableAt(int index) {
      return c.getLocalVariableAt(index);
   }

   public int depth() { return 1 + c.depth(); }
}
