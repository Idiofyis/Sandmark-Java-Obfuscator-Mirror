package sandmark.watermark.ct.encode.ir;

public class Formal extends sandmark.watermark.ct.encode.ir.IR {
   public String name;
   public String type;

   public Formal (String name, String type){
       this.name = name;
       this.type = type;
   }

   public String toString(String indent)  {
      return indent + "Formal("  + name  +  ","  + type + ")";
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      return new sandmark.util.javagen.Formal(name,type);
   }

    public boolean equals(java.lang.Object f) {
	sandmark.watermark.ct.encode.ir.Formal F = (sandmark.watermark.ct.encode.ir.Formal) f;
        return (name.equals(F.name) && type.equals(F.type));
    }

    public int hashCode() {
	return name.hashCode() + type.hashCode();
    }
}



