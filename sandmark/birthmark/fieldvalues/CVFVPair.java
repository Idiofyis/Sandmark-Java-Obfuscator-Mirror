package sandmark.birthmark.fieldvalues;

public class CVFVPair implements java.util.Comparator{

   private String initValue;
   private String type;

   public CVFVPair(String initValue, String type){
      this.initValue = initValue;
      this.type = type;
   }

   public String getInitValue(){
      return initValue;
   }

   public String getType(){
      return type;
   }

   public boolean equals(Object o){
      CVFVPair p = (CVFVPair)o;
      if(this.getInitValue().equals(p.getInitValue()) &&
         this.getType().equals(p.getType()))
         return true;
      else
         return false;
   }

   public int compare(Object o1, Object o2){
      CVFVPair p1 = (CVFVPair)o1;
      CVFVPair p2 = (CVFVPair)o2;
      return p1.getType().compareTo(p2.getType());
   }

   public String toString(){
      return "(" + type + ", " + initValue + ")";
   }
}
