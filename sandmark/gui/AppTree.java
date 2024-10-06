/*
 * Created on Mar 4, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.gui;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AppTree extends javax.swing.JTree {
   public static final int SHOW_APPS = 1 << 0;
   public static final int SHOW_CLASSES = 1 << 1;
   public static final int SHOW_METHODS = 1 << 2;
   public static final int SHOW_FIELDS = 1 << 3;
   
   private int mFlags;
   
   public AppTree(java.util.List apps,int flags,
                  int selectionType) {
      super(new AppTreeModel(apps,flags));
      setRootVisible(false);

      getSelectionModel().setSelectionMode(selectionType);

      setShowsRootHandles(true);
      setEditable(false);
      mFlags = flags;
      
      for(int i = getRowCount() - 1 ; i >= 0 ; i--)
         expandRow(i);
   }
   
   public AppTree(sandmark.program.Application app,int flags,
                  int selectType) {
      this(java.util.Arrays.asList(new sandmark.program.Application[] { app }),
           flags,selectType);
   }
   
   private static class AppTreeModel implements javax.swing.tree.TreeModel {
      private static class ModelNode {
         Object object;
         java.util.ArrayList kids = new java.util.ArrayList();
         ModelNode(Object o) { object = o; }
      }
      private static class ModelNodeComparator 
         implements java.util.Comparator {
         private java.util.Comparator mComp;
         ModelNodeComparator(java.util.Comparator c) {
            mComp = c;
         }
         public int compare(Object o1,Object o2) {
            ModelNode mn1 = (ModelNode)o1,mn2 = (ModelNode)o2;
            return mComp.compare(mn1.object,mn2.object);
         }
      }
      private ModelNode mRoot;
      private java.util.Hashtable mObjectToNode;
      private java.util.Set mListeners = new java.util.HashSet();
      AppTreeModel(java.util.List apps,int flags) {
         mObjectToNode = new java.util.Hashtable();
         Object rootObj = new Object();
         mRoot = new ModelNode(rootObj);
         mObjectToNode.put(rootObj,mRoot);
         ModelNode parent = mRoot;
         for(java.util.Iterator appIt = apps.iterator() ;
             appIt.hasNext() ; ) {
            sandmark.program.Application app =
               (sandmark.program.Application)appIt.next();
            ModelNode oldAppParent = null;
            if((flags & SHOW_APPS) != 0) {
               ModelNode appNode = new ModelNode(app);
               mObjectToNode.put(app,appNode);
               parent.kids.add(appNode);
               oldAppParent = parent;
               parent = appNode;
            }
            for(java.util.Iterator classes = app.classes() ;
                classes.hasNext() ; ) {
               sandmark.program.Class clazz =
                  (sandmark.program.Class)classes.next();
               ModelNode oldClassParent = null;
               if((flags & SHOW_CLASSES) != 0) {
                  ModelNode classNode = new ModelNode(clazz);
                  mObjectToNode.put(clazz,classNode);
                  parent.kids.add(classNode);
                  oldClassParent = parent;
                  parent = classNode;
               }
               if((flags & SHOW_METHODS) != 0)
                  for(java.util.Iterator methods = clazz.methods() ;
                      methods.hasNext() ; ) {
                     Object method = methods.next();
                     ModelNode methodNode = new ModelNode(method);
                     mObjectToNode.put(method,methodNode);
                     parent.kids.add(methodNode);
                  }
               if((flags & SHOW_FIELDS) != 0)
                  for(java.util.Iterator fields = clazz.fields() ;
                      fields.hasNext() ; ) {
                     Object field = fields.next();
                     ModelNode fieldNode = new ModelNode(field);
                     mObjectToNode.put(field,fieldNode);
                     parent.kids.add(fieldNode);
                  }
               if(oldClassParent != null)
                  parent = oldClassParent;
            }
            if(oldAppParent != null)
               parent = oldAppParent;
         }
      }
      public Object getRoot() { return mRoot.object; }
      public void addTreeModelListener
         (javax.swing.event.TreeModelListener l) {
         mListeners.add(l);
      }
      public void removeTreeModelListener
         (javax.swing.event.TreeModelListener l) {
         mListeners.remove(l);
      }
      public void valueForPathChanged(javax.swing.tree.TreePath path,
                                      Object newValue) {
         throw new Error("No editing");
      }
      public boolean isLeaf(Object node) {
         ModelNode mn = (ModelNode)mObjectToNode.get(node);
         return mn.kids.size() == 0;
      }
      public int getChildCount(Object node) {
         ModelNode mn = (ModelNode)mObjectToNode.get(node);
         return mn.kids.size();
      }
      public Object getChild(Object node,int n) {
         ModelNode mn = (ModelNode)mObjectToNode.get(node);
         return ((ModelNode)mn.kids.get(n)).object;
      }
      public int getIndexOfChild(Object parent,Object child) {
         ModelNode mn = (ModelNode)mObjectToNode.get(parent);
         return mn.kids.indexOf(mObjectToNode.get(child));
      }
      private void onModelChanged() {
         javax.swing.event.TreeModelEvent event =
            new javax.swing.event.TreeModelEvent
            (this,new Object[] { mRoot.object });
         for(java.util.Iterator listeners = mListeners.iterator() ;
             listeners.hasNext() ; )
            ((javax.swing.event.TreeModelListener)listeners.next()).
               treeStructureChanged(event);
      }
      void setAppComparator(java.util.Comparator comp) {
         for(java.util.Iterator mns = mObjectToNode.values().iterator() ;
             mns.hasNext() ; ) {
            ModelNode mn = (ModelNode)mns.next();
            if(mn.kids.size() == 0)
               continue;
            ModelNode mnKid = (ModelNode)mn.kids.get(0);
            if(mnKid.object instanceof sandmark.program.Application)
               java.util.Collections.sort
                  (mn.kids,new ModelNodeComparator(comp));
         }
         onModelChanged();
      }
      void setClassComparator(java.util.Comparator comp) {
         for(java.util.Iterator mns = mObjectToNode.values().iterator() ;
             mns.hasNext() ; ) {
            ModelNode mn = (ModelNode)mns.next();
            if(mn.kids.size() == 0)
               continue;
            ModelNode mnKid = (ModelNode)mn.kids.get(0);
            if(mnKid.object instanceof sandmark.program.Class)
               java.util.Collections.sort
                  (mn.kids,new ModelNodeComparator(comp));
         }
         onModelChanged();
      }
      void setMethodAndFieldComparator(java.util.Comparator comp) {
         for(java.util.Iterator mns = mObjectToNode.values().iterator() ;
             mns.hasNext() ; ) {
            ModelNode mn = (ModelNode)mns.next();
            if(mn.kids.size() == 0)
               continue;
            ModelNode mnKid = (ModelNode)mn.kids.get(0);
            if(mnKid.object instanceof sandmark.program.Method ||
               mnKid.object instanceof sandmark.program.Field)
               java.util.Collections.sort
                  (mn.kids,new ModelNodeComparator(comp));
         }
         onModelChanged();
      }
   }
   private static class TreeViewState {
      java.util.ArrayList expandedNodes;
      javax.swing.tree.TreePath[] selectionPaths;
      TreeViewState(java.util.Enumeration e,
                    javax.swing.tree.TreePath s[]) {
         java.util.ArrayList list = new java.util.ArrayList();
         while(e.hasMoreElements())
            list.add(e.nextElement());
         expandedNodes = list;
         selectionPaths = s;
      }
   }
   public Object saveTreeState() { 
      javax.swing.tree.TreePath rootPath = 
         new javax.swing.tree.TreePath(getModel().getRoot());
      return new TreeViewState(getExpandedDescendants(rootPath),
                               getSelectionPaths());
   }
   public void restoreTreeState(Object state) {
      TreeViewState viewState = (TreeViewState)state;
      for(java.util.Iterator expandedNodes = 
          viewState.expandedNodes.iterator() ; expandedNodes.hasNext() ; ) {
         javax.swing.tree.TreePath expandedPath =
            (javax.swing.tree.TreePath)expandedNodes.next();
         setExpandedState(expandedPath,true);
      }
      setSelectionPaths(viewState.selectionPaths);
   }
   public void sortApps(java.util.Comparator comp) {
      Object viewState = saveTreeState();
      ((AppTreeModel)getModel()).setAppComparator(comp);
      restoreTreeState(viewState);
   }
   public void sortClasses(java.util.Comparator comp) {
      Object viewState = saveTreeState();
      ((AppTreeModel)getModel()).setClassComparator(comp);
      restoreTreeState(viewState);
   }
   public void sortMethods(java.util.Comparator comp) {
      Object viewState = saveTreeState();
      ((AppTreeModel)getModel()).setMethodAndFieldComparator(comp);
      restoreTreeState(viewState);
   }
   public void selectNode(sandmark.program.Object node) {
      java.util.LinkedList pathList = new java.util.LinkedList();
      
      for(sandmark.program.Object n = node ; n != null ; n = n.getParent()) {
         if(n instanceof sandmark.program.Method &&
            (mFlags & SHOW_METHODS) != 0)
            pathList.add(n);
         else if(n instanceof sandmark.program.Field &&
                 (mFlags & SHOW_FIELDS) != 0)
            pathList.add(n);
         else if(n instanceof sandmark.program.Class &&
                 (mFlags & SHOW_CLASSES) != 0)
            pathList.add(n);
         else if(n instanceof sandmark.program.Application &&
                 (mFlags & SHOW_APPS) != 0)
            pathList.add(n);
         else
            throw new Error("Unknown object type " + n.getClass());
      }
      pathList.addFirst(getModel().getRoot());
      Object pathArr[] = pathList.toArray(new Object[0]);
      javax.swing.tree.TreePath path = new javax.swing.tree.TreePath(pathArr);
      setSelectionPath(path);
   }
}
