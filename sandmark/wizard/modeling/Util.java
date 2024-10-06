package sandmark.wizard.modeling;


/**
   The Util class specifies convience methods for determining the
   dependencies between obfuscations, watermarkers, etc (any
   <code>sandmark.Algorithm</code> algorithm).
   @author Kelly Heffner
   @since 3.4.0 11/18/03
 */
public class Util
{

    public static final int PRE_REQUIRE = 0;
    public static final int POST_REQUIRE = 1;
    public static final int PRE_PROHIBIT = 2;
    public static final int POST_PROHIBIT  = 3;
    public static final int PRE_SUGGEST = 4;
    public static final int POST_SUGGEST = 5;
    private static java.util.ArrayList allAlgorithms;

    private static sandmark.obfuscate.GeneralObfuscator [] obfs;
    private static sandmark.watermark.DynamicWatermarker [] dynSwm;
    private static sandmark.watermark.StaticWatermarker [] stcSwm;

    private static sandmark.util.newgraph.Graph algDependencies;

    //mapping from sandmark.config.RequisiteProperty
    //to a list of algorithms that prohibit/require it
    private static java.util.HashMap propToPreprohibit = new java.util.HashMap();
    private static java.util.HashMap propToPostprohibit = new java.util.HashMap();
    private static java.util.HashMap propToPrerequisite = new java.util.HashMap();
    private static java.util.HashMap propToPostrequisite = new java.util.HashMap();


    //mapping from sandmark.config.RequisiteProperty
    //to a list of algorithms that have that property
    private static java.util.HashMap propToAlgorithms = new java.util.HashMap();

    static{
        initializeAlgs();
        initializeGraph();
    }

    public static sandmark.obfuscate.GeneralObfuscator [] getObfuscators(){
        return (sandmark.obfuscate.GeneralObfuscator[])obfs.clone();
    }

    public static sandmark.watermark.DynamicWatermarker [] getDynamicWatermarkers(){
        return (sandmark.watermark.DynamicWatermarker[])dynSwm.clone();
    }

    public static sandmark.watermark.StaticWatermarker [] getStaticWatermarkers(){
        return (sandmark.watermark.StaticWatermarker[])stcSwm.clone();
    }

    /**
       Returns a list of all algorithms that Xs the given
       algorithm, where X is one of the given dependency
       relationships.
       @param alg the target algorithm
       @param relation a dependency relationship
    */
    public static java.util.ArrayList getXers(sandmark.Algorithm alg,
                                               int relation){
        java.util.ArrayList retVal = new java.util.ArrayList();
        java.util.Iterator inEdges = algDependencies.inEdges(alg);
        while(inEdges.hasNext()){
            sandmark.wizard.modeling.Util.DependencyEdge edge =
                (sandmark.wizard.modeling.Util.DependencyEdge)inEdges.next();
            if(edge.getType() == relation)
                retVal.add(edge.sourceNode());
        }
        return retVal;
    }

    /**
       Returns a list of all algorithms that the given
       algorithm Xs, where X is one of the given dependency
       relationships.
       @param alg the source algorithm
       @param relation a dependency relationship
       @return a list of algorithms that <code>alg</code> Xs
    */
    public static java.util.ArrayList getXed(sandmark.Algorithm alg,
                                             int relation){
        java.util.ArrayList retVal = new java.util.ArrayList();
        java.util.Iterator outEdges = algDependencies.outEdges(alg);
        while(outEdges.hasNext()){
            sandmark.wizard.modeling.Util.DependencyEdge edge =
                (sandmark.wizard.modeling.Util.DependencyEdge)outEdges.next();
            if(edge.getType() == relation)
                retVal.add(edge.sinkNode());
        }
        return retVal;
    }

    /**
       Returns a list of all algorithms which prerequire the
       property in question.
    */
    public static java.util.ArrayList
        getPrerequisiters(sandmark.config.RequisiteProperty prop)
    {
        try{
            Class clazz = Class.forName("sandmark.Algorithm");
            java.lang.reflect.Method toUse =
                clazz.getMethod("getPrerequisites", null);

            return  getSpecifiers(prop,
                                  propToPrerequisite,
                                  toUse);
        }catch(Exception e){
            e.printStackTrace();
            //System.exit(0);
        }
        return null;
    }

