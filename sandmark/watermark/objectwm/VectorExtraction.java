package sandmark.watermark.objectwm;

/*  This class implements the vector extraction APIs to extract the watermarkvector
 *  from the existing jar file 
 */

public class VectorExtraction
{
    Config config = null;
    ObjectHelper helper = null;

    private boolean DEBUG = false;
    
    /*  Constructor 
     */
    VectorExtraction()
    {
        config = new Config();
        helper = new ObjectHelper();
    }
 

    /*  Extracts the frequency vector from the @classFile 
     */
    public java.util.Vector extractVector(sandmark.program.Class classObj)
    {
        java.util.Vector codeList = new java.util.Vector(50, 10);
        java.util.Vector origVector = new java.util.Vector(10, 2);
     
        sandmark.program.Method[] methods = classObj.getMethods();
        if(methods==null)
            return null;
     
        int methodIndex = 0;
        while(methodIndex<methods.length){
            org.apache.bcel.generic.InstructionList instrList = methods[methodIndex++].getInstructionList();
            if(instrList==null)
                continue;
            org.apache.bcel.generic.InstructionHandle[] instrHandles =
                instrList.getInstructionHandles();
            if(instrHandles==null)
                continue;
      
            /* else lookup for instructions that matches with codeBook codes */
            
            for(int i=0; i< instrHandles.length; i++){
                org.apache.bcel.generic.InstructionHandle iHandle = instrHandles[i];
                org.apache.bcel.generic.Instruction instr = iHandle.getInstruction();
                String instrCode = instr.toString((classObj.getConstantPool()).getConstantPool());
                codeList.addElement( new String(instrCode));
            }
        }

        java.util.Vector argList = new java.util.Vector(50, 10);
        int codeId = 0;
     
        /* call codeBook to create vector from the VectorGrp */
        CodeBook CodeBk = new CodeBook();
	if(DEBUG) System.out.println(" codeList.size(): "+codeList.size());
        while(codeId<codeList.size()){
            for(int gid = 0; gid<CodeBk.numVectorGroups; gid++){
                int eqFlag = 1;
                for(int id=0; id<CodeBk.elemsVectorGrp[gid]; id++){
                    String codeEntry = CodeBk.vectorGrp[gid][id];
                    if( (codeId+CodeBk.elemsVectorGrp[gid])>codeList.size()){
                        eqFlag = 0;
                        break;
                    }
                    if(!((String)codeList.elementAt(codeId+id)).startsWith(codeEntry)){/* compareTo */
                        eqFlag = 0;
                        break;
                    }
                }
                if(eqFlag==1){ /* code found; add to vector */
                    CodeBk.codeVector[gid]++;
                    break;  /* increment once for the group. even for multiple set matches */
                }
            }
            codeId++;
        }
     
        int codeGrp = 0; 
        while(codeGrp<CodeBk.numVectorGroups){
            origVector.addElement(new Integer(CodeBk.codeVector[codeGrp]));
            codeGrp++;
        }
        return(origVector);
    }
 

    /* for test purpose ...
    public static void main( String args[] )
       throws java.io.IOException {
       extractVector(args[0], 10);
       int vIndex = 0;
       System.out.println("Extracted vector:\n");
       while( vIndex < origVector.size() ) {
          System.out.print( (Integer)origVector.elementAt(vIndex) );
          vIndex++;
       }
       System.out.println();
       return;
    }*/
}

