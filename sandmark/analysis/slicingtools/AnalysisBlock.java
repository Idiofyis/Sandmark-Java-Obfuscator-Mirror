package sandmark.analysis.slicingtools;

public class AnalysisBlock extends sandmark.util.newexprtree.ExprTreeBlock {
   
	private java.util.ArrayList in;
	private java.util.ArrayList out;
	private java.util.ArrayList gen;
	private java.util.ArrayList kill;

	public AnalysisBlock(sandmark.util.newexprtree.MethodExprTree met,
sandmark.util.newexprtree.ExprTreeBlock block){
      super(met, block.getBasicBlock());
      in = new java.util.ArrayList();
      out = new java.util.ArrayList();
      gen = new java.util.ArrayList();
      kill = new java.util.ArrayList();

	}

   public java.util.ArrayList getIn(){
      return in;
   }

   public java.util.ArrayList getOut(){
      return out;
   }

   public java.util.ArrayList getGen(){
      return gen;
   }

   public java.util.ArrayList getKill(){
      return kill;
   }

   public void setIn(java.util.ArrayList in){
      this.in = in;
   }

   public void setOut(java.util.ArrayList out){
      this.out = out;
   }

   public void setGen(java.util.ArrayList gen){
      this.gen = gen;
   }

   public void setKill(java.util.ArrayList kill){
      this.kill = kill;
   }

   public sandmark.analysis.controlflowgraph.BasicBlock getBB(){
      return this.bb;
   }

   public String toString(){
      String contents = "hdr: " + this.bb.getIH() + "\n";
      contents += "Gen: " + this.in + "\n";
      contents += "Kill: " + this.kill + "\n";
      contents += "In: " + this.in + "\n";
      contents += "Out: "  + this.out + "\n";

      return contents;
   }

} //end class
