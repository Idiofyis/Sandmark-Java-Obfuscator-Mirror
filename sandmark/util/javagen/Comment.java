package sandmark.util.javagen;

public class Comment
   extends sandmark.util.javagen.Statement {
   String code;

   public Comment (String code){
      this.code = code;
   }

   public String toString(String indent)  {
     String P = commentText(code, indent);
     return P;
   }

   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg,
      org.apache.bcel.generic.MethodGen mg) {
   }

      public void toCode(
      	           sandmark.program.Class cg,
          sandmark.program.Method mg) {
	}
}



