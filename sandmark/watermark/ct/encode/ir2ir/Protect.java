package sandmark.watermark.ct.encode.ir2ir;

public class Protect extends Transformer {

    String[] protectionMethods;

    public Protect (
      sandmark.watermark.ct.encode.ir.Build p, 
      sandmark.util.ConfigProperties props) {
      super(p,props);
      String protection = props.getProperty("Protection Method");
      protectionMethods = protectionKinds(protection);
    }

   static String[] protectionKinds(String protectionMethods) {
     java.util.StringTokenizer S = new java.util.StringTokenizer(protectionMethods,":");
     int C = S.countTokens();
     String[] res = new String[C];
     for(int i=0; i<C; i++)
        res[i] = S.nextToken();
     return res;
   }

   String pickRandomProtection() {
      return protectionMethods[sandmark.util.Random.getRandom().nextInt(protectionMethods.length)];
   }

sandmark.watermark.ct.encode.ir.List findUnsafe(sandmark.watermark.ct.encode.ir.List p) {
   java.util.HashSet safe = new java.util.HashSet();
   java.util.Iterator iter = p.iterator();
   while (iter.hasNext()) {
       sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
       if (f instanceof sandmark.watermark.ct.encode.ir.CreateNode) {
          sandmark.watermark.ct.encode.ir.CreateNode c = (sandmark.watermark.ct.encode.ir.CreateNode) f;
          safe.add(c.node);
       } else if (f instanceof sandmark.watermark.ct.encode.ir.AddEdge) {
         sandmark.watermark.ct.encode.ir.AddEdge a = (sandmark.watermark.ct.encode.ir.AddEdge) f;
         java.lang.Object source = a.edge.sourceNode();
         if (safe.contains(source))
            a.protection = "OK";
         else {
            a.protection = pickRandomProtection();
         }
       } else if (f instanceof sandmark.watermark.ct.encode.ir.FollowLink) {
         sandmark.watermark.ct.encode.ir.FollowLink l = (sandmark.watermark.ct.encode.ir.FollowLink) f;
         java.lang.Object source = l.edge.sourceNode();
         if (safe.contains(source))
            l.protection = "OK";
         else {
            l.protection = pickRandomProtection();
            if (l.protection.equals("safe"))
               safe.add(l.node);
         }
      }
   }
   return p;
}

sandmark.watermark.ct.encode.ir.List protectRegion(sandmark.watermark.ct.encode.ir.List p) {
   sandmark.watermark.ct.encode.ir.List tryBlock = new sandmark.watermark.ct.encode.ir.List();
   sandmark.watermark.ct.encode.ir.List body = new sandmark.watermark.ct.encode.ir.List();
   boolean inTryBlock = false;
   java.util.Iterator iter = p.iterator();
   while (iter.hasNext()) {
       sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
       if (f instanceof sandmark.watermark.ct.encode.ir.AddEdge) {
          sandmark.watermark.ct.encode.ir.AddEdge a = (sandmark.watermark.ct.encode.ir.AddEdge) f;
          if (a.protection.equals("try"))
             inTryBlock = true;
       } else if (f instanceof sandmark.watermark.ct.encode.ir.FollowLink) {
          sandmark.watermark.ct.encode.ir.FollowLink l = (sandmark.watermark.ct.encode.ir.FollowLink) f;
          if (l.protection.equals("try"))
             inTryBlock = true;
       }
       if (inTryBlock) {
          tryBlock.cons(f);
          if (f instanceof sandmark.watermark.ct.encode.ir.AddEdge) {
             sandmark.watermark.ct.encode.ir.AddEdge a = (sandmark.watermark.ct.encode.ir.AddEdge) f;
             if (!a.protection.equals("OK"))
                a.protection = "protected";
           } else if (f instanceof sandmark.watermark.ct.encode.ir.FollowLink) {
             sandmark.watermark.ct.encode.ir.FollowLink l = (sandmark.watermark.ct.encode.ir.FollowLink) f;
             if (!l.protection.equals("OK"))
                l.protection = "protected";
           }
       } else
          body.cons(f);
   }
   if (tryBlock.size() > 0)
     body.cons(new sandmark.watermark.ct.encode.ir.ProtectRegion(tryBlock));

   return body;
}


sandmark.watermark.ct.encode.ir.List addProtection(
   sandmark.watermark.ct.encode.ir.List methods) {
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator iter = methods.iterator();
    while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.Method f = (sandmark.watermark.ct.encode.ir.Method) iter.next();
      f.ops = findUnsafe(f.ops);
      f.ops = protectRegion(f.ops);
      P.cons(f);
   };
   return P;
}

public sandmark.watermark.ct.encode.ir.Build mutate() {
   orig.creators = addProtection(orig.creators);
   orig.fixups = addProtection(orig.fixups);
   orig.destructors = addProtection(orig.destructors);
   return orig;
}

   public static void main (String[] args) {

     System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
     System.out.println("++++++++++++++++++++++++++ ir.Protect +++++++++++++++++++++++");
     System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
   }
}

