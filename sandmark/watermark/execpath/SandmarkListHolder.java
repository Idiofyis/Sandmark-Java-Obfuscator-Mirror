package sandmark.watermark.execpath;

class SMLinkedList {
	SMLinkedList next;
	String data;
	SMLinkedList(SMLinkedList l,String d) { 
	    if(l != null)
		l.next = this;
	    data = d; 
	}
}

public class SandmarkListHolder extends Thread{
   private static java.util.Hashtable thread2str = new java.util.Hashtable();
   public static Object mutex = new Object();
	public static SMLinkedList head,tail;
	public static int count;
   public static String filename;
   public static java.io.PrintWriter writer;


   public static void start(String init){
      synchronized(mutex){
         Integer thread = new Integer(System.identityHashCode(Thread.currentThread()));
         thread2str.put(thread, init);
      }
   }
   
   public static void finish(){
      synchronized(mutex){
         Integer thread = new Integer(System.identityHashCode(Thread.currentThread()));
         String current = (String)thread2str.get(thread);
         
         tail = new SMLinkedList(tail,current);
         if(head == null)
            head = tail;
         count++;
         if((count % 1000) == 0)
            new SandmarkListHolder().start();
      }
   }

   
   public void run() {
      if(filename == null)
         return;
      
      synchronized(mutex) {
         if(filename != null && writer == null)
            try {
               writer = new java.io.PrintWriter
                  (new java.io.FileOutputStream(filename));
            } catch(java.io.IOException e) {
               filename = null;
               return;
            }
      }
      
      SMLinkedList curList;
      synchronized(mutex) {
         curList = head;
         head = tail = null;
         
         for( ; curList != null ; curList = curList.next )
            writer.println(curList.data);
         
         writer.flush();
      }
   }
   

   public static void concat(String name, int i){
      synchronized(mutex){      
         Integer thread = new Integer(System.identityHashCode(Thread.currentThread()));
         String current = (String)thread2str.get(thread);
         if (current==null)
            current="";
         current += name + i;
         thread2str.put(thread, current);
      }
   }
   
   public static void concat(String name, long l){
      synchronized(mutex){
         Integer thread = new Integer(System.identityHashCode(Thread.currentThread()));
         String current = (String)thread2str.get(thread);
         if (current==null)
            current="";
         current += name + l;
         thread2str.put(thread, current);
      }
   }

   public static void concat(String name, float f){
      synchronized(mutex){
         Integer thread = new Integer(System.identityHashCode(Thread.currentThread()));
         String current = (String)thread2str.get(thread);
         if (current==null)
            current="";
         current += name + f;
         thread2str.put(thread, current);
      }
   }

   public static void concat(String name, double d){
      synchronized(mutex){
         Integer thread = new Integer(System.identityHashCode(Thread.currentThread()));
         String current = (String)thread2str.get(thread);
         if (current==null)
            current="";
         current += name + d;
         thread2str.put(thread, current);
      }
   }

   public static void concat(String name, Object obj){
      synchronized(mutex){
         Integer thread = new Integer(System.identityHashCode(Thread.currentThread()));
         String current = (String)thread2str.get(thread);
         if (current==null)
            current="";
         current += name + System.identityHashCode(obj);
         thread2str.put(thread, current);
      }
   }
}
