package sandmark.watermark;

public class StaticRecognize {
    public static java.util.Iterator runRecognition
	(sandmark.Algorithm alg,StaticRecognizeParameters params) throws java.io.IOException {
       try {
	    return ((StaticWatermarker)alg).recognize(params);
	} catch (WatermarkingException e) {
         sandmark.util.Log.message(0,"Recognition failed: " + e);
         e.printStackTrace();
       }
	return null;
    }
}

