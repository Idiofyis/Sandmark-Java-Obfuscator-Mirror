package sandmark.util.newgraph;

public interface Edge {
   abstract public java.lang.Object sourceNode();
   abstract public java.lang.Object sinkNode();
   abstract public Edge clone(Object source,Object sink)
      throws CloneNotSupportedException;
}
