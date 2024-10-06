package sandmark.util.newexprtree;

public class BasicBlock{
   java.util.ArrayList instList;

   BasicBlock fallthroughTo;
   BasicBlock fallthroughFrom;
   ExprTreeCFG graph;

   public BasicBlock(ExprTreeCFG _graph){
      graph = _graph;
      instList = new java.util.ArrayList();
   }

   public ExprTreeCFG graph(){
      return graph;
   }

   public java.util.ArrayList getInstList(){
      return instList;
   }

   public void addExpr(Expr expr){
      instList.add(expr);
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

   public Expr getExpr(){
      if (instList.size()>0)
         return (Expr)instList.get(0);
      return null;
   }
}
