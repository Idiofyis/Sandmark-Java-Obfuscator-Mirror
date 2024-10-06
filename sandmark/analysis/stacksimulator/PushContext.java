package sandmark.analysis.stacksimulator;

class PushContext extends Context {
   private Context c;
   private int index;
   private int size;
   private StackData[] data;

   PushContext(Context c, int index, StackData[] data) {
      this.c = c;
      this.index = index;
      this.data = data;
      size = c.getStackSize();
      if (index < 0 || index > size)
         throw new IndexOutOfBoundsException(index + "");
      size++;
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
      if (index < this.index)
         return c.getStackAt(index);
      else if (index == this.index)
         return data;
      else
         return c.getStackAt(index-1);
   }

   public StackData[] getLocalVariableAt(int index) {
      return c.getLocalVariableAt(index);
   }

   public int depth() { return 1 + c.depth(); }
}
