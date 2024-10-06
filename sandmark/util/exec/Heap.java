package sandmark.util.exec;

/*
 * Recursively traverse the heap. We can either dump the entire heap
 * (starting at the roots) or starting at a particular object.
 * The result is a HeapData object which encodes a unique identifier
 * for the object and a list of its references (also unique IDs).
 * Null pointers are maintained, primitive values are discarded.
 * Thus, the following code will print out all the live references
 * coming out of an object and the objects it points to:
 * <PRE>
 *     sandmark.util.exec.Heap heap = new sandmark.util.exec.Heap(vm, root);
 *     while (heap.hasNext()) {
 *        sandmark.util.exec.HeapData obj = (sandmark.util.exec.HeapData) heap.next();
 *        for(int i=0; i<obj.refs.length; i++)
 *           if (obj.refs[i] != sandmark.util.exec.HeapData.NULL)
 *              System.out.println(obj.uniqueID + "[" + i + "]=" + obj.refs[i]);
 *      }
 * </PRE>
 * By including 'null' pointers we will make sure that the same field
 * value is always generated in the same position, regardless of whether
 * there are any null pointers or not in an object.
 */
public class Heap implements java.util.Iterator {
   java.util.HashSet seen = new java.util.HashSet(1000);
   java.util.LinkedList queue = new java.util.LinkedList();
   java.util.LinkedList objects = new java.util.LinkedList();
   sandmark.util.exec.HeapData nextObject = null;

   com.sun.jdi.VirtualMachine vm; 

   public Heap(com.sun.jdi.VirtualMachine vm) {
      this.vm = vm;
      dumpRoots();
   }

   public Heap(com.sun.jdi.VirtualMachine vm,
               com.sun.jdi.ObjectReference root) {
      this.vm = vm;
      if(!root.isCollected())
         queue.add(root);
   }

   public Heap(com.sun.jdi.VirtualMachine vm,
               java.util.List roots) {
      this.vm = vm;
      queue.addAll(roots);
      for(java.util.Iterator it = queue.iterator() ; it.hasNext() ; ) {
         com.sun.jdi.ObjectReference ref = (com.sun.jdi.ObjectReference)it.next();

         if(ref.isCollected())
            it.remove();
      }
   }

   public int size() {
      return objects.size();
   }

   //----------------------------------------------------------
   void get() {
      if (nextObject != null) return;

      while (objects.isEmpty() & (!queue.isEmpty())) {
         com.sun.jdi.ObjectReference var = (com.sun.jdi.ObjectReference) queue.removeFirst();
         com.sun.jdi.Type type = var.type();
         processVar("", type, var);
      }

      if (!objects.isEmpty()) 
         nextObject = (sandmark.util.exec.HeapData) objects.removeFirst();
   }

   public boolean hasNext() {
      get();
      return nextObject != null;
   }

   public Object next() throws java.util.NoSuchElementException {
      get();
      if (nextObject == null)
         throw new java.util.NoSuchElementException();
      sandmark.util.exec.HeapData n = nextObject;
      nextObject = null;
      return n;
   }

   public void remove() {
   }


   //----------------------------------------------------------
   void dumpRoots() {
      dumpThreads();
      dumpClasses();
   }

   void dumpClasses() {
      java.util.List classes = vm.allClasses();
      java.util.Iterator iter = classes.iterator();
      while (iter.hasNext()) {
         com.sun.jdi.ReferenceType Class = (com.sun.jdi.ReferenceType)iter.next();
         dumpStaticFields(Class);
      }
   }

   void dumpStaticFields (com.sun.jdi.ReferenceType Class) {
      String sig = Class.signature();
      if (excludeStandardClass(sig)) return;
      String className = Class.name();
      java.util.List fields = Class.allFields();
      java.util.Iterator iter = fields.iterator();
      while (iter.hasNext()) {
         com.sun.jdi.Field field = (com.sun.jdi.Field)iter.next();
         String name = field.name();
         if (field.isStatic()) {
            try {
               com.sun.jdi.Value value = Class.getValue(field);
               com.sun.jdi.Type type = field.type();
               processVar(className + "." + name, type, value);
            } catch (Exception e) {}
         }
      }
   }


