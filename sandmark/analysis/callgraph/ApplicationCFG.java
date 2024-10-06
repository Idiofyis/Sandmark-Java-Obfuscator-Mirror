package sandmark.analysis.callgraph;

/** This class is an application-wide control flow graph.
 *  Its nodes are BasicBlocks, with the exception of 3 special 
 *  nodes that are just placeholder Objects. This class computes
 *  the callgraph of an application and uses that to determine
 *  inter-procedural control flow edges of the application-wide CFG. There are 
 *  3 special nodes: JAVA_LIB_SOURCE_BLOCK, JAVA_LIB_BODY_BLOCK, and
 *  JAVA_LIB_SINK_BLOCK. These represent the source, body, and sink of 
 *  any java library method, respectively. Any time an application basic block B
 *  calls a java library method, the edges (B, JAVA_LIB_SOURCE_BLOCK) and
 *  (JAVA_LIB_SINK_BLOCK, B) are added. Similarly, for any method M that could 
 *  be called by a java library method, the edges (JAVA_LIB_BODY_BLOCK, M.source)
 *  and (M.sink, JAVA_LIB_BODY_BLOCK) are added. In addition, the following edges 
 *  are always present: 
 *     (JAVA_LIB_SOURCE_BLOCK, JAVA_LIB_BODY_BLOCK), 
 *     (JAVA_LIB_BODY_BLOCK, JAVA_LIB_SINK_BLOCK)
 */
public class ApplicationCFG extends sandmark.util.newgraph.MutableGraph{
   public static final Object JAVA_LIB_SOURCE_BLOCK = new Object();
   public static final Object JAVA_LIB_BODY_BLOCK = new Object();
   public static final Object JAVA_LIB_SINK_BLOCK = new Object();

   private sandmark.program.Application app;
   
   /** Initialize and build the application CFG for the given application.
    */
   public ApplicationCFG(sandmark.program.Application APP){
      app = APP;
      try{
         build();
      }catch(Exception ex){
         throw new Error("Cannot build application cfg");
      }
   }

