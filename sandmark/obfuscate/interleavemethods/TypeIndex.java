package sandmark.obfuscate.interleavemethods;

public class TypeIndex {
        
    public org.apache.bcel.generic.Type type;
    public int index;
        
    public TypeIndex(org.apache.bcel.generic.Type type,
                     int index){
        this.type = type;
        this.index = index;
    }

    public boolean equals(java.lang.Object o){            
        TypeIndex ti = (TypeIndex)o;            
        return ti.index == index && typesMatch(ti.type, type);
    }

    private boolean typesMatch(org.apache.bcel.generic.Type t1,
                               org.apache.bcel.generic.Type t2){
        if(t1 == t2) 
            return true;
        if(t1 instanceof org.apache.bcel.generic.ReferenceType &&
           t2 instanceof org.apache.bcel.generic.ReferenceType)
            return true;
        if((t1 == org.apache.bcel.generic.Type.BOOLEAN ||
            t1 == org.apache.bcel.generic.Type.INT ||
            t1 == org.apache.bcel.generic.Type.CHAR ||
            t1 == org.apache.bcel.generic.Type.BYTE) &&
           (t2 == org.apache.bcel.generic.Type.BOOLEAN ||
            t2 == org.apache.bcel.generic.Type.INT ||
            t2 == org.apache.bcel.generic.Type.CHAR ||
            t2 == org.apache.bcel.generic.Type.BYTE))
            return true;
        return false;                
    }    

    public int hashCode(){            
        return index;
    }

    public String toString(){
        return type + "/" + index;
    }
}
