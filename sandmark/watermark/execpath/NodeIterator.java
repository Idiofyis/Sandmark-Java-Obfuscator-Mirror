package sandmark.watermark.execpath;

public class NodeIterator implements java.util.Iterator{
   private java.io.RandomAccessFile file;
   private int current;
   private java.util.List offsetList;

   public NodeIterator(java.util.List offsets, java.io.File f) throws java.io.IOException{
      file = new java.io.RandomAccessFile(f, "r");
      offsetList = offsets;
      current = 0;
   }

   public boolean hasNext(){
      return (current<offsetList.size());
   }

   public Object next(){
      long nextOffset = ((Long)offsetList.get(current)).longValue();

      TraceNode node = null;
      try{
         file.seek(nextOffset);
         node = new TraceNode(file.readLine(), "");
         current++;
      }catch(Exception ex){
         node = null;
      }
      return node;
   }

   public void remove(){ throw new UnsupportedOperationException(); }
}
