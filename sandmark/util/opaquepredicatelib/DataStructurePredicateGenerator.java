package sandmark.util.opaquepredicatelib;

/**
 * DynamicStructure OpaquePredicates
 * @author Ashok P. Ramasamy Venkatraj (ashok@cs.arizona.edu)
 * DynamicStructure OpaquePredicates constructs a set of dynamic structures ,then 
 * maintain global  pointers to these structures. Now they define opaquepredicates
 * by performing operations on the dynamic structures while maintaining some invariants
 */
public abstract class DataStructurePredicateGenerator extends OpaquePredicateGenerator {
   private static final boolean DEBUG=true;
   private static final String SMNODE_CURRENT_PATH = 
      "/sandmark/util/opaquepredicatelib/smNode.class";
   private static final String SMNODE_CLASS_NAME =
      "sandmark.util.opaquepredicatelib.smNode";

   sandmark.program.Class addSMNode(sandmark.program.Application app) {
      sandmark.program.Class clazz = app.getClass(SMNODE_CLASS_NAME);
      if(clazz != null)
         return clazz;

      try {
         java.io.InputStream smNodeStream =
            getClass().getResourceAsStream
            (SMNODE_CURRENT_PATH);
         org.apache.bcel.classfile.JavaClass jc = 
            new org.apache.bcel.classfile.ClassParser
            (smNodeStream,SMNODE_CURRENT_PATH).parse();
         return new sandmark.program.LocalClass(app,jc);
      } catch(Exception e) {
         throw new Error("couldn't get smNode class");
      }
   }

   public boolean canInsertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {
      return true;
   }
}

class DSPGF1 extends DataStructurePredicateGenerator {
   
