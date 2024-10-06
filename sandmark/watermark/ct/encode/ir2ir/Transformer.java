package sandmark.watermark.ct.encode.ir2ir;

/**
 *  sandmark.watermark.ct.encode.ir2ir.Transformer is the base-class
 *  for various classes that transform intermediate code,
 *  i.e. sandmark.watermark.ct.encode.ir.
 */

public abstract class Transformer {

   sandmark.watermark.ct.encode.ir.Build orig;
   sandmark.util.ConfigProperties props;

    public Transformer (
      sandmark.watermark.ct.encode.ir.Build orig, 
      sandmark.util.ConfigProperties props) {
	this.orig = orig;
        this.props = props;
    }

   public abstract sandmark.watermark.ct.encode.ir.Build mutate ();
}