   void dumpThreads() {
      java.util.List threads = vm.allThreads();
      java.util.Iterator iter = threads.iterator();
      while (iter.hasNext()) {
         com.sun.jdi.ThreadReference thread = (com.sun.jdi.ThreadReference)iter.next();
         dumpThread(thread);
      }
   }

   void dumpThread(com.sun.jdi.ThreadReference thread) {
      try {
         java.util.List frames = thread.frames();
         java.util.Iterator iter = frames.iterator();
         while (iter.hasNext()) {
            com.sun.jdi.StackFrame frame = (com.sun.jdi.StackFrame)iter.next();
            dumpFrame(frame);
         }
      } catch (Exception e) {
         //	    e.printStackTrace();
      }
   }

   void dumpFrame(com.sun.jdi.StackFrame frame) {
      try {
         java.util.List variables = frame.visibleVariables();
         java.util.Iterator iter = variables.iterator();

         while (iter.hasNext()) {
            com.sun.jdi.LocalVariable var = (com.sun.jdi.LocalVariable)iter.next();
            dumpLocal(frame, var);
         } 
      } catch (Exception e) {
         //	    System.out.println( "***" + frame.location().method() );
         //e.printStackTrace();
      }
   }

   void dumpLocal(com.sun.jdi.StackFrame frame, 
                  com.sun.jdi.LocalVariable var) {
      try {
         String name = var.name();
         com.sun.jdi.Value value = frame.getValue(var);
         com.sun.jdi.Type type = var.type();
         processVar(name, type, value);
      } catch(Exception e){
         //	  e.printStackTrace();
      }
   }

   //----------------------------------------------------------
   java.util.LinkedList outgoingRefs(com.sun.jdi.Value value) {
      java.util.LinkedList newRefs = new java.util.LinkedList();
      try {
         com.sun.jdi.Type type = value.type();
         if ((!excludeType(type))) {
            if (type instanceof com.sun.jdi.ArrayType) {
               newRefs = outgoingRefsArray((com.sun.jdi.ArrayReference)value);
            } else if (type instanceof com.sun.jdi.ClassType) {
               newRefs = outgoingRefsObject((com.sun.jdi.ObjectReference)value);
            } 
         }
      } catch (Exception e) {}
      //System.out.println( "Outgoing refs:" + newRefs );
      return newRefs;
   }

   // May have to change fields() to allFields() once we
   // allow Watermark.java to extend a preexisting class.
   java.util.LinkedList outgoingRefsObject(
                                           com.sun.jdi.ObjectReference object) {
      java.util.LinkedList newRefs = new java.util.LinkedList();
      com.sun.jdi.ReferenceType refType = object.referenceType();
      java.util.List fields = refType.fields();
      java.util.Iterator iter = fields.iterator();
      //       System.out.println(">>>>>>> " + refType.name() + " : " + object.uniqueID());
      while (iter.hasNext()) {
         com.sun.jdi.Field field = (com.sun.jdi.Field)iter.next();
         if (!field.isStatic()) {
            com.sun.jdi.Value val = object.getValue(field);
            String name = field.name();
            //              System.out.print("   >>>>>>> " + name);
            if (val == null) {
               //                 System.out.println(" == null");
               newRefs.add(val);
            } else if (val instanceof com.sun.jdi.ObjectReference) {
               long ID = ((com.sun.jdi.ObjectReference)val).uniqueID();
               //                 System.out.println("(" + ID + ") = " + val);
               newRefs.add(val);
            }
         }
      }
      //System.out.println( "Outgoing objectrefs:" + newRefs );
      return newRefs;
   }

