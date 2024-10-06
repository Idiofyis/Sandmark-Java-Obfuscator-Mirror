package sandmark.watermark.execpath;

public class ConditionGenerator{
   private static final long ISUNIQUESOFAR       = 0x00000001L;
   private static final long ISEVEN              = 0x00000002L;
   private static final long ISGTZERO            = 0x00000004L;
   private static final long ISLTZERO            = 0x00000008L;
   private static final long ISNAN               = 0x00000010L;
   private static final long ISPOSINF            = 0x00000020L;
   private static final long ISNEGINF            = 0x00000040L;
   private static final long ISNULL              = 0x00000080L;
   private static final long ISLOWERCASE         = 0x00000100L;
   private static final long ISUPPERCASE         = 0x00000200L;
   private static final long ISDIGIT             = 0x00000400L;
   private static final long ISNONNULLUNHASHABLE = 0x00000800L;
   private static final long MASK                = 0x00000FFFL;


   private org.apache.bcel.generic.Type[] types;
   private java.util.Iterator traceNodes;
    private java.util.ArrayList storedTraceNodes = new java.util.ArrayList();
   private sandmark.program.Application application;
   private sandmark.program.Class clazz;
   
   public ConditionGenerator(java.util.Iterator nodes, sandmark.program.Application app){
      application = app;
      traceNodes=nodes;
      TraceNode node = (TraceNode)nodes.next();
      storedTraceNodes.add(node);
      String bbinfo2=null, bbinfo =   
         node.getThreadName() + ":" + 
         node.getClassName()  + ":" +
         node.getMethodName() + node.getMethodSignature() + ":" +
         node.getOffset();
      
      clazz = application.getClass(node.getClassName());

      VarValue[] values = node.getVarValues();
      types = new org.apache.bcel.generic.Type[values.length];
      for (int i=0;i<types.length;i++){
         types[i] = values[i].getType();
      }
   }



