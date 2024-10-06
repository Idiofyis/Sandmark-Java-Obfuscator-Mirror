package sandmark.diff.methoddiff;

 public class Tuple extends java.util.LinkedList 
        implements Comparable{
        
        public Tuple(){        
        }

        public int compareTo(Object o){
            Tuple t = (Tuple)o;            
            if(t.size() < size())
                return -1;
            else if(t.size() > size())
                return 1;
            else {
                java.util.Collections.sort(this);
                java.util.Collections.sort(t);
                for(int i = 0; i < size(); i++){
                    Comparable c = (Comparable)get(i);
                    int ret = c.compareTo(t.get(i));
                    if( ret != 0)
                        return ret;
                }
            }
            return 0;
        }             
 }
