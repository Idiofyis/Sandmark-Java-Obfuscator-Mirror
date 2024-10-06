package sandmark.util.newgraph;

class NodeWrapper {
   private static java.util.TreeSet slots;
   private static int nextSlot;

   public static int lockSlot() {
//        Exception e2 = null;
//        try {
// 	   throw new RuntimeException();
//        }
//        catch (RuntimeException re) {
// 	   e2 = re;
//        }

      boolean empty = false;
      synchronized (slots) {
	 empty = slots.isEmpty();
      }
      if (empty) {
	 System.gc();
	 System.runFinalization();
      }
      synchronized (slots) {
	 if (slots.isEmpty()) {
	    try {
	       slots.wait(10000);
	    }
	    catch (java.lang.InterruptedException e) {

	    }
	 }
	 if (slots.isEmpty()) {
	    slots.add(new Integer(nextSlot++));
// 	    System.out.println("locking slot " + (nextSlot - 1));
// 	    e2.printStackTrace();
	    return nextSlot - 1;
	 }
	 else {
	    java.util.Iterator i = slots.iterator();
	    Integer j = (Integer)i.next();
	    i.remove();
// 	    System.out.println("locking slot " + j.intValue());
// 	    e2.printStackTrace();
	    return j.intValue();
	 }
      }
   }

   public static void unlockSlot(int id) {
//        try {
// 	   throw new RuntimeException();
//        }
//        catch (RuntimeException re) {
// 	   System.out.println("unlocking slot " + id);
// 	   re.printStackTrace();
//        }

      synchronized (slots) {
	 slots.add(new Integer(id));
	 slots.notify();
      }
   }

   private static final int FAST_SLOTS = 3;
   private static final int NUM_SLOTS = 32 + FAST_SLOTS;

   static {
      slots = new java.util.TreeSet();
      for (int i = 0; i < NUM_SLOTS; i++)
	 slots.add(new Integer(i));
      nextSlot = NUM_SLOTS;
   }

   private byte s[];
   private byte s0, s1, s2;

   public NodeWrapper(Graph originator, java.lang.Object node) {
      s = new byte[NUM_SLOTS - FAST_SLOTS];
      this.node = node;
      this.originator = originator;
   }

   private void checkBounds(int id) {
      if (id >= s.length) {
	 byte tmp[] = s;
	 int len = tmp.length * 2;
	 while (id >= len)
	    len *= 2;
	 s = new byte[len];
	 System.arraycopy(tmp, 0, s, 0, tmp.length);
      }
   }

   public synchronized final byte getSlot(int id) {
      switch(id) {
      case 0:
	 return s0;
      case 1:
	 return s1;
      case 2:
	 return s2;
      default:
	 checkBounds(id - FAST_SLOTS);
	 return s[id - FAST_SLOTS];
      }
   }

   public synchronized final void setSlot(int id, byte val) {
      switch(id) {
      case 0:
	 s0 = val;
	 break;
      case 1:
	 s1 = val;
	 break;
      case 2:
	 s2 = val;
	 break;
      default:
	 checkBounds(id - FAST_SLOTS);
	 s[id - FAST_SLOTS] = val;
	 break;
      }
   }

   public java.lang.Object node;
   public Graph originator;
   // only use this field from a graph where this == originator
   public byte mySlot;
}
