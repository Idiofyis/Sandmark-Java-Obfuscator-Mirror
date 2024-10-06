/** This class is used by the ArraySplitter obfuscation.
 *  It is added into obfuscated applications, but not used
 *  directly by ArraySplitter.
 */
public abstract class HashHolder{
   private static java.util.HashMap name2front;
   private static java.util.HashMap front2end;
   private static java.util.HashMap front2frontlength;
   static{
      name2front = new java.util.HashMap();
      front2end = new java.util.HashMap();
      front2frontlength = new java.util.HashMap();
   }

   // Called when a putfield or putstatic is done
   // on one of the split arrays. The return value
   // goes in the 'front'.
   public static Object putvar(Object array, String fieldname){
      if (array==null){
         Object front = name2front.get(fieldname);
         if (front!=null){
            name2front.put(fieldname, null);
            front2end.remove(front);
            front2frontlength.remove(front);
         }
         return null;
      }

      int length = java.lang.reflect.Array.getLength(array);
      Object end = java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), length-(length/2));
      System.arraycopy(array, length/2, end, 0, length-(length/2));
      name2front.put(fieldname, array);
      front2end.put(array, end);
      front2frontlength.put(array, new Integer(length/2));
      return array;
   }

   public static Object getEnd(String fieldname){
      Object front = name2front.get(fieldname);
      if (front==null)
         return null;
      return front2end.get(front);
   }

   public static void copyOut(){
      for (java.util.Iterator keys=front2end.keySet().iterator();keys.hasNext();){
         Object front = keys.next();
         Object end = front2end.get(front);
         Integer frontlength = (Integer)front2frontlength.get(front);
         System.arraycopy(end, 0, front, frontlength.intValue(), java.lang.reflect.Array.getLength(end));
      }
   }

   // replace every ARRAYLENGTH with a call to this function
   public static int arraylength(Object array){
      if (array==null)
         return java.lang.reflect.Array.getLength(array);

      Object end = front2end.get(array);
      if (end==null)
         return java.lang.reflect.Array.getLength(array);
      Integer frontlength = (Integer)front2frontlength.get(array);
      return (frontlength.intValue()+java.lang.reflect.Array.getLength(end));
   }

   // replace every BALOAD with a call to this function or the next one
   public static int baload(Object array, int index){
      Object end = front2end.get(array);
      if (end==null){
         if (array instanceof byte[])
            return java.lang.reflect.Array.getByte(array, index);
         else
            return java.lang.reflect.Array.getBoolean(array, index) ? 1 : 0;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         if (array instanceof byte[])
            return (int)java.lang.reflect.Array.getByte(array, index);
         else
            return java.lang.reflect.Array.getBoolean(array, index) ? 1 : 0;
      }else{
         index-=frontlength.intValue();
         if (array instanceof byte[])
            return (int)java.lang.reflect.Array.getByte(end, index);
         else 
            return java.lang.reflect.Array.getBoolean(end, index) ? 1 : 0;
      }
   }

   // replace every CALOAD with a call to this function
   public static char caload(char[] array, int index){
      Object end = front2end.get(array);
      if (end==null){
         return array[index];
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         return array[index];
      }else{
         index-=frontlength.intValue();
         return java.lang.reflect.Array.getChar(end, index);
      }
   }

   // replace every DALOAD with a call to this function
   public static double daload(double[] array, int index){
      Object end = front2end.get(array);

      if (end==null){
         return array[index];
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         return array[index];
      }else{
         index-=frontlength.intValue();
         return java.lang.reflect.Array.getDouble(end, index);
      }
   }

   // replace every FALOAD with a call to this function
   public static float faload(float[] array, int index){
      Object end = front2end.get(array);
      if (end==null){
         return array[index];
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         return array[index];
      }else{
         index-=frontlength.intValue();
         return java.lang.reflect.Array.getFloat(end, index);
      }
   }

   // replace every IALOAD with a call to this function
   public static int iaload(int[] array, int index){
      Object end = front2end.get(array);
      if (end==null){
         return array[index];
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         return array[index];
      }else{
         index-=frontlength.intValue();
         return java.lang.reflect.Array.getInt(end, index);
      }
   }

   // replace every LALOAD with a call to this function
   public static long laload(long[] array, int index){
      Object end = front2end.get(array);
      if (end==null){
         return array[index];
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         return array[index];
      }else{
         index-=frontlength.intValue();
         return java.lang.reflect.Array.getLong(end, index);
      }
   }

   // replace every SALOAD with a call to this function
   public static short saload(short[] array, int index){
      Object end = front2end.get(array);
      if (end==null){
         return array[index];
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         return array[index];
      }else{
         index-=frontlength.intValue();
         return java.lang.reflect.Array.getShort(end, index);
      }
   }

   // replace every AALOAD with a call to this function
   // and follow it with a CHECKCAST to the correct array type
   public static Object aaload(Object array, int index){
      Object end = front2end.get(array);
      if (end==null){
         return java.lang.reflect.Array.get(array, index);
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         return java.lang.reflect.Array.get(array, index);
      }else{
         index-=frontlength.intValue();
         return java.lang.reflect.Array.get(end, index);
      }
   }

   ////////////////////////////////////////////////////


   // replace every BASTORE with a call to this function or the next one
   public static void bastore(Object array, int index, int value){
      Object end = front2end.get(array);
      if (end==null){
         if (array instanceof byte[])
            java.lang.reflect.Array.setByte(array, index, (byte)value);
         else
            java.lang.reflect.Array.setBoolean(array, index, value==1);
         return;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         if (array instanceof byte[])
            java.lang.reflect.Array.setByte(array, index, (byte)value);
         else
            java.lang.reflect.Array.setBoolean(array, index, value==1);
      }else{
         index-=frontlength.intValue();
         if (end instanceof byte[])
            java.lang.reflect.Array.setByte(end, index, (byte)value);
         else
            java.lang.reflect.Array.setBoolean(end, index, value==1);
      }
   }

   // replace every CASTORE with a call to this function
   public static void castore(char[] array, int index, char value){
      Object end = front2end.get(array);
      if (end==null){
         array[index] = value;
         return;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         array[index] = value;
      }else{
         index-=frontlength.intValue();
         java.lang.reflect.Array.setChar(end, index, value);
      }
   }

   // replace every DASTORE with a call to this function 
   public static void dastore(double[] array, int index, double value){
      Object end = front2end.get(array);
      if (end==null){
         array[index] = value;
         return;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         array[index] = value;
      }else{
         index-=frontlength.intValue();
         java.lang.reflect.Array.setDouble(end, index, value);
      }
   }

   // replace every FASTORE with a call to this function
   public static void fastore(float[] array, int index, float value){
      Object end = front2end.get(array);
      if (end==null){
         array[index] = value;
         return;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         array[index] = value;
      }else{
         index-=frontlength.intValue();
         java.lang.reflect.Array.setFloat(end, index, value);
      }
   }

   // replace every IASTORE with a call to this function
   public static void iastore(int[] array, int index, int value){
      Object end = front2end.get(array);
      if (end==null){
         array[index] = value;
         return;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         array[index] = value;
      }else{
         index-=frontlength.intValue();
         java.lang.reflect.Array.setInt(end, index, value);
      }
   }

   // replace every LASTORE with a call to this function
   public static void lastore(long[] array, int index, long value){
      Object end = front2end.get(array);
      if (end==null){
         array[index] = value;
         return;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         array[index] = value;
      }else{
         index-=frontlength.intValue();
         java.lang.reflect.Array.setLong(end, index, value);
      }
   }

   // replace every SASTORE with a call to this function
   public static void sastore(short[] array, int index, short value){
      Object end = front2end.get(array);
      if (end==null){
         array[index] = value;
         return;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         array[index] = value;
      }else{
         index-=frontlength.intValue();
         java.lang.reflect.Array.setShort(end, index, value);
      }
   }

   // replace every AASTORE with a call to this function
   public static void aastore(Object array, int index, Object value){
      Object end = front2end.get(array);
      if (end==null){
         java.lang.reflect.Array.set(array, index, value);
         return;
      }
      
      Integer frontlength = (Integer)front2frontlength.get(array);
      if (index<frontlength.intValue()){
         java.lang.reflect.Array.set(array, index, value);
      }else{
         index-=frontlength.intValue();
         java.lang.reflect.Array.set(end, index, value);
      }
   }
}
