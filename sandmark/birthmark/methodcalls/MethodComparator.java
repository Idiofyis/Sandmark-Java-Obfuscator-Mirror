package sandmark.birthmark.methodcalls;

public class MethodComparator implements java.util.Comparator {

   public int compare(Object o1, Object o2){
      sandmark.program.Method m1 = (sandmark.program.Method)o1;
      sandmark.program.Method m2 = (sandmark.program.Method)o2;

      int m1ListLength = 0;
      int m2ListLength = 0;

      if(m1.getInstructionList() != null)
         m1ListLength = m1.getInstructionList().getLength();

      if(m2.getInstructionList() != null)
         m2ListLength = m2.getInstructionList().getLength();

      if((m1.getSignature().equals(m2.getSignature()) && 
         m1ListLength == m2ListLength))
         return 0;
      
      int sigComp = m1.getSignature().compareTo(m2.getSignature());

      if(sigComp == 0)
         return m1ListLength < m2ListLength ? -1 : 1;
      else
         return sigComp;

   }

/*
   public boolean equals(Object o){
      sandmark.program.Method m = (sandmark.program.Method)o;

      int thisListLength = 0;
      int mListLength = 0;
      if(this.getInstructionList() != null)
         thisListLength = this.getInstructionList().getLength();

      if(m.getInstructionList() != null)
         mListLength = m.getInstructionList().getLength();

      if(this.getSignature().equals(m.getSignature()) && 
         thisListLength == mListLength)
         return true;
      else 
         return false;
   }
*/
}
