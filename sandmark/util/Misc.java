package sandmark.util;

/**
 *  The sandmark.util.Misc class holds utility methods that
 *  don't fit anywhere else.
 */

public class Misc {

    /**
       Returns a String to use as a hashkey to hash a class.
    */
    public static String getKeyForClass(String classname)
    {
        return "CLASS:" + classname;
    }

    /**
       Returns a String to use as a hashkey to hash a method.
    */
    public static String getKeyForMethod(String classname,
                                         String methodname,
                                         String signature)
    {
        return "CLASS:" + classname + "METHOD:" +
            methodname + signature;
    }

    public static String getKeyForMethod(sandmark.util.MethodID mid)
    {
        return "CLASS:" + mid.getClassName() + "METHOD:" +
            mid.getName() + mid.getSignature();
    }

    public static String getKeyForMethod(sandmark.program.Method method) {
	return getKeyForMethod(new MethodID(method));
    }

    /**
       Given the hashkey from getKeyForMethod, returns an array of
       Strings {class name, method name, signature} for the method.
       If a malformed key is passed to this method, the result is
       not defined.
    */
    public static String[] getMethodForKey(String key)
    {
        String [] retVal = new String[3];
        try{
            retVal[0] = key.substring(6, key.indexOf("METHOD:"));
            retVal[1] = key.substring(key.indexOf("METHOD:")+7, key.indexOf("("));
            retVal[2] = key.substring(key.indexOf("("), key.length());
        }
        catch( java.lang.StringIndexOutOfBoundsException e){
            retVal[0] = null;
            retVal[1] = null;
            retVal[2] = null;
        }

        return retVal;
    }

    public static String getClassForKey(String key)
    {
        return key.substring(key.indexOf(":"+1, key.length()));
    }

    /**
       Returns a String to use as a hashkey to hash the whole application.
    */
    public static String getKeyForApp()
    {
        return "WHOLEJAR";
    }

    /*
     *  Throws an Error exception to abort the current action.
     */
    public static void abort(String msg) {
        throw new Error(msg);
    }

    public static void exit(int val, String msg) {
        System.err.println(msg);
	throw new Error(val + ": " + msg);
    }

    /*
     * Format en integer matrix.
     */
    public static String matrix2String(int[][] M) {
        String S = "";

        int[] head = new int[M.length+1];
        for(int i=0; i<M.length; i++)
            head[i+1] = i;
        head[0] = 0;
        S += row2String(head) + "\n";
        for(int i=0; i<(M.length*4+1); i++)
            S += "-";
        S += "\n";

        for(int i=0; i<M.length; i++)
            S += int2String(i) + "|" + row2String(M[i]) + "\n";

        return S;
    }

    public static String row2String(int[] row) {
        String S = "";
        for(int j=0; j<row.length; j++)
            S += int2String(row[j]);
        return S;
    }

    public static String int2String(int k) {
        String W = k + "";
        String S = W;
        int L = 4-W.length();
        if (L <= 0) L = 1;
   for(int i=0; i<L; i++)
       S += " ";
   return S;
    }

    /*
     *  Concatenates an argument list to form a single string.
     *  Each argument is preceded by a single space character.
     *  A space or backslash in an argument is preceded by an escaping backslash.
     */
    public static String joinArgs(String[] arglist, int first, int len) {
        StringBuffer b = new StringBuffer();
        while (len-- > 0) {
            b.append(' ');
            String s = arglist[first++];
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == ' ' ||  c == '\\') {
                    b.append('\\');
                }
                b.append(c);
            }
        }
        return b.toString();
    }



    /*
     *  Splits an argument list created by {@link #joinArgs}.
     */
    public static String[] splitArgs(String argstring) {

        int len = argstring.length();
        int nargs = 0;
        for (int i = 0; i < len; i++) {
            char c = argstring.charAt(i);
            if (c == ' ') {
                nargs++;
            } else if (c == '\\') {
                i++;
        }
        }

        String[] arglist = new String[nargs];
        int a = 0;
        int i = 0;
        while (i < len) {
            StringBuffer b = new StringBuffer();
            while (++i < len) {
                char c = argstring.charAt(i);
                if (c == ' ') {
                    break;
                } else if (c == '\\') {
                    b.append(argstring.charAt(++i));
                } else  {
                    b.append(c);
                }
            }
        arglist[a++] = b.toString();
        }

        return arglist;
    }



   /**
    * Returns an array of the items returned by a java.util.Iterator.
    * If the supplied array is large enough, it is used; otherwise
    * a new array of the same type and exactly adequate size is created.
    */
   public static java.lang.Object[] buildArray(
         java.util.Iterator it, java.lang.Object[] a) {

      java.util.List list = new java.util.ArrayList();
      while (it.hasNext()) {
         list.add(it.next());
      }
      return list.toArray(a);
   }



   /**
    * Returns an iterator that filters another iterator
    * to produce only objects that are instances of a given class.
    *
    * @param it   underlying iterator
    * @param c    class to select for
    */
   public static java.util.Iterator instanceFilter(
         final java.util.Iterator it, final java.lang.Class c) {
      java.util.Vector v = new java.util.Vector();
      while (it.hasNext()) {
	 java.lang.Object o = it.next();
	 if (c.isInstance(o))
	    v.add(o);
      }
      return v.iterator();
   }


   /**
    * Loads a file into a byte array.
    */
   public static byte[] loadBytes(java.io.InputStream instream)
         throws java.io.IOException {
      instream = new java.io.BufferedInputStream(instream);
      java.io.ByteArrayOutputStream bstream =
         new java.io.ByteArrayOutputStream();
      byte[] buf = new byte[8192];      // arbitrary size
      int n;
      while ((n = instream.read(buf)) >= 0) {
         bstream.write(buf, 0, n);
      }
      return bstream.toByteArray();
   }



    //------------------------------------------------------------

    /**
     * Write a string to a file.
     * <P>
     * @param fileName The name of the file to be written.
     * @param string   The string to be written.
     **/
    public static void writeToFile (
                                    String fileName,
                                    String string) throws Exception {
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(
                                                                 new java.io.FileWriter(fileName));
            writer.println(string);
            writer.close();
        } catch (Exception e) {
            throw new Exception("Failed to write file '" + fileName + "': " + e.toString());
        }
    }
    //------------------------------------------------------------



    public static void main(String args[]) {

        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("++++++++++++++ Testing util.Misc +++++++++++++++++");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");

        argtest(new String[] { });
        argtest(new String[] { "foo" });
        argtest(new String[] { "fourscore", "and", "seven", "years" });
        argtest(new String[] { "a\\b\\c" });
        argtest(new String[] { "d e f" });
        argtest(new String[] { "", " ", "" });
        argtest(new String[] { "ab", "", " c\\d ", "", " ", "\\", "\\\\", "" });
        System.out.println("\n+++++++++++++++++++++++++++++++++++++++++++++++++");
    }



