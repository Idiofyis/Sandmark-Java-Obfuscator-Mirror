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
 * ObjectProvider that provides sandmark Objects and allows
 * adding, removing, etc.
 */
public class DefaultObjectProvider implements ObjectProvider {
   protected java.util.ArrayList mObjects = new java.util.ArrayList();
   private int mCursor;
   public void addObject(sandmark.program.Object o) {
      if(!mObjects.contains(o))
         mObjects.add(o);
      for(java.util.Iterator members = o.members() ; members.hasNext() ; )
         addObject((sandmark.program.Object)members.next());
   }
   public boolean hasNext() { return mCursor < mObjects.size(); }
   public Object next() { return mObjects.get(mCursor++); }
   public void remove() {
      mObjects.remove(--mCursor);
   }
   public void reset() { mCursor = 0; }
   public sandmark.program.Object [] getObjects() {
      return (sandmark.program.Object [])mObjects.toArray
         (new sandmark.program.Object [0]);
   }
   public void removeObject(sandmark.program.Object o) {
      for(java.util.Iterator members = o.members() ; members.hasNext() ; )
         removeObject((sandmark.program.Object)members.next());
      mObjects.remove(o);
   }
}
