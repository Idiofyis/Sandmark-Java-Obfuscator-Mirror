/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard;

/**
 * @author ash
 *
 * AlgorithmProvider that provides sandmark static watmerking algorithms only.
 */
public class StaticWatermarkProvider extends DefaultAlgorithmProvider {
   static {
      java.util.ArrayList algs = new java.util.ArrayList 
         (sandmark.util.classloading.ClassFinder.getClassesWithAncestor
          (sandmark.util.classloading.IClassFinder.STAT_WATERMARKER));
      for(java.util.Iterator it = algs.iterator() ; it.hasNext() ; ) {  
         try {
            sAlgs.add(Class.forName((String)it.next()).newInstance());
         } catch(Exception e) {}
      }
   }
}
