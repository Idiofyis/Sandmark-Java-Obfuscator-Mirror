package sandmark.obfuscate.exceptionbranches;

/** This is the dispatcher class that gets added to each jarfile
 *  that has been obfuscated with ExceptionBranches. It maintains
 *  a static hashtable of position values to their corresponding
 *  lists of exceptions to throw. For switch statements, it also 
 *  hashes their positions to their list of integer matches, and their
 *  'default' exceptions to throw. These hashtables are built up by
 *  having obfuscated classes make calls to DispatcherException.register
 *  and DispatcherException.registerSwitch. This class is not used by the obfuscation itself,
 *  only the obfuscated jarfile. 
 *  This class is also an Exception class, so that if anything goes wrong
 *  with returning an instance of the correct exception type, a DispatcherException
 *  with be returned instead of the correct exception type.
 */
public class DispatcherException extends Throwable{
   // these constants define all the different integer 'if' statements
   private static final short IF_ICMPEQ = 0;
   private static final short IF_ICMPGE = 1;
   private static final short IF_ICMPGT = 2;
   private static final short IF_ICMPLE = 3;
   private static final short IF_ICMPLT = 4;
   private static final short IF_ICMPNE = 5;
   private static final short IFEQ = 0;
   private static final short IFGE = 1;
   private static final short IFGT = 2;
   private static final short IFLE = 3;
   private static final short IFLT = 4;
   private static final short IFNE = 5;
   ///////////////////////////////////

   // hashes Integers to String[]s
   private static java.util.Hashtable position2classes;
   // hashes Integers to int[]s
   private static java.util.Hashtable position2matches;
   // hashes Integers to Strings
   private static java.util.Hashtable position2default;
   // hashes String names to instances
   private static java.util.Hashtable name2instance;


   static{
      position2classes = new java.util.Hashtable();
      position2matches = new java.util.Hashtable();
      position2default = new java.util.Hashtable();
      name2instance = new java.util.Hashtable();
   }

   /** Standard exception class constructor
    */
   public DispatcherException(String message){
      super(message);
   }

   /** Called by <clinit> methods to register branches with their 
    *  exception types. This is only for ifs and gotos.
    *  @param position a globally unique integer representing a given branch instruction.
    *  @param class a String of the following form "name1=name2=name3" where the nameX
    *  are the fully-qualified classnames of the various exception types, in the right order.
    *  (i.e. "sandmark.obfuscate.exceptionbranches.SandmarkException1=sandmark.obfuscate.exceptionbranches.SandmarkException2=")
    */
   public static void register(int position, String classes){
      position2classes.put(new Integer(position), classes.split("=+"));
   }

   /** Similar to register, but for switch statements.
    *  @param position a globally unique integer representing a given switch instruction.
    *  @param matches a string of the integer matches for this switch, separated by '=' characters (i.e. "1=2=3=4=5436756928")
    *  @param classes a string of exception classnames separated by '=' characters (see register).
    *  @param defaultclass the name of the exception to throw on the 'default' case
    */
   public static void registerSwitch(int position, String matches, String classes, String defaultclass){
      Integer pos = new Integer(position);
      position2classes.put(pos, classes.split("=+"));
      position2default.put(pos, defaultclass);
      String[] strmatches = matches.split("=+");
      int[] intmatches = new int[strmatches.length];
      for (int i=0;i<intmatches.length;i++)
         intmatches[i] = Integer.parseInt(strmatches[i]);
      position2matches.put(pos, intmatches);
   }


   public static Throwable if_acmpeq(Object o1, Object o2, int position){
      String[] exceptions = (String[])position2classes.get(new Integer(position));
      if (exceptions==null || exceptions.length!=2)
         return new DispatcherException("Exceptions for this position are invalid");
      String tothrow = (o1==o2 ? exceptions[0] : exceptions[1]);
      
      Throwable t = (Throwable)name2instance.get(tothrow);
      if (t!=null)
         return t;

      try{
         t = (Throwable)Class.forName(tothrow).newInstance();
         name2instance.put(tothrow, t);
         return t;
      }catch(java.lang.ClassNotFoundException cnfe){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.InstantiationException ie){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.IllegalAccessException iae){
         return new DispatcherException("Exception classes cannot be instantiated");
      }
   }