   private void build() throws Exception{
      addNode(JAVA_LIB_SOURCE_BLOCK);
      addNode(JAVA_LIB_BODY_BLOCK);
      addNode(JAVA_LIB_SINK_BLOCK);
      addEdge(JAVA_LIB_SOURCE_BLOCK, JAVA_LIB_BODY_BLOCK);
      addEdge(JAVA_LIB_BODY_BLOCK, JAVA_LIB_SINK_BLOCK);

      CallGraph callgraph = new CallGraph(app);
      for (java.util.Iterator miter=callgraph.nodes();miter.hasNext();){
         Object next = miter.next();
         if (next instanceof sandmark.program.Method){
            sandmark.program.Method method = (sandmark.program.Method)next;
            if (method.isAbstract() || method.isNative()){
               callgraph.removeNode(method);
            }else{
               sandmark.analysis.controlflowgraph.MethodCFG cfg = method.getCFG();
               for (java.util.Iterator bbiter=cfg.nodes();bbiter.hasNext();)
                  addNode(bbiter.next());

               for (java.util.Iterator edges=cfg.edges();edges.hasNext();){
                  sandmark.util.newgraph.Edge edge = 
                     (sandmark.util.newgraph.Edge)edges.next();
                  if (edge instanceof sandmark.analysis.controlflowgraph.FallthroughEdge)
                     addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge(edge.sourceNode(), edge.sinkNode()));
                  else if (edge instanceof sandmark.analysis.controlflowgraph.ExceptionEdge){
                     sandmark.analysis.controlflowgraph.ExceptionEdge exedge = 
                        (sandmark.analysis.controlflowgraph.ExceptionEdge)edge;
                     addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge(edge.sourceNode(), edge.sinkNode(), exedge.exception()));
                  }else
                     addEdge(edge.sourceNode(), edge.sinkNode());
               }
            }
         }
      }

      for (java.util.Iterator edges=callgraph.edges();edges.hasNext();){
         CallGraphEdge edge = (CallGraphEdge)edges.next();
         
         if (edge.sourceNode()==CallGraph.JAVA_LIB_METHOD){
            // (JAVA_LIB_METHOD, method)
            sandmark.analysis.controlflowgraph.MethodCFG calleecfg = 
               ((sandmark.program.Method)edge.sinkNode()).getCFG();
            addEdge(JAVA_LIB_BODY_BLOCK, calleecfg.source());
            addEdge(calleecfg.sink(), JAVA_LIB_BODY_BLOCK);

         }else if (edge.sinkNode()==CallGraph.JAVA_LIB_METHOD){
            // (method, JAVA_LIB_METHOD)
            sandmark.analysis.controlflowgraph.MethodCFG callercfg = 
               ((sandmark.program.Method)edge.sourceNode()).getCFG();

            for (java.util.Iterator handles=edge.getHandles().iterator();handles.hasNext();){
               org.apache.bcel.generic.InstructionHandle handle = 
                  (org.apache.bcel.generic.InstructionHandle)handles.next();
               sandmark.analysis.controlflowgraph.BasicBlock invokeblock = 
                  callercfg.getBlock(handle);
               
               // change the fallthrough and exception edges
               for (java.util.Iterator outedges=outEdges(invokeblock);outedges.hasNext();){
                  Object next = outedges.next();
                  if (next instanceof sandmark.analysis.controlflowgraph.FallthroughEdge){
                     sandmark.analysis.controlflowgraph.FallthroughEdge fte = 
                        (sandmark.analysis.controlflowgraph.FallthroughEdge)next;
                     removeEdge(fte);
                     addEdge(invokeblock, JAVA_LIB_SOURCE_BLOCK);
                     addEdge(JAVA_LIB_SINK_BLOCK, fte.sinkNode());
                  }else if (next instanceof sandmark.analysis.controlflowgraph.ExceptionEdge){
                     sandmark.analysis.controlflowgraph.ExceptionEdge exedge = 
                        (sandmark.analysis.controlflowgraph.ExceptionEdge)next;
                     removeEdge(exedge);
                     addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge(JAVA_LIB_SINK_BLOCK, exedge.sinkNode(), exedge.exception()));
                  }
               }
            }

         }else{
            // (method, method)
            sandmark.analysis.controlflowgraph.MethodCFG callercfg = 
               ((sandmark.program.Method)edge.sourceNode()).getCFG();
            sandmark.analysis.controlflowgraph.MethodCFG calleecfg = 
               ((sandmark.program.Method)edge.sinkNode()).getCFG();
            ((sandmark.program.Method)edge.sourceNode()).getInstructionList().setPositions();
            
            for (java.util.Iterator handles=edge.getHandles().iterator();handles.hasNext();){
               org.apache.bcel.generic.InstructionHandle handle = 
                  (org.apache.bcel.generic.InstructionHandle)handles.next();
               sandmark.analysis.controlflowgraph.BasicBlock invokeblock = 
                  callercfg.getBlock(handle);

               // change the fallthrough and exception edges
               for (java.util.Iterator outedges=outEdges(invokeblock);outedges.hasNext();){
                  Object next = outedges.next();
                  if (next instanceof sandmark.analysis.controlflowgraph.FallthroughEdge){
                     sandmark.analysis.controlflowgraph.FallthroughEdge fte = 
                        (sandmark.analysis.controlflowgraph.FallthroughEdge)next;
                     removeEdge(fte);
                     addEdge(invokeblock, calleecfg.source());
                     addEdge(calleecfg.sink(), fte.sinkNode());
                  }else if (next instanceof sandmark.analysis.controlflowgraph.ExceptionEdge){
                     sandmark.analysis.controlflowgraph.ExceptionEdge exedge = 
                        (sandmark.analysis.controlflowgraph.ExceptionEdge)next;
                     removeEdge(exedge);
                     addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge(calleecfg.sink(), exedge.sinkNode(), exedge.exception()));
                  }
               }
            }
         }
      }
   }

   /** Returns the basic block that contains the given handle
    *  or null if it is not found.
    */
   public sandmark.analysis.controlflowgraph.BasicBlock getBlock(org.apache.bcel.generic.InstructionHandle ih){
      for (java.util.Iterator nodes=nodes();nodes.hasNext();){
         Object next = nodes.next();
         if (next instanceof sandmark.analysis.controlflowgraph.BasicBlock){
            sandmark.analysis.controlflowgraph.BasicBlock bb = 
               (sandmark.analysis.controlflowgraph.BasicBlock)next;
            if (bb.getInstList().contains(ih))
               return bb;
         }
      }
      return null;
   }


   public static void main(String args[]) throws Exception{
      if (args.length<1) return;
      
      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);
      
      ApplicationCFG cfg = new ApplicationCFG(app);

      sandmark.util.newgraph.Graphs.dotInFile(cfg, args[0]+".dot");
   }
}
