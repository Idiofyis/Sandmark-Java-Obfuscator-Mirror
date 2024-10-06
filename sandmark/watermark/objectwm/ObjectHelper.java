package sandmark.watermark.objectwm;

/* This class contains the support methods required by the "Insertion" and 
 * other "Util" classes 
 */

public class ObjectHelper
{
   private static boolean DEBUG = false;
    /*  Constructor 
     */
    ObjectHelper()
    {}
 
    /*  Returns a random value within specified boundary;
     *  taking values from low to high-1 
     */
    
    public int getRandomValue(int low, int high)
    {
        if(high==low)
            return high;
        java.util.Random rNum = sandmark.util.Random.getRandom();
        Integer iVal = new Integer(rNum.nextInt()%(high-low));
        int rVal = iVal.intValue();
        if(rVal<0)
            rVal = 0-rVal;
        return(rVal+low);
    }
    
    /*  Compares two instruction opcode in string format
     */
    public boolean codeMatch(String str1[], String str2[], int numInstr)
    {
        int id=0;
        while(id<numInstr)
            if(!str1[id].startsWith(str2[id++]))
                return false;
        return true;
    }
    
    /*  Displays watermark vector information 
     */
    public static void display_VectorInfo(java.util.Vector vec, String mesg)
    {
        System.out.println("\n\n DISPLAYING " + mesg + " VECTOR ");
        System.out.println(" ________________________________ ");
 
        for(int v=0; v<vec.size(); v++)
            System.out.print(" " +  ((Integer)vec.elementAt(v)).intValue() + " " );
        System.out.println("\n");
        return;
    }
 
    /*  Extracts the short className from the full classFile name
     */
    public String extractShortFileName(String classFile)
    {
        int id = classFile.lastIndexOf('.');
        String substr = classFile.substring(0, id);
        int id2 = substr.lastIndexOf('.');
        return classFile.substring(id2+1);
    }
 
    /*  Extract the opcode from a verbose instruction 
     */
    public String getOpcodeFromInstr(String instrCode)
    {
        int cmdIndex = instrCode.indexOf('[');
        if(cmdIndex==-1)
           if(DEBUG)
            System.out.println(" Error in reading verbose Instruction ");
        return( instrCode.substring(0, cmdIndex) );
    }
 
    /*  Extracts the opcode from the instruction 
     */
    public String getOpcode(String instrCode)
    {
        int cmdIndex = instrCode.indexOf(' ');
        if(cmdIndex==-1){
            int idx = instrCode.indexOf('_');
            if(idx==-1)
                cmdIndex = instrCode.length();
            else{
                char nextCh = instrCode.charAt(idx+1);
                if( nextCh>='0' && nextCh<='9')
                    cmdIndex = idx;
                else
                    cmdIndex = instrCode.length();
            }
        }
        return( instrCode.substring(0, cmdIndex) );
    } 
 
  
    /*  Returns true if the String is a branch statement, else returns false
     */
    public boolean isOfTypeBranch(String opcode)
    {
        if(opcode.startsWith("if_icmple")||
           opcode.startsWith("if_icmpge")||
           opcode.startsWith("ifgt")||
           opcode.startsWith("iflt")||
           opcode.startsWith("if_icmpgt")||
           opcode.startsWith("if_icmplt")||
           opcode.startsWith("ifne")||
           opcode.startsWith("ifeq")||
           opcode.startsWith("if_icmpne")||
           opcode.startsWith("if_icmpeq")||
           opcode.startsWith("goto")){
            return true;
        }
        return false;
    }

    /*  Overloaded from previous method 
     */
    public boolean isOfTypeBranch(org.apache.bcel.generic.Instruction instr)
    {
        if(instr instanceof org.apache.bcel.generic.IF_ICMPGT ||
           instr instanceof org.apache.bcel.generic.IF_ICMPLT ||
           instr instanceof org.apache.bcel.generic.IF_ICMPGE ||
           instr instanceof org.apache.bcel.generic.IF_ICMPLE ||
           instr instanceof org.apache.bcel.generic.IF_ICMPEQ ||
           instr instanceof org.apache.bcel.generic.IF_ICMPNE ||
           instr instanceof org.apache.bcel.generic.IFGT ||
           instr instanceof org.apache.bcel.generic.IFLT ||
           instr instanceof org.apache.bcel.generic.IFGE ||
           instr instanceof org.apache.bcel.generic.IFLE ||
           instr instanceof org.apache.bcel.generic.IFEQ ||
           instr instanceof org.apache.bcel.generic.IFNE ||
           instr instanceof org.apache.bcel.generic.GOTO)
            return true;
        
        return false;
    }
 
    /* Returns 'true' if the @instr is an access tyoe instruction, else 
     * returns 'false'
     */
    private boolean isOfTypeAccess(String instr)
    {
        String str = instr.substring(1);
        if(str.startsWith("load") ||
           str.startsWith("store") ||
           str.startsWith("inc"))
            return true;
        else
            return false;
    }
 
    /* Extracts the argument val in the instruction 
     */
    public int getArgumentValInInstruction(String instr)
    {
        if(!this.isOfTypeAccess(instr)) 
            return -1;
  
        String substr = null;
        int index1 = instr.indexOf('_');
        int index2 = -1;
        
        if(index1!=-1)
            index2 = instr.indexOf('[');
        else{
            index1 = instr.indexOf(' ');
            if(index1==-1)
                System.out.println(" error in reading instruction ... check code ");
            index2 = instr.lastIndexOf(' ');
            if(index2==index1) 
                index2 = instr.length();
        }
        String localchar = instr.substring(index1+1, index2);
        return (new Integer(localchar)).intValue();
    }
}