   java.util.LinkedList outgoingRefsArray (
                                           com.sun.jdi.ArrayReference array) {
      java.util.LinkedList newRefs = new java.util.LinkedList();
      java.util.List values = array.getValues();
      java.util.Iterator iter = values.iterator();
      while (iter.hasNext()) {
         com.sun.jdi.Value val = (com.sun.jdi.Value)iter.next();
         if (val == null || val instanceof com.sun.jdi.ObjectReference)
            newRefs.add(val);
      }
      //System.out.println( "Outgoing arrayrefs:" + newRefs );
      return newRefs;
   }

   //----------------------------------------------------------

   void processVar (
                    String name, 
                    com.sun.jdi.Type type, 
                    com.sun.jdi.Value ref) {
      boolean notSeen = seen.add(ref);
      if (notSeen) {
         java.util.LinkedList newRefs = outgoingRefs(ref);
         //System.out.println("Refs size:" + newRefs.size() );
         saveVar(name, type, ref, newRefs);
         java.util.Iterator iter = newRefs.iterator();
         while (iter.hasNext()) {
            com.sun.jdi.ObjectReference r = (com.sun.jdi.ObjectReference)iter.next();
            if (r != null)
               queue.add(r);
         }
      }
   }

   void saveVar(
                String name, 
                com.sun.jdi.Type type, 
                com.sun.jdi.Value value, 
                java.util.LinkedList refs) {
      sandmark.util.exec.HeapData data = null;
      if (value instanceof com.sun.jdi.ObjectReference) {
         long[] R = new long[refs.size()];
         int i = 0;
         java.util.Iterator iter = refs.iterator();
         while (iter.hasNext()) {
            com.sun.jdi.ObjectReference ref = (com.sun.jdi.ObjectReference)iter.next();
            if (ref == null)
               R[i++] = sandmark.util.exec.HeapData.NULL;
            else
               R[i++] = ref.uniqueID();
         }
         com.sun.jdi.ObjectReference obj = (com.sun.jdi.ObjectReference) value;
         long uniqueID = obj.uniqueID();
         String sig = type.signature();
         data = new sandmark.util.exec.HeapData(name, uniqueID, sig, R, System.currentTimeMillis());
         objects.add(data);
      }
   }

   //----------------------------------------------------------
   boolean excludeType(com.sun.jdi.Type type) {
      if (!(type instanceof com.sun.jdi.ReferenceType)) return true;
      String sig = type.signature();
      return excludeStandardClass(sig);
   }

   boolean excludeStandardClass(String sig) {
      if (sig.charAt(0) == '[')
         sig = sig.substring(1,sig.length());
      if (sig.startsWith("Ljava")) return true;
      if (sig.startsWith("Lsun")) return true;
      if (sig.startsWith("Lcom/sun")) return true;
      return false;
   }

   //----------------------------------------------------------
   public static void print(
                            com.sun.jdi.VirtualMachine vm,
                            com.sun.jdi.ObjectReference root) {
      sandmark.util.exec.Heap heap = new sandmark.util.exec.Heap(vm, root);
      while (heap.hasNext()) {
         sandmark.util.exec.HeapData obj = (sandmark.util.exec.HeapData) heap.next();
         System.out.println(obj.toString());
      }
   }

   public static void print(
                            com.sun.jdi.VirtualMachine vm) {
      sandmark.util.exec.Heap heap = new sandmark.util.exec.Heap(vm);
      while (heap.hasNext()) {
         sandmark.util.exec.HeapData obj = (sandmark.util.exec.HeapData) heap.next();
         System.out.println(obj.toString());
      }
   }
  
   public static void print(com.sun.jdi.VirtualMachine vm,java.util.List roots) {
      for(Heap h = new Heap(vm,roots) ; h.hasNext() ; ) {
         HeapData hd = (HeapData)h.next();
         System.out.println(hd);
      }
   }
}
