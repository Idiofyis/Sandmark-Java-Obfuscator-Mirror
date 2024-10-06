package sandmark.analysis.stacksimulator;

class EmptyContext extends Context {
   private int lvcount;

   EmptyContext(int lvcount) {
      this.lvcount = lvcount;
   }

   public int getStackSize() {
      return 0;
   }

   public int getLocalVariableCount() {
      return lvcount;
   }

   public StackData[] getStackAt(int index) {
      throw new IndexOutOfBoundsException();
   }

   public StackData[] getLocalVariableAt(int index) {
      if (index < 0 || index >= lvcount)
	 throw new IndexOutOfBoundsException();

      return null;
   }

   public int depth() { return 0; }
}
