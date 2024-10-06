package sandmark.watermark.ct;

public class FindWindows {
   private static class Window {
      Window prev;
      int opcode;
      int mHash;
      public Window(Window p,int o) {
         prev = p;
         opcode = o;
         mHash = ((p == null ? 0 : p.hashCode()) << 3) + o;
      }
      public String toString() { 
         return (prev == null ? "" : (prev.toString() + " ")) + opcode;
      }
      public int hashCode() { return mHash; }
      public boolean equals(Object o) {
         Window w = (Window)o;
         return w.opcode == opcode && 
            ((w.prev == null && prev == null) ||
             (w.prev != null && prev != null && w.prev.equals(prev)));
      }
   }

   private static class WindowState {
      java.util.Hashtable mWindows = new java.util.Hashtable();
      java.util.Set mFilter;
      Window mCurrentWindows[];
      int mWindowCount[];

      WindowState(int n,String filterFile) {
         mCurrentWindows = new Window[n + 1];
         mWindowCount = new int[n + 1];
         parseFilter(filterFile);
      }
      void clear() {
         for(int i = 0 ; i < mCurrentWindows.length ; i++)
            mCurrentWindows[i] = null;
      }
      void collect(org.apache.bcel.generic.InstructionHandle ih,
                   boolean emptyStack) {
         for(int i = mCurrentWindows.length - 1 ; i > 0 ; i--) {
            if(i != 1 && mCurrentWindows[i - 1] == null) {
               mCurrentWindows[i] = null;
               continue;
            }
            if(i == 1 && !emptyStack) {
               mCurrentWindows[i] = null;
               continue;
            }
            mWindowCount[i]++;
            Window cur = new Window
               (mCurrentWindows[i - 1],ih.getInstruction().getOpcode());
            mCurrentWindows[i] = cur;
            Integer count = (Integer)mWindows.get(cur);
            if(mFilter == null || mFilter.contains(cur)) {
               if(count == null)
                  mWindows.put(cur,new Integer(1));
               else
                  mWindows.put(cur,new Integer(1 + count.intValue()));
            }
         }
      }

      public void dump(){
         for(java.util.Iterator windows = mWindows.keySet().iterator() ; 
             windows.hasNext() ; ) {
            Object window = windows.next();
            Object count = mWindows.get(window);
            System.out.println(window + " : " + count);
         }
         for(int i = 1 ; i < mWindowCount.length ; i++)
            System.out.println("windows of size " + i + ": " + mWindowCount[i]);
      }

      public String toString() {
         String val = "";
         for(java.util.Iterator windows = mWindows.keySet().iterator() ; 
             windows.hasNext() ; ) {
            Object window = windows.next();
            Object count = mWindows.get(window);
            val += window + " : " + count + "\n";
         }
         for(int i = 1 ; i < mWindowCount.length ; i++)
            val += "windows of size " + i + ": " + mWindowCount[i] + "\n";

         return val;
      }
      private void parseFilter(String file) {
         if(file == null)
            return;

         try {
            java.io.BufferedReader br =
               new java.io.BufferedReader
               (new java.io.InputStreamReader
                (new java.io.FileInputStream(file)));
            java.util.Set filter = new java.util.HashSet();
            try {
               for(String line ; (line = br.readLine()) != null ; ) {
                  String ops[] = line.split("\\s+");
                  Window cur = null;
                  for(int i = 0 ; i < ops.length && !ops[i].equals(":") ; i++) {
                     cur = new Window(cur,Integer.parseInt(ops[i]));
                  }
                  filter.add(cur);
               }
            } catch(NumberFormatException e) {
            }
            br.close();
            mFilter = filter;
         } catch(java.io.IOException e) {
            System.out.println("not filtering");
            e.printStackTrace();
         }
      }
   }

   public static void main(String argv[]) throws Exception {
      java.util.ArrayList methodSizes = new java.util.ArrayList();
      java.util.ArrayList methodInstructions = new java.util.ArrayList();
      int maxWindowSize = Integer.parseInt(argv[0]);
      sandmark.program.Application app;
      if(argv.length > 1)
         app = new sandmark.program.Application(argv[1]);
      else 
         app = Evaluate.doit
            (3,new java.math.BigInteger("123124342324"),
             "sandmark.util.newgraph.codec.RadixGraph",false);
      WindowState ws = new WindowState(maxWindowSize,
                                       argv.length > 2 ? argv[2] : null);

      for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();
         for(java.util.Iterator methods = cls.methods() ; methods.hasNext() ; ) {
            sandmark.program.Method method = 
               (sandmark.program.Method)methods.next();
            if(method.getInstructionList() == null)
               continue;
            //            methodInstructions.add(new Integer(method.getInstructionList().getLength()));
            //            methodSizes.add(new Integer(method.getInstructionList().getByteCode().length));
            for(org.apache.bcel.generic.InstructionHandle ih =
                   method.getInstructionList().getStart() ; ih != null ; 
                ih = ih.getNext())
               ws.collect(ih,true);
            ws.clear();
         }
      }
      
      ws.dump();

      /*
      System.out.print("Method Sizes: ");
      for(int i = 0 ; i < methodSizes.size() ; i++) 
         System.out.print(methodSizes.get(i) + " ");
      System.out.println();
      System.out.print("Method Lengths: ");
      for(int i = 0 ; i < methodInstructions.size() ; i++)
         System.out.print(methodInstructions.get(i) + " ");
      System.out.println();
      */
   }
}
