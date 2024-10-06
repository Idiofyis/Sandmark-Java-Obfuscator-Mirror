package sandmark.util.opaquepredicatelib;

/** This class is used to parse a string representing a
 *  boolean expression that compares two integer expressions,
 *  and turn it into a set of instruction lists that will perform
 *  that test (for use as an algebraic opaque predicate). 
 *  The grammar for the expressions is as follows:
 *
 * <bool_expr> := <int_expr> <cmp_op> <int_expr>
 * <int_expr>  := <int_term> | <int_expr> <int_op1> <int_term>
 * <int_term>  := <int_var> | <int_term> <int_op2> <int_var>
 * <int_var>   := <literal_int> | <variable> | '(' <int_expr> ')'
 * <int_op1>   := '+' | '-'
 * <int_op2>   := '*' | '/' | '%'
 * <cmp_op>    := '==' | '>' | '<' | '<=' | '>=' | '!='
 * <variable>  := 'a', 'b', ..., 'z'
 */
public class ExprTree{
   private static final int BOOL_EXPR   = 0;
   private static final int INT_BINOP   = 1;
   private static final int LITERAL_INT = 2;
   private static final int VARIABLE    = 3;

   private static final int EQ = 0;
   private static final int NE = 1;
   private static final int GT = 2;
   private static final int GE = 3;
   private static final int LT = 4;
   private static final int LE = 5;

   private static final int PLUS  = 0;
   private static final int MINUS = 1;
   private static final int MULT  = 2;
   private static final int DIV   = 3;
   private static final int MOD   = 4;
   ////////////////////////////////////////

   private int type;

   private int operator;
   private ExprTree left, right;
   private int literal_value;
   private int varnumber;
   // varnumber is NOT local var index, it denotes that this
   // is the Nth variable I've encountered...so like
   // 7*y*z-1 != x*x
   // could be though of as
   // 7*[0]*[1]-1 != [2]*[2]
   // ...get it?

   private ExprTree(){}

   public static ExprTree parse(String str){
      if (str==null)
         return null;

      java.util.LinkedList queue = new java.util.LinkedList();
      char[] chars = str.toCharArray();
      for (int i=0;i<chars.length;i++)
         queue.add(new Character(chars[i]));

      return parseBoolExpr(queue, new java.util.Vector(26));
   }

   private static ExprTree parseBoolExpr(java.util.LinkedList queue, java.util.List varlist){
      ExprTree root = new ExprTree();
      root.type = BOOL_EXPR;
      root.left = parseIntExpr(queue, varlist);
      if (root.left==null)
         return null;

      if (!skipWhite(queue))
         return null;

      Character first, next;
      first = (Character)queue.removeFirst();
      switch(first.charValue()){
         case '=':{
            if (queue.isEmpty() || ((Character)queue.removeFirst()).charValue()!='=')
               return null;
            root.operator = EQ;
            break;
         }

         case '!':{
            if (queue.isEmpty() || ((Character)queue.removeFirst()).charValue()!='=')
               return null;
            root.operator = NE;
            break;
         }

         case '<':{
            if (queue.isEmpty())
               return null;
            next = (Character)queue.getFirst();
            if (next.charValue()=='='){
               queue.removeFirst();
               root.operator = LE;
            }else{
               root.operator = LT;
            }
            break;
         }

         case '>':{
            if (queue.isEmpty())
               return null;
            next = (Character)queue.getFirst();
            if (next.charValue()=='='){
               queue.removeFirst();
               root.operator = GE;
            }else{
               root.operator = GT;
            }
            break;
         }

         default:
            return null;
      }

      root.right = parseIntExpr(queue, varlist);
      if (root.right==null)
         return null;

      if (skipWhite(queue))
         return null;

      return root;
   }

   private static ExprTree parseIntExpr(java.util.LinkedList queue, java.util.List varlist){
      ExprTree root = parseIntTerm(queue, varlist);
      if (root==null)
         return null;

      if (!skipWhite(queue))
         return root;

      while(!queue.isEmpty() &&
            ( ((Character)queue.getFirst()).charValue()=='+' ||
              ((Character)queue.getFirst()).charValue()=='-')){

         ExprTree newroot = new ExprTree();
         newroot.type = INT_BINOP;
         newroot.left = root;
         Character first = (Character)queue.removeFirst();
         switch(first.charValue()){
            case '+':{
               newroot.operator = PLUS;
               break;
            }

            case '-':{
               newroot.operator = MINUS;
               break;
            }
         }

         newroot.right = parseIntTerm(queue, varlist);
         if (newroot.right==null)
            return null;
         root = newroot;

         skipWhite(queue);
      }

      return root;
   }

