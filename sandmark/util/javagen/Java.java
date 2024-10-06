package sandmark.util.javagen;

public class Java {
   String comment = "";

   public void setComment(String c) {
      comment = c;
   }

protected String renderListTerminate (
   sandmark.util.javagen.List L, String terminator, String indent)  {
   String P = "";
   java.util.Iterator iter = L.iterator();
   while (iter.hasNext()) {
      Java s = (Java) iter.next();
      P += s.toString(indent) + terminator;
   };
   return P;
}

protected String renderListTerminate (
   String L[], String terminator, String indent)  {
   String P = "";
   for(int i=0; i<L.length; i++) {
      P += indent + L[i] + terminator;
   };
   return P;
}

protected String renderListSeparate (
   sandmark.util.javagen.List L, String separator, String indent)  {
   String P = "";
   java.util.Iterator iter = L.iterator();
   while (iter.hasNext()) {
      Java s = (Java) iter.next();
      P += s.toString(indent);
      if (iter.hasNext())
	  P += separator;
   };
   return P;
}

protected String renderStat(sandmark.util.javagen.Java stat, String indent) {
   indent += "   ";
   String P = "";
   String C = stat.comment;
   stat.comment = "";
   String R = stat.toString(indent);
   if (!(stat instanceof sandmark.util.javagen.Comment)) 
      R  += ";";
   stat.comment = C;
   P += inlineComment(R, stat) + "\n";
   return P;
}

protected String renderStats(sandmark.util.javagen.List stats, String indent) {
   String P = "";
   java.util.Iterator iter = stats.iterator();
   while (iter.hasNext()) {
      Java stat = (Java) iter.next();
      P += renderStat(stat, indent);
   };
   return P;
}

protected String renderBlock(sandmark.util.javagen.List stats, String indent) {
   //String P = "";
   //   if (stats.size() == 1)
   //       P = "\n" +  renderStat(stats.car(), indent);
   //   else
   String    P = "{\n"  +  renderStats(stats, indent) + indent +  "}";
   return P;
}

protected String inlineComment(String P, sandmark.util.javagen.Java prog)  {
   int length = 50 - P.length();
   if (length < 0)
     length = 1;
   String B = "";
   for (int i=0; i<length; i++)
      B += " ";
   if (prog.comment.length() > 0)
      P += B + "// " +  prog.comment;
   return P;
}

protected String commentText(String C, String indent)  {
   String P = indent + "// ";
   for(int i=0; i<C.length(); i++) {
      char c = C.charAt(i);
      if ((c == '\n') & (i < (C.length()+1)))
         P  +=  "\n" + indent + "// ";
      else
         P += c;
   }
   return P;
}

protected String outlineComment()  {
   String P = "";
   if (!comment.equals("")) {
      P = commentText(comment, "");
      comment = "";
   }
   return P + "\n";
}

   public String toString(String indent)  {
      return "";
   }

   public String toString()  {
      return toString("");
   }

  //-----------------------------------------------------------------------------
   public static int accessFlagsToByteCode (String[] flags) {
     int f = 0;
     for(int i=0; i<flags.length; i++)
        if (flags[i].equals("public"))
           f |= org.apache.bcel.Constants.ACC_PUBLIC;
        else if (flags[i].equals("static"))
           f |= org.apache.bcel.Constants.ACC_STATIC;
        else if (flags[i].equals("super"))
           f |= org.apache.bcel.Constants.ACC_SUPER;
     return f;
   }

  public static org.apache.bcel.generic.Type typeToByteCode(String type) {
     if (type.equals("void")) 
        return org.apache.bcel.generic.Type.VOID;
     else {
        String S = org.apache.bcel.classfile.Utility.getSignature(type);
        return org.apache.bcel.generic.Type.getType(S);
     }
  }
}



