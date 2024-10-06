package sandmark.util.outliner;

public class LiveVar {
    int slot;
    org.apache.bcel.generic.Type localtype;
    java.util.ArrayList defList;
    java.util.ArrayList useList;

    LiveVar(int index, org.apache.bcel.generic.Type type)
    {
        slot = index;
        localtype = type;
    }
        
    public void setDefList(java.util.ArrayList list)
    {
        defList = list;
    }
    /* returns the definition instruction handles */
    public java.util.ArrayList getDefList()
    {
        return defList;
    }

    public void setUseList(java.util.ArrayList list)
    {
        useList = list;
    }
    /* returns the use instruction handles */
    public java.util.ArrayList getUseList()
    {
        return useList;
    }

    public org.apache.bcel.generic.Type getType()
    {
        return localtype;
    }
    public int getSlot()
    {
        return slot;
    }

    boolean isObjectType()
    {
        if(localtype instanceof org.apache.bcel.generic.ReferenceType)
            return true;
        else
            return false;
    }
}
