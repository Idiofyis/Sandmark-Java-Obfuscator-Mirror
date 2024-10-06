package sandmark.analysis.stacksimulator;

class ReplaceVarContext extends Context {
   private Context c;
   private int index;
   private int lvcount;
   private StackData[] data;

   ReplaceVarContext(Context c, int index, StackData[] data) {
      this.c = c;
      this.index = index;
      this.data = data;
      lvcount = c.getLocalVariableCount();
      if (index < 0 || index >= lvcount)
         throw new IndexOutOfBoundsException();
   }

   public int getStackSize() {
      return c.getStackSize();
   }

   public int getLocalVariableCount() {
      return lvcount;
   }

   public StackData[] getStackAt(int index) {
      return c.getStackAt(index);
   }

   public StackData[] getLocalVariableAt(int index) {
      if (index == this.index)
         return data;
      else
         return c.getLocalVariableAt(index);
   }

   public int depth() { return 1 + c.depth(); }
}
