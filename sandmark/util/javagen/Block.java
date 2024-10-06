package sandmark.util.javagen;

public class Block
   extends sandmark.util.javagen.Statement {
   sandmark.util.javagen.List stats;

   public Block (sandmark.util.javagen.List stats){
      this.stats = stats;
   }

   public String toString(String indent)  {
      return stats.toString(indent);
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
      java.util.Iterator siter = stats.iterator();
      while (siter.hasNext()) {
          sandmark.util.javagen.Statement s = (sandmark.util.javagen.Statement) siter.next();
          s.toByteCode(cg,mg);
      };


   }
   public void toCode(
   	      sandmark.program.Class cg,
          sandmark.program.Method mg) {
         java.util.Iterator siter = stats.iterator();
         while (siter.hasNext()) {
             sandmark.util.javagen.Statement s = (sandmark.util.javagen.Statement) siter.next();
             s.toCode(cg,mg);
         };


   }
}



