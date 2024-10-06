package sandmark.watermark.assignlv;

public class ToBinary {

   private String bMark = "";

   public ToBinary(String wm, boolean addLength){

      String bwm = convert(wm);

      if(addLength){
         int len = bwm.length();
         String slen = "";
         slen += len;
         String blen = convertLength(slen);
         bMark = blen + bwm;
      }else{
         bMark += bwm;
      }

   }

   public String getBinary(){

      return bMark;
   }


   /**
    * precondition: length must be a power of 2.
    */
   private String convertLength(String length){
      //System.out.println("length comming into convertLength: " + length);
      int power = 0;
      Integer iLength = new Integer(length);
      int divisor = iLength.intValue();
      //System.out.println("divisor: " + divisor);
      String blen = "00000000";

      while(divisor != 1){
         divisor = divisor / 2;
         power++;
      }
      //System.out.println("power: " + power);
      String startOfString = blen.substring(0, 8-power);
      //System.out.println("start of string: " + startOfString);
      String endOfString = blen.substring((8-power)+1);
      //System.out.println("end of string: " + endOfString);
      endOfString = "1" + endOfString;
      blen = startOfString + endOfString;
   
      return blen;

   }

   private String convert(String wmString){
      String binaryVersion = "";

      //we have to go through each character and change it to it binary
      for(int i=0; i < wmString.length(); i++){
         int currentByte = (int) wmString.charAt(i);

         if(currentByte >= 128){
            binaryVersion += 1;
            currentByte -= 128;
         }else{
            binaryVersion += 0;
         }

         if(currentByte >= 64){
            binaryVersion += 1;
            currentByte -= 64;
         }else{
            binaryVersion += 0;
         }

         if(currentByte >= 32){
            binaryVersion += 1;
            currentByte -= 32;
         }else{
            binaryVersion += 0;
         }

         if(currentByte >= 16){
            binaryVersion += 1;
            currentByte -= 16;
         }else{
            binaryVersion += 0;
         }

         if(currentByte >= 8){
            binaryVersion += 1;
            currentByte -= 8;
         }else{
            binaryVersion += 0;
         }

         if(currentByte >= 4){
            binaryVersion += 1;
            currentByte -= 4;
         }else{
            binaryVersion += 0;
         }

         if(currentByte >= 2){
            binaryVersion += 1;
            currentByte -= 2;
         }else{
            binaryVersion += 0;
         }

         if(currentByte >= 1){
            binaryVersion += 1;
            currentByte -= 1;
         }else{
            binaryVersion += 0;
         }
      }
      return binaryVersion;
   }
} // class ToBinary

