package sandmark.watermark.ct.encode.ir;

public class Build extends IR {
   public sandmark.util.newgraph.MutableGraph[] subGraphs;
   public sandmark.util.newgraph.MutableGraph componentGraph;
   public sandmark.watermark.ct.encode.ir.List init;
   public sandmark.watermark.ct.encode.ir.List creators;
   public sandmark.watermark.ct.encode.ir.List fixups;
   public sandmark.watermark.ct.encode.ir.List destructors;
   public sandmark.watermark.ct.encode.ir.Method construct;
   public sandmark.watermark.ct.encode.ir.Method destruct;
   public sandmark.watermark.ct.encode.ir.List staticFields;
   public sandmark.watermark.ct.encode.ir.List storageCreators;
   public sandmark.watermark.ct.encode.ir.Method storageBuilder;
   public sandmark.watermark.ct.encode.storage.GlobalStorage storage = null;

   public Build (sandmark.util.newgraph.MutableGraph graph, 
                 sandmark.util.newgraph.MutableGraph[] subGraphs, 
                 sandmark.util.newgraph.MutableGraph componentGraph, 
                 sandmark.watermark.ct.encode.ir.List init, 
                 sandmark.watermark.ct.encode.ir.List creators, 
                 sandmark.watermark.ct.encode.ir.List fixups, 
                 sandmark.watermark.ct.encode.ir.List destructors, 
                 sandmark.watermark.ct.encode.storage.GlobalStorage storage){
      this.graph = graph;
      this.subGraphs = subGraphs;
      this.componentGraph = componentGraph;
      this.init = init;
      this.creators = creators;
      this.fixups = fixups;
      this.destructors = destructors;
      this.storage = storage;
      this.staticFields = new sandmark.watermark.ct.encode.ir.List();
      this.storageCreators = new sandmark.watermark.ct.encode.ir.List();
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new Build(graph, subGraphs, componentGraph, 
                      (sandmark.watermark.ct.encode.ir.List)init.copy(),   
                      (sandmark.watermark.ct.encode.ir.List)creators.copy(), 
                      (sandmark.watermark.ct.encode.ir.List)fixups.copy(), 
                      (sandmark.watermark.ct.encode.ir.List)destructors.copy(), 
                      storage);
   }

   public String toString(String indent) {
      String S = "";
      if (storage != null)
         S += storage.toString(indent);
      String R = "";
      R += indent + "Build()\n";
      R += renderOps(init, indent);
      R += renderOps(creators, indent);
      R += renderOps(fixups, indent);
      R += renderOps(destructors, indent);
      R += construct + "\n";
      R += destruct + "\n";
      R += renderOps(staticFields, indent) + "\n";
      R += renderOps(storageCreators, indent) + "\n";
      R += indent + "Storage:\n" + S;
      return R;
   }

//======================       Methods    ===========================
   static void methodsToJava(
           sandmark.util.ConfigProperties props,
      sandmark.watermark.ct.encode.ir.List proc,
      sandmark.util.javagen.List res) {
      java.util.Iterator iter = proc.iterator();
      while (iter.hasNext()) {
         sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
         sandmark.util.javagen.Java j = f.toJava(props);
         res.cons(j);
      }
   }

   sandmark.util.javagen.List genMethods(sandmark.util.ConfigProperties props) {
      sandmark.util.javagen.List methods = new sandmark.util.javagen.List();
      methodsToJava(props,init,methods);
      methodsToJava(props,creators,methods);
      methodsToJava(props,destructors,methods);
      methodsToJava(props,fixups,methods);
      methodsToJava(props,storageCreators,methods);

      //   These are not needed anymore.
      //  methods.cons(construct.toJava(props)); 
      //  methods.cons(destruct.toJava(props));
      return methods;
    }

//======================       Fields    ===========================
    sandmark.util.javagen.List genNodeFields(sandmark.util.ConfigProperties props) {
      sandmark.util.javagen.List fields = new sandmark.util.javagen.List();

      String nodeType = props.getProperty("Node Class");
      String attributes[] = {"public"};
      java.util.Set seen = new java.util.HashSet();
   
      // Horrible hack, assuming that field names with
      // '$'s in them are the ones that need to be declared.
      for (java.util.Iterator i = graph.edges(); i.hasNext(); ) {
	  java.lang.Object o = i.next();
	  if (o instanceof sandmark.util.newgraph.LabeledEdge) {
	      sandmark.util.newgraph.LabeledEdge e =
		  (sandmark.util.newgraph.LabeledEdge)o;
	      String label = e.getLabel();
	      if (!seen.contains(label) && label.indexOf('$') >= 0) {
		  sandmark.util.javagen.Java field =
		      new sandmark.util.javagen.Field(label, nodeType, attributes);
		  fields.cons(field);
	      }
	      seen.add(label);
	  }
      }
      return fields;
   }

