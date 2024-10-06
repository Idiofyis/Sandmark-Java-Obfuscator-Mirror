package sandmark.watermark.assignlv;

public class FromBinary {

   private String mark = "";

   public FromBinary(String binary, boolean isLength){
   
      if(isLength){
         mark = convertLength(binary);
      }else{
         mark = convert(binary);
      }

   }

   public String getString(){

      return mark;

   }

   public String convertLength(String binary){
      int pos = -1;

      for(int i = 0; i < binary.length(); i++){
         String c = "";
         c += binary.charAt(i);
         if(c.compareTo("1") == 0){
            pos = i;
            break;
         }
      }

      int power = 8-pos;
      //System.out.println("power: " + power);
      int length = (int)Math.pow(2, power);
      //System.out.println("length: " + length);
      Integer iLength = new Integer(length);
      //System.out.println("iLength: " + iLength);
      String slength = iLength.toString();

      return slength;
   }

   private String convert(String binary){
      String stringVersion = "";
 
      //each set of 8 bits is one character
      //so we have to convert each 8 bit set into a character
      //System.out.println("binary length: " + binary.length());
      for(int i=0; i < binary.length(); i++){
         int currentByte = 0;
         if(binary.charAt(i) == '1'){
            currentByte += 128;
         }
         i++;
         if(binary.charAt(i) == '1'){
            currentByte += 64;
         }
         i++;
         if(binary.charAt(i) == '1'){
            currentByte += 32;
         }
         i++;
         if(binary.charAt(i) == '1'){
            currentByte += 16;
         }
         i++;
         if(binary.charAt(i) == '1'){
            currentByte += 8;
         }
         i++;
         if(binary.charAt(i) == '1'){
            currentByte += 4;
         }
         i++;
         if(binary.charAt(i) == '1'){
            currentByte += 2;
         }
         i++;
         if(binary.charAt(i) == '1'){
            currentByte += 1;
         }
         stringVersion += (char) currentByte;
      }
      return stringVersion;

   }

} //class FromBinary

