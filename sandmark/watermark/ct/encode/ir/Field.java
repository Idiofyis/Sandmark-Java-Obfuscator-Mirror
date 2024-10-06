package sandmark.watermark.ct.encode.ir;

public class Field extends sandmark.watermark.ct.encode.ir.IR {
   public String name;
   public String type;
   public boolean Static;

   public Field (String name, String type, boolean Static){
       this.name = name;
       this.type = type;
       this.Static = Static;
   }

   public String toString(String indent)  {
      return indent + "Field("  + name  +  ","  + type + ","  + Static + ")";
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      String[] sattributes = {"public","static"};
      String[] dattributes = {"public"};
      String[] attributes  = {};

      if (Static)
         attributes = sattributes;
      else 
         attributes = dattributes;

      sandmark.util.javagen.Field field =
	  new sandmark.util.javagen.Field(
	     name,
             type,
             attributes);
      return field;
   }

    public boolean equals(java.lang.Object f) {
	sandmark.watermark.ct.encode.ir.Field F = (sandmark.watermark.ct.encode.ir.Field) f;
        return (name.equals(F.name) && type.equals(F.type) && (F.Static==Static));
    }

    public int hashCode() {
	return name.hashCode() + type.hashCode() + ((Static)?1:0);
    }
}