   /** This method returns an iterator of InstructionLists that all end
    *  in an IfInstruction branch that may be taken if you were to insert
    *  these instructions right before the nth occurrence of the tracepoint
    *  specified in the constructor. The IfInstructions will all target themselves
    *  to start with, and must be reset.
    */
   public java.util.Iterator getConditions(int nth, boolean taken){
       while(storedTraceNodes.size() < nth && traceNodes.hasNext())
	   storedTraceNodes.add(traceNodes.next());

      if (nth<0 || nth>=storedTraceNodes.size())
         throw new IllegalArgumentException("Argument out of bounds");

      long[] nthfeatures = new long[types.length];
      long[] firsttrue = new long[types.length];
      long[] firstfalse = new long[types.length];


      ///// compute single-var info /////////
      getFeatures(nthfeatures, firsttrue, firstfalse, nth);

      ///// compute variable pair info //////
      java.util.Hashtable areequal = new java.util.Hashtable(nth*nth+10);
      for (int stage=0;stage<=nth;stage++){
         VarValue[] vars = ((TraceNode)storedTraceNodes.get(stage)).getVarValues();
         for (int i=0;i<vars.length;i++){
            for (int j=i+1;j<vars.length;j++){
               if (vars[i].getType().equals(vars[j].getType()) && (vars[i].getType() instanceof org.apache.bcel.generic.BasicType)){
                  // if equal basic types
                  if (vars[i].getValue().equals(vars[j].getValue())){
                     String hash = "equal["+stage+"]("+i+","+j+")";
                     areequal.put(hash, hash);
                  }
               }else if (((vars[i].getType() instanceof org.apache.bcel.generic.ObjectType ||
                          vars[i].getType() instanceof org.apache.bcel.generic.ArrayType) &&
                         (vars[j].getType() instanceof org.apache.bcel.generic.ObjectType || 
                          vars[j].getType() instanceof org.apache.bcel.generic.ArrayType))){
                  // if types are ReferenceTypes but not UninitializedObjectTypes

                  org.apache.bcel.generic.ReferenceType t1 = (org.apache.bcel.generic.ReferenceType)vars[i].getType();
                  org.apache.bcel.generic.ReferenceType t2 = (org.apache.bcel.generic.ReferenceType)vars[j].getType();
                  if (t1.isCastableTo(t2) || t2.isCastableTo(t1)){
                     Object val1 = vars[i].getValue();
                     Object val2 = vars[j].getValue();

                     boolean theseequal = (val1==null && val2==null) || 
                        (val1!=null && val2!=null && (val1 instanceof Integer) && (val2 instanceof Integer) && val1.equals(val2));

                     if (theseequal){
                        String hash = "equal["+stage+"]("+i+","+j+")";
                        areequal.put(hash, hash);
                     }
                  }
               }
            }
         }
      }
      
      for (int i=0;i<types.length;i++){
         for (int j=i+1;j<types.length;j++){
            boolean always = (nth==0 ? false : true);
            for (int stage=0;stage<nth;stage++){
               if (areequal.get("equal["+stage+"]("+i+","+j+")")!=null){
                  String ever = "ever("+i+","+j+")";
                  areequal.put(ever, ever);
               }else{
                  always=false;
               }
            }
            if (always){
               String alwaysstr = "always("+i+","+j+")";
               areequal.put(alwaysstr, alwaysstr);
            }
         }
      }


      // get instruction lists
      java.util.List[] ilists = generateInstructionLists(nthfeatures, firsttrue, firstfalse, nth, areequal, taken);
      org.apache.bcel.generic.InstructionHandle handle;
      org.apache.bcel.generic.IfInstruction ifinstr;


      // compute all pairs
      for (int i=0;i<ilists[0].size();i++){
         for (int j=i+1;j<ilists[0].size();j++){
            org.apache.bcel.generic.InstructionList part1 = 
               ((org.apache.bcel.generic.InstructionList)ilists[0].get(i)).copy();
            org.apache.bcel.generic.InstructionList part2 = 
               ((org.apache.bcel.generic.InstructionList)ilists[0].get(j)).copy();
            
            part1.append(part2);
            handle = part1.append(new org.apache.bcel.generic.IAND());
            if (taken)
               handle = part1.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
            else
               handle = part1.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));             
            ifinstr.setTarget(handle);

            ilists[1].add(part1);
         }
      }

      // compute all triples
      for (int i=0;i<ilists[0].size();i++){
         for (int j=i+1;j<ilists[0].size();j++){
            for (int k=j+1;k<ilists[0].size();k++){
               org.apache.bcel.generic.InstructionList part1 = 
                  ((org.apache.bcel.generic.InstructionList)ilists[0].get(i)).copy();
               org.apache.bcel.generic.InstructionList part2 = 
                  ((org.apache.bcel.generic.InstructionList)ilists[0].get(j)).copy();
               org.apache.bcel.generic.InstructionList part3 = 
                  ((org.apache.bcel.generic.InstructionList)ilists[0].get(k)).copy();
               
               part1.append(part2);
               part1.append(new org.apache.bcel.generic.IAND());
               part1.append(part3);
               handle = part1.append(new org.apache.bcel.generic.IAND());
               if (taken)
                  handle = part1.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
               else
                  handle = part1.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));                
               ifinstr.setTarget(handle);
               
               ilists[1].add(part1);
            }
         }
      }

      return ilists[1].iterator();
   }


   private void generatePAIRSEQUAL(java.util.Hashtable areequal, VarValue[] vars, int nth, 
                                   org.apache.bcel.generic.Instruction[][] fetch, boolean taken,
                                   java.util.List combinable, java.util.List standalone){

      for (int i=0;i<vars.length;i++){
         for (int j=i+1;j<vars.length;j++){
            String equal = "equal["+nth+"]("+i+","+j+")";
            String always = "always("+i+","+j+")";
            String ever = "ever("+i+","+j+")";
        
            if ((areequal.get(ever)==null && areequal.get(equal)!=null) || (areequal.get(always)!=null && areequal.get(equal)==null)){
               boolean which = (areequal.get(ever)==null && areequal.get(equal)!=null);

               org.apache.bcel.generic.InstructionList clist = 
                  new org.apache.bcel.generic.InstructionList();
               org.apache.bcel.generic.InstructionList slist = 
                  new org.apache.bcel.generic.InstructionList();
               org.apache.bcel.generic.InstructionHandle handle = null;
               org.apache.bcel.generic.IfInstruction ifinstr;
               
               for (int k=0;k<fetch[i].length;k++){
                  clist.append(fetch[i][k]);
                  handle = slist.append(fetch[i][k]);
               }
               
               for (int k=0;k<fetch[j].length;k++){
                  clist.append(fetch[j][k]);
                  handle = slist.append(fetch[j][k]);
               }
               
               if (vars[i].getType() instanceof org.apache.bcel.generic.ReferenceType){
                  boolean cantest=true;
                  
                  if (which){
                     // first true, look for several false falses (two unhashables that may be equal)
                     for (int k=0;k<nth;k++){
                        VarValue val1 = ((TraceNode)storedTraceNodes.get(k)).getVarValues()[i];
                        VarValue val2 = ((TraceNode)storedTraceNodes.get(k)).getVarValues()[j];
                        if (val1.isNonnullUnhashable() && val2.isNonnullUnhashable()){
                           cantest=false;
                           break;
                        }
                     }
                  }else{
                     // first false
                     if (vars[i].isNonnullUnhashable() && vars[j].isNonnullUnhashable())
                        cantest = false;
                  }
                     
                  if (cantest){
                     if (which==taken)
                        handle = slist.append(ifinstr = new org.apache.bcel.generic.IF_ACMPEQ(handle));
                     else
                        handle = slist.append(ifinstr = new org.apache.bcel.generic.IF_ACMPNE(handle));
                     ifinstr.setTarget(handle);
                     standalone.add(slist);
                  }
               }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.DOUBLE)){
                  slist.append(new org.apache.bcel.generic.DCMPG());
                  if (which==taken)
                     handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
                  else
                     handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
                  ifinstr.setTarget(handle);
                  standalone.add(slist);

                  clist.append(new org.apache.bcel.generic.DCMPG());
                  clist.append(new org.apache.bcel.generic.ICONST(1));
                  clist.append(new org.apache.bcel.generic.IAND());
                  if (taken){
                     clist.append(new org.apache.bcel.generic.ICONST(1));
                     clist.append(new org.apache.bcel.generic.IXOR());
                  }
                  combinable.add(clist);
                  // puts a 1/0

               }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.FLOAT)){
                  slist.append(new org.apache.bcel.generic.FCMPG());
                  if (which==taken)
                     handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
                  else
                     handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
                  ifinstr.setTarget(handle);
                  standalone.add(slist);

                  clist.append(new org.apache.bcel.generic.FCMPG());
                  clist.append(new org.apache.bcel.generic.ICONST(1));
                  clist.append(new org.apache.bcel.generic.IAND());
                  if (taken){
                     clist.append(new org.apache.bcel.generic.ICONST(1));
                     clist.append(new org.apache.bcel.generic.IXOR());
                  }
                  combinable.add(clist);
                  // puts a 1/0

               }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.LONG)){
                  slist.append(new org.apache.bcel.generic.LCMP());
                  if (which==taken)
                     handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
                  else
                     handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
                  ifinstr.setTarget(handle);
                  standalone.add(slist);

                  clist.append(new org.apache.bcel.generic.LCMP());
                  clist.append(new org.apache.bcel.generic.ICONST(1));
                  clist.append(new org.apache.bcel.generic.IAND());
                  if (taken){
                     clist.append(new org.apache.bcel.generic.ICONST(1));
                     clist.append(new org.apache.bcel.generic.IXOR());
                  }
                  combinable.add(clist);
                  // puts a 1/0

               }else{
                  // int, char, byte, short, boolean
                  
                  if (which==taken)
                     handle = slist.append(ifinstr = new org.apache.bcel.generic.IF_ICMPEQ(handle));
                  else
                     handle = slist.append(ifinstr = new org.apache.bcel.generic.IF_ICMPNE(handle));
                  ifinstr.setTarget(handle);
                  standalone.add(slist);

                  clist.append(new org.apache.bcel.generic.ISUB());
                  clist.append(new org.apache.bcel.generic.I2L());
                  clist.append(new org.apache.bcel.generic.LCONST(0L));
                  clist.append(new org.apache.bcel.generic.LCMP());
                  clist.append(new org.apache.bcel.generic.ICONST(1));
                  clist.append(new org.apache.bcel.generic.IAND());
                  if (taken){
                     clist.append(new org.apache.bcel.generic.ICONST(1));
                     clist.append(new org.apache.bcel.generic.IXOR());
                  }
                  combinable.add(clist);
                  // puts a 1/0

               }
            }
         }
      }
   }


   private void getFeatures(long[] nthfeatures, long[] firsttrue, long[] firstfalse, int nth){
      long features[][] = new long[nth+1][types.length];

      for (int stage=0;stage<=nth;stage++){
         for (int i=0;i<types.length;i++){
            if (types[i] instanceof org.apache.bcel.generic.BasicType){
               features[stage][i] = getBasicTypeFeatures(stage, ((TraceNode)storedTraceNodes.get(stage)).getVarValues()[i], i);
            }else{
               features[stage][i] = getObjectTypeFeatures(stage, ((TraceNode)storedTraceNodes.get(stage)).getVarValues()[i], i);
            }
         }
      }

      long[] always=new long[types.length];
      long[] ever=new long[types.length];
      
      for (int i=0;i<types.length;i++){
         always[i] = (nth==0 ? 0L : -1L);
         for (int j=0;j<nth;j++){
            always[i] &= features[j][i];
            ever[i] |= features[j][i];
         }

         firstfalse[i] = (always[i] & ~features[nth][i]) & MASK;
         firsttrue[i] = ((~ever[i]) & features[nth][i]) & MASK;
      }

      for (int i=0;i<types.length;i++)
         nthfeatures[i] = features[nth][i];
   }


   private static void printFeatures(long features){
      System.out.print("{");
      if ((features&ISUNIQUESOFAR)!=0) System.out.print(" ISUNIQUESOFAR");
      if ((features&ISEVEN)!=0) System.out.print(" ISEVEN");
      if ((features&ISGTZERO)!=0) System.out.print(" ISGTZERO");
      if ((features&ISLTZERO)!=0) System.out.print(" ISLTZERO");
      if ((features&ISNAN)!=0) System.out.print(" ISNAN");
      if ((features&ISPOSINF)!=0) System.out.print(" ISPOSINF");
      if ((features&ISNEGINF)!=0) System.out.print(" ISNEGINF");
      if ((features&ISNULL)!=0) System.out.print(" ISNULL");
      if ((features&ISLOWERCASE)!=0) System.out.print(" ISLOWERCASE");
      if ((features&ISUPPERCASE)!=0) System.out.print(" ISUPPERCASE");
      if ((features&ISDIGIT)!=0) System.out.print(" ISDIGIT");
      if ((features&ISNONNULLUNHASHABLE)!=0) System.out.print(" ISNONNULLUNHASHABLE");
      System.out.println(" }");
   }

   
   private void generateISUNIQUESOFAR(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                      VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                      org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                      java.util.List combinable, java.util.List standalone){

      org.apache.bcel.generic.InstructionList clist = 
         new org.apache.bcel.generic.InstructionList();
      org.apache.bcel.generic.InstructionList slist = 
         new org.apache.bcel.generic.InstructionList();
      org.apache.bcel.generic.InstructionHandle handle;
      org.apache.bcel.generic.IfInstruction ifinstr;


      if ((nthfeatures[i] & ISUNIQUESOFAR & ~ISNAN)!=0){
         for (int k=0;k<fetch.length;k++){
            slist.append(fetch[k]);
            clist.append(fetch[k]);
         }
         
         if (vars[i].getType() instanceof org.apache.bcel.generic.ReferenceType){
            // exclude this case
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.DOUBLE)){
            Double value = (Double)vars[i].getValue();
            
            handle = slist.append(new org.apache.bcel.generic.LDC2_W(cpg.addDouble(value.doubleValue())));
            slist.append(new org.apache.bcel.generic.DCMPG());
            if (taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));             
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.LDC2_W(cpg.addDouble(value.doubleValue())));
            clist.append(new org.apache.bcel.generic.DCMPG());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (taken){
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.FLOAT)){
            Float value = (Float)vars[i].getValue();
            
            handle = slist.append(new org.apache.bcel.generic.LDC(cpg.addFloat(value.floatValue())));
            slist.append(new org.apache.bcel.generic.FCMPG());
            if (taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));             
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.LDC(cpg.addFloat(value.floatValue())));
            clist.append(new org.apache.bcel.generic.FCMPG());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (taken){
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.LONG)){
            Long value = (Long)vars[i].getValue();
            
            handle = slist.append(new org.apache.bcel.generic.LDC2_W(cpg.addLong(value.longValue())));
            slist.append(new org.apache.bcel.generic.LCMP());
            if (taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));             
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.LDC2_W(cpg.addLong(value.longValue())));
            clist.append(new org.apache.bcel.generic.LCMP());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (taken){
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else{
            // int, short, byte, char, boolean
            // ((b-a)>>31 | (a-b)>>31) ^ 1
            int value = 0;
            Object objvalue = vars[i].getValue();
            if (objvalue instanceof Byte){
               value = (int)((Byte)objvalue).byteValue();
            }else if (objvalue instanceof Character){
               value = 0xFFFF & ((Character)objvalue).charValue();
            }else if (objvalue instanceof Short){
               value = (int)((Short)objvalue).shortValue();
            }else if (objvalue instanceof Boolean){
               value = ((Boolean)objvalue).booleanValue() ? 1 : 0;
            }else{
               value = ((Integer)objvalue).intValue();
            }
            
            handle = slist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(value)));
            if (taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IF_ICMPEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IF_ICMPNE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.I2L());
            clist.append(new org.apache.bcel.generic.LDC2_W(cpg.addLong((long)value)));
            clist.append(new org.apache.bcel.generic.LCMP());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (taken){
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
         }           
      }
   }


   private void generateISEVEN(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                               VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                               org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                               java.util.List combinable, java.util.List standalone){
      boolean which;
      
      if ((firsttrue[i] & ISEVEN)!=0 || (firstfalse[i] & ISEVEN)!=0){
         which = ((firsttrue[i] & ISEVEN)!=0);

         org.apache.bcel.generic.InstructionList clist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionList slist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionHandle handle;
         org.apache.bcel.generic.IfInstruction ifinstr;
         
         for (int k=0;k<fetch.length;k++){
            slist.append(fetch[k]);
            clist.append(fetch[k]);
         }
         
         if (vars[i].getType() instanceof org.apache.bcel.generic.ReferenceType){
            // do nothing
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.LONG)){
            handle = slist.append(new org.apache.bcel.generic.LCONST(1L));
            slist.append(new org.apache.bcel.generic.LAND());
            slist.append(new org.apache.bcel.generic.L2I());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));             
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.LCONST(1L));
            clist.append(new org.apache.bcel.generic.LAND());
            clist.append(new org.apache.bcel.generic.L2I());
            if (which){
               // which==true
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else{
            // int, short, byte, char, boolean
            
            handle = slist.append(new org.apache.bcel.generic.ICONST(1));
            slist.append(new org.apache.bcel.generic.IAND());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (which){
               // which==true
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
         }           
      }
   }


   private void generateISGTZERO(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                 VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                 org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                 java.util.List combinable, java.util.List standalone){
      boolean which;

      if ((firsttrue[i] & ISGTZERO)!=0 || (firstfalse[i] & ISGTZERO)!=0){
         which = ((firsttrue[i] & ISGTZERO)!=0);

         org.apache.bcel.generic.InstructionList clist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionList slist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionHandle handle;
         org.apache.bcel.generic.IfInstruction ifinstr;
         
         
         if (vars[i].getType() instanceof org.apache.bcel.generic.ReferenceType){
            // do nothing
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.LONG)){
            for (int k=0;k<fetch.length;k++)
               slist.append(fetch[k]);
            handle = slist.append(new org.apache.bcel.generic.LCONST(0L));
            slist.append(new org.apache.bcel.generic.LCMP());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFGT(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFLE(handle));             
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            for (int k=0;k<fetch.length;k++)
               clist.append(fetch[k]);
            clist.append(new org.apache.bcel.generic.LCONST(0L));
            clist.append(new org.apache.bcel.generic.LCMP());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IADD());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IUSHR());
            if (!which){
               // which==false
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.DOUBLE)){
            handle = slist.append(new org.apache.bcel.generic.DCONST(0.0));
            for (int k=0;k<fetch.length;k++)
               slist.append(fetch[k]);
            slist.append(new org.apache.bcel.generic.DCMPG());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFLT(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFGE(handle));             
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.DCONST(0.0));
            for (int k=0;k<fetch.length;k++)
               clist.append(fetch[k]);
            clist.append(new org.apache.bcel.generic.DCMPG());
            clist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(31)));
            clist.append(new org.apache.bcel.generic.IUSHR());
            if (!which){
               // which==false
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.FLOAT)){
            handle = slist.append(new org.apache.bcel.generic.FCONST(0.0f));
            for (int k=0;k<fetch.length;k++)
               slist.append(fetch[k]);
            slist.append(new org.apache.bcel.generic.FCMPG());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFLT(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFGE(handle));             
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.FCONST(0.0f));
            for (int k=0;k<fetch.length;k++)
               clist.append(fetch[k]);
            clist.append(new org.apache.bcel.generic.FCMPG());
            clist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(31)));
            clist.append(new org.apache.bcel.generic.IUSHR());
            if (!which){
               // which==false
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else{
            // int, byte, short, boolean, char
            
            handle = null;
            for (int k=0;k<fetch.length;k++)
               handle = slist.append(fetch[k]);
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFGT(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFLE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            for (int k=0;k<fetch.length;k++)
               clist.append(fetch[k]);
            clist.append(new org.apache.bcel.generic.I2L());
            clist.append(new org.apache.bcel.generic.LCONST(0L));
            clist.append(new org.apache.bcel.generic.LCMP());
            if(which){
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IADD());
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IUSHR());
            }else{
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.ISUB());
               clist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(31)));
               clist.append(new org.apache.bcel.generic.IUSHR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }
      }
   }

   private void generateISLTZERO(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                 VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                 org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                 java.util.List combinable, java.util.List standalone){
      boolean which;

      if ((firsttrue[i] & ISLTZERO)!=0 || (firstfalse[i] & ISLTZERO)!=0){
         which = ((firsttrue[i] & ISLTZERO)!=0);
         
         org.apache.bcel.generic.InstructionList clist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionList slist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionHandle handle;
         org.apache.bcel.generic.IfInstruction ifinstr;
         
         
         if (vars[i].getType() instanceof org.apache.bcel.generic.ReferenceType){
            // do nothing
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.LONG)){
            for (int k=0;k<fetch.length;k++)
               slist.append(fetch[k]);
            handle = slist.append(new org.apache.bcel.generic.LCONST(0L));
            slist.append(new org.apache.bcel.generic.LCMP());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFLT(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFGE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            for (int k=0;k<fetch.length;k++)
               clist.append(fetch[k]);
            clist.append(new org.apache.bcel.generic.LCONST(0L));
            clist.append(new org.apache.bcel.generic.LCMP());
            clist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(31)));
            clist.append(new org.apache.bcel.generic.IUSHR());
            if (!which){
               // which==false
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.DOUBLE)){
            handle = slist.append(new org.apache.bcel.generic.DCONST(0.0));
            for (int k=0;k<fetch.length;k++)
               slist.append(fetch[k]);
            slist.append(new org.apache.bcel.generic.DCMPL());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFGT(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFLE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            
            clist.append(new org.apache.bcel.generic.DCONST(0.0));
            for (int k=0;k<fetch.length;k++)
               clist.append(fetch[k]);
            clist.append(new org.apache.bcel.generic.DCMPL());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IADD());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IUSHR());
            if (!which){
               // which==false
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else if (vars[i].getType().equals(org.apache.bcel.generic.Type.FLOAT)){
            handle = slist.append(new org.apache.bcel.generic.FCONST(0.0f));
            for (int k=0;k<fetch.length;k++)
               slist.append(fetch[k]);
            slist.append(new org.apache.bcel.generic.FCMPL());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFGT(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFLE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.FCONST(0.0f));
            for (int k=0;k<fetch.length;k++)
               clist.append(fetch[k]);
            clist.append(new org.apache.bcel.generic.FCMPL());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IADD());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IUSHR());
            if (!which){
               // which==false
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else{
            // int, byte, short, boolean, char
            handle = null;
            for (int k=0;k<fetch.length;k++)
               handle = slist.append(fetch[k]);
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFLT(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFGE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            for (int k=0;k<fetch.length;k++)
               clist.append(fetch[k]);
            clist.append(new org.apache.bcel.generic.I2L());               
            clist.append(new org.apache.bcel.generic.LCONST(0L));
            clist.append(new org.apache.bcel.generic.LCMP());
            clist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(31)));
            clist.append(new org.apache.bcel.generic.IUSHR());
            if (!which){
               // which==false
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }
      }
   }


   private void generateISPOSINF(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                 VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                 org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                 java.util.List combinable, java.util.List standalone){
      boolean which;
      
      if ((firsttrue[i] & ISPOSINF)!=0 || (firstfalse[i] & ISPOSINF)!=0){
         which = ((firsttrue[i] & ISPOSINF)!=0);

         org.apache.bcel.generic.InstructionList clist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionList slist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionHandle handle;
         org.apache.bcel.generic.IfInstruction ifinstr;
         
         for (int k=0;k<fetch.length;k++){
            clist.append(fetch[k]);
            slist.append(fetch[k]);
         }
         
         if (vars[i].getType().equals(org.apache.bcel.generic.Type.DOUBLE)){
            handle = slist.append(new org.apache.bcel.generic.LDC2_W(cpg.addDouble(Double.POSITIVE_INFINITY)));
            slist.append(new org.apache.bcel.generic.DCMPG());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.LDC2_W(cpg.addDouble(Double.POSITIVE_INFINITY)));
            clist.append(new org.apache.bcel.generic.DCMPG());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (which){
               // which==true
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else{
            handle = slist.append(new org.apache.bcel.generic.LDC(cpg.addFloat(Float.POSITIVE_INFINITY)));
            slist.append(new org.apache.bcel.generic.FCMPG());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            
            clist.append(new org.apache.bcel.generic.LDC(cpg.addFloat(Float.POSITIVE_INFINITY)));
            clist.append(new org.apache.bcel.generic.FCMPG());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (which){
               // which==true
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());              
            }
            combinable.add(clist);
            // puts a 1/0
            
         }
      }
   }
   
   private void generateISNEGINF(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                 VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                 org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                 java.util.List combinable, java.util.List standalone){
      boolean which;

      if ((firsttrue[i] & ISNEGINF)!=0 || (firstfalse[i] & ISNEGINF)!=0){
         which = ((firsttrue[i] & ISPOSINF)!=0);

         org.apache.bcel.generic.InstructionList clist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionList slist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionHandle handle;
         org.apache.bcel.generic.IfInstruction ifinstr;
         
         for (int k=0;k<fetch.length;k++){
            clist.append(fetch[k]);
            slist.append(fetch[k]);
         }
         
         if (vars[i].getType().equals(org.apache.bcel.generic.Type.DOUBLE)){
            handle = slist.append(new org.apache.bcel.generic.LDC2_W(cpg.addDouble(Double.NEGATIVE_INFINITY)));
            slist.append(new org.apache.bcel.generic.DCMPL());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            
            clist.append(new org.apache.bcel.generic.LDC2_W(cpg.addDouble(Double.NEGATIVE_INFINITY)));
            clist.append(new org.apache.bcel.generic.DCMPL());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (which){
               // which==true
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());              
            }
            combinable.add(clist);
            // puts a 1/0
            
         }else{
            handle = slist.append(new org.apache.bcel.generic.LDC(cpg.addFloat(Float.NEGATIVE_INFINITY)));
            slist.append(new org.apache.bcel.generic.FCMPL());
            if (which==taken)
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
            else
               handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
            ifinstr.setTarget(handle);
            standalone.add(slist);
            
            clist.append(new org.apache.bcel.generic.LDC(cpg.addFloat(Float.NEGATIVE_INFINITY)));
            clist.append(new org.apache.bcel.generic.FCMPL());
            clist.append(new org.apache.bcel.generic.ICONST(1));
            clist.append(new org.apache.bcel.generic.IAND());
            if (which){
               // which==true
               clist.append(new org.apache.bcel.generic.ICONST(1));
               clist.append(new org.apache.bcel.generic.IXOR());              
            }
            combinable.add(clist);
            // puts a 1/0
         }
      }
   }

   private void generateISNULL(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                               VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                               org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                               java.util.List combinable, java.util.List standalone){
      boolean which;

      if ((firsttrue[i] & ISNULL)!=0 || (firstfalse[i] & ISNULL)!=0){
         which = ((firsttrue[i] & ISNULL)!=0);

         org.apache.bcel.generic.InstructionList slist = 
            new org.apache.bcel.generic.InstructionList();
         org.apache.bcel.generic.InstructionHandle handle;
         org.apache.bcel.generic.IfInstruction ifinstr;
         
         handle = null;
         for (int k=0;k<fetch.length;k++)
            handle = slist.append(fetch[k]);

         if (which==taken)
            handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNULL(handle));
         else
            handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNONNULL(handle));           
         ifinstr.setTarget(handle);
         standalone.add(slist);
      }
   }

   private void generateISINTINRANGE(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                     VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                     org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                     java.util.List combinable, java.util.List standalone, boolean which, int min, int max){

      org.apache.bcel.generic.InstructionList slist = 
         new org.apache.bcel.generic.InstructionList();
      org.apache.bcel.generic.InstructionList clist = 
         new org.apache.bcel.generic.InstructionList();
      org.apache.bcel.generic.InstructionHandle handle;
      org.apache.bcel.generic.IfInstruction ifinstr;

      int val1 = max+1;
      int offset = val1-(min-1);
      
      for (int k=0;k<fetch.length;k++){
         slist.append(fetch[k]);
         clist.append(fetch[k]);
      }
      
      slist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(val1)));
      slist.append(new org.apache.bcel.generic.ISUB());
      slist.append(new org.apache.bcel.generic.DUP());
      slist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(offset)));
      slist.append(new org.apache.bcel.generic.IADD());
      slist.append(new org.apache.bcel.generic.INEG());
      slist.append(new org.apache.bcel.generic.IAND());
      slist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(31)));            
      slist.append(new org.apache.bcel.generic.IUSHR());
      // on stack: isUpperCase, isLowerCase
      handle = slist.append(new org.apache.bcel.generic.IOR());
      if (which==taken)
         handle = slist.append(ifinstr = new org.apache.bcel.generic.IFNE(handle));
      else
         handle = slist.append(ifinstr = new org.apache.bcel.generic.IFEQ(handle));
      ifinstr.setTarget(handle);
      standalone.add(slist);
      
      clist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(val1)));
      clist.append(new org.apache.bcel.generic.ISUB());
      clist.append(new org.apache.bcel.generic.DUP());
      clist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(offset)));
      clist.append(new org.apache.bcel.generic.IADD());
      clist.append(new org.apache.bcel.generic.INEG());
      clist.append(new org.apache.bcel.generic.IAND());
      clist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(31)));            
      clist.append(new org.apache.bcel.generic.IUSHR());
      clist.append(new org.apache.bcel.generic.IOR());
      if (!which){
         // which==false
         clist.append(new org.apache.bcel.generic.ICONST(1));
         clist.append(new org.apache.bcel.generic.IXOR());
      }

      combinable.add(clist);
      // puts a 1/0
   }


   private void generateISLOWERCASE(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                    VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                    org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                    java.util.List combinable, java.util.List standalone){
      boolean which;

      if ((firsttrue[i] & ISLOWERCASE)!=0 || (firstfalse[i] & ISLOWERCASE)!=0){
         which = ((firsttrue[i] & ISLOWERCASE)!=0);

         generateISINTINRANGE(nthfeatures, firsttrue, firstfalse, vars, i, fetch,
                              cpg, taken, combinable, standalone, which, 65, 90);
      }
   }

   private void generateISUPPERCASE(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                    VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                    org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                    java.util.List combinable, java.util.List standalone){
      boolean which;

      if ((firsttrue[i] & ISUPPERCASE)!=0 || (firstfalse[i] & ISUPPERCASE)!=0){
         which = ((firsttrue[i] & ISUPPERCASE)!=0);

         generateISINTINRANGE(nthfeatures, firsttrue, firstfalse, vars, i, fetch,
                              cpg, taken, combinable, standalone, which, 97, 122);
      }
   }

   private void generateISDIGIT(long[] nthfeatures, long[] firsttrue, long[] firstfalse,
                                VarValue[] vars, int i, org.apache.bcel.generic.Instruction[] fetch, 
                                org.apache.bcel.generic.ConstantPoolGen cpg, boolean taken, 
                                java.util.List combinable, java.util.List standalone){
      boolean which;

      
      if ((firsttrue[i] & ISDIGIT)!=0 || (firstfalse[i] & ISDIGIT)!=0){
         which = ((firsttrue[i] & ISDIGIT)!=0);
         
         generateISINTINRANGE(nthfeatures, firsttrue, firstfalse, vars, i, fetch,
                              cpg, taken, combinable, standalone, which, 48, 57);
      }
   }


   private java.util.List[] generateInstructionLists(long[] nthfeatures, long[] firsttrue, 
                                                     long[] firstfalse, int nth, java.util.Hashtable areequal, 
                                                     boolean taken){

      TraceNode trace = (TraceNode)storedTraceNodes.get(nth);
      java.util.Vector combinable = new java.util.Vector(100);
      java.util.Vector standalone = new java.util.Vector(100);
      org.apache.bcel.generic.ConstantPoolGen cpg = clazz.getConstantPool();
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(cpg);
      VarValue[] vars = trace.getVarValues();
      
      org.apache.bcel.generic.Instruction[][] fetch = 
         new org.apache.bcel.generic.Instruction[vars.length][];
      for (int i=0;i<vars.length;i++){
         fetch[i] = getFetch(vars[i]);

         generateISUNIQUESOFAR(nthfeatures, firsttrue, firstfalse, vars, i, 
                               fetch[i], cpg, taken, combinable, standalone);

         generateISEVEN(nthfeatures, firsttrue, firstfalse, vars, i, 
                        fetch[i], cpg, taken, combinable, standalone);

         generateISGTZERO(nthfeatures, firsttrue, firstfalse, vars, i, 
                          fetch[i], cpg, taken, combinable, standalone);

         generateISLTZERO(nthfeatures, firsttrue, firstfalse, vars, i, 
                          fetch[i], cpg, taken, combinable, standalone);

         generateISPOSINF(nthfeatures, firsttrue, firstfalse, vars, i, 
                          fetch[i], cpg, taken, combinable, standalone);

         generateISNEGINF(nthfeatures, firsttrue, firstfalse, vars, i, 
                          fetch[i], cpg, taken, combinable, standalone);

         generateISNULL(nthfeatures, firsttrue, firstfalse, vars, i, 
                        fetch[i], cpg, taken, combinable, standalone);

         generateISLOWERCASE(nthfeatures, firsttrue, firstfalse, vars, i, 
                             fetch[i], cpg, taken, combinable, standalone);

         generateISUPPERCASE(nthfeatures, firsttrue, firstfalse, vars, i, 
                             fetch[i], cpg, taken, combinable, standalone);

         generateISDIGIT(nthfeatures, firsttrue, firstfalse, vars, i, 
                         fetch[i], cpg, taken, combinable, standalone);
      }

      generatePAIRSEQUAL(areequal, vars, nth, fetch, taken, combinable, standalone);

      return new java.util.List[]{combinable, standalone};
   }

   
   private org.apache.bcel.generic.Instruction[] getFetch(VarValue var){
      org.apache.bcel.generic.ConstantPoolGen cpg = clazz.getConstantPool();

      switch(var.getKind()){
      case VarValue.LOCAL:{
         if (var.getType().equals(org.apache.bcel.generic.Type.FLOAT)){
            return new org.apache.bcel.generic.Instruction[]{
               new org.apache.bcel.generic.FLOAD(var.getLocalIndex())
            };
         }else if (var.getType().equals(org.apache.bcel.generic.Type.DOUBLE)){
            return new org.apache.bcel.generic.Instruction[]{
               new org.apache.bcel.generic.DLOAD(var.getLocalIndex())
            };
         }else if (var.getType().equals(org.apache.bcel.generic.Type.LONG)){
            return new org.apache.bcel.generic.Instruction[]{
               new org.apache.bcel.generic.LLOAD(var.getLocalIndex())
            };
         }else if (var.getType() instanceof org.apache.bcel.generic.ReferenceType){
            return new org.apache.bcel.generic.Instruction[]{
               new org.apache.bcel.generic.ALOAD(var.getLocalIndex())
            };
         }else{
            // int, char, bool, short, byte
            return new org.apache.bcel.generic.Instruction[]{
               new org.apache.bcel.generic.ILOAD(var.getLocalIndex())
            };
         }
      }

      case VarValue.INSTANCE:{
         int fieldref = cpg.lookupFieldref(clazz.getName(), var.getName(), var.getType().getSignature());
         return new org.apache.bcel.generic.Instruction[]{
            new org.apache.bcel.generic.ALOAD(0),
            new org.apache.bcel.generic.GETFIELD(fieldref)
         };
      }

      case VarValue.STATIC:{
         int fieldref = cpg.lookupFieldref(clazz.getName(), var.getName(), var.getType().getSignature());
         return new org.apache.bcel.generic.Instruction[]{
            new org.apache.bcel.generic.GETSTATIC(fieldref)
         };
      }

      default:
         return null;
      }
   }


   private long getBasicTypeFeatures(int stage, VarValue var, int varindex){
      // var is the VarValue for the desired variable and stage

      long result = 0L;
      Object objvalue = var.getValue();

      // test ISUNIQUESOFAR
      result |= ISUNIQUESOFAR;
      for (int i=0;i<stage;i++){
         if (objvalue.equals(((TraceNode)storedTraceNodes.get(i)).getVarValues()[varindex].getValue())){
            result &= ~ISUNIQUESOFAR;
            break;
         }
      }
      

      if ((objvalue instanceof Float) || (objvalue instanceof Double)){
         double value=0.0;

         // get the value and turn it into a double (safe?)
         if (objvalue instanceof Float){
            value = (double)((Float)objvalue).floatValue();
         }else{
            value = ((Double)objvalue).doubleValue();
         }

         // test ISGTZERO
         result |= (value>0.0 ? ISGTZERO : 0);
         
         // test ISLTZERO
         result |= (value<0.0 ? ISLTZERO : 0);
         
         // test ISNAN
         result |= (Double.isNaN(value) ? ISNAN : 0);

         // test ISPOSINF
         result |= ((value==Double.POSITIVE_INFINITY) ? ISPOSINF : 0);

         // test ISNEGINF
         result |= ((value==Double.NEGATIVE_INFINITY) ? ISNEGINF : 0);
      }else{
         long value = 0;
         boolean isintegral=true;

         // get the value and turn it into a long
         if (objvalue instanceof Byte){
            value = (long)((Byte)objvalue).byteValue();
         }else if (objvalue instanceof Character){
            value = (long)((Character)objvalue).charValue();
         }else if (objvalue instanceof Boolean){
            value = ((Boolean)objvalue).booleanValue() ? 1 : 0;
         }else if (objvalue instanceof Integer){
            value = (long)((Integer)objvalue).intValue();
         }else if (objvalue instanceof Short){
            value = (long)((Short)objvalue).shortValue();
         }else{ // Long
            value = ((Long)objvalue).longValue();
            isintegral=false;
         }

         result |= integralFeatures(value, isintegral);
      }
      return result;
   }

   private long integralFeatures(long value, boolean isSmallIntegral){
      long result=0;

      // test ISEVEN
      result |= (((value & 0x1L)==0) ? ISEVEN : 0);

      // test ISGTZERO
      result |= (value>0 ? ISGTZERO : 0);
      
      // test ISLTZERO
      result |= (value<0 ? ISLTZERO : 0);
      
      if (isSmallIntegral){
         // test ISLOWERCASE
         result |= ((97<=value && value<=122) ? ISLOWERCASE : 0);
         
         // test ISUPPERCASE
         result |= ((65<=value && value<=90) ? ISUPPERCASE : 0);
         
         // test ISDIGIT
         result |= ((48<=value && value<=57) ? ISDIGIT : 0);
      }
      return result;
   }

   
   private long getObjectTypeFeatures(int stage, VarValue var, int varindex){
      long result=0;

      Object objvalue = var.getValue();
      
      // test for ISUNIQUESOFAR
      if (objvalue==null){
         result |= ISNULL;
         result |= ISUNIQUESOFAR;

         for (int i=0;i<stage;i++){
            Object obj = ((TraceNode)storedTraceNodes.get(i)).getVarValues()[varindex].getValue();
            if (obj==null){
               result &= ~ISUNIQUESOFAR;
               break;
            }
         }
      }else if (var.isNonnullUnhashable()){
         result |= ISNONNULLUNHASHABLE;
         result |= ISUNIQUESOFAR;

         for (int i=0;i<stage;i++){
            Object obj = ((TraceNode)storedTraceNodes.get(i)).getVarValues()[varindex].getValue();
            if (obj!=null){
               result &= ~ISUNIQUESOFAR;
               break;
            }
         }
      }else{
         // nonnull hashable object
         result |= ISUNIQUESOFAR;
         
         for (int i=0;i<stage;i++){
            Object obj = ((TraceNode)storedTraceNodes.get(i)).getVarValues()[varindex].getValue();
            if (obj!=null && obj.equals(objvalue)){
               result &= ~ISUNIQUESOFAR;
               break;
            }
         }
      }

      return result;
   }

   public static void main(String args[]) throws Exception{
      java.util.Vector list = new java.util.Vector(100);
      if (args.length<3)
         System.exit(0);
      
      sandmark.program.Application app = new sandmark.program.Application(args[1]);
      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(args[0]));
      while(reader.ready())
         list.add(0, reader.readLine().trim());
      reader.close();

      int nth = Integer.parseInt(args[2]);

      TraceNode[] nodes = null;new Analyzer(list.iterator()).getTrace("main"); //XXXash need to use TraceIndexer

      ConditionGenerator cg = new ConditionGenerator(null, app); //XXXash need an iterator
      for (java.util.Iterator iter = cg.getConditions(nth, true); iter.hasNext();){
         System.out.println(iter.next());
         System.out.println();
      }
      System.out.println("---------------");
      for (java.util.Iterator iter = cg.getConditions(nth, false); iter.hasNext();){
         System.out.println(iter.next());
         System.out.println();
      }
   }


}
