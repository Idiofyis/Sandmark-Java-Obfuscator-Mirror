package sandmark.wizard.modeling.set;

/**
   CandidateSet objects contain information about the algorithms
   run on a specific object.
*/
public class CandidateSet implements java.lang.Cloneable{

    /** List of sandmark.config.RequisiteProperty */
    private java.util.Set unfilledPostreqs = new java.util.LinkedHashSet();

    /** List of sandmark.config.RequisiteProperty */
    private java.util.Set unfilledPostsuggs = new java.util.LinkedHashSet();

    /** List of sandmark.config.RequisiteProperty */
    private java.util.Set runProperties = new java.util.LinkedHashSet();

    /** List of sandmark.Algorithm -- algorithms not banned forever*/
    private java.util.List runnableAlgorithms = new java.util.ArrayList();

    private sandmark.program.UserObjectConstraints myConst;

    public CandidateSet(java.util.List targetAlgorithms,
                        sandmark.program.UserObjectConstraints consts){
        runnableAlgorithms.addAll(targetAlgorithms);
        myConst = new sandmark.program.UserObjectConstraints(consts);
    }

    public java.lang.Object clone(){
        CandidateSet retVal = null;
        try{
            retVal = (CandidateSet)super.clone();
        }catch(java.lang.CloneNotSupportedException cnse){
            return null;
        }
        return retVal;
    }

    public sandmark.program.UserObjectConstraints getConstraints(){
        return myConst;
    }

    public java.util.Iterator getAlgorithms(){
        return runnableAlgorithms.iterator();
    }

    public int numAlgorithms(){
        return runnableAlgorithms.size();
    }

    /**
       Returns a shallow copy of the algorithm list.
    */
    public java.util.ArrayList getAlgorithmList(){
        return new java.util.ArrayList(runnableAlgorithms);
    }

    public boolean hasRun(sandmark.config.RequisiteProperty p){
        return runProperties.contains(p);
    }

    public void run(sandmark.config.RequisiteProperty p){
        runProperties.add(p);
        unfilledPostreqs.remove(p);
    }

    public void run(java.util.Collection p){
        runProperties.addAll(p);
        unfilledPostreqs.removeAll(p);
    }

    public void addPostreqs(sandmark.config.RequisiteProperty p){
        unfilledPostreqs.add(p);
    }

    public void addPostreqs(sandmark.config.RequisiteProperty [] p){
        if(p != null)
            for(int i = 0; i < p.length; i++)
                unfilledPostreqs.add(p[i]);
    }

    public void addPostreqs(java.util.Collection p){
        unfilledPostreqs.addAll(p);
    }

    public java.util.Iterator postReqs(){
        return unfilledPostreqs.iterator();
    }

    public void removeAlgorithms(java.util.Collection toRemove){
        runnableAlgorithms.removeAll(toRemove);
    }

    public void addPostsuggs(sandmark.config.RequisiteProperty p){
        unfilledPostsuggs.add(p);
    }

    public void addPostsuggs(sandmark.config.RequisiteProperty [] p){
        if(p != null)
            for(int i = 0; i < p.length; i++)
                unfilledPostsuggs.add(p[i]);
    }

    public void addPostsuggs(java.util.Collection p){
        unfilledPostsuggs.addAll(p);
    }

    public boolean verify(){
        java.util.Iterator itr = postReqs();
        while(itr.hasNext()){
            sandmark.config.RequisiteProperty req =
                (sandmark.config.RequisiteProperty)itr.next();

            java.util.List algsForReq =
                sandmark.wizard.modeling.Util.getAlgsForProp(req);
            if(sandmark.wizard.modeling.Util.setIntersect
               (algsForReq, runnableAlgorithms).size() <= 0)
                return false;
        }
        return true;
    }


}
