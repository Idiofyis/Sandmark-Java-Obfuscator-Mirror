package sandmark.util.exec;

public class HeapData {
    public String name;
    public long uniqueID;
    public String type;
    public long[] refs;
    public long timestamp;

   public static final long NULL = -1;

    public HeapData (
       String name,
       long uniqueID,
       String type,
       long[] refs,
       long timestamp) {
       this.name = name;
       this.uniqueID = uniqueID;
       this.type = type;
       this.refs = refs;
       this.timestamp = timestamp;
    }

   public String toString() { 
      String S = "HeapData[";
      S += "name=" + name;
      S += "; ID=" + uniqueID;
      S += "; type=" + type;
      S += "; timestamp=" + timestamp;
      S += "; refs[";
      for(int i=0; i<refs.length; i++)
         S += refs[i] + " ";
      S += "]]";
      return S;
   }

}