private static void argtest(String[] arglist) {

    String s = joinArgs(arglist, 0, arglist.length);
    String[] newlist = splitArgs(s);
    boolean okay = (arglist.length == newlist.length);

    if (okay) {
        for (int i = 0; i < arglist.length; i++) {
            okay &= arglist[i].equals(newlist[i]);
        }
    }

    System.out.println();
    if (okay) {
        dumpargs("OK: ", arglist);
    } else {
        dumpargs("in: ", arglist);
        dumpargs("out:", newlist);
    }
}



private static void dumpargs(String label, String[] slist) {
    System.out.print(label + "  (" + slist.length + ")  ");
    for (int i = 0; i < slist.length; i++) {
        System.out.print(" <" + slist[i] + ">");
    }
    System.out.println();
}



    /**
     * Return the string that results by replacing the first occurence of 
     * string <code>pattern</code> with string <code>rep</code> in string 
     * <code>from</code>.
     * <P>
     * @param from      The target string.
     * @param pattern   What we're looking for.
     * @param rep       What we're replacing with.
     **/
   public static String stringReplace (String from, String pattern, String rep) {
     int startPos = from.indexOf(pattern);
     if (startPos >= 0) {
        int endPos   = startPos + pattern.length();
        StringBuffer buf = new StringBuffer(from);
        buf.replace(startPos, endPos, rep);
        String result = buf.toString();
        return result;
      } else 
        return from;
  }
 
    /**
     * Return the result of reading the entire conents of text file.
     * <P>
     * @param in      The file we're reading from.
     **/
   public static String readResult (java.io.BufferedReader in) throws Exception {
      String result = "";
      try {
         while (true) {
            String read = in.readLine();
            if (read == null) break;
            result = result + "\n" + read;
         };
      } catch (Exception e) {
         throw new Exception("readResult: Error reading.");
      } finally {
        in.close();
      };
      return result;
   }

    /**
     * Return the result of reading the entire conents of <code>stdout</code>.
     * <P>
     * @param proc      The process we're reading from.
     **/
   public static String readStdOut (java.lang.Process proc) throws Exception {
      java.io.InputStream is = proc.getInputStream();
      java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(is));
      String stdOutResult = readResult(in);
      return stdOutResult;
   }

    /**
     * Return the result of reading the entire contents of <code>stderr</code>.
     * <P>
     * @param proc      The process we're reading from.
     **/
   public static String readStdErr (java.lang.Process proc) throws Exception {
      java.io.InputStream is = proc.getErrorStream();
      java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(is));
      String stdErrResult = readResult(in);
      return stdErrResult;
   }

   public static final int RETURN_STDOUT = 0;
   public static final int RETURN_STDERR = 1;
   public static final int RETURN_STDOUT_STDERR = 2;
    /**
     * Execute a command, wait for termination, return stdout and/or
     * stderr depending on the value of <code>returnWhat</code>.
     * <P>
     * @param cmd         The command to be executed.
     * @param returnWhat  Return stdout and/or stderr.
     **/
   public static String execute (String cmd, int returnWhat) throws Exception {
      java.lang.Runtime runtime = java.lang.Runtime.getRuntime();
      java.lang.Process proc = runtime.exec(cmd);

      String stdOutResult = readStdOut(proc);
      String stdErrResult = readStdErr(proc);
      String result = "";
      if (returnWhat == RETURN_STDOUT)
         result = stdOutResult;
      else if (returnWhat == RETURN_STDERR)
         result = stdErrResult;
      else if (returnWhat == RETURN_STDOUT_STDERR)
         result = stdOutResult + stdErrResult;

      proc.waitFor();
      int exitValue = proc.exitValue();
      if (exitValue != 0)
         throw new Exception(result);
      return result;
    }

} // class Misc

