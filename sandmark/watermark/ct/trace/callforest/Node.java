package sandmark.watermark.ct.trace.callforest;

public class Node extends sandmark.util.newgraph.Node {

    public static final int MISSING_int  = -1;
    public static final int MISSING_kind = -1;

    public static final int ENTER  = 0;
    public static final int EXIT   = 1;
    public static final int CALL   = 2;
    public static final int RETURN = 3;

    private int kind = MISSING_kind;
    private int weight = MISSING_int;
    private sandmark.util.StackFrame frame = null;
    private boolean isMark = false;

    public Node (
      sandmark.util.ByteCodeLocation location,
      long threadID,
      long frameID,
      int weight,
      int kind,
      boolean isMark ) {
      frame = new sandmark.util.StackFrame( location, threadID, frameID );
      this.weight = weight;
      this.kind = kind;
      this.isMark = isMark;
    }

     public Node (
      int number,
      sandmark.util.StackFrame frame,
      int weight,
      int kind ) {
      super(number);
      this.frame = frame;
      this.weight = weight;
      this.kind = kind;
    }

   public Node (
       sandmark.util.ByteCodeLocation location,
       long threadID,
       long frameID,
       int weight ) {
      frame = new sandmark.util.StackFrame( location, threadID, frameID );
      this.weight = weight;
    }


    public Node (
       sandmark.util.ByteCodeLocation location,
       long threadID ) {
      frame = new sandmark.util.StackFrame( location, threadID );
    }

    public Node (
       sandmark.util.ByteCodeLocation location,
       long threadID,
       long frameID ) {
      frame = new sandmark.util.StackFrame( location, threadID, frameID );
    }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new Node(number, frame, weight, kind);
   }

    /*
     * Returns a new copy of this node
     */
    public Node copy () {
       return new Node(number, frame, weight, kind);
    }

/**
 * Format the data in an easy to parse form.
 */  
    public String toString() {
       return 
          number + ":" +
          "FRAME[" + 
            frame.getLocation().toString() + 
            ", THRD=" + frame.getThreadID() + 
            ", ID=" + frame.getFrameID() + 
            ", KIND="+kind2String(kind) + 
            ", WEIGHT=" + weight + "]";
    }

/**
 * Format the data in a compact form.
 */  
    public String toStringShortFormat() {
       return 
            frame.getLocation().toStringShortFormat() + "\n" +
            "THRD=" + frame.getThreadID() + 
            ",ID=" + frame.getFrameID() + 
            ",WEIGHT=" + weight;
    }

/**
 * Format the data in a format suitable for dot.
 */  
    public String toStringDotFormat () {
       if (isMarkNode())
          return "{" + frame.getLocation().getMethod().getName() +
                   "|WGHT=" + weight +
                   "|" + "#=" + nodeNumber() +
                  "}";
       return frame.getLocation().toStringShortDotFormat() + "|" +
              "{" + "ID=" + frame.getFrameID() + 
                   "|" + "WGHT=" + weight +
                   "|" + "#=" + nodeNumber() +
                   "|" + kind2StringSmall(kind) +
              "}";
    }

    public String kind2String(int kind) {
      switch(kind) {
      case ENTER  : return "ENTER";
      case EXIT   : return "EXIT";
      case CALL   : return "CALL";
      case RETURN : return "RETURN";
      default     : return "?";
      }
    }

    public String kind2StringSmall(int kind) {
      switch(kind) {
      case ENTER  : return "EN";
      case EXIT   : return "EX";
      case CALL   : return "CA";
      case RETURN : return "RT";
      default     : return "?";
      }
    }

/**
 * Return the method represented by this node.
 */  
public sandmark.util.MethodID getMethod () {
    return frame.getLocation().getMethod();
}

/**
 * Return the ID number of the frame represented by this node.
 */  
public long frameID () {
    return frame.getFrameID();
}

/**
 * Return true if this node represents one of the mark()-methods in the annotator class.
 */  
public boolean isMarkNode() {
   return isMark;
}

/**
 * Return the weight of this node.
 */  
public int getWeight() {
  return weight;
}

/**
 * Set the weight of this node.
 */  
public void setWeight( int v ) {
  this.weight = v;
}

/**
 * Get the weight of this node.
 */  
public int getKind() {
  return kind;
}

/**
 * Return true is this is an ENTER node.
 */  
public boolean isEnterNode() {
   return (kind==ENTER);
}
/**
 * Return true is this is an EXIT node.
 */  
public boolean isExitNode() {
   return (kind==EXIT);
}

/**
 * Return true is this is an CALL node.
 */  
public boolean isCallNode() {
   return (kind==CALL);
}

/**
 * Return true is this is an RETURN node.
 */  
public boolean isReturnNode() {
   return (kind==RETURN);
}

/**
 * Get the stack frame corresponding to this node.
 */  
public sandmark.util.StackFrame getFrame() {
  return frame;
}

    
public boolean equals(java.lang.Object o) {
  Node c = (Node) o;
  return (number == c.number) &&
          c.frame.equals(frame) &&
          (c.kind==kind);
}

public int hashCode() {
   return (int)(kind) + this.number * this.number + 
          frame.hashCode();
}

}

