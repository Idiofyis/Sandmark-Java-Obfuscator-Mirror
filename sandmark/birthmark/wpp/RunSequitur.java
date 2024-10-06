package sandmark.birthmark.wpp;

public class RunSequitur {

   int annotationPoints[];
   String trace;
   String rules;

   public RunSequitur(String trace){
      this.trace = trace;
      rules = "";
   }

   public RunSequitur(int[] annoPoints){
      annotationPoints = annoPoints;
      rules = "";
   }

   public void runSequitur(){
      rule firstRule = new rule();

      // Reset number of rules and Hashtable.

      rule.numRules = 0;
      symbol.theDigrams.clear();
      //for (i=0; i < trace.length(); i++){
      for(int i=0; i < annotationPoints.length; i++){
         //firstRule.last().insertAfter(new terminal(trace.charAt(i)));
         firstRule.last().insertAfter(new terminal(annotationPoints[i]));
         firstRule.last().p.check();
      }
      rules += firstRule.getRules();
   }

   public String getRules(){
      return rules;
   }

   public static void main(String[] argv){
      int[] annoPoints = {2345, 345, 574, 2345, 345, 574};
      RunSequitur seq = new RunSequitur(annoPoints);
      seq.runSequitur();
      String finalRules = seq.getRules();
      System.out.println(finalRules);
   }
}