   private static ExprTree parseIntTerm(java.util.LinkedList queue, java.util.List varlist){
      ExprTree root = parseIntVar(queue, varlist);
      if (root==null)
         return null;

      if (!skipWhite(queue))
         return root;

      while(!queue.isEmpty() &&
            ( ((Character)queue.getFirst()).charValue()=='*' ||
              ((Character)queue.getFirst()).charValue()=='/' ||
              ((Character)queue.getFirst()).charValue()=='%')){

         ExprTree newroot = new ExprTree();
         newroot.type = INT_BINOP;
         newroot.left = root;
         Character first = (Character)queue.removeFirst();
         switch(first.charValue()){
            case '*':{
               newroot.operator = MULT;
               break;
            }

            case '/':{
               newroot.operator = DIV;
               break;
            }

            case '%':{
               newroot.operator = MOD;
               break;
            }
         }

         newroot.right = parseIntVar(queue, varlist);
         if (newroot.right==null){
            return null;
         }
         root = newroot;

         skipWhite(queue);
      }

      return root;
   }

   private static ExprTree parseIntVar(java.util.LinkedList queue, java.util.List varlist){
      ExprTree root = new ExprTree();

      if (!skipWhite(queue))
         return null;

      Character first = (Character)queue.removeFirst();
      if (first.charValue()=='-'){
         root.type = LITERAL_INT;

         if (queue.isEmpty())
            return null;

         Character next = (Character)queue.removeFirst();
         if (!Character.isDigit(next.charValue()))
            return null;

         String str="-"+next.charValue();
         while(!queue.isEmpty() && Character.isDigit(((Character)queue.getFirst()).charValue())){
            str += ((Character)queue.removeFirst()).charValue();
         }
         root.literal_value = Integer.parseInt(str);

      }else if (Character.isDigit(first.charValue())){
         root.type = LITERAL_INT;
         String str=""+first.charValue();
         while(!queue.isEmpty() && Character.isDigit(((Character)queue.getFirst()).charValue())){
            str += ((Character)queue.removeFirst()).charValue();
         }
         root.literal_value = Integer.parseInt(str);

      }else if (first.charValue()=='('){
         root = parseIntExpr(queue, varlist);

         if (!skipWhite(queue))
            return null;

         if (((Character)queue.removeFirst()).charValue()!=')')
            return null;

      }else if ('a'<=first.charValue() && first.charValue()<='z'){
         root.type = VARIABLE;
         if (!varlist.contains(first))
            varlist.add(first);
         root.varnumber = varlist.indexOf(first);
      }

      return root;
   }

   public String toString(){
      String[] iops = {"+", "-", "*", "/", "%"};
      String[] cops = {"==", "!=", ">", ">=", "<", "<="};
      String str="";
      switch(type){
         case BOOL_EXPR:{
            str = left + cops[operator] + right;
            break;
         }

         case INT_BINOP:{
            if (left.type==INT_BINOP && operator>MINUS && left.operator<=MINUS)
               str += "("+left+")";
            else
               str += left;
            str += iops[operator];
            if (right.type==INT_BINOP && operator>MINUS && right.operator<=MINUS)
               str += "("+right+")";
            else
               str+=right;
            break;
         }

         case LITERAL_INT:{
            str += literal_value;
            break;
         }

         case VARIABLE:{
            str += (char)('a'+varnumber);
            break;
         }
      }
      return str;
   }

   private static boolean skipWhite(java.util.LinkedList queue){
      while(!queue.isEmpty()){
         Character next = (Character)queue.getFirst();
         if (next.charValue()==' ' || next.charValue()=='\t')
            queue.removeFirst();
         else
            break;
      }
      return (!queue.isEmpty());
   }

   // returns the number of different VARIABLE nodes in this
   // tree (different === different varName field)
   public int numVars(){
      return getAllVars().size();
   }

   private java.util.Set getAllVars(){
      java.util.Set allvars = new java.util.HashSet();

      if (type==LITERAL_INT)
         return allvars;
      else if (type==VARIABLE){
         allvars.add(new Integer(varnumber));
         return allvars;
      }else{
         allvars.addAll(left.getAllVars());
         allvars.addAll(right.getAllVars());
         return allvars;
      }
   }