   sandmark.util.javagen.List genStaticFields(sandmark.util.ConfigProperties props)  {
      sandmark.util.javagen.List fields = new sandmark.util.javagen.List();
      java.util.Iterator iter = staticFields.iterator();
      while (iter.hasNext()) {
	 sandmark.watermark.ct.encode.ir.Field field = (sandmark.watermark.ct.encode.ir.Field) iter.next();
         fields.cons(field.toJava(props)); 
      }
      return fields;
   }

   public sandmark.util.javagen.List genFields(sandmark.util.ConfigProperties props) {
      sandmark.util.javagen.List f1 = genNodeFields(props);
      sandmark.util.javagen.List f2 = genStaticFields(props);
      return f1.cons(f2);
   }

//======================================================================
String genHeader(
   sandmark.util.newgraph.MutableGraph origGraph, 
   sandmark.util.ConfigProperties props) {
   String date = props.getProperty("Date");
   String H = "Watermark class generated at " + date + " from:\n";
   for(java.util.Iterator it = props.properties() ;
       it.hasNext() ; ) {
       String p = (String) it.next();
      H += "   " + p + " = '" + props.getProperty(p) + "'\n";
   }
   //H += "\n" + sandmark.util.newgraph.Graphs.toDot(origGraph)  +  "\n\n";
   //if (origGraph != graph)
   //H  += sandmark.util.newgraph.Graphs.toDot(graph)  +  "\n\n" ;
   //H += sandmark.util.newgraph.Graphs.toDot(componentGraph)  +  "\n\n" ;
   H += " <<<<<<<< intermediate representation >>>>>>>>\n"  + 
        toString()  +  "\n\n" ;

   return H;
}

//======================     Build    ===========================
   sandmark.util.javagen.Java genRootMethod(sandmark.util.ConfigProperties  props)  {
      String nodeType = props.getProperty("Node Class");

      sandmark.util.newgraph.Node root = 
	 (sandmark.util.newgraph.Node)graph.getRoot();
      sandmark.watermark.ct.encode.storage.NodeStorage S = storage.lookup(root);

      sandmark.util.javagen.Statement load = S.toJavaLoad(props);

      sandmark.util.javagen.Statement ret  = 
         new sandmark.util.javagen.Return(
            new sandmark.util.javagen.LocalRef(root.name(),nodeType)
        );

      sandmark.util.javagen.List stats = 
         new sandmark.util.javagen.List(load,ret);

      String[] attributes = {"public","static"};
      sandmark.util.javagen.List args = new sandmark.util.javagen.List();
      sandmark.util.javagen.List formals = new sandmark.util.javagen.List();
      sandmark.util.javagen.Method rootMethod =
         new sandmark.util.javagen.Method("root", nodeType, attributes, formals, stats);

      return rootMethod;
  }

//======================================================================
   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {

     String header = genHeader(graph,props);  // Should be origGraph.

      sandmark.util.javagen.List fields  = genFields(props);
      sandmark.util.javagen.List methods = genMethods(props);
      sandmark.util.javagen.Java root    = genRootMethod(props);

      String parent = props.getProperty("DWM_CT_Encode_ParentClass");
      String name = props.getProperty("DWM_CT_Encode_ClassName");
      String Package = props.getProperty("DWM_CT_Encode_Package");
      sandmark.util.javagen.Java Class =
         new sandmark.util.javagen.Class(parent, name, Package, fields, methods);
      Class.setComment(header);

      return Class;
   }
}


