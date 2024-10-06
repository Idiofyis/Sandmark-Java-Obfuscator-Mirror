package sandmark.wizard.modeling.set;

/**
   A small modification of a LinkedList to make a priority queue.
   The priorities are defined by the compareTo method (in Comparable),
   but duplicates are allowed.
   @author Kelly Heffner
*/
public class PriorityQueue
{
    private java.util.LinkedList myList;
    private java.util.Comparator myComparer;
    private sandmark.util.newgraph.MutableGraph myGraph;

    /**
       Constructs a PriorityQueue given the means to determine
       priority.
    */
    public PriorityQueue(java.util.Comparator comp)
    {
        myList = new java.util.LinkedList();
        myComparer = comp;
        myGraph = new sandmark.util.newgraph.MutableGraph();
    }

    /**
       Adds an object to the queue, behind the last object with
       priority greater than or equal to its priority. (things with
       lower priority numbers are closer to the back of the list)
       @param o the object to add - must be a type that the
       Comparator can compare
    */
    public void add(Object o)
    {
        //System.out.println("adding " + o + " " + ((sandmark.program.Object)o).getUserConstraints().obfuscationLevel);
        myGraph.addNode(o);
        if(myList.isEmpty()){
            myList.addFirst(o);
            return;
        }


        //yes, I mean size(), not size()-1, I want the iterator after the last
        //element
        java.util.ListIterator listItr = myList.listIterator(myList.size());
        Object curr = listItr.previous();
        while(myComparer.compare(curr, o) < 0){
            if(listItr.hasPrevious())
                curr = listItr.previous();
            else
                break;
        }
        listItr.next();
        //System.out.println(myList);
        if(listItr.hasPrevious()){
            myGraph.addEdge(listItr.previous(), o);
            listItr.next();
        }
        listItr.add(o);
        if(listItr.hasNext())
            myGraph.addEdge(o, listItr.next());
    }

    public sandmark.util.newgraph.MutableGraph getVisualization(){
        return myGraph;
    }

    public void remove(Object o){
        myList.remove(o);
        removeNode(o);
    }

    public Object dequeue()
    {
        removeNode(myList.getFirst());
        return myList.removeFirst();
    }

    private void removeNode(Object o){
        java.util.Iterator itr = myGraph.inEdges(o);
        while(itr.hasNext())
            myGraph.removeEdge((sandmark.util.newgraph.Edge)itr.next());

        itr = myGraph.outEdges(o);
        while(itr.hasNext())
            myGraph.removeEdge((sandmark.util.newgraph.Edge)itr.next());

        myGraph.removeNode(o);
    }

    public Object getFront()
    {
        return myList.getFirst();
    }

    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

}

