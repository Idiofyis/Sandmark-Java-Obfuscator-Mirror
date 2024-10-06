package sandmark.wizard.executive;

public class DAGWatermark extends sandmark.wizard.Wizard{

   private sandmark.program.Application app;
   private String[] watermarks;
   private String key;

   private sandmark.watermark.StaticEmbedParameters params;
   private java.util.ArrayList appliedWM;
   private sandmark.Algorithm[] watermarkers;
   private sandmark.wizard.modeling.wmdag.WMDAG dag;

   private sandmark.wizard.modeling.Choice myLastChoice;

   private int watermarkChoice;
   private int index;

   public DAGWatermark(sandmark.program.Application app,
                          String[] watermarks,
                          String key){
      this.app = app;
      this.watermarks = watermarks;
      this.key = key;

      params = new sandmark.watermark.StaticEmbedParameters();

      appliedWM = new java.util.ArrayList();

      sandmark.wizard.ObjectProvider op = 
         new sandmark.wizard.AppProvider();
      op.addObject(app);
      sandmark.wizard.AlgorithmProvider ap =
         new sandmark.wizard.StaticWatermarkProvider();
      sandmark.wizard.evaluation.Evaluator e =
         new sandmark.wizard.evaluation.FixedChange(0.0f);
      dag = new sandmark.wizard.modeling.wmdag.WMDAG();
      dag.filter(op);
      dag.filter(ap);
      sandmark.wizard.ChoiceRunner r = new sandmark.wizard.ChoiceRunner();
      e.init(dag,r);
      dag.init(e,r,op,ap);
      watermarkers = dag.getSequence();

      watermarkChoice = 0;
      index = 0;
   }

   public boolean step(){

      if(index == watermarkers.length)
         return false;

      sandmark.Algorithm swm = watermarkers[index];
      sandmark.wizard.modeling.Choice choice = new
         sandmark.wizard.modeling.Choice(swm, app);
      params.app = app;
      params.watermark = watermarks[watermarkChoice];
      params.key = key;

      watermarkChoice = (watermarkChoice + 1) % watermarks.length;
      

      try{
         sandmark.watermark.StaticEmbed.runEmbed(swm, params);
      }catch(Exception e){
         sandmark.util.Log.message(sandmark.util.Log.INTERNAL_EXCEPTION,
                                      "Running " + choice + " caused", e);
         e.printStackTrace();
      }

      appliedWM.add(watermarkers[index]);
      index++;
      myLastChoice = choice;
      return true;
   }

   public sandmark.wizard.modeling.Choice getLastChoice(){
      return myLastChoice;
   }

   public java.util.ArrayList getAppliedWatermarkers(){
      return appliedWM;
   }

   public static void main(String[] args) throws Exception{

      sandmark.program.Application app =
         new sandmark.program.Application(args[0]);
      String[] watermarks = {"wm1", "wm2", "wm3", "wm4", 
                             "wm5", "wm6", "wm7", "wm8", 
                             "wm9", "wm10", "wm11", "wm12", 
                             "wm13"};
      String key = "10";

      sandmark.wizard.executive.DAGWatermark exec =
         new sandmark.wizard.executive.DAGWatermark(app, watermarks, key);

      while(exec.step())
         System.out.println("Watermarking");

      System.out.println("Done Watermarking!\n Applied:");
      java.util.ArrayList applied = exec.getAppliedWatermarkers();
      for(int i = 0; i < applied.size(); i++){
         sandmark.watermark.StaticWatermarker swm = 
            (sandmark.watermark.StaticWatermarker)applied.get(i);
         System.out.println(swm.getShortName());
      }
      
   }
}
