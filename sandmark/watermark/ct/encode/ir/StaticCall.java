package sandmark.watermark.ct.encode.ir;

public class StaticCall 
   extends sandmark.watermark.ct.encode.ir.IR {
   String Class;
   String name;
   String type;
   sandmark.watermark.ct.encode.ir.List args;

   public StaticCall (
      String Class, 
      String name, 
      String type, 
      sandmark.watermark.ct.encode.ir.List args){
      this.Class = Class;
      this.name = name;
      this.type = type;
      this.args = args;
   }

   public String toString(String indent)  {
      String P = indent + 
                 Class + "." + name +
                "(" + renderListSeparate(args, ", ", indent) + ")" ;
      return P;
   }


   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
       sandmark.util.javagen.List args = new sandmark.util.javagen.List();
       //       java.util.Iterator iter = args.iterator();
       //       while (iter.hasNext()) {
       //          sandmark.util.javagen.Expression s = (sandmark.util.javagen.Expression) iter.next();
       //       };

      return new sandmark.util.javagen.StaticCall(Class, name, type, args);
   }
}



