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
 * AlgorithmProvider that provides sandmark Algorithms that are not
 * dynamic watermarks or birthmarks (i.e. obfuscations, static watermarks, etc.)
 */
public class DefaultAlgorithmProvider implements AlgorithmProvider {
   protected static java.util.ArrayList sAlgs = new java.util.ArrayList();
   private java.util.ArrayList mAlgs = (java.util.ArrayList)sAlgs.clone();
   int mCursor;
   static {
      java.util.ArrayList algs = new java.util.ArrayList 
         (sandmark.util.classloading.ClassFinder.getClassesWithAncestor
          (sandmark.util.classloading.IClassFinder.ALGORITHM));
      algs.removeAll
         (sandmark.util.classloading.ClassFinder.getClassesWithAncestor
          (sandmark.util.classloading.IClassFinder.GEN_BIRTHMARK));
      algs.removeAll
         (sandmark.util.classloading.ClassFinder.getClassesWithAncestor
          (sandmark.util.classloading.IClassFinder.DYN_WATERMARKER));
      for(java.util.Iterator it = algs.iterator(); it.hasNext(); ) {
	 try {
            sAlgs.add(Class.forName((String)it.next()).newInstance());
         } catch(Exception e) {}
      }
   }
   public sandmark.Algorithm[] getAlgorithms() 
   { return (sandmark.Algorithm [])mAlgs.toArray(new sandmark.Algorithm[0]); }
   public void reset() { mCursor = 0; }
   public boolean hasNext() { return mCursor < mAlgs.size(); }
   public Object next() { return mAlgs.get(mCursor++); }
   public void remove() { mAlgs.remove(--mCursor); }
}