   public static Throwable if_acmpne(Object o1, Object o2, int position){
      String[] exceptions = (String[])position2classes.get(new Integer(position));
      if (exceptions==null || exceptions.length!=2)
         return new DispatcherException("Exceptions for this position are invalid");
      String tothrow = (o1!=o2 ? exceptions[0] : exceptions[1]);
      
      Throwable t = (Throwable)name2instance.get(tothrow);
      if (t!=null)
         return t;

      try{
         t = (Throwable)Class.forName(tothrow).newInstance();
         name2instance.put(tothrow, t);
         return t;
      }catch(java.lang.ClassNotFoundException cnfe){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.InstantiationException ie){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.IllegalAccessException iae){
         return new DispatcherException("Exception classes cannot be instantiated");
      }
   }

   public static Throwable ifnonnull(Object o1, int position){
      String[] exceptions = (String[])position2classes.get(new Integer(position));
      if (exceptions==null || exceptions.length!=2)
         return new DispatcherException("Exceptions for this position are invalid");
      String tothrow = (o1!=null ? exceptions[0] : exceptions[1]);
      
      Throwable t = (Throwable)name2instance.get(tothrow);
      if (t!=null)
         return t;

      try{
         t = (Throwable)Class.forName(tothrow).newInstance();
         name2instance.put(tothrow, t);
         return t;
      }catch(java.lang.ClassNotFoundException cnfe){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.InstantiationException ie){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.IllegalAccessException iae){
         return new DispatcherException("Exception classes cannot be instantiated");
      }
   }

   public static Throwable ifnull(Object o1, int position){
      String[] exceptions = (String[])position2classes.get(new Integer(position));
      if (exceptions==null || exceptions.length!=2)
         return new DispatcherException("Exceptions for this position are invalid");
      String tothrow = (o1==null ? exceptions[0] : exceptions[1]);
      
      Throwable t = (Throwable)name2instance.get(tothrow);
      if (t!=null)
         return t;

      try{
         t = (Throwable)Class.forName(tothrow).newInstance();
         name2instance.put(tothrow, t);
         return t;
      }catch(java.lang.ClassNotFoundException cnfe){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.InstantiationException ie){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.IllegalAccessException iae){
         return new DispatcherException("Exception classes cannot be instantiated");
      }
   }
   
   ///////////////////////////////////////

   // a helper method for code-factoring
   private static Throwable if_binaryop(int i1, int i2, int position, short op){
      String[] exceptions = (String[])position2classes.get(new Integer(position));
      if (exceptions==null || exceptions.length!=2)
         return new DispatcherException("Exceptions for this position are invalid");
      boolean first=false;

      switch(op){
      case IF_ICMPEQ:
         first = (i1==i2);
         break;
      case IF_ICMPGE:
         first = (i1>=i2);
         break;
      case IF_ICMPGT:
         first = (i1>i2);
         break;
      case IF_ICMPLE:
         first = (i1<=i2);
         break;
      case IF_ICMPLT:
         first = (i1<i2);
         break;
      case IF_ICMPNE:
         first = (i1!=i2);
         break;
      }

      String tothrow = (first ? exceptions[0] : exceptions[1]);
      
      Throwable t = (Throwable)name2instance.get(tothrow);
      if (t!=null)
         return t;

      try{
         t = (Throwable)Class.forName(tothrow).newInstance();
         name2instance.put(tothrow, t);
         return t;
      }catch(java.lang.ClassNotFoundException cnfe){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.InstantiationException ie){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.IllegalAccessException iae){
         return new DispatcherException("Exception classes cannot be instantiated");
      }
   }

   public static Throwable if_icmpeq(int i1, int i2, int position){
      return if_binaryop(i1, i2, position, IF_ICMPEQ);
   }

   public static Throwable if_icmpge(int i1, int i2, int position){
      return if_binaryop(i1, i2, position, IF_ICMPGE);
   }

   public static Throwable if_icmpgt(int i1, int i2, int position){
      return if_binaryop(i1, i2, position, IF_ICMPGT);
   }