   // every element of vars must be either
   // 1. a sandmark.program.Field object denoting an
   //    INT field in the enclosing class
   // 2. an Integer object containing the index of an
   //    INT local var that will be initialized before the target IH
   // also, vars must contain as many entries as this.numVars() (but there could be duplicates in the list)
   // also, any field given must be loadable (so no instance fields too early in <init>)
   public java.util.List[] getInstructionLists(sandmark.program.Method method,
                                               Object[] vars){
      java.util.Vector listlist = new java.util.Vector(20);

      switch(type){
         case BOOL_EXPR:{// makes 3 blocks
            java.util.List[] leftlists = left.getInstructionLists(method, vars);
            java.util.List[] rightlists = right.getInstructionLists(method, vars);

            int index1=-1, index2=-1;
            if (!(left.type==LITERAL_INT || left.type==VARIABLE)){
               index1 = method.calcMaxLocals();
               leftlists[leftlists.length-1].add(new org.apache.bcel.generic.ISTORE(index1));
               for (int i=0;i<leftlists.length;i++)
                  listlist.add(leftlists[i]);
            }
            if (!(right.type==LITERAL_INT || right.type==VARIABLE)){
               index2 = (index1==-1 ? method.calcMaxLocals() : index1+1);
               rightlists[rightlists.length-1].add(new org.apache.bcel.generic.ISTORE(index2));
               for (int i=0;i<rightlists.length;i++)
                  listlist.add(rightlists[i]);
            }

            java.util.List mylist = new java.util.Vector(10);
            switch(operator){
               case EQ:{
                  if (index1!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index1));
                  else{
                     for (int i=0;i<leftlists.length;i++)
                        mylist.addAll(leftlists[i]);
                  }

                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  if (index2!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index2));
                  else{
                     for (int i=0;i<rightlists.length;i++)
                        mylist.addAll(rightlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.LCMP);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ICONST_1);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.IXOR);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ICONST_1);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.IAND);
                  break;
               }

               case NE:{
                  if (index1!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index1));
                  else{
                     for (int i=0;i<leftlists.length;i++)
                        mylist.addAll(leftlists[i]);
                  }
                  if (index2!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index2));
                  else{
                     for (int i=0;i<rightlists.length;i++)
                        mylist.addAll(rightlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ISUB);
                  break;
               }

               case GE:{
                  if (index1!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index1));
                  else{
                     for (int i=0;i<leftlists.length;i++)
                        mylist.addAll(leftlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  if (index2!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index2));
                  else{
                     for (int i=0;i<rightlists.length;i++)
                        mylist.addAll(rightlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.LCMP);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ICONST_1);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.IADD);
                  break;
               }

               case GT:{
                  if (index1!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index1));
                  else{
                     for (int i=0;i<leftlists.length;i++)
                        mylist.addAll(leftlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  if (index2!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index2));
                  else{
                     for (int i=0;i<rightlists.length;i++)
                        mylist.addAll(rightlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.LCMP);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ICONST_1);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.IADD);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ICONST_1);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.IUSHR);
                  break;
               }

               case LE:{
                  if (index1!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index1));
                  else{
                     for (int i=0;i<leftlists.length;i++)
                        mylist.addAll(leftlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  if (index2!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index2));
                  else{
                     for (int i=0;i<rightlists.length;i++)
                        mylist.addAll(rightlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.LCMP);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ICONST_1);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ISUB);
                  break;
               }

               case LT:{
                  if (index1!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index1));
                  else{
                     for (int i=0;i<leftlists.length;i++)
                        mylist.addAll(leftlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  if (index2!=-1)
                     mylist.add(new org.apache.bcel.generic.ILOAD(index2));
                  else{
                     for (int i=0;i<rightlists.length;i++)
                        mylist.addAll(rightlists[i]);
                  }
                  mylist.add(org.apache.bcel.generic.InstructionConstants.I2L);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.LCMP);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ICONST_1);
                  mylist.add(org.apache.bcel.generic.InstructionConstants.IUSHR);
                  break;
               }
            }

            listlist.add(mylist);
            break;
         }

         case INT_BINOP:{// makes 1--3 lists
            java.util.List[] leftlists = left.getInstructionLists(method, vars);
            java.util.List[] rightlists = right.getInstructionLists(method, vars);

            int index1=-1, index2=-1;
            if (!(left.type==LITERAL_INT || left.type==VARIABLE)){
               index1 = method.calcMaxLocals();
               leftlists[leftlists.length-1].add(new org.apache.bcel.generic.ISTORE(index1));
            }
            if (!(right.type==LITERAL_INT || right.type==VARIABLE)){
               index2 = (index1==-1 ? method.calcMaxLocals() : index1+1);
               rightlists[rightlists.length-1].add(new org.apache.bcel.generic.ISTORE(index2));
            }

            org.apache.bcel.generic.Instruction[] OPS = {
               org.apache.bcel.generic.InstructionConstants.IADD,
               org.apache.bcel.generic.InstructionConstants.ISUB,
               org.apache.bcel.generic.InstructionConstants.IMUL,
               org.apache.bcel.generic.InstructionConstants.IDIV,
               org.apache.bcel.generic.InstructionConstants.IREM
            };

            if (index1!=-1 && index2!=-1){
               // both got stored
               for (int i=0;i<leftlists.length;i++)
                  listlist.add(leftlists[i]);
               for (int i=0;i<rightlists.length;i++)
                  listlist.add(rightlists[i]);

               java.util.List mylist = new java.util.Vector(10);
               mylist.add(new org.apache.bcel.generic.ILOAD(index1));
               mylist.add(new org.apache.bcel.generic.ILOAD(index2));
               mylist.add(OPS[operator]);

               listlist.add(mylist);
            }else if (index1!=-1){
               // left was stored
               for (int i=0;i<leftlists.length;i++)
                  listlist.add(leftlists[i]);

               java.util.List mylist = new java.util.Vector(10);
               mylist.add(new org.apache.bcel.generic.ILOAD(index1));
               for (int i=0;i<rightlists.length;i++)
                  mylist.addAll(rightlists[i]);
               // should always have length 1
               mylist.add(OPS[operator]);

               listlist.add(mylist);
            }else if (index2!=-1){
               // right got stored
               for (int i=0;i<rightlists.length;i++)
                  listlist.add(rightlists[i]);

               java.util.List mylist = new java.util.Vector(10);
               for (int i=0;i<leftlists.length;i++)
                  mylist.addAll(leftlists[i]);
               mylist.add(new org.apache.bcel.generic.ILOAD(index2));
               mylist.add(OPS[operator]);

               listlist.add(mylist);
            }else{
               // neither stored

               java.util.List mylist = new java.util.Vector(10);
               for (int i=0;i<leftlists.length;i++)
                  mylist.addAll(leftlists[i]);
               for (int i=0;i<rightlists.length;i++)
                  mylist.addAll(rightlists[i]);
               mylist.add(OPS[operator]);

               listlist.add(mylist);
            }
            break;
         }

         case LITERAL_INT:{// makes 1 list
            java.util.List mylist = new java.util.Vector(2);
            if ((-1)<=literal_value && literal_value<=5){
               mylist.add(new org.apache.bcel.generic.ICONST(literal_value));
            }else{
               org.apache.bcel.generic.ConstantPoolGen cpg = method.getConstantPool();
               mylist.add(new org.apache.bcel.generic.LDC(cpg.addInteger(literal_value)));
            }
            listlist.add(mylist);
            break;
         }

         case VARIABLE:{// makes 1 list
            if (vars[varnumber] instanceof Integer){
               // Integer
               Integer localindex = (Integer)vars[varnumber];
               java.util.List mylist = new java.util.Vector(1);
               mylist.add(new org.apache.bcel.generic.ILOAD(localindex.intValue()));
               listlist.add(mylist);
            }else{
               // sandmark.program.Field
               sandmark.program.Field field = (sandmark.program.Field)vars[varnumber];
               java.util.List mylist = new java.util.Vector(5);
               org.apache.bcel.generic.ConstantPoolGen cpg = method.getConstantPool();
               int fieldindex = cpg.addFieldref(field.getEnclosingClass().getName(),
                                                field.getName(), "I");

               if (field.isStatic()){
                  mylist.add(new org.apache.bcel.generic.GETSTATIC(fieldindex));
               }else{
                  mylist.add(org.apache.bcel.generic.InstructionConstants.ALOAD_0);
                  mylist.add(new org.apache.bcel.generic.GETFIELD(fieldindex));
               }
               listlist.add(mylist);
            }
            break;
         }
      }

      return (java.util.List[])listlist.toArray(new java.util.List[0]);
   }

   public static void main(String args[]) throws Exception{
      String expr = "x*(x-1)%2==0";

      ExprTree tree = parse(expr);
      System.out.println(tree);
      System.out.println(tree.numVars());

      sandmark.program.Application app =
         new sandmark.program.Application("bench.jar");
      sandmark.program.Method method = app.getClasses()[0].getMethods()[0];
      method.setMaxLocals(10);

      Object[] vars = {new Integer(1), new Integer(2), new Integer(3),
                       new Integer(4), new Integer(5), new Integer(6),
                       new Integer(7), new Integer(8), new Integer(9)
                      };

      java.util.List[] lists = tree.getInstructionLists(method, vars);
      for (int i=0;i<lists.length;i++)
         System.out.println(lists[i]);
   }
}
