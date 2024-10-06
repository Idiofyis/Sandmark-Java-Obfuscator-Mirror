package sandmark.analysis.stacksimulator;

class PopContext extends Context {
   private Context c;
   private int size;

   PopContext(Context c) {
      this.c = c;
      size = c.getStackSize() - 1;
   }

   public int getStackSize() {
      return size;
   }

   public int getLocalVariableCount() {
      return c.getLocalVariableCount();
   }

   public StackData[] getStackAt(int index) {
      if (index < 0 || index >= size)
	 throw new IndexOutOfBoundsException();
      return c.getStackAt(index+1);
   }

   public StackData[] getLocalVariableAt(int index) {
      return c.getLocalVariableAt(index);
   }

   public int depth() { return 1 + c.depth(); }
}
