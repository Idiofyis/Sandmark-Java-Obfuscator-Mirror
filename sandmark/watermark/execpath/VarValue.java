package sandmark.watermark.execpath;


/** This class contains a snapshot of a variable and its value at a particular point in time.
 *  The "value" will be an Integer wrapper around the hashcode of the object if it is a ReferenceType.
 *  Alternatively, if the object was null at this point, the value will be null.
 *  For a BasicType, the value will be the appropriate wrapper class around the given value.
 *  For a local variable, the name of the variable will be "L[i]" where i is the local index.
 *  If a VarValue represents a local variable, then the getLocalIndex() method returns the index (i).
 */
public class VarValue{
   public static final Object NONNULL = new Object();
   public static final int STATIC = 0;
   public static final int INSTANCE = 1;
   public static final int LOCAL = 2;
	
   private org.apache.bcel.generic.Type type;
   private String name;
   private Object value;
   private int localIndex;
   private int kind;

   private static java.util.Hashtable typeCache = new java.util.Hashtable();

   // static type name=value
   // this type name=value
   // type L[i]=value

   protected VarValue(String line){
      localIndex = -1;
      java.util.StringTokenizer st = new java.util.StringTokenizer(line);
      String typeStr = null;

      String varType = st.nextToken();
      if (varType.equals("static")){
         kind = STATIC;
         typeStr = st.nextToken();
      }else if (varType.equals("this")){
         kind = INSTANCE;
         typeStr = st.nextToken();
      }else{
         kind = LOCAL;
         typeStr = varType;
      }

      type = org.apache.bcel.generic.Type.getType(typeStr);
      
      String nameValueStr = st.nextToken();
      int equalsIndex = nameValueStr.indexOf('=');
      name = nameValueStr.substring(0, equalsIndex);

      String valueStr = nameValueStr.substring(equalsIndex+1);
      
      if (kind==LOCAL){
         localIndex = Integer.parseInt(name.substring(2, name.indexOf(']')));
      }

      Object o = typeCache.get(typeStr);
      if(o == null)
         typeCache.put(typeStr,type);
      else
         type = (org.apache.bcel.generic.Type)o;

      if (type instanceof org.apache.bcel.generic.BasicType){
         if (type.equals(org.apache.bcel.generic.Type.BYTE)){
            value = new Byte((byte)(0xFF & Integer.parseInt(valueStr)));
         }else if (type.equals(org.apache.bcel.generic.Type.BOOLEAN)){
            value = new Boolean(valueStr);
         }else if (type.equals(org.apache.bcel.generic.Type.CHAR)){
            value = new Character((char)(0xFFFF & Integer.parseInt(valueStr)));
         }else if (type.equals(org.apache.bcel.generic.Type.DOUBLE)){
            value = new Double(valueStr);
         }else if (type.equals(org.apache.bcel.generic.Type.FLOAT)){
            value = new Float(valueStr);
         }else if (type.equals(org.apache.bcel.generic.Type.INT)){
            value = new Integer(valueStr);
         }else if (type.equals(org.apache.bcel.generic.Type.SHORT)){
            value = new Short((short)(0xFFFF & Integer.parseInt(valueStr)));
         }else if (type.equals(org.apache.bcel.generic.Type.LONG)){
            value = new Long(valueStr);
         }
      }else if (valueStr.equals("null")){
         value = null;
      }else if (valueStr.equals("nonnull")){
         value = NONNULL;
      }else{
         value = new Integer(Integer.parseInt(valueStr));
      }
   }

   /** Returns true iff getValue() will be an Integer around the hashcode of this variable
    */
   public boolean valueIsHashCode(){
      return !(type instanceof org.apache.bcel.generic.BasicType);
   }

   /** Returns the name of this variable. Locals are named "L[i]" where i is the local index.
    */
   public String getName(){
      return name;
   }

   /** Returns a flag int determining what kind of variable this is. 
    *	 One of STATIC, INSTANCE, or LOCAL.
    */
   public int getKind(){
      return kind;
   }

   /** Returns true iff this value came from an object
    *  that has an unsafe hashCode method, but is nonnull.
    */
   public boolean isNonnullUnhashable(){
      return (value==NONNULL);
   }

   /** Returns true iff the value is a null reference.
    */
   public boolean isNull(){
      return (value==null);
   }

   /** Returns a Type class for this variable.
    */
   public org.apache.bcel.generic.Type getType(){
      return type;
   }

   /** Returns the index of the local variable if this VarValue
    *	 represents a local variable. If not, it returns -1.
    */
   public int getLocalIndex(){
      return localIndex;
   }

   /** Returns the value of this variable at this particular trace point.
    *  For BasicTypes, this will be the appropriate wrapper class around the 
    *	 given value. For ReferenceTypes, this will be an Integer wrapper around the hashcode
    *  of the object at this particular tracepoint (or possibly null).
    */
   public Object getValue(){
      return value;
   }

   /** Returns true iff the given VarValue refers to the same variable as this one
    *  (compares name, type, and kind).
    */
   public boolean equals(Object o){
      if (o==null || !(o instanceof VarValue))
         return false;
      VarValue v = (VarValue)o;
      return (v.kind==kind && v.name.equals(name) && v.type.equals(type));
   }

   public String toString(){
      String[] arr = {"STATIC ", "INSTANCE ", "LOCAL "};
      String result = arr[kind] + type.toString() + " "+name+" = " +value;
      return result;
   }
}