   public static Throwable if_icmple(int i1, int i2, int position){
      return if_binaryop(i1, i2, position, IF_ICMPLE);
   }

   public static Throwable if_icmplt(int i1, int i2, int position){
      return if_binaryop(i1, i2, position, IF_ICMPLT);
   }

   public static Throwable if_icmpne(int i1, int i2, int position){
      return if_binaryop(i1, i2, position, IF_ICMPNE);
   }

   ///////////////////////////////////////

   // a helper method for code-factoring
   private static Throwable if_unaryop(int i1, int position, short op){
      String[] exceptions = (String[])position2classes.get(new Integer(position));
      if (exceptions==null || exceptions.length!=2)
         return new DispatcherException("Exceptions for this position are invalid");
      boolean first=false;

      switch(op){
      case IFEQ:
         first = (i1==0);
         break;
      case IFGE:
         first = (i1>=0);
         break;
      case IFGT:
         first = (i1>0);
         break;
      case IFLE:
         first = (i1<=0);
         break;
      case IFLT:
         first = (i1<0);
         break;
      case IFNE:
         first = (i1!=0);
         break;
      }

      String tothrow = (first ? exceptions[0] : exceptions[1]);
      
      Throwable t = (Throwable)name2instance.get(tothrow);
      if (t!=null)
         return t;

      try{
         t = (Throwable)Class.forName(tothrow).newInstance();
         name2instance.put(tothrow, t);
         return t;
      }catch(java.lang.ClassNotFoundException cnfe){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.InstantiationException ie){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.IllegalAccessException iae){
         return new DispatcherException("Exception classes cannot be instantiated");
      }
   }

   public static Throwable ifeq(int i1, int position){
      return if_unaryop(i1, position, IFEQ);
   }

   public static Throwable ifge(int i1, int position){
      return if_unaryop(i1, position, IFGE);
   }

   public static Throwable ifgt(int i1, int position){
      return if_unaryop(i1, position, IFGT);
   }

   public static Throwable ifle(int i1, int position){
      return if_unaryop(i1, position, IFLE);
   }

   public static Throwable iflt(int i1, int position){
      return if_unaryop(i1, position, IFLT);
   }

   public static Throwable ifne(int i1, int position){
      return if_unaryop(i1, position, IFNE);
   }

   ///////////////////////////////////////

   // replaces a 'goto' or 'goto_w' instruction
   public static Throwable dogoto(int position){
      String[] exceptions = (String[])position2classes.get(new Integer(position));
      if (exceptions==null || exceptions.length!=1)
         return new DispatcherException("Exceptions for this position are invalid");
      String tothrow = exceptions[0];

      Throwable t = (Throwable)name2instance.get(tothrow);
      if (t!=null)
         return t;

      try{
         t = (Throwable)Class.forName(tothrow).newInstance();
         name2instance.put(tothrow, t);
         return t;
      }catch(java.lang.ClassNotFoundException cnfe){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.InstantiationException ie){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.IllegalAccessException iae){
         return new DispatcherException("Exception classes cannot be instantiated");
      }
   }

   // replaces a 'lookupswitch' or 'tableswitch' instruction
   public static Throwable doswitch(int value, int position){
      Integer pos = new Integer(position);
      String[] exceptions = (String[])position2classes.get(pos);
      int[] matches = (int[])position2matches.get(pos);
      String defaultclass = (String)position2default.get(pos);

      if (exceptions==null || matches==null || defaultclass==null || 
          exceptions.length!=matches.length){
         return new DispatcherException("Exceptions for this position are invalid");
      }

      String tothrow = defaultclass;
      for (int i=0;i<matches.length;i++){
         if (matches[i]==value){
            tothrow = exceptions[i];
            break;
         }
      }

      Throwable t = (Throwable)name2instance.get(tothrow);
      if (t!=null)
         return t;

      try{
         t = (Throwable)Class.forName(tothrow).newInstance();
         name2instance.put(tothrow, t);
         return t;
      }catch(java.lang.ClassNotFoundException cnfe){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.InstantiationException ie){
         return new DispatcherException("Exception classes cannot be instantiated");
      }catch(java.lang.IllegalAccessException iae){
         return new DispatcherException("Exception classes cannot be instantiated");
      }
   }
}
