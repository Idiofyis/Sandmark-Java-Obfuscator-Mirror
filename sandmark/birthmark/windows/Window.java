package sandmark.birthmark.windows;

public class Window {
      Window prev;
      int opcode;
      int mHash;
      public Window(Window p,int o) {
	 prev = p;
	 opcode = o;
	 mHash = ((p == null ? 0 : p.hashCode()) << 3) + o;
      }
      public String toString() { 
	 return (prev == null ? "" : (prev.toString() + " ")) + opcode;
      }
      public int hashCode() { return mHash; }
      public boolean equals(Object o) {
	 Window w = (Window)o;
	 return w.opcode == opcode && 
	    ((w.prev == null && prev == null) ||
	     (w.prev != null && prev != null && w.prev.equals(prev)));
      }
   }