   /** This method takes the methodgen as input and inserts an Opaquely false predicate before BasicBlock ,bb_ins_before.
    *  This particular module provides a bytecode embedding  of the following code listing 
    *  Node n= new smNode();
    *  smNode a= n.Insert(5,1,null);      
    *  smNode b= n.Insert(7,8,null);
    *  if(a.Move(1)== b.Move(2))
    *	    System.out.println(" Wrong :Isn't this supposed to be false ?");
    *	else
    *    System.out.println("Right");
    */
   public void insertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {
      sandmark.program.Class smNode = addSMNode(method.getApplication());
      sandmark.program.Method smNodeInit = smNode.getMethod("<init>","()V");
      sandmark.program.Method smNodeMove = 
         smNode.getMethod
         ("Move","(I)Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeInsert = 
         smNode.getMethod
         ("Insert","(IILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");

      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory
         (method.getConstantPool());

      org.apache.bcel.generic.InstructionList list = 
         new org.apache.bcel.generic.InstructionList();

      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append(factory.createNew(smNode.getType()));
      list.append(new org.apache.bcel.generic.DUP());
      list.append
         (factory.createInvoke
          (smNodeInit.getEnclosingClass().getName(),smNodeInit.getName(),
           smNodeInit.getReturnType(),smNodeInit.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKESPECIAL));

      int nodeIndex = method.calcMaxLocals();
      list.append(new org.apache.bcel.generic.ASTORE(nodeIndex));
      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append(new org.apache.bcel.generic.ICONST(5));
      list.append(new org.apache.bcel.generic.ACONST_NULL());
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
	
      int insertedNode = nodeIndex + 1;
      list.append(new org.apache.bcel.generic.ASTORE(insertedNode));
      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(2));
      list.append(new org.apache.bcel.generic.ICONST(3));
      list.append(new org.apache.bcel.generic.ACONST_NULL());
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));

      int insertedNode2 = insertedNode + 1;
      list.append(new org.apache.bcel.generic.ASTORE(insertedNode2));
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode));
      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append
         (factory.createInvoke
          (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
           smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));

      list.append(new org.apache.bcel.generic.ALOAD(insertedNode2));
      list.append(new org.apache.bcel.generic.ICONST(2));
      list.append
         (factory.createInvoke
          (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
           smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));

      org.apache.bcel.generic.BranchHandle comparisonIH =
         list.append(new org.apache.bcel.generic.IF_ACMPEQ(null));
      list.append(new org.apache.bcel.generic.POP());
      list.append(new org.apache.bcel.generic.ICONST(0));
      comparisonIH.setTarget(list.append(new org.apache.bcel.generic.NOP()));


      ThreadPredicateGenerator.updateTargeters(ih,list.getStart());
      method.getInstructionList().insert(ih,list);
      method.setMaxLocals();
      method.setMaxStack();
      method.mark();
   }


   public void insertInterproceduralPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih, int valueType) {

      sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
         findInterproceduralDominators(method, ih, 5);

      if (blocks==null || blocks.length==0){
         insertPredicate(method, ih, valueType);
         return;
      }

      sandmark.program.Class smNode = addSMNode(method.getApplication());
      sandmark.program.Method smNodeInit = smNode.getMethod("<init>","()V");
      sandmark.program.Method smNodeMove = 
         smNode.getMethod("Move","(I)Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeInsert = 
         smNode.getMethod
         ("Insert","(IILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");


      sandmark.util.IdentifierIterator nameiter = 
         new sandmark.util.IdentifierIterator();
      java.util.Random random = sandmark.util.Random.getRandom();

      sandmark.program.Field field1=null, field2=null, field3=null;
      sandmark.program.Class class1=null, class2=null, class3=null;


      org.apache.bcel.generic.InstructionHandle insertpoint = 
         (org.apache.bcel.generic.InstructionHandle)
         blocks[0].getInstList().get(random.nextInt(blocks[0].getInstList().size()));
      sandmark.program.Method dommethod = blocks[0].graph().method();
      sandmark.program.Class domclass = dommethod.getEnclosingClass();
      org.apache.bcel.generic.InstructionList list = 
         dommethod.getInstructionList();
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());


      // block 0
      list.insert(insertpoint, factory.createNew(smNode.getType()));
      list.insert(insertpoint, new org.apache.bcel.generic.DUP());
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeInit.getEnclosingClass().getName(),smNodeInit.getName(),
                   smNodeInit.getReturnType(),smNodeInit.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKESPECIAL));
      String fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class1 = domclass;
      field1 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class1.getName(), field1.getName(),
                                                       smNode.getType()));
      dommethod.mark();
      dommethod.setMaxStack();
      
      


      // block 1
      if (blocks[1]!=blocks[0]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[1].getInstList().get(random.nextInt(blocks[1].getInstList().size()));
         dommethod = blocks[1].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), 
                                                       smNode.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_5);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      list.insert(insertpoint, 
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class2 = domclass;
      field2 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class2.getName(), field2.getName(), 
                                                       smNode.getType()));
      dommethod.mark();
      dommethod.setMaxStack();


      // block 2
      if (blocks[2]!=blocks[1]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[2].getInstList().get(random.nextInt(blocks[2].getInstList().size()));
         dommethod = blocks[2].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class2.getName(), field2.getName(), 
                                                       smNode.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_2);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_3);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class3 = domclass;
      field3 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class3.getName(), field3.getName(), 
                                                       smNode.getType()));
      dommethod.mark();
      dommethod.setMaxStack();


      // block 3
      if (blocks[3]!=blocks[2]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[3].getInstList().get(random.nextInt(blocks[3].getInstList().size()));
         dommethod = blocks[3].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class2.getName(), field2.getName(), 
                                                       smNode.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(insertpoint, 
                  factory.createInvoke
                  (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
                   smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.insert(insertpoint, factory.createPutStatic(class2.getName(), field2.getName(), 
                                                       smNode.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // block 4
      if (blocks[4]!=blocks[3]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[4].getInstList().get(random.nextInt(blocks[4].getInstList().size()));
         dommethod = blocks[4].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class3.getName(), field3.getName(), 
                                                       smNode.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_2);
      list.insert(insertpoint, 
                  factory.createInvoke
                  (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
                   smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.insert(insertpoint, factory.createPutStatic(class3.getName(), field3.getName(), 
                                                       smNode.getType()));
      dommethod.mark();
      dommethod.setMaxStack();      


      
      // last block
      factory = new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());
      list = method.getInstructionList();
      org.apache.bcel.generic.InstructionHandle starthandle = 
         list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(ih, factory.createGetStatic(class2.getName(), field2.getName(), 
                                              smNode.getType()));
      list.insert(ih, factory.createGetStatic(class3.getName(), field3.getName(), 
                                              smNode.getType()));
      org.apache.bcel.generic.BranchHandle comparisonIH =
         list.insert(ih, new org.apache.bcel.generic.IF_ACMPEQ(null));
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.POP);
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_0);
      comparisonIH.setTarget
	  (list.insert(ih, org.apache.bcel.generic.InstructionConstants.NOP));



      ThreadPredicateGenerator.updateTargeters(ih,starthandle);
      method.setMaxLocals();
      method.setMaxStack();
      method.mark();
   }


   private static PredicateInfo sInfo;
   public static PredicateInfo getInfo() {
      if(sInfo == null)
         sInfo = new PredicateInfo(OpaqueManager.PT_DATA_STRUCTURE_OP,
                                   OpaqueManager.PV_FALSE);
      return sInfo;
   }
}

class DSPGF2 extends DataStructurePredicateGenerator {
   /** This method takes the methodgen as input and inserts an Opaquely false predicate before BasicBlock ,bb_ins_before.
    *  This particular module provides a bytecode embedding  of the following code listing 
    *  smNode n= new smNode();
    *  smNode a= n.Insert(5,1,null);      
    *  smNode b= n.Insert(7,8,null);
    *
    *  smNode.g=a.Move(2);
    *   smNode.h=b.Move(3);
    *	smNode.h=n.Insert(2,1,smNode.h);
    *  if(smNode.g==smNode.h)
    *	    System.out.println(" Wrong :Isn't this supposed to be false ?");
    *	else
    *    System.out.println("Right");
    */

   public void insertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {
      sandmark.program.Class smNode = addSMNode(method.getApplication());
      sandmark.program.Method smNodeInit = smNode.getMethod("<init>","()V");
      sandmark.program.Method smNodeMove = 
         smNode.getMethod
         ("Move","(I)Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeInsert = 
         smNode.getMethod
         ("Insert",
          "(IILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeSplit = 
         smNode.getMethod
         ("Split","(ILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Field smNodeG = 
         smNode.getField("g","Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Field smNodeH = 
         smNode.getField("h","Lsandmark/util/opaquepredicatelib/smNode;");

      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory
         (method.getConstantPool());

      org.apache.bcel.generic.InstructionList list = 
         new org.apache.bcel.generic.InstructionList();

      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append(factory.createNew(smNode.getType()));
      list.append(new org.apache.bcel.generic.DUP());
      list.append
         (factory.createInvoke
          (smNodeInit.getEnclosingClass().getName(),smNodeInit.getName(),
           smNodeInit.getReturnType(),smNodeInit.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKESPECIAL));

      int nodeIndex = method.calcMaxLocals();
      list.append(new org.apache.bcel.generic.ASTORE(nodeIndex));
      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append(new org.apache.bcel.generic.ICONST(5));
      list.append(new org.apache.bcel.generic.ACONST_NULL());
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
	
      int insertedNode = nodeIndex + 1;
      list.append(new org.apache.bcel.generic.ASTORE(insertedNode));
      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(2));
      list.append(new org.apache.bcel.generic.ICONST(3));
      list.append(new org.apache.bcel.generic.ACONST_NULL());
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));

      int insertedNode2 = insertedNode + 1;
      list.append(new org.apache.bcel.generic.ASTORE(insertedNode2));
	
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode));
      list.append(new org.apache.bcel.generic.ICONST(2));
      list.append
         (factory.createInvoke
          (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
           smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.append
         (factory.createPutStatic
          (smNodeG.getEnclosingClass().getName(),smNodeG.getName(),
           smNodeG.getType()));

      list.append(new org.apache.bcel.generic.ALOAD(insertedNode2));
      list.append(new org.apache.bcel.generic.ICONST(3));
      list.append
         (factory.createInvoke
          (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
           smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.append
         (factory.createPutStatic
          (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
           smNodeH.getType()));

      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(2));
      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append
         (factory.createGetStatic
          (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
           smNodeH.getType()));
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.append
         (factory.createPutStatic
          (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
           smNodeH.getType()));

      int randChoice = sandmark.util.Random.getRandom().nextInt() & 1;
	
      if(randChoice == 0) {
         list.append
            (factory.createGetStatic
             (smNodeG.getEnclosingClass().getName(),smNodeG.getName(),
              smNodeG.getType()));
         list.append
            (factory.createGetStatic
             (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
              smNodeH.getType()));
      } else {
         list.append(new org.apache.bcel.generic.ALOAD(insertedNode2));
         list.append(new org.apache.bcel.generic.ICONST(2));
         list.append
            (factory.createGetStatic
             (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
              smNodeH.getType()));
         list.append
            (factory.createInvoke
             (smNodeSplit.getEnclosingClass().getName(),smNodeSplit.getName(),
              smNodeSplit.getReturnType(),smNodeSplit.getArgumentTypes(),
              org.apache.bcel.Constants.INVOKEVIRTUAL));
         list.append(new org.apache.bcel.generic.POP());
         list.append
            (factory.createGetStatic
             (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
              smNodeH.getType()));
         list.append(new org.apache.bcel.generic.ALOAD(insertedNode2));
      }

      org.apache.bcel.generic.BranchHandle comparisonIH =
         list.append(new org.apache.bcel.generic.IF_ACMPEQ(null));
      list.append(new org.apache.bcel.generic.POP());
      list.append(new org.apache.bcel.generic.ICONST(0));
      comparisonIH.setTarget
	  (list.append(new org.apache.bcel.generic.NOP()));

      ThreadPredicateGenerator.updateTargeters(ih,list.getStart());
      method.getInstructionList().insert(ih,list);
      method.setMaxLocals();
      method.setMaxStack();
      method.mark();
   }



   public void insertInterproceduralPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih, int valueType) {

      sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
         findInterproceduralDominators(method, ih, 6);
      
      if (blocks==null || blocks.length==0){
         insertPredicate(method, ih, valueType);
         return;
      }


      sandmark.program.Class smNode = addSMNode(method.getApplication());
      sandmark.program.Method smNodeInit = smNode.getMethod("<init>","()V");
      sandmark.program.Method smNodeMove = 
         smNode.getMethod
         ("Move","(I)Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeInsert = 
         smNode.getMethod
         ("Insert",
          "(IILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeSplit = 
         smNode.getMethod
         ("Split","(ILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Field smNodeG = 
         smNode.getField("g","Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Field smNodeH = 
         smNode.getField("h","Lsandmark/util/opaquepredicatelib/smNode;");


      sandmark.util.IdentifierIterator nameiter = 
         new sandmark.util.IdentifierIterator();
      java.util.Random random = sandmark.util.Random.getRandom();

      sandmark.program.Field field1=null, field2=null, field3=null;
      sandmark.program.Class class1=null, class2=null, class3=null;


      org.apache.bcel.generic.InstructionHandle insertpoint = 
         (org.apache.bcel.generic.InstructionHandle)
         blocks[0].getInstList().get(random.nextInt(blocks[0].getInstList().size()));
      sandmark.program.Method dommethod = blocks[0].graph().method();
      sandmark.program.Class domclass = dommethod.getEnclosingClass();
      org.apache.bcel.generic.InstructionList list = 
         dommethod.getInstructionList();
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());


      // block 0
      list.insert(insertpoint, factory.createNew(smNode.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.DUP);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeInit.getEnclosingClass().getName(),smNodeInit.getName(),
                   smNodeInit.getReturnType(),smNodeInit.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKESPECIAL));
      String fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class1 = domclass;
      field1 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class1.getName(), 
                                                       field1.getName(), 
                                                       smNode.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // block 1
      if (blocks[1]!=blocks[0]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[1].getInstList().get(random.nextInt(blocks[1].getInstList().size()));
         dommethod = blocks[1].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_5);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class2 = domclass;
      field2 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class2.getName(), 
                                                       field2.getName(), 
                                                       smNode.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // block 2
      if (blocks[2]!=blocks[1]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[2].getInstList().get(random.nextInt(blocks[2].getInstList().size()));
         dommethod = blocks[2].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_2);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_3);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class3 = domclass;
      field3 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class3.getName(), 
                                                       field3.getName(), 
                                                       smNode.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // block 3
      if (blocks[3]!=blocks[2]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[3].getInstList().get(random.nextInt(blocks[3].getInstList().size()));
         dommethod = blocks[3].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class2.getName(), field2.getName(), field2.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_2);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
                   smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.insert(insertpoint,
                  factory.createPutStatic
                  (smNodeG.getEnclosingClass().getName(),smNodeG.getName(),
                   smNodeG.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // block 4
      if (blocks[4]!=blocks[3]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[4].getInstList().get(random.nextInt(blocks[4].getInstList().size()));
         dommethod = blocks[4].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class3.getName(), field3.getName(), field3.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_3);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
                   smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.insert(insertpoint,
                  factory.createPutStatic
                  (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
                   smNodeH.getType()));
      dommethod.mark();
      dommethod.setMaxStack();


      // block 5
      if (blocks[5]!=blocks[4]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[5].getInstList().get(random.nextInt(blocks[5].getInstList().size()));
         dommethod = blocks[5].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_2);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(insertpoint,
                  factory.createGetStatic
                  (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
                   smNodeH.getType()));
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.insert(insertpoint,
                  factory.createPutStatic
                  (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
                   smNodeH.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // last block
      list = method.getInstructionList();
      factory = new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());

      org.apache.bcel.generic.InstructionHandle starthandle = 
         list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_1);         

      int randChoice = sandmark.util.Random.getRandom().nextInt() & 1;
      if(randChoice == 0) {
         list.insert(ih,
                     factory.createGetStatic
                     (smNodeG.getEnclosingClass().getName(),smNodeG.getName(),
                      smNodeG.getType()));
         list.insert(ih,
                     factory.createGetStatic
                     (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
                      smNodeH.getType()));
      } else {
         list.insert(ih, factory.createGetStatic(class3.getName(), field3.getName(), field3.getType()));
         list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_2);
         list.insert(ih,
                     factory.createGetStatic
                     (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
                      smNodeH.getType()));
         list.insert(ih,
                     factory.createInvoke
                     (smNodeSplit.getEnclosingClass().getName(),smNodeSplit.getName(),
                      smNodeSplit.getReturnType(),smNodeSplit.getArgumentTypes(),
                      org.apache.bcel.Constants.INVOKEVIRTUAL));
         list.insert(ih, org.apache.bcel.generic.InstructionConstants.POP);
         list.insert(ih,
                     factory.createGetStatic
                     (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
                      smNodeH.getType()));
         list.insert(ih, factory.createGetStatic(class3.getName(), field3.getName(), field3.getType()));
      }

      org.apache.bcel.generic.BranchHandle comparisonIH =
         list.insert(ih, new org.apache.bcel.generic.IF_ACMPEQ(null));
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.POP);
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_0);
      comparisonIH.setTarget
	  (list.insert(ih, org.apache.bcel.generic.InstructionConstants.NOP));

      ThreadPredicateGenerator.updateTargeters(ih,starthandle);
      method.setMaxLocals();
      method.setMaxStack();
      method.mark();
   }

   private static PredicateInfo sInfo;
   public static PredicateInfo getInfo() {
      if(sInfo == null)
         sInfo = new PredicateInfo(OpaqueManager.PT_DATA_STRUCTURE_OP,
                                   OpaqueManager.PV_FALSE);
      return sInfo;
   }
}

class DSPGT1 extends DataStructurePredicateGenerator {	    
   /** This method takes the methodgen as input and inserts an Opaquely true predicate before BasicBlock ,bb_ins_before.
    *  This particular module provides a bytecode embedding  of the following code listing 	
    *  smNode n= new smNode();
    * 	smNode a= n.Insert(5,1,null);
    *	smNode b= n.Insert(7,8,null);
    *	n.Merge(a,b);
    *	if(a.Move(1)==b.Move(2)) // op true		
    *     System.out.println(" the Opaque predicate is correct..  ");
    */

   public void insertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {
      sandmark.program.Class smNode = addSMNode(method.getApplication());
      sandmark.program.Method smNodeInit = smNode.getMethod("<init>","()V");
      sandmark.program.Method smNodeMove = 
         smNode.getMethod
         ("Move","(I)Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeInsert = 
         smNode.getMethod
         ("Insert",
          "(IILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeMerge = 
         smNode.getMethod
         ("Merge","(Lsandmark/util/opaquepredicatelib/smNode;" + 
          "Lsandmark/util/opaquepredicatelib/smNode;)V");

      if(smNode == null || smNodeInit == null || smNodeMove == null ||
         smNodeInsert == null || smNodeMerge == null)
         throw new Error("smNode lacks a method");

      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory
         (method.getConstantPool());

      org.apache.bcel.generic.InstructionList list = 
         new org.apache.bcel.generic.InstructionList();

      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append(factory.createNew(smNode.getType()));
      list.append(new org.apache.bcel.generic.DUP());
      list.append
         (factory.createInvoke
          (smNodeInit.getEnclosingClass().getName(),smNodeInit.getName(),
           smNodeInit.getReturnType(),smNodeInit.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKESPECIAL));

      int nodeIndex = method.calcMaxLocals();
      list.append(new org.apache.bcel.generic.ASTORE(nodeIndex));
      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append(new org.apache.bcel.generic.ICONST(5));
      list.append(new org.apache.bcel.generic.ACONST_NULL());
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
	
      int insertedNode = nodeIndex + 1;
      list.append(new org.apache.bcel.generic.ASTORE(insertedNode));
      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(2));
      list.append(new org.apache.bcel.generic.ICONST(3));
      list.append(new org.apache.bcel.generic.ACONST_NULL());
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));

      int insertedNode2 = insertedNode + 1;
      list.append(new org.apache.bcel.generic.ASTORE(insertedNode2));

      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode));
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode2));
      list.append
         (factory.createInvoke
          (smNodeMerge.getEnclosingClass().getName(),smNodeMerge.getName(),
           smNodeMerge.getReturnType(),smNodeMerge.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode));
      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append
         (factory.createInvoke
          (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
           smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode2));
      list.append(new org.apache.bcel.generic.ICONST(2));
      list.append
         (factory.createInvoke
          (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
           smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));

      org.apache.bcel.generic.BranchHandle comparisonIH =
         list.append(new org.apache.bcel.generic.IF_ACMPEQ(null));
      list.append(new org.apache.bcel.generic.POP());
      list.append(new org.apache.bcel.generic.ICONST(0));
      comparisonIH.setTarget
	  (list.append(new org.apache.bcel.generic.NOP()));

      ThreadPredicateGenerator.updateTargeters(ih,list.getStart());
      method.getInstructionList().insert(ih,list);
      method.setMaxLocals();
      method.setMaxStack();
      method.mark();
   }


   public void insertInterproceduralPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih, int valueType) {


      sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
         findInterproceduralDominators(method, ih, 6);
      
      if (blocks==null || blocks.length==0){
         insertPredicate(method, ih, valueType);
         return;
      }


      sandmark.program.Class smNode = addSMNode(method.getApplication());
      sandmark.program.Method smNodeInit = smNode.getMethod("<init>","()V");
      sandmark.program.Method smNodeMove = 
         smNode.getMethod
         ("Move","(I)Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeInsert = 
         smNode.getMethod
         ("Insert",
          "(IILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeMerge = 
         smNode.getMethod
         ("Merge","(Lsandmark/util/opaquepredicatelib/smNode;" + 
          "Lsandmark/util/opaquepredicatelib/smNode;)V");


      sandmark.util.IdentifierIterator nameiter = 
         new sandmark.util.IdentifierIterator();
      java.util.Random random = sandmark.util.Random.getRandom();

      sandmark.program.Field field1=null, field2=null, field3=null;
      sandmark.program.Class class1=null, class2=null, class3=null;


      org.apache.bcel.generic.InstructionHandle insertpoint = 
         (org.apache.bcel.generic.InstructionHandle)
         blocks[0].getInstList().get(random.nextInt(blocks[0].getInstList().size()));
      sandmark.program.Method dommethod = blocks[0].graph().method();
      sandmark.program.Class domclass = dommethod.getEnclosingClass();
      org.apache.bcel.generic.InstructionList list = 
         dommethod.getInstructionList();
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());



      // block 0
      list.insert(insertpoint, factory.createNew(smNode.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.DUP);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeInit.getEnclosingClass().getName(),smNodeInit.getName(),
                   smNodeInit.getReturnType(),smNodeInit.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKESPECIAL));
      String fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class1 = domclass;
      field1 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class1.getName(), 
                                                       field1.getName(), 
                                                       field1.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // block 1
      if (blocks[1]!=blocks[0]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[1].getInstList().get(random.nextInt(blocks[1].getInstList().size()));
         dommethod = blocks[1].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      } 
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_5);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class2 = domclass;
      field2 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class2.getName(), 
                                                       field2.getName(), 
                                                       field2.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // block 2
      if (blocks[2]!=blocks[1]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[2].getInstList().get(random.nextInt(blocks[2].getInstList().size()));
         dommethod = blocks[2].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      } 
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_2);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_3);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      list.insert(insertpoint, 
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class3 = domclass;
      field3 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class3.getName(), 
                                                       field3.getName(), 
                                                       field3.getType()));
      dommethod.mark();
      dommethod.setMaxStack();


      // block 3
      if (blocks[3]!=blocks[2]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[3].getInstList().get(random.nextInt(blocks[3].getInstList().size()));
         dommethod = blocks[3].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      } 
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, factory.createGetStatic(class2.getName(), field2.getName(), field2.getType()));
      list.insert(insertpoint, factory.createGetStatic(class3.getName(), field3.getName(), field3.getType()));
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeMerge.getEnclosingClass().getName(),smNodeMerge.getName(),
                   smNodeMerge.getReturnType(),smNodeMerge.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      dommethod.mark();
      dommethod.setMaxStack();


      // block 4
      if (blocks[4]!=blocks[3]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[4].getInstList().get(random.nextInt(blocks[4].getInstList().size()));
         dommethod = blocks[4].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class2.getName(), field2.getName(), field2.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(insertpoint, 
                  factory.createInvoke
                  (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
                   smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.insert(insertpoint, factory.createPutStatic(class2.getName(), 
                                                       field2.getName(), 
                                                       field2.getType()));
      dommethod.mark();
      dommethod.setMaxStack();


      // block 5
      if (blocks[5]!=blocks[4]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[5].getInstList().get(random.nextInt(blocks[5].getInstList().size()));
         dommethod = blocks[5].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class3.getName(), field3.getName(), field3.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_2);
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
                   smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.insert(insertpoint, factory.createPutStatic(class3.getName(), 
                                                       field3.getName(), 
                                                       field3.getType()));
      dommethod.mark();
      dommethod.setMaxStack();


      // last block
      list = method.getInstructionList();
      factory = new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());

      org.apache.bcel.generic.InstructionHandle starthandle = 
         list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(ih, factory.createGetStatic(class2.getName(), field2.getName(), field2.getType()));
      list.insert(ih, factory.createGetStatic(class3.getName(), field3.getName(), field3.getType()));
      org.apache.bcel.generic.BranchHandle comparisonIH =
         list.insert(ih, new org.apache.bcel.generic.IF_ACMPEQ(null));
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.POP);
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_0);
      comparisonIH.setTarget
	  (list.insert(ih, org.apache.bcel.generic.InstructionConstants.NOP));

      ThreadPredicateGenerator.updateTargeters(ih,starthandle);
      method.setMaxLocals();
      method.setMaxStack();
      method.mark();
   }

   private static PredicateInfo sInfo;
   public static PredicateInfo getInfo() {
      if(sInfo == null)
         sInfo = new PredicateInfo(OpaqueManager.PT_DATA_STRUCTURE_OP,
                                   OpaqueManager.PV_TRUE);
      return sInfo;
   }
}

