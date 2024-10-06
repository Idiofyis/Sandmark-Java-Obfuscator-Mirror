package sandmark.watermark.assignlv;

public class ToArray {

   public ToArray() {}

   public ClassNameMethodBundle[] myToArray(java.util.ArrayList list){

      ClassNameMethodBundle[] bundleArray = 
       new ClassNameMethodBundle[list.size()];

      for(int i=0; i < list.size(); i++){
         bundleArray[i] = (ClassNameMethodBundle)list.get(i);
      }

      return bundleArray;
   }
}

