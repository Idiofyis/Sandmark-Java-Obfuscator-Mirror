package sandmark.wizard.modeling.lazydfa;


public class LazyDFANode
{
    private java.util.HashMap ranList;
    private java.util.HashMap prohibList;
    private java.util.LinkedList reqList;

    private LazyDFAEdge[] myEdges;
    private java.util.HashMap mRangeMap;

    /**
       Creates a LazyDFANode with empty mappings (ie a start state).
    */
    public LazyDFANode(java.util.HashMap rangeMap)
    {
       mRangeMap = rangeMap;
        ranList = new java.util.HashMap();
        prohibList = new java.util.HashMap();
        reqList = new java.util.LinkedList();
    }

    /**
       Creates a LazyDFANode from another LazyDFANode, copying
       the entries in each list.
    */
    /*package*/ LazyDFANode(java.util.HashMap rangeMap,LazyDFANode toCopy)
    {
       mRangeMap = rangeMap;
        ranList = new java.util.HashMap(toCopy.ranList);
        prohibList = new java.util.HashMap(toCopy.prohibList);
        reqList = new java.util.LinkedList(toCopy.reqList);
    }

    public boolean equals(Object o)
    {
        if(!(o instanceof LazyDFANode))
            return false;

        LazyDFANode other =
            (LazyDFANode)o;

        if(other.ranList.equals(ranList) &&
           other.prohibList.equals(prohibList)){
            java.util.Iterator itr = other.reqList.iterator();
            while(itr.hasNext()){
                if(!reqList.contains(itr.next()))
                    return false;
            }

            itr = reqList.iterator();
            while(itr.hasNext()){
                if(!other.reqList.contains(itr.next()))
                    return false;
            }
            return true;
        }

        return false;
    }

    public LazyDFAEdge[] getOutgoingEdges(LazyDFA dfa)
    {
        dfa.buildEdges(this);
        return myEdges;
    }

    /*package*/ LazyDFAEdge[] getCurrentEdgeSet()
    {
        return myEdges;
    }

    /*package*/ void setEdgeSet(LazyDFAEdge[] edges)
    {
        myEdges = edges;
    }

    public boolean ranListContains(sandmark.config.RequisiteProperty p,
                                   sandmark.program.Object obj)
    {
        java.util.Set list = (java.util.Set)ranList.get(p);
        if(list == null) return false;

        return list.contains(obj);
    }

    public boolean prohibListContains(sandmark.config.RequisiteProperty p,
                                      sandmark.program.Object obj)
    {
        java.util.Set list =(java.util.Set) prohibList.get(p);
        if(list == null) return false;
        return list.contains(obj);
    }

    public boolean isAccept()
    {
        return reqList.size() == 0;
    }

    /*package*/ void addRan(sandmark.config.RequisiteProperty p,
                            sandmark.program.Object obj)
    {
        java.util.Set list = (java.util.Set)ranList.get(p);
        if(list == null){
            list = new java.util.HashSet();
            ranList.put(p, list);
        }

        list.addAll(LazyDFA.getRange(mRangeMap,obj));
    }

    /*package*/ void addProhib(sandmark.config.RequisiteProperty p,
                               sandmark.program.Object obj)
    {
        java.util.Set list = (java.util.Set)prohibList.get(p);
        if(list == null){
            list = new java.util.HashSet();
            prohibList.put(p, list);
        }

        list.addAll(LazyDFA.getRange(mRangeMap,obj));
    }

    /*package*/ void addReq(sandmark.config.RequisiteProperty p,
                            sandmark.program.Object obj)
    {
        java.util.ArrayList range = LazyDFA.getRange(mRangeMap,obj);
        for(int i = 0; i < range.size(); i++){
            Tuple temp = new Tuple(p, (sandmark.program.Object)range.get(i));
            if(!reqList.contains(temp))
                reqList.add(temp);
        }
    }

    /*package*/ void removeReq(sandmark.config.RequisiteProperty p,
                               sandmark.program.Object obj)
    {
        java.util.ArrayList range = LazyDFA.getRange(mRangeMap,obj);
        for(int i = 0; i < range.size(); i++){
            Tuple temp = new Tuple(p, (sandmark.program.Object)range.get(i));
            reqList.remove(temp);
        }
    }

    private class Tuple
    {
        public sandmark.config.RequisiteProperty prop;
        public sandmark.program.Object targ;

        public Tuple(sandmark.config.RequisiteProperty p,
              sandmark.program.Object x)
        {
            prop = p;
            targ = x;
        }

        public boolean equals(Object o)
        {
            Tuple other = (Tuple)o;
            return other.prop.equals(prop) && other.targ.equals(targ);
        }
    }
}

