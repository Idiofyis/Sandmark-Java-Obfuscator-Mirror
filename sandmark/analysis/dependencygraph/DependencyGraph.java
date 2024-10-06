/***********************************************************
 *  ClassSplitting Algorithm  :                             *
 ************************************************************
 *  Idea Suggested by :                                     *
 *  ==================                                      *
 *  Dr.Christian Collberg                                   *
 *  collberg@cs.arizona.edu                                 *
 *							   *
 *  References : Dependency Graph (earlier version )        *
 *							   *
 *  Date : 4 May 2002                                       *
 *  -----------------                                       *
 *  Ashok Purushotham       &&      RathnaPrabhu            *
 *  ashok@cs.arizona.edu            prabhu@cs.arizona.edu   *
 ************************************************************/
package sandmark.analysis.dependencygraph;

/** 
 * A directed graph with field and method names as nodes
 * and edges from referrers to referees. 
 */
public class DependencyGraph extends sandmark.util.newgraph.MutableGraph {
   public DependencyGraph(java.util.Collection fields,
                          java.util.Collection methods) {
      java.util.Hashtable nameToObject =
         new java.util.Hashtable();

      for(java.util.Iterator methodIt = methods.iterator() ;
          methodIt.hasNext() ;) {
         sandmark.program.Method method  =
            (sandmark.program.Method)methodIt.next();
         addNode(method);
         nameToObject.put(method.getEnclosingClass().getName()+"."+method.getName()+method.getSignature(), method);
      }
      for(java.util.Iterator fieldIt = fields.iterator() ; 
          fieldIt.hasNext() ; ) {
         sandmark.program.Field field =
            (sandmark.program.Field)fieldIt.next();
         addNode(field);
         nameToObject.put(field.getEnclosingClass().getName()+"."+field.getName()+field.getSignature(), field);
      }

      for (java.util.Iterator methodIt = methods.iterator() ;
           methodIt.hasNext() ; ) {
         sandmark.program.Method method =
            (sandmark.program.Method)methodIt.next();

         if(method.getInstructionList() == null)
            continue;
         org.apache.bcel.generic.InstructionHandle ihs[] = 
            method.getInstructionList().getInstructionHandles();
         for (int i =0; i < ihs.length ; i++) {
            if(ihs[i].getInstruction() instanceof 
               org.apache.bcel.generic.FieldOrMethod) {
               org.apache.bcel.generic.FieldOrMethod fom = 
                  (org.apache.bcel.generic.FieldOrMethod)ihs[i].getInstruction();
               String name = 
                  fom.getClassName(method.getConstantPool())+"."+
                  fom.getName(method.getConstantPool())+
                  fom.getSignature(method.getConstantPool());

               Object node = nameToObject.get(name);
               if(node != null)
                  addEdge(method,node);
            }
         }
      }
   }

   public String nodeName(Object o) {
      if(o instanceof sandmark.program.Method)
         return sandmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName
            ((sandmark.program.Method)o);
      else if(o instanceof sandmark.program.Field)
         return sandmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName
            ((sandmark.program.Field)o);
      else
         throw new RuntimeException();
   }
}
