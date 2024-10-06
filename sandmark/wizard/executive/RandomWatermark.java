package sandmark.wizard.executive;

public class RandomWatermark extends sandmark.wizard.Wizard{

   private sandmark.program.Application app;
   private String[] watermarks;
   private String key;

   private sandmark.watermark.StaticEmbedParameters params;
   private sandmark.util.Random myRandom;
   private java.util.ArrayList myPostProhibits;
   private java.util.ArrayList appliedWM;
   private java.util.ArrayList watermarkers;

   private sandmark.wizard.modeling.Choice myLastChoice;

   private int watermarkChoice;

   public RandomWatermark(sandmark.program.Application app,
                          String[] watermarks,
                          String key){
      this.app = app;
      this.watermarks = watermarks;
      this.key = key;

      params = new sandmark.watermark.StaticEmbedParameters();

      long seed;
      if(key == null || key.equals("")){
         seed = 42;
      }else{
         java.math.BigInteger bigIntKey = sandmark.util.StringInt.encode(key);
         seed = bigIntKey.longValue();
      }
      myRandom = sandmark.util.Random.getRandom();
      myRandom.setSeed(seed);

      myPostProhibits = new java.util.ArrayList();
      appliedWM = new java.util.ArrayList();
      watermarkers = new java.util.ArrayList();

      watermarkChoice = 0;

      sandmark.watermark.StaticWatermarker[] swms =
         sandmark.wizard.modeling.Util.getStaticWatermarkers();
      for(int i = 0; i < swms.length; i++)
         watermarkers.add(swms[i]);
      
   }

   public boolean step(){

      if(watermarkers.size() == 0)
         return false;

      int currentChoice = myRandom.nextInt(watermarkers.size());
      sandmark.watermark.StaticWatermarker swm =
         (sandmark.watermark.StaticWatermarker)watermarkers.get(currentChoice);
      sandmark.config.ModificationProperty[] mutationProps = swm.getMutations();

      boolean apply = true;
      if(mutationProps == null){
         apply = false;
      }else{
         for(int i = 0; i < mutationProps.length; i++){
            if(myPostProhibits.contains(mutationProps[i]))
               apply = false;
         }
      }

      watermarkers.remove(currentChoice);

      sandmark.wizard.modeling.Choice choice = null;
      if(apply){
         choice = new sandmark.wizard.modeling.Choice(swm, app);
         params.app = app;
         params.key = key;
         params.watermark = watermarks[watermarkChoice];

         watermarkChoice = (watermarkChoice + 1) % watermarks.length;
         
         try{
            sandmark.watermark.StaticEmbed.runEmbed(swm, params);
            
         }catch(Exception e){
            sandmark.util.Log.message(sandmark.util.Log.INTERNAL_EXCEPTION,
                                      "Running " + choice + " caused", e);
            e.printStackTrace();
         }

         sandmark.config.RequisiteProperty[] posts = swm.getPostprohibited();
         if(posts != null){
            for(int i = 0; i < posts.length; i++)
               myPostProhibits.add(posts[i]);
            appliedWM.add(swm);
         }
      }

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

      sandmark.wizard.executive.RandomWatermark exec =
         new sandmark.wizard.executive.RandomWatermark(app, watermarks, key);

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
