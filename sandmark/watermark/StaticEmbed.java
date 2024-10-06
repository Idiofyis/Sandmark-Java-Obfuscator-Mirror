package sandmark.watermark;

public class StaticEmbed {
    public static void runEmbed(sandmark.Algorithm alg,
                                StaticEmbedParameters params) throws Exception {
       try {
	    ((StaticWatermarker)alg).embed(params);
          sandmark.util.Log.message(0,"Done embedding the watermark!");
	} catch (WatermarkingException e) {
         sandmark.util.Log.message(0,"Embedding failed: " + e);
       }
    }
} // class Embed
