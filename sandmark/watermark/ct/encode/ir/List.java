package sandmark.watermark.ct.encode.ir;

public class List extends sandmark.watermark.ct.encode.ir.IR {
    java.util.LinkedList list;

    public List() {
	list = new java.util.LinkedList();
    }

    public List(sandmark.watermark.ct.encode.ir.IR r) {
	list = new java.util.LinkedList();
        list.add(r);
    }

    public List(
        sandmark.watermark.ct.encode.ir.IR r1,
        sandmark.watermark.ct.encode.ir.IR r2) {
	list = new java.util.LinkedList();
        list.add(r1);
        list.add(r2);
    }

    public List(
        sandmark.watermark.ct.encode.ir.IR r1,
        sandmark.watermark.ct.encode.ir.IR r2,
        sandmark.watermark.ct.encode.ir.IR r3) {
	list = new java.util.LinkedList();
        list.add(r1);
        list.add(r2);
        list.add(r3);
    }

    public List(
        sandmark.watermark.ct.encode.ir.IR r1,
        sandmark.watermark.ct.encode.ir.IR r2,
        sandmark.watermark.ct.encode.ir.IR r3,
        sandmark.watermark.ct.encode.ir.IR r4) {
	list = new java.util.LinkedList();
        list.add(r1);
        list.add(r2);
        list.add(r3);
        list.add(r4);
    }

   public java.lang.Object clone() throws CloneNotSupportedException {
      List L = new List();
      java.util.Iterator iter = list.iterator();
      while (iter.hasNext()) {
         sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
         L.cons(f.copy());
      }
      return L;
   }

    public List cons (sandmark.watermark.ct.encode.ir.IR r1) {
       list.add(r1);
       return this;
    }

    public List snoc (sandmark.watermark.ct.encode.ir.IR r1) {
       list.addFirst(r1);
       return this;
    }

    public List cons (
       sandmark.watermark.ct.encode.ir.IR r1,
       sandmark.watermark.ct.encode.ir.IR r2) {
       list.add(r1);
       list.add(r2);
       return this;
    }

    public List cons (
       sandmark.watermark.ct.encode.ir.IR r1,
       sandmark.watermark.ct.encode.ir.IR r2,
       sandmark.watermark.ct.encode.ir.IR r3) {
       list.add(r1);
       list.add(r2);
       list.add(r3);
       return this;
    }

    public List cons (
       List l1) {
       list.addAll(l1.list);
       return this;
    }

    public List cons (
       List l1, List l2) {
       list.addAll(l1.list);
       list.addAll(l2.list);
       return this;
    }

    public List cons (
       List l1, List l2, List l3) {
       list.addAll(l1.list);
       list.addAll(l2.list);
       list.addAll(l3.list);
       return this;
    }

    public int size () {
       return list.size();
    }

    public java.util.Iterator iterator() {
	return list.listIterator();
    }
    
    class Backwards implements java.util.Iterator {
       java.util.ListIterator iter;  

       public Backwards() {
           iter = list.listIterator(list.size());  
       }

       public boolean hasNext() {
          return iter.hasPrevious();
       }

       public Object next() throws java.util.NoSuchElementException {
          return iter.previous();
       }

       public void remove() {
       }
    }

    public java.util.Iterator backwards() {
	return this.new Backwards();
    }
    
    public String toString() {
       String S = "";
       java.util.Iterator iter = iterator();
       while (iter.hasNext()) {
	   IR I = (IR) iter.next();
           S += I.toString() + "\n";
       }
       return S;
    }
}

