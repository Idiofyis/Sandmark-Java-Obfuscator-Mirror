package sandmark.birthmark.windows;

public class WindowState {
      java.util.Hashtable mWindows = new java.util.Hashtable();
      java.util.Set mFilter;
      Window mCurrentWindows[];
      int mWindowCount[];
      public WindowState(int n,String filterFile) {
	 mCurrentWindows = new Window[n + 1];
	 mWindowCount = new int[n + 1];
	 parseFilter(filterFile);
      }
      public void clear() {
	 for(int i = 0 ; i < mCurrentWindows.length ; i++)
	    mCurrentWindows[i] = null;
      }
      public void collect(org.apache.bcel.generic.InstructionHandle ih,
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

      public java.util.Iterator getWindows(){
         return mWindows.keySet().iterator();
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
