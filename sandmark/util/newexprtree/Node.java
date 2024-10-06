package sandmark.util.newexprtree;

public class Node{
sandmark.util.newgraph.MutableGraph mg;

 Node()
{
mg=null;
}

public void setGraph(sandmark.util.newgraph.MutableGraph gr)
{
 mg=gr;
}

public sandmark.util.newgraph.MutableGraph graph()
{
 return mg;
}

}