class DSPGT2 extends DataStructurePredicateGenerator {

   /** This method takes the methodgen as input and inserts an Opaquely true predicate before BasicBlock ,bb_ins_before.
    *  This particular module provides a bytecode embedding  of the following code listing 	
    *    smNode n= new smNode();
    *    smNode a= n.Insert(5,1,null);
    *    smNode b= n.Insert(7,8,null);	
    *    n.Merge(a,b);   
    *    if(smNode.h==b.Move(3))
    *	    	System.out.println(" Opaquely true predicate generated ");
    */
    
   public void insertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {
      sandmark.program.Class smNode = addSMNode(method.getApplication());
      sandmark.program.Method smNodeInit = smNode.getMethod("<init>","()V");
      sandmark.program.Method smNodeMove = 
         smNode.getMethod
         ("Move","(I)Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeInsert = 
         smNode.getMethod
         ("Insert","(IILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeMerge = 
         smNode.getMethod
         ("Merge","(Lsandmark/util/opaquepredicatelib/smNode;" + 
          "Lsandmark/util/opaquepredicatelib/smNode;)V");
      sandmark.program.Field smNodeH = 
         smNode.getField("h","Lsandmark/util/opaquepredicatelib/smNode;");

      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory
         (method.getConstantPool());

      org.apache.bcel.generic.InstructionList list = 
         new org.apache.bcel.generic.InstructionList();

      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append(factory.createNew(smNode.getType()));
      list.append(new org.apache.bcel.generic.DUP());
      list.append
         (factory.createInvoke
          (smNodeInit.getEnclosingClass().getName(),smNodeInit.getName(),
           smNodeInit.getReturnType(),smNodeInit.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKESPECIAL));

      int nodeIndex = method.calcMaxLocals();
      list.append(new org.apache.bcel.generic.ASTORE(nodeIndex));
      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(1));
      list.append(new org.apache.bcel.generic.ICONST(5));
      list.append(new org.apache.bcel.generic.ACONST_NULL());
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
	
      int insertedNode = nodeIndex + 1;
      list.append(new org.apache.bcel.generic.ASTORE(insertedNode));
      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ICONST(2));
      list.append(new org.apache.bcel.generic.ICONST(3));
      list.append(new org.apache.bcel.generic.ACONST_NULL());
      list.append
         (factory.createInvoke
          (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
           smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));

      int insertedNode2 = insertedNode + 1;
      list.append(new org.apache.bcel.generic.ASTORE(insertedNode2));

      list.append(new org.apache.bcel.generic.ALOAD(nodeIndex));
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode));
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode2));
      list.append
         (factory.createInvoke
          (smNodeMerge.getEnclosingClass().getName(),smNodeMerge.getName(),
           smNodeMerge.getReturnType(),smNodeMerge.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.append
         (factory.createGetStatic
          (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
           smNodeH.getType()));
      list.append(new org.apache.bcel.generic.ALOAD(insertedNode2));
      list.append(new org.apache.bcel.generic.ICONST(3));
      list.append
         (factory.createInvoke
          (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
           smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
           org.apache.bcel.Constants.INVOKEVIRTUAL));

      org.apache.bcel.generic.BranchHandle comparisonIH =
         list.append(new org.apache.bcel.generic.IF_ACMPEQ(null));
      list.append(new org.apache.bcel.generic.POP());
      list.append(new org.apache.bcel.generic.ICONST(0));
      comparisonIH.setTarget
	  (list.append(new org.apache.bcel.generic.NOP()));

      ThreadPredicateGenerator.updateTargeters(ih,list.getStart());
      method.getInstructionList().insert(ih,list);
      method.setMaxLocals();
      method.setMaxStack();
      method.mark();

      //Here's what it used to do:
      //n = new smNode();
      //a = n.insert(1,5,null);
      //b = n.insert(2,3,null);

      //smNode.g = a.move(2);
      //smNode.h = b.move(3);
      //smNode.h = n.insert(2,1,smNode.h)
      //if(smNode.h != b.move(3)) goto ih;
   }


    
   public void insertInterproceduralPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih, int valueType) {


      sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
         findInterproceduralDominators(method, ih, 4);
      
      if (blocks==null || blocks.length==0){
         insertPredicate(method, ih, valueType);
         return;
      }

      sandmark.program.Class smNode = addSMNode(method.getApplication());
      sandmark.program.Method smNodeInit = smNode.getMethod("<init>","()V");
      sandmark.program.Method smNodeMove = 
         smNode.getMethod
         ("Move","(I)Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeInsert = 
         smNode.getMethod
         ("Insert","(IILsandmark/util/opaquepredicatelib/smNode;)" + 
          "Lsandmark/util/opaquepredicatelib/smNode;");
      sandmark.program.Method smNodeMerge = 
         smNode.getMethod
         ("Merge","(Lsandmark/util/opaquepredicatelib/smNode;" + 
          "Lsandmark/util/opaquepredicatelib/smNode;)V");
      sandmark.program.Field smNodeH = 
         smNode.getField("h","Lsandmark/util/opaquepredicatelib/smNode;");


      sandmark.util.IdentifierIterator nameiter = 
         new sandmark.util.IdentifierIterator();
      java.util.Random random = sandmark.util.Random.getRandom();

      sandmark.program.Field field1=null, field2=null, field3=null;
      sandmark.program.Class class1=null, class2=null, class3=null;


      org.apache.bcel.generic.InstructionHandle insertpoint = 
         (org.apache.bcel.generic.InstructionHandle)
         blocks[0].getInstList().get(random.nextInt(blocks[0].getInstList().size()));
      sandmark.program.Method dommethod = blocks[0].graph().method();
      sandmark.program.Class domclass = dommethod.getEnclosingClass();
      org.apache.bcel.generic.InstructionList list = 
         dommethod.getInstructionList();
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());



      
      // block 0
      list.insert(insertpoint, factory.createNew(smNode.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.DUP);
      list.insert(insertpoint, 
                  factory.createInvoke
                  (smNodeInit.getEnclosingClass().getName(),smNodeInit.getName(),
                   smNodeInit.getReturnType(),smNodeInit.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKESPECIAL));
      String fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class1 = domclass;
      field1 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class1.getName(), 
                                                       field1.getName(), 
                                                       field1.getType()));
      dommethod.mark();
      dommethod.setMaxStack();


      // block 1
      if (blocks[1]!=blocks[0]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[1].getInstList().get(random.nextInt(blocks[1].getInstList().size()));
         dommethod = blocks[1].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_5);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      list.insert(insertpoint, 
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class2 = domclass;
      field2 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class2.getName(), 
                                                       field2.getName(), 
                                                       field2.getType()));
      dommethod.mark();
      dommethod.setMaxStack();


      // block 2
      if (blocks[2]!=blocks[1]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[2].getInstList().get(random.nextInt(blocks[2].getInstList().size()));
         dommethod = blocks[2].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_2);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ICONST_3);
      list.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      list.insert(insertpoint, 
                  factory.createInvoke
                  (smNodeInsert.getEnclosingClass().getName(),smNodeInsert.getName(),
                   smNodeInsert.getReturnType(),smNodeInsert.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      fieldname = (String)nameiter.next();
      while(domclass.getField(fieldname, smNode.getType().getSignature())!=null)
         fieldname = (String)nameiter.next();
      class3 = domclass;
      field3 = 
         new sandmark.program.LocalField(domclass, 
                                         org.apache.bcel.Constants.ACC_PUBLIC |
                                         org.apache.bcel.Constants.ACC_STATIC,
                                         smNode.getType(),
                                         fieldname);
      list.insert(insertpoint, factory.createPutStatic(class3.getName(), 
                                                       field3.getName(), 
                                                       field3.getType()));
      dommethod.mark();
      dommethod.setMaxStack();



      // block 3
      if (blocks[3]!=blocks[2]){
         insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[3].getInstList().get(random.nextInt(blocks[3].getInstList().size()));
         dommethod = blocks[3].graph().method();
         domclass = dommethod.getEnclosingClass();
         list = dommethod.getInstructionList();
         factory =
            new org.apache.bcel.generic.InstructionFactory(dommethod.getConstantPool());
      }
      list.insert(insertpoint, factory.createGetStatic(class1.getName(), field1.getName(), field1.getType()));
      list.insert(insertpoint, factory.createGetStatic(class2.getName(), field2.getName(), field2.getType()));
      list.insert(insertpoint, factory.createGetStatic(class3.getName(), field3.getName(), field3.getType()));
      list.insert(insertpoint,
                  factory.createInvoke
                  (smNodeMerge.getEnclosingClass().getName(),smNodeMerge.getName(),
                   smNodeMerge.getReturnType(),smNodeMerge.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      dommethod.mark();
      dommethod.setMaxStack();



      // last block
      list = method.getInstructionList();
      factory = new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());

      org.apache.bcel.generic.InstructionHandle starthandle = 
         list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.insert(ih, 
                  factory.createGetStatic
                  (smNodeH.getEnclosingClass().getName(),smNodeH.getName(),
                   smNodeH.getType()));
      list.insert(ih, factory.createGetStatic(class3.getName(), field3.getName(), field3.getType()));
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_3);
      list.insert(ih, 
                  factory.createInvoke
                  (smNodeMove.getEnclosingClass().getName(),smNodeMove.getName(),
                   smNodeMove.getReturnType(),smNodeMove.getArgumentTypes(),
                   org.apache.bcel.Constants.INVOKEVIRTUAL));
      org.apache.bcel.generic.BranchHandle comparisonIH =
         list.insert(ih, new org.apache.bcel.generic.IF_ACMPEQ(null));
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.POP);
      list.insert(ih, org.apache.bcel.generic.InstructionConstants.ICONST_0);
      comparisonIH.setTarget
	  (list.insert(ih, org.apache.bcel.generic.InstructionConstants.NOP));

      ThreadPredicateGenerator.updateTargeters(ih,starthandle);
      method.setMaxStack();
      method.mark();
   }



   private static PredicateInfo sInfo;
   public static PredicateInfo getInfo() {
      if(sInfo == null)
         sInfo = new PredicateInfo(OpaqueManager.PT_DATA_STRUCTURE_OP,
                                   OpaqueManager.PV_FALSE);
      return sInfo;
   }
}