    /**
       Returns a list of all algorithms which postrequire the
       property in question.
    */
    public static java.util.ArrayList
        getPostrequisiters(sandmark.config.RequisiteProperty prop)
    {
        try{
            return getSpecifiers(prop,
                                 propToPostrequisite,
                                 Class.forName("sandmark.Algorithm").getMethod
                                 ("getPostrequisites", null));
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }


    /**
       Returns a list of all algorithms which preprohibit the
       property in question.
    */
    public static java.util.ArrayList
        getPreprohibitors(sandmark.config.RequisiteProperty prop)
    {

        try{
            return getSpecifiers(prop,
                                 propToPreprohibit,
                                 Class.forName("sandmark.Algorithm").getMethod
                                 ("getPreprohibited", null));
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    /**
       Returns a list of all algorithms which postprohibit the
       property in question.
    */
    public static java.util.ArrayList
        getPostprohibitors(sandmark.config.RequisiteProperty prop)
    {

        try{
            return getSpecifiers(prop,
                                 propToPostprohibit,
                                 Class.forName("sandmark.Algorithm").getMethod
                                 ("getPostprohibited", null));
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    /**
       Returns a list of all algorithms which have the property in question.
    */
    public static java.util.ArrayList
        getAlgsForProp(sandmark.config.RequisiteProperty prop)
    {

        //change this to an array? the list never changes...
        java.util.ArrayList retVal = (java.util.ArrayList)
            propToAlgorithms.get(prop);
        if(retVal != null)
            return retVal;

        retVal = new java.util.ArrayList();
        java.util.Iterator algItr = allAlgorithms.iterator();
        while(algItr.hasNext()){
            sandmark.Algorithm alg = (sandmark.Algorithm)algItr.next();
            java.util.Iterator mutations = getMutationProps(alg).iterator();
            while(mutations.hasNext())
                if(mutations.next().equals(prop)){
                    retVal.add(alg);
                    break;
                }
        }
        propToAlgorithms.put(prop, retVal);
        return retVal;
    }

    public static java.util.ArrayList getMutationProps(sandmark.Algorithm alg)
    {
        java.util.ArrayList retVal = new java.util.ArrayList();
        sandmark.config.ModificationProperty[] mutations =
            alg.getMutations();
        if(mutations != null){
            for(int i = 0; i < mutations.length; i++)
                retVal.add(mutations[i]);
        }
        retVal.add(new sandmark.config.AlgorithmProperty(alg));
        return retVal;
    }

    public static void main(String [] args)
    {
        sandmark.util.newgraph.EditableGraphStyle style =
            new sandmark.util.newgraph.EditableGraphStyle
            (sandmark.util.newgraph.EditableGraphStyle.BLACK,
             sandmark.util.newgraph.EditableGraphStyle.CIRCLE,
             sandmark.util.newgraph.EditableGraphStyle.SOLID,
             10 /*font size*/, /*labeled nodes*/true,
             sandmark.util.newgraph.EditableGraphStyle.BLACK,
             sandmark.util.newgraph.EditableGraphStyle.SOLID,
             10, true);
        sandmark.util.newgraph.Graphs.dotInFile
            (algDependencies, style, "algorithms.dot");
    }


    /**
       Returns a list of all algorithms which are related to
       the property in question via the supplied relation.
    */
    private static java.util.ArrayList
        getSpecifiers(sandmark.config.RequisiteProperty prop,
                      java.util.HashMap propToSpecifier,
                      java.lang.reflect.Method getSpecifys)
    {
        java.util.ArrayList retVal = (java.util.ArrayList)
            propToSpecifier.get(prop);
        if(retVal != null)
            return retVal;

        retVal = new java.util.ArrayList();
        java.util.Iterator algItr = allAlgorithms.iterator();
        while(algItr.hasNext()){
            try{
                sandmark.Algorithm alg = (sandmark.Algorithm)algItr.next();
                sandmark.config.RequisiteProperty [] list =
                    (sandmark.config.RequisiteProperty[])
                    getSpecifys.invoke(alg, null);

                if(list == null) continue;
                for(int i = 0; i < list.length; i++){
                    if(list[i].equals(prop)){
                        retVal.add(alg);
                        break;
                    }
                }
            }catch(/*IllegalAccessException,
                   IllegalArgumentException,
                   InvocationTargetException*/Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }
        propToSpecifier.put(prop, retVal);
        return retVal;
    }


    /**
       Takes the intersection of two Lists.
       @param l1 one list
       @param l2 a second list
       @return the intersection of l1 and l2
     */
    public static java.util.ArrayList setIntersect(java.util.List l1,
                                                    java.util.List l2)
    {
        java.util.ArrayList retVal = new java.util.ArrayList();
        java.util.Iterator itr = l1.iterator();
        while(itr.hasNext()){
            Object temp = itr.next();
            if(l2.contains(temp))
                retVal.add(temp);
        }
        return retVal;
    }

    private static void initializeAlgs(){
        allAlgorithms = new java.util.ArrayList();

        String [] allObNames = (String [])
            sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	    (sandmark.util.classloading.IClassFinder.GEN_OBFUSCATOR).toArray
	    (new String[0]);

        String [] allDynWatermarkers = (String [])
            sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	    (sandmark.util.classloading.IClassFinder.DYN_WATERMARKER).toArray
	    (new String[0]);

        String [] allStcWatermarkers = (String [])
            sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	    (sandmark.util.classloading.IClassFinder.STAT_WATERMARKER).toArray
	    (new String[0]);


        obfs = new sandmark.obfuscate.GeneralObfuscator[allObNames.length];
        if(allObNames != null) //no obfuscators returns null list
            for(int i = 0; i < allObNames.length; i++){
		try {
		    sandmark.obfuscate.GeneralObfuscator ob =
			(sandmark.obfuscate.GeneralObfuscator)Class.forName
			(allObNames[i]).newInstance();
		    obfs[i] = ob;
		    allAlgorithms.add(ob);
		} catch(Exception e) {
		    throw new Error("could not instantiate " + allObNames[i]);
		}
            }

        dynSwm = new
            sandmark.watermark.DynamicWatermarker[allDynWatermarkers.length];
        if(allDynWatermarkers != null)
            for(int i = 0; i < allDynWatermarkers.length; i++){
		try {
		    sandmark.watermark.DynamicWatermarker ob =
			(sandmark.watermark.DynamicWatermarker)Class.forName
			(allDynWatermarkers[i]).newInstance();
		    dynSwm[i] = ob;
		    allAlgorithms.add(ob);
		} catch(Exception e) {
		    throw new Error("could not instantiate " + 
				    allDynWatermarkers[i]);
		}
            }

        stcSwm = new
            sandmark.watermark.StaticWatermarker[allStcWatermarkers.length];
        if(allStcWatermarkers != null)
            for(int i = 0; i < allStcWatermarkers.length; i++){
		try {
		    sandmark.watermark.StaticWatermarker ob =
			(sandmark.watermark.StaticWatermarker)Class.forName
			(allStcWatermarkers[i]).newInstance();
		    stcSwm[i] = ob;
		    allAlgorithms.add(ob);
		} catch(Exception e) {
		    throw new Error("could not instanstiate " +
				    allStcWatermarkers[i]);
		}
            }
    }

    private static void initializeGraph(){
        java.util.ArrayList nodes = (java.util.ArrayList)allAlgorithms.clone();
        java.util.ArrayList edges = new java.util.ArrayList();
        java.util.Iterator algorithms = allAlgorithms.iterator();
        while(algorithms.hasNext()){
            sandmark.Algorithm alg = (sandmark.Algorithm)algorithms.next();
            java.util.Iterator mutations = getMutationProps(alg).iterator();
            while(mutations.hasNext()){
                sandmark.config.RequisiteProperty prop =
                    (sandmark.config.RequisiteProperty)mutations.next();

                java.util.Iterator relations = getPrerequisiters(prop).iterator();
                while(relations.hasNext())
                    edges.add(new DependencyEdge
                              ((sandmark.Algorithm)relations.next(),
                               alg, PRE_REQUIRE));

                relations = getPostrequisiters(prop).iterator();
                while(relations.hasNext())
                    edges.add(new DependencyEdge
                              ((sandmark.Algorithm)relations.next(),
                               alg, POST_REQUIRE));

                relations = getPostprohibitors(prop).iterator();
                while(relations.hasNext())
                    edges.add(new DependencyEdge
                              ((sandmark.Algorithm)relations.next(),
                               alg, POST_PROHIBIT));

                relations = getPreprohibitors(prop).iterator();
                while(relations.hasNext())
                    edges.add(new DependencyEdge
                              ((sandmark.Algorithm)relations.next(),
                               alg, PRE_PROHIBIT));

            }
        }
        algDependencies = sandmark.util.newgraph.Graphs.createGraph(nodes.iterator(), edges.iterator());
    }

    public static boolean isTargetOf(sandmark.program.Object o,
                                     sandmark.Algorithm alg){
        if(
           (alg instanceof sandmark.MethodAlgorithm &&
            o instanceof sandmark.program.Method) ||
           (alg instanceof sandmark.ClassAlgorithm &&
            o instanceof sandmark.program.Class) ||
           (alg instanceof sandmark.AppAlgorithm &&
            o instanceof sandmark.program.Application)
           )
            return true;

        return false;

    }

    public static java.util.List getAlgsForTarget(sandmark.program.Object o,
                                                  sandmark.Algorithm[] algs){
        java.util.List retVal = new java.util.LinkedList();
        for(int i = 0; i < algs.length; i++){
            if(isTargetOf(o, algs[i]))
                retVal.add(algs[i]);
        }
        return retVal;
    }

    public static boolean isAncestorOf(sandmark.program.Object child,
                                        sandmark.program.Object parent){
        java.util.Iterator members = parent.members();
        while(members.hasNext()){
            sandmark.program.Object temp =
                (sandmark.program.Object)members.next();
            if(temp.equals(child))
                return true;
        }
        members = parent.members();
        while(members.hasNext()){
            sandmark.program.Object temp =
                (sandmark.program.Object)members.next();
            if(isAncestorOf(child, temp))
                return true;
        }
        return false;
    }


    private static class DependencyEdge extends sandmark.util.newgraph.LabeledEdge{
        //private sandmark.Algorithm from;
        //private sandmark.Algorithm to;
        private int type;

        private static final String [] LABELS = { "prerequires", "postrequires",
                                                  "preprohibits", "postprohibits",
                                                  "presuggests", "postsuggests"};

        public DependencyEdge(sandmark.Algorithm a, sandmark.Algorithm b,
                              int relation){
            super(a, b, LABELS[relation]);
            type = relation;
        }

        public int getType(){
            return type;
        }

        //public java.lang.Object sinkNode(){
        //return to;
        //}

        //public java.lang.Object sourceNode(){
        //return from;
        //}
    }

}


