package sandmark.watermark.arboit;

public class Bundle {

   private String name = "";
   private sandmark.program.Method method = null;
   private java.util.ArrayList indexList = null;

   public Bundle(String name, sandmark.program.Method method, java.util.ArrayList indexList){
      this.name = name;
      this.method = method;
      this.indexList = indexList;
   }

   public sandmark.program.Method getMethod(){
      return method;
   }

   public String getClassName(){
      return name;
   }

   public java.util.ArrayList getIndexList(){
      return indexList;
   }

   public String toString(){
      String s =
      "Slot: contains class " + this.name + " and method " + this.method.getName() + "\n"
      + "indexList: " + indexList.toString();
      return s;
   }

   public boolean equals(Object e){
      Bundle b = (Bundle)e;
      String thisMName = (this.getMethod()).getName();
      String bMName = (b.getMethod()).getName();
      String thisCName = this.getClassName();
      String bCName = b.getClassName();
      if(thisMName.equals(bMName) && thisCName.equals(bCName))
         return true;
      else
         return false;
   }
}
