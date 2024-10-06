package sandmark.util.javagen;

public class List {
    java.util.LinkedList list;

    public List() {
	list = new java.util.LinkedList();
    }

    public List(sandmark.util.javagen.Java r) {
	list = new java.util.LinkedList();
        list.add(r);
    }

    public List(
        sandmark.util.javagen.Java r1,
        sandmark.util.javagen.Java r2) {
	list = new java.util.LinkedList();
        list.add(r1);
        list.add(r2);
    }

    public List(
        sandmark.util.javagen.Java r1,
        sandmark.util.javagen.Java r2,
        sandmark.util.javagen.Java r3) {
	list = new java.util.LinkedList();
        list.add(r1);
        list.add(r2);
        list.add(r3);
    }

    public List(
        sandmark.util.javagen.Java r1,
        sandmark.util.javagen.Java r2,
        sandmark.util.javagen.Java r3,
        sandmark.util.javagen.Java r4) {
	list = new java.util.LinkedList();
        list.add(r1);
        list.add(r2);
        list.add(r3);
        list.add(r4);
    }

    public List cons (sandmark.util.javagen.Java r1) {
       list.add(r1);
       return this;
    }

    public List cons (
       sandmark.util.javagen.Java r1,
       sandmark.util.javagen.Java r2) {
       list.add(r1);
       list.add(r2);
       return this;
    }

    public List cons (
       sandmark.util.javagen.Java r1,
       sandmark.util.javagen.Java r2,
       sandmark.util.javagen.Java r3) {
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

    public Java car () {
       return (Java) list.getFirst();
    }

    public int size () {
       return list.size();
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

    public java.util.Iterator iterator() {
	return list.listIterator();
    }
    
    public String toString(String indent) {
       String S = "";
       java.util.Iterator iter = iterator();
       while (iter.hasNext()) {
	   sandmark.util.javagen.Java I = (sandmark.util.javagen.Java) iter.next();
           S += indent + I.toString() + ";\n";
       }
       return S;
    }

    public String toString() {
       return toString("");
    }
}

