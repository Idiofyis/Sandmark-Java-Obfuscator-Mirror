package sandmark.analysis.controlflowgraph;

/**
 * BasicBlock represents a basic block in a control flow graph. Some of 
 * the ideas in this class are based upon the BLOAT project from Purdue.
 *
 * Written by Ginger Myles, 6/6/02.
 */
public class BasicBlock implements sandmark.util.newgraph.LabeledNode {
   java.util.ArrayList instructions; //This block's list of instructions

   BasicBlock fallthroughTo;
   BasicBlock fallthroughFrom;

   MethodCFG graph;

   public int mBlockNum;

   public BasicBlock(MethodCFG graph) {
      this.graph = graph;
      instructions = new BBArrayList(this);
      fallthroughTo = fallthroughFrom = null;
      mBlockNum = graph.mBlockCounter++;
   }

   public MethodCFG graph() {
      return graph;
   }

   public void setFallthrough(BasicBlock fallthrough) {
      if(fallthroughTo != null)
         fallthroughTo.fallthroughFrom = null;
      fallthroughTo = fallthrough;
      if(fallthrough != null)
         fallthrough.fallthroughFrom = this;
   }
    
   public BasicBlock fallthrough() {
      return fallthroughTo;
   }

   public BasicBlock fallthroughFrom() {
      return fallthroughFrom;
   }

   /**
    * Returns the instruction handler associated with this block.
    *
    * @return The instruction handle.
    */
   public org.apache.bcel.generic.InstructionHandle getIH(){
      return instructions.size() > 0 ? 
         (org.apache.bcel.generic.InstructionHandle)instructions.get(0) :
         null;
   }

   /**
    * Returns the instruction handler associated with the last 
    * instruction in the block.
    *
    * @return The instruction handle.
    */
   public org.apache.bcel.generic.InstructionHandle getLastInstruction(){
      int listSize = instructions.size();
      return listSize > 0 ? (org.apache.bcel.generic.InstructionHandle)instructions.get(listSize - 1) : null;
   }

   public boolean containsIH(
                             org.apache.bcel.generic.InstructionHandle searchHandle){
      return instructions.contains(searchHandle);
   }

   /**
    * Adds an instruction to this basic block's instruction list.
    */
   public void addInst(org.apache.bcel.generic.InstructionHandle inst){
      instructions.add(inst);
   }

   /**
    * Returns this blocks instruction list.
    *
    * @return The instruction list.
    */
   public java.util.ArrayList getInstList(){
      return instructions;
   }

   /**
    * Returns a string representation of the block.
    */
   public String toString(int limit){
      String s = "<block " + mBlockNum;
      MethodCFG cfg = graph();      
    
      if(this == cfg.source()){ 
         return s += " source>";
      }else if(this == cfg.sink()){
         return s += " sink>";
      }else{
         s += "\n";
         if(limit != -1 && instructions.size() > limit) {
            s += instructions.get(0);
            s += "\n ... \n";
            s += instructions.get(instructions.size() - 1);
            s += "\n";
         } else {
            for(int i=0; i<instructions.size(); i++){
               s += instructions.get(i);
               s += "\n";
            }
         }
         return s + ">";
      }
   }
   
   public String toString() { return toString(5); }

   public int hashCode() { return mBlockNum; }
    
   public String getLongLabel() {
      return toString(-1);//.replaceAll("\n","\\l");
   }
   public String getShortLabel() { 
      return toString();//.replaceAll("\n","\\l"); 
   }

   /////////////////////////////////////////////////////

   private static class BBArrayList extends java.util.ArrayList{
      private BasicBlock BB;

      public BBArrayList(BasicBlock bb){
         BB = bb;
      }

      public void add(int index, Object o){
         if (index>=0 && index<=size() && o!=null){
            BB.graph.instr2bb.put(o, BB);
         }
         super.add(index,o);
      }

      public boolean add(Object o){
         if (o!=null){
            BB.graph.instr2bb.put(o,BB);
         }
         return super.add(o);
      }

      public boolean addAll(java.util.Collection c){
         if (c!=null){
            for (java.util.Iterator iter=c.iterator();iter.hasNext();){
               BB.graph.instr2bb.put(iter.next(), BB);
            }
         }
         return super.addAll(c);
      }

      public boolean addAll(int index, java.util.Collection c){
         if (c!=null && index>=0 && index<=size()){
            for (java.util.Iterator iter=c.iterator();iter.hasNext();){
               BB.graph.instr2bb.put(iter.next(), BB);
            }
         }
         return super.addAll(index, c);
      }

      public void clear(){
         for (java.util.Iterator iter=iterator();iter.hasNext();){
            BB.graph.instr2bb.remove(iter.next());
         }
         super.clear();
      }

      public Object remove(int index){
         if (index>=0 && index<size()){
            BB.graph.instr2bb.remove(get(index));
         }
         return super.remove(index);
      }

      protected void removeRange(int fromIndex, int toIndex){
         if (fromIndex>=0 && fromIndex<size() && 
             toIndex>=fromIndex && toIndex<=size()){
            for (int i=fromIndex;i<toIndex;i++){
               BB.graph.instr2bb.remove(get(i));
            }
         }
         
         super.removeRange(fromIndex, toIndex);
      }

      public Object set(int index, Object element){
         if (index>=0 && index<size()){
            BB.graph.instr2bb.remove(get(index));
            BB.graph.instr2bb.put(element, BB);
         }
         return super.set(index, element);
      }

      public boolean remove(Object o){
         boolean result = super.remove(o);
         if (result){
            BB.graph.instr2bb.remove(o);
         }
         return result;
      }

      public boolean removeAll(java.util.Collection c){
         if (c!=null){
            for (java.util.Iterator iter=c.iterator();iter.hasNext();){
               Object obj = iter.next();
               if (contains(obj)){
                  BB.graph.instr2bb.remove(obj);
               }
            }
         }
         return super.removeAll(c);
      }

      public boolean retainAll(java.util.Collection c){
         if (c!=null){
            for (java.util.Iterator iter=iterator();iter.hasNext();){
               Object obj= iter.next();
               if (!c.contains(obj)){
                  BB.graph.instr2bb.remove(obj);
               }
            }
         }
         return super.retainAll(c);
      }
   }
}

