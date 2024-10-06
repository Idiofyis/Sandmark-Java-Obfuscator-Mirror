package sandmark.watermark.objectwm;

/*  This class implements the code profiler that collects the frequently
 *  occuring instruction groups from various applications;
 *  Required for constructing the codeBook 
 *
 *  USAGE : java -classpath .... Profiler <numJars> <profileLength> <printThreshold> <jarFiles...>
 */


public class Profiler
{
   private static final boolean DEBUG = false;
    java.util.Hashtable masterTable;
    int maxProfileLength;
    sandmark.program.Application myApp;

    Profiler(String jarInput, int length)
    {
        masterTable = new java.util.Hashtable();
        maxProfileLength = length;
	try {
            myApp = new sandmark.program.Application(jarInput);
	}catch(java.lang.Exception e){
	    throw new Error(" Exception caught @ Profiler.java ->"+e);
	}
    }

    private void merge(java.util.Hashtable childTable, java.util.Hashtable parentTable)
    {
        java.util.Enumeration childKeys = childTable.keys();
        while (childKeys.hasMoreElements())
        {
            String keyStr[] = (java.lang.String[])childKeys.nextElement();
            Integer childCount = (Integer)childTable.get(keyStr);
            java.lang.String[] key = containsKey(parentTable,keyStr);
            if(key != null){
                Integer parentCount = (Integer)parentTable.get(key);
                parentTable.put(key, new Integer(parentCount.intValue()+childCount.intValue()));
            }
            else{
                parentTable.put( keyStr, childCount );
            } 
        }
    }


    /* 
     *  Gets the bytecode usage in terms of profile length
     *  and prints the information
     */
    private void getProfiles(int profileLength)
    {
        java.util.Iterator itr = myApp.classes();

        while(itr.hasNext()){
            sandmark.program.Class classObj = (sandmark.program.Class)itr.next();
            String className = classObj.getName();
            if(DEBUG)
            System.out.println("className = "+className);
            sandmark.program.Method methods[] = classObj.getMethods();
            if(methods==null) 
                continue;

            java.util.Hashtable classTable = new java.util.Hashtable();
            for(int i=0; i<methods.length; i++){
                if(methods[i].getInstructionList()==null)
                    continue;

                org.apache.bcel.generic.InstructionHandle ihs[] =
                    (methods[i].getInstructionList()).getInstructionHandles();
                java.util.Hashtable methodTable = new java.util.Hashtable();
                if(DEBUG)
                System.out.println("Method = "+ methods[i].getName());
                for(int j=0; j<ihs.length-profileLength; j++){
                    String keyStr[] = new String[profileLength];

                    int offset=0, instrCnt=0;
                    while(instrCnt<profileLength){
                        keyStr[instrCnt++] = org.apache.bcel.Constants.OPCODE_NAMES
                                             [ihs[j + offset].getInstruction().getOpcode()];
                        offset++;
                    }
                    
                    if(methodTable.containsKey(keyStr)){
                        Integer count = (Integer) methodTable.get(keyStr);
                        methodTable.put(keyStr, new Integer(count.intValue()+1));
                    }
                    else
                        methodTable.put(keyStr, new Integer(1));
                }
                merge(methodTable,classTable);
            }
            merge(classTable,masterTable);
        }
    }


    public static void printHashTable(java.util.Hashtable hTable,
                                      String mesg,
                                      int profLength,
                                      int printThreshold)
    {
        System.out.println("\n\nHASHTABLE "+ mesg);

        java.util.Enumeration hKeys = hTable.keys();
        while(hKeys.hasMoreElements()){
            String keyStr[] = (java.lang.String[])hKeys.nextElement();
            if(keyStr.length != profLength)
                continue;
            
            Integer count = (Integer)hTable.get(keyStr);
            if(count.intValue()>=printThreshold){
                System.out.println("\n");
                for(int i=0; i<keyStr.length; i++)
                    System.out.println(keyStr[i]);
                System.out.println("                        " + count);
            }
        }
    }


    public static java.lang.String[] containsKey(java.util.Hashtable hTable, Object key)
    {
        String cmpStr[] = (java.lang.String[]) key;
        java.util.Enumeration myKeys = hTable.keys();
        while (myKeys.hasMoreElements()){
            String keyStr[] = (java.lang.String[])myKeys.nextElement();
            int eqFlag=1;
            if(cmpStr.length==keyStr.length){
                for(int i=0;i< keyStr.length; i++)
                    if(!keyStr[i].equals(cmpStr[i])){ 
                        eqFlag = 0;
                        break;
                    }
            }
            else
                eqFlag = 0;
            
            if (eqFlag == 1)
                return (keyStr);
        }
        return(null);
    }

   
    public static void getCommonTable(Profiler[] p, int prLength, int prThresh)
    {
        java.util.Enumeration initKeys = p[0].masterTable.keys();
        java.util.Hashtable commonTable = new java.util.Hashtable();

        while (initKeys.hasMoreElements()){
            String keyStr[] = (java.lang.String[])initKeys.nextElement();
            Integer initCount = (Integer) p[0].masterTable.get(keyStr);
            int count = initCount.intValue();
            if(count < prThresh) 
                continue;
            boolean common = true;
            
            for (int i=1; i<p.length; i++){
                java.lang.String[] key = containsKey(p[i].masterTable,keyStr);
                if(key == null){
                    common = false;
                    break;
                }
                int value = ((Integer)p[i].masterTable.get(key)).intValue();
                if(value < prThresh){
                    common = false;
                    break;
                }
                count = count + value;
            }
            if(common)
                commonTable.put(keyStr, new Integer(count));
        }
        printHashTable(commonTable, "common", prLength, prThresh);
    }

    public static void main(String args[])
    {
        int numJars = Integer.parseInt(args[0]);
        int prLength = Integer.parseInt(args[1]);
        int prThresh = Integer.parseInt(args[2]); 
        Profiler[] p = new Profiler[numJars];
        
        java.util.Hashtable commonTable = new java.util.Hashtable();
        for (int i=0; i<numJars; i++){
            String jarInput = args[i+3];
            p[i] = new Profiler(jarInput,prLength);
            p[i].getProfiles(p[i].maxProfileLength);
            Profiler.printHashTable(p[i].masterTable,"master",p[i].maxProfileLength,prThresh);
        }

        getCommonTable(p, prLength, prThresh);
    }
}

