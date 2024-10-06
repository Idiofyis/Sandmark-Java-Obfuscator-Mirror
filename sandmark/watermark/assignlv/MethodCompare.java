package sandmark.watermark.assignlv;

public class MethodCompare implements java.util.Comparator {

   private boolean DEBUG = false;
   public MethodCompare(){}

   public int compare(Object o1, Object o2){
     
      int retVal = 0;

      sandmark.program.Method m1 = (sandmark.program.Method)o1;
      sandmark.program.Method m2 = (sandmark.program.Method)o2;

      String m1Sig = m1.getSignature();
      if(DEBUG)System.out.println("Signature 1: " + m1Sig);
      String m2Sig = m2.getSignature();
      if(DEBUG)System.out.println("Signature 2: " + m2Sig);

      if(m1Sig.compareTo(m2Sig) < 0)
         retVal = -1;

      if(m1Sig.compareTo(m2Sig) > 0)
         retVal = 1;

      if(m1Sig.compareTo(m2Sig) == 0){

         //org.apache.bcel.classfile.Code m1code = m1.getCode();
         org.apache.bcel.generic.InstructionList m1il = 
            m1.getInstructionList();
         int m1Length = m1il.getLength();

         //org.apache.bcel.classfile.Code m2code = m2.getCode();
         org.apache.bcel.generic.InstructionList m2il = 
            m2.getInstructionList();
         int m2Length = m2il.getLength();

         if(m1Length < m2Length){
            retVal = -1;
         }

         if(m1Length > m2Length){
            retVal = 1;
         }

         if(m1Length == m2Length){
            //System.out.println("method lengths are the same");
            org.apache.bcel.generic.InstructionList il1 =
               m1.getInstructionList();
            org.apache.bcel.generic.InstructionList il2 =
               m2.getInstructionList();
            org.apache.bcel.generic.InstructionHandle ih1 =
               il1.getStart();
            org.apache.bcel.generic.InstructionHandle ih2 =
               il2.getStart();
            String sih1 = ih1.toString();
            String sih2 = ih2.toString();
            int hash1 = sih1.hashCode();
            int hash2 = sih2.hashCode();

            if(hash1 < hash2)
               retVal = -1;

            if(hash1 > hash2)
               retVal = 1;
 
            if(hash1 == hash2){
               String m1Name = m1.getName();
               String m2Name = m2.getName();

               retVal = m1Name.compareTo(m2Name);
               //System.out.println("m1: " + m1Name);
               //System.out.println("m2: " + m2Name);
               //System.out.println("retVal1: " + retVal);

               if(retVal == 0){
                  String m1ClassName = m1.getClassName();
                  String m2ClassName = m2.getClassName();
                  //System.out.println("all the way in here");
                  retVal = m1ClassName.compareTo(m2ClassName);
                  //System.out.println("retVal: " + retVal);
               }
            }

         }
      }
      return retVal;
   }

/*
   public boolean equals(Object o){
      ClassNameMethodBundle c1 = ClassNameMethodBundle.this;
      ClassNameMethodBundle c2 = (ClassNameMethodBundle)o;

      sandmark.program.Method m1 = c1.getMethod();
      sandmark.program.Method m2 = c2.getMethod();

      String sig1 = m1.getSignature();
      String sig2 = m2.getSignature();

      org.apache.bcel.classfile.Code m1code = m1.getCode();
      int m1Length = m1code.getLength();

      org.apache.bcel.classfile.Code m2code = m2.getCode();
      int m2Length = m2code.getLength();

      org.apache.bcel.generic.InstructionList il1 =
         m1.getInstructionList();
      org.apache.bcel.generic.InstructionList il2 =
         m2.getInstructionList();
      org.apache.bcel.generic.InstructionHandle ih1 =
         il1.getStart();
      org.apache.bcel.generic.InstructionHandle ih2 =
         il2.getStart();
      String sih1 = ih1.toString();
      String sih2 = ih2.toString();
      int hash1 = sih1.hashCode();
      int hash2 = sih2.hashCode();

      String c1 = m1.getClassName();
      String c2 = m2.getClassName();

      if((sig1.compareTo(sig2) == 0) && 
         (m1Length == m2Length) &&
         (hash1 == hash2) &&
         (c1.compareTo(c2) == 0))
         return true;
      else 
         return false;


   }
*/
/*
   public boolean equals(Object o){

      org.apache.bcel.classfile.Method m = (org.apache.bcel.classfile.Method) o;
      org.apache.bcel.classfile.Code m1code = org.apache.bcel.classfile.Method.this.getCode();
      int m1Length = m1code.getLength(); 

      org.apache.bcel.classfile.Code mcode = m.getCode();
      int mLength = mcode.getLength();

      if(m1Length == mLength){
         return true;
      }else{
         return false;
      }

   }
*/
} //class methodCompare

