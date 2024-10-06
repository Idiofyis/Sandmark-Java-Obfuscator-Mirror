public abstract class Folder{
   public static int arraylength(Object array){
      if (array==null)
         throw new NullPointerException();
      if ((array instanceof byte[][]) ||
          (array instanceof boolean[][]) ||
          (array instanceof char[][]) ||
          (array instanceof double[][]) ||
          (array instanceof float[][]) ||
          (array instanceof int[][]) ||
          (array instanceof long[][]) ||
          (array instanceof short[][])){

         Object r0 = java.lang.reflect.Array.get(array, 0);
         Object r1 = java.lang.reflect.Array.get(array, 1);
         return 
            java.lang.reflect.Array.getLength(r0)+
            java.lang.reflect.Array.getLength(r1);
      }else{
         // 2
         Object r00 = java.lang.reflect.Array.get(java.lang.reflect.Array.get(array, 0), 0);
         Object r01 = java.lang.reflect.Array.get(java.lang.reflect.Array.get(array, 0), 1);
         Object r10 = java.lang.reflect.Array.get(java.lang.reflect.Array.get(array, 1), 0);
         Object r11 = java.lang.reflect.Array.get(java.lang.reflect.Array.get(array, 1), 1);
         return 
            java.lang.reflect.Array.getLength(r00)+
            java.lang.reflect.Array.getLength(r01)+
            java.lang.reflect.Array.getLength(r10)+
            java.lang.reflect.Array.getLength(r11);
      }
   }

   private static Class elementType(String arraysig){
      switch(arraysig.charAt(1)){
      case '[':
      case 'L':
         try{
            return Class.forName(arraysig.substring(1));
         }catch(ClassNotFoundException cnfe){
            throw new RuntimeException("This shouldn't happen!");
         }
      case 'B':
         return Byte.TYPE;
      case 'C':
         return Character.TYPE;
      case 'D':
         return Double.TYPE;
      case 'F':
         return Float.TYPE;
      case 'I':
         return Integer.TYPE;
      case 'J':
         return Long.TYPE;
      case 'S':
         return Short.TYPE;
      default:
         return null;
      }
   }

   /* [Ljava.lang.String;
      java.lang.String
      [B
      Byte.TYPE
   */
   public static Object newarray(int count, String newsig){
      if (count<0)
         throw new NegativeArraySizeException();
      int numextra = newsig.indexOf(',');
      String oldsig = newsig.substring(newsig.indexOf(',')+1).replace('/','.');
      String realsig = oldsig + (numextra==1 ? "[" : "[[");

      try{
         if (numextra==1){
            if (count==0)
               return java.lang.reflect.Array.newInstance(Class.forName(oldsig), 0);
           
            int length = (count/2)+(count%2);
         
            Object result = java.lang.reflect.Array.newInstance(Class.forName(oldsig), 2);
            Object r0 = java.lang.reflect.Array.newInstance(elementType(oldsig), length);
            Object r1 = java.lang.reflect.Array.newInstance(elementType(oldsig), count-length);
            java.lang.reflect.Array.set(result, 0, r0);
            java.lang.reflect.Array.set(result, 1, r1);
            return result;
         }else{
            if (count==0)
               return java.lang.reflect.Array.newInstance(Class.forName('['+oldsig), 0);

            int length = (count/4)+(count%4==0 ? 0 : 1);
         
            Object result = java.lang.reflect.Array.newInstance(Class.forName(oldsig), new int[]{2,2});
            int size=Math.min(length, count);
            Object r00 = java.lang.reflect.Array.newInstance(elementType(oldsig), size);
            count-=size;
            size=Math.min(length, count);
            Object r01 = java.lang.reflect.Array.newInstance(elementType(oldsig), size);
            count-=size;
            size=Math.min(length, count);
            Object r10 = java.lang.reflect.Array.newInstance(elementType(oldsig), size);
            count-=size;
            size=Math.min(length, count);
            Object r11 = java.lang.reflect.Array.newInstance(elementType(oldsig), size);
            
            Object r0 = java.lang.reflect.Array.get(result, 0);
            Object r1 = java.lang.reflect.Array.get(result, 1);
            
            java.lang.reflect.Array.set(r0, 0, r00);
            java.lang.reflect.Array.set(r0, 1, r01);
            java.lang.reflect.Array.set(r1, 0, r10);
            java.lang.reflect.Array.set(r1, 1, r11);
            
            return result;
         }
      }catch(ClassNotFoundException cnfe){
         throw new RuntimeException("This shouldn't happen!");
      }
   }

   public static int baload(Object array, int index){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();
      
      if (array instanceof byte[][]){
         byte[][] temp = (byte[][])array;
         if (index<temp[0].length){
            return (int)temp[0][index];
         }else{
            return (int)temp[1][index-temp[0].length];
         }
      }else if (array instanceof byte[][][]){
         byte[][][] temp = (byte[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            return (int)temp[0][0][index];
         }else if (index < len00+len01){
            return (int)temp[0][1][index-len00];
         }else if (index < len00+len01+len10){
            return (int)temp[1][0][index-len00-len01];
         }else{
            return (int)temp[1][1][index-len00-len01-len10];
         }
      }else if (array instanceof boolean[][]){
         boolean[][] temp = (boolean[][])array;
         
         if (index<temp[0].length){
            return (temp[0][index] ? 1 : 0);
         }else{
            return (temp[1][index-temp[0].length] ? 1 : 0);
         }
      }else if (array instanceof boolean[][][]){
         boolean[][][] temp = (boolean[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            return temp[0][0][index] ? 1 : 0;
         }else if (index < len00+len01){
            return temp[0][1][index-len00] ? 1 : 0;
         }else if (index < len00+len01+len10){
            return temp[1][0][index-len00-len01] ? 1 : 0;
         }else{
            return temp[1][1][index-len00-len01-len10] ? 1 : 0;
         }
      }else throw new RuntimeException("This shouldn't happen!");
   }


   public static char caload(Object array, int index){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();
      
      if (array instanceof char[][]){
         char[][] temp = (char[][])array;

         if (index<temp[0].length){
            return temp[0][index];
         }else{
            return temp[1][index-temp[0].length];
         }
      }else{
         char[][][] temp = (char[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            return temp[0][0][index];
         }else if (index < len00+len01){
            return temp[0][1][index-len00];
         }else if (index < len00+len01+len10){
            return temp[1][0][index-len00-len01];
         }else{
            return temp[1][1][index-len00-len01-len10];
         }
      }
   }


   public static double daload(Object array, int index){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();
      
      if (array instanceof double[][]){
         double[][] temp = (double[][])array;

         if (index<temp[0].length){
            return temp[0][index];
         }else{
            return temp[1][index-temp[0].length];
         }
      }else{
         double[][][] temp = (double[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            return temp[0][0][index];
         }else if (index < len00+len01){
            return temp[0][1][index-len00];
         }else if (index < len00+len01+len10){
            return temp[1][0][index-len00-len01];
         }else{
            return temp[1][1][index-len00-len01-len10];
         }
      }
   }



   public static float faload(Object array, int index){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();
      
      if (array instanceof float[][]){
         float[][] temp = (float[][])array;

         if (index<temp[0].length){
            return temp[0][index];
         }else{
            return temp[1][index-temp[0].length];
         }
      }else{
         float[][][] temp = (float[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            return temp[0][0][index];
         }else if (index < len00+len01){
            return temp[0][1][index-len00];
         }else if (index < len00+len01+len10){
            return temp[1][0][index-len00-len01];
         }else{
            return temp[1][1][index-len00-len01-len10];
         }
      }
   }




   public static int iaload(Object array, int index){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();

      if (array instanceof int[][]){
         int[][] temp = (int[][])array;

         if (index<temp[0].length){
            return temp[0][index];
         }else{
            return temp[1][index-temp[0].length];
         }
      }else{
         int[][][] temp = (int[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            return temp[0][0][index];
         }else if (index < len00+len01){
            return temp[0][1][index-len00];
         }else if (index < len00+len01+len10){
            return temp[1][0][index-len00-len01];
         }else{
            return temp[1][1][index-len00-len01-len10];
         }
      }
   }




   public static long laload(Object array, int index){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();

      if (array instanceof long[][]){
         long[][] temp = (long[][])array;

         if (index<temp[0].length){
            return temp[0][index];
         }else{
            return temp[1][index-temp[0].length];
         }
      }else{
         long[][][] temp = (long[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            return temp[0][0][index];
         }else if (index < len00+len01){
            return temp[0][1][index-len00];
         }else if (index < len00+len01+len10){
            return temp[1][0][index-len00-len01];
         }else{
            return temp[1][1][index-len00-len01-len10];
         }
      }
   }



   public static short saload(Object array, int index){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();

      if (array instanceof short[][]){
         short[][] temp = (short[][])array;

         if (index<temp[0].length){
            return temp[0][index];
         }else{
            return temp[1][index-temp[0].length];
         }
      }else{
         short[][][] temp = (short[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            return temp[0][0][index];
         }else if (index < len00+len01){
            return temp[0][1][index-len00];
         }else if (index < len00+len01+len10){
            return temp[1][0][index-len00-len01];
         }else{
            return temp[1][1][index-len00-len01-len10];
         }
      }
   }

   //////////////////////////////////


   public static void dastore(Object array, int index, double value){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();
      
      if (array instanceof double[][]){
         double[][] temp = (double[][])array;

         if (index<temp[0].length){
            temp[0][index] = value;
         }else{
            temp[1][index-temp[0].length] = value;
         }
      }else{
         double[][][] temp = (double[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            temp[0][0][index] = value;
         }else if (index < len00+len01){
            temp[0][1][index-len00] = value;
         }else if (index < len00+len01+len10){
            temp[1][0][index-len00-len01] = value;
         }else{
            temp[1][1][index-len00-len01-len10] = value;
         }
      }
   }



   public static void fastore(Object array, int index, float value){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();

      if (array instanceof float[][]){
         float[][] temp = (float[][])array;

         if (index<temp[0].length){
            temp[0][index] = value;
         }else{
            temp[1][index-temp[0].length] = value;
         }
      }else{
         float[][][] temp = (float[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            temp[0][0][index] = value;
         }else if (index < len00+len01){
            temp[0][1][index-len00] = value;
         }else if (index < len00+len01+len10){
            temp[1][0][index-len00-len01] = value;
         }else{
            temp[1][1][index-len00-len01-len10] = value;
         }
      }
   }



   public static void lastore(Object array, int index, long value){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();

      if (array instanceof long[][]){
         long[][] temp = (long[][])array;

         if (index<temp[0].length){
            temp[0][index] = value;
         }else{
            temp[1][index-temp[0].length] = value;
         }
      }else{
         long[][][] temp = (long[][][])array;

         int len00 = temp[0][0].length;
         int len01 = temp[0][1].length;
         int len10 = temp[1][0].length;

         if (index<len00){
            temp[0][0][index] = value;
         }else if (index < len00+len01){
            temp[0][1][index-len00] = value;
         }else if (index < len00+len01+len10){
            temp[1][0][index-len00-len01] = value;
         }else{
            temp[1][1][index-len00-len01-len10] = value;
         }
      }
   }


   public static void iastore(Object array, int index, int value){
      if (array==null)
         throw new NullPointerException();
      if (index<0)
         throw new ArrayIndexOutOfBoundsException();
      boolean dim2 = ((array instanceof byte[][]) ||
                      (array instanceof boolean[][]) ||
                      (array instanceof char[][]) ||
                      (array instanceof int[][]) ||
                      (array instanceof short[][]));

      if (dim2){
         Object r0 = java.lang.reflect.Array.get(array, 0);
         Object r1 = java.lang.reflect.Array.get(array, 1);

         int r0length=java.lang.reflect.Array.getLength(r0);
         Object array2set = (index<r0length ? r0 : r1);
         int index2set = (index<r0length ? index : index-r0length);

         if (array instanceof byte[][])
            ((byte[])array2set)[index2set] = (byte)value;
         else if (array instanceof boolean[][])
            ((boolean[])array2set)[index2set] = (value==1);
         else if (array instanceof char[][])
            ((char[])array2set)[index2set] = (char)value;
         else if (array instanceof int[][])
            ((int[])array2set)[index2set] = value;               
         else if (array instanceof short[][])
            ((short[])array2set)[index2set] = (short)value;

      }else{
         Object r0 = java.lang.reflect.Array.get(array, 0);
         Object r1 = java.lang.reflect.Array.get(array, 1);

         Object r00 = java.lang.reflect.Array.get(r0, 0);
         Object r01 = java.lang.reflect.Array.get(r0, 1);
         Object r10 = java.lang.reflect.Array.get(r1, 0);
         Object r11 = java.lang.reflect.Array.get(r1, 1);

         int r00length=java.lang.reflect.Array.getLength(r00);
         int r01length=java.lang.reflect.Array.getLength(r01);
         int r10length=java.lang.reflect.Array.getLength(r10);
         int r11length=java.lang.reflect.Array.getLength(r11);

         Object array2set;
         int index2set;

         if (index<r00length){
            array2set=r00;
            index2set=index;
         }else if (index < r00length+r01length){
            array2set=r01;
            index2set=index-r00length;
         }else if (index < r00length+r01length+r10length){
            array2set=r10;
            index2set=index-r00length-r01length;
         }else{
            array2set=r11;
            index2set=index-r00length-r01length-r10length;
         }

         if (array instanceof byte[][][])
            ((byte[])array2set)[index2set] = (byte)value;
         else if (array instanceof boolean[][][])
            ((boolean[])array2set)[index2set] = (value==1);
         else if (array instanceof char[][][])
            ((char[])array2set)[index2set] = (char)value;
         else if (array instanceof int[][][])
            ((int[])array2set)[index2set] = value;
         else if (array instanceof short[][][])
            ((short[])array2set)[index2set] = (short)value;
      }
   }
}
