package sandmark.watermark.objectwm;

/*  This class implements the entire CodeBook features including its interface
 */

public class CodeBook
{
    public static int wmarkLength = 8;

    int numGroups;
    int numSets[] = new int[20];
    int numInInstr[][] = new int[20][20];
    int numOutInstr[][] = new int[20][20];
    int numEmbedInstr[] = new int[20];
    int numNullifyInstr[] = new int[20];
    String inInstr[][][] = new String[20][20][20];
    String outInstr[][][] = new String[20][20][20];
    String embedInstr[][] = new String[20][20];
    String nullifyInstr[][] = new String[20][20];

    int codeVector[] = new int[20];

    String vectorGrp[][] = new String[20][20];
    int numVectorGroups;
    int elemsVectorGrp[] = new int[20];

    int numDep[] = new int[10];
    int instrDep[][][] = new int[20][10][2];

    int nullifyEffect[] = new int[20];

    /*  Constructor
     */
    CodeBook()
    {
        /* start with a 8 bit watermark embedding  */
        numVectorGroups = 8;

        elemsVectorGrp[0] = 2;
        vectorGrp[0][0] = "bipush";
        vectorGrp[0][1] = "iload";

        elemsVectorGrp[1] = 2;
        vectorGrp[1][0] = "iconst"; 
        vectorGrp[1][1] = "if"; 

        elemsVectorGrp[2] = 2;
        vectorGrp[2][0] = "iload";
        vectorGrp[2][1] = "iadd";
 
        elemsVectorGrp[3] = 3;
        vectorGrp[3][0] = "iload";
        vectorGrp[3][1] = "iload";
        vectorGrp[3][2] = "if_icmplt";

        elemsVectorGrp[4] = 2;
        vectorGrp[4][0] = "iconst_m1";
        vectorGrp[4][1] = "isub";

        elemsVectorGrp[5] = 2;
        vectorGrp[5][0] = "bipush";
        vectorGrp[5][1] = "iadd";

        elemsVectorGrp[6] = 2;
        vectorGrp[6][0] = "idiv";
        vectorGrp[6][1] = "istore";

        elemsVectorGrp[7] = 2;
        vectorGrp[7][0] = "iload";
        vectorGrp[7][1] = "isub";

        nullifyEffect[0] = -1; nullifyEffect[1] =  1;
        nullifyEffect[2] = -1; nullifyEffect[3] =  3;
        nullifyEffect[4] = -1; nullifyEffect[5] =  4;
        nullifyEffect[6] = -1; nullifyEffect[7] = -1;

        /* start with a 8 group multiple instructions set CodeBook */
        numGroups = 8;
         
           numSets[0] = 12; // ... group0

        numInInstr[0][0] = 3;                  numOutInstr[0][0] = 3;
           inInstr[0][0][0] = "iload X";          outInstr[0][0][0] = "bipush Y";
           inInstr[0][0][1] = "bipush Y";         outInstr[0][0][1] = "iload X";
           inInstr[0][0][2] = "if_icmpne -> Z";   outInstr[0][0][2] = "if_icmpne -> Z";

        numInInstr[0][1] = 3;                  numOutInstr[0][1] = 3;
           inInstr[0][1][0] = "iload_X";          outInstr[0][1][0] = "bipush Y";
           inInstr[0][1][1] = "bipush Y";         outInstr[0][1][1] = "iload_X";
           inInstr[0][1][2] = "if_icmpne -> Z";   outInstr[0][1][2] = "if_icmpne -> Z";

        numInInstr[0][2] = 3;                  numOutInstr[0][2] = 3;
           inInstr[0][2][0] = "iload X";          outInstr[0][2][0] = "bipush Y";
           inInstr[0][2][1] = "bipush Y";         outInstr[0][2][1] = "iload X";
           inInstr[0][2][2] = "if_icmpeq -> Z";   outInstr[0][2][2] = "if_icmpeq -> Z";

        numInInstr[0][3] = 3;                  numOutInstr[0][3] = 3;
           inInstr[0][3][0] = "iload_X";          outInstr[0][3][0] = "bipush Y";
           inInstr[0][3][1] = "bipush Y";         outInstr[0][3][1] = "iload_X";
           inInstr[0][3][2] = "if_icmpeq -> Z";   outInstr[0][3][2] = "if_icmpeq -> Z";

        numInInstr[0][4] = 3;                  numOutInstr[0][4] = 3;
           inInstr[0][4][0] = "iload X";          outInstr[0][4][0] = "bipush Y";
           inInstr[0][4][1] = "bipush Y";         outInstr[0][4][1] = "iload X";
           inInstr[0][4][2] = "if_icmpgt -> Z";   outInstr[0][4][2] = "if_icmplt -> Z";

        numInInstr[0][5] = 3;                  numOutInstr[0][5] = 3;
           inInstr[0][5][0] = "iload_X";          outInstr[0][5][0] = "bipush Y";
           inInstr[0][5][1] = "bipush Y";         outInstr[0][5][1] = "iload_X";
           inInstr[0][5][2] = "if_icmpgt -> Z";   outInstr[0][5][2] = "if_icmplt -> Z";

        numInInstr[0][6] = 3;                  numOutInstr[0][6] = 3;
           inInstr[0][6][0] = "iload X";          outInstr[0][6][0] = "bipush Y";
           inInstr[0][6][1] = "bipush Y";         outInstr[0][6][1] = "iload X";
           inInstr[0][6][2] = "if_icmpge -> Z";   outInstr[0][6][2] = "if_icmple -> Z";

        numInInstr[0][7] = 3;                  numOutInstr[0][7] = 3;
           inInstr[0][7][0] = "iload_X";          outInstr[0][7][0] = "bipush Y";
           inInstr[0][7][1] = "bipush Y";         outInstr[0][7][1] = "iload_X";
           inInstr[0][7][2] = "if_icmpge -> Z";   outInstr[0][7][2] = "if_icmple -> Z";

        numInInstr[0][8] = 3;                  numOutInstr[0][8] = 3;
           inInstr[0][8][0] = "iload X";          outInstr[0][8][0] = "bipush Y";
           inInstr[0][8][1] = "bipush Y";         outInstr[0][8][1] = "iload X";
           inInstr[0][8][2] = "if_icmplt -> Z";   outInstr[0][8][2] = "if_icmpgt -> Z";

        numInInstr[0][9] = 3;                  numOutInstr[0][9] = 3;
           inInstr[0][9][0] = "iload_X";          outInstr[0][9][0] = "bipush Y";
           inInstr[0][9][1] = "bipush Y";         outInstr[0][9][1] = "iload_X";
           inInstr[0][9][2] = "if_icmplt -> Z";   outInstr[0][9][2] = "if_icmpgt -> Z";

        numInInstr[0][10] = 3;                 numOutInstr[0][10] = 3;
           inInstr[0][10][0] = "iload X";         outInstr[0][10][0] = "bipush Y";
           inInstr[0][10][1] = "bipush Y";        outInstr[0][10][1] = "iload X";
           inInstr[0][10][2] = "if_icmple -> Z";  outInstr[0][10][2] = "if_icmpge -> Z";

        numInInstr[0][11] = 3;                 numOutInstr[0][11] = 3;
           inInstr[0][11][0] = "iload_X";         outInstr[0][11][0] = "bipush Y";
           inInstr[0][11][1] = "bipush Y";        outInstr[0][11][1] = "iload_X";
           inInstr[0][11][2] = "if_icmple -> Z";  outInstr[0][11][2] = "if_icmpge -> Z";

     numEmbedInstr[0] = 6;                 numNullifyInstr[0] = 6;
        embedInstr[0][0] = "bipush X";            nullifyInstr[0][0] = "iinc Y 1";
        embedInstr[0][1] = "iload Y";             nullifyInstr[0][1] = "iload Y";
        embedInstr[0][2] = "iconst_1";            nullifyInstr[0][2] = "bipush X";
        embedInstr[0][3] = "iadd";                nullifyInstr[0][3] = "swap";
        embedInstr[0][4] = "isub";                nullifyInstr[0][4] = "isub";
        embedInstr[0][5] = "istore Y";            nullifyInstr[0][5] = "istore Y";


           numSets[1] = 6; // ... group1
        
        numInInstr[1][0] = 1;                  numOutInstr[1][0] = 2; 
           inInstr[1][0][0] = "ifeq -> X";        outInstr[1][0][0] = "iconst_0";
                                                  outInstr[1][0][1] = "if_icmpeq -> X";
        
        numInInstr[1][1] = 1;                  numOutInstr[1][1] = 2; 
           inInstr[1][1][0] = "ifne -> X";        outInstr[1][1][0] = "iconst_0";
                                                  outInstr[1][1][1] = "if_icmpne -> X";

        numInInstr[1][2] = 1;                  numOutInstr[1][2] = 2;
           inInstr[1][2][0] = "ifgt -> X";        outInstr[1][2][0] = "iconst_0";
                                                  outInstr[1][2][1] = "if_icmpgt -> X";
                                              
        numInInstr[1][3] = 1;                  numOutInstr[1][3] = 2;
           inInstr[1][3][0] = "ifge -> X";        outInstr[1][3][0] = "iconst_0";
                                                  outInstr[1][3][1] = "if_icmpge -> X";
                                                  
        numInInstr[1][4] = 1;                  numOutInstr[1][4] = 2; 
           inInstr[1][4][0] = "ifle -> X";        outInstr[1][4][0] = "iconst_0";
                                                  outInstr[1][4][1] = "if_icmple -> X";
                                                  
        numInInstr[1][5] = 1;                  numOutInstr[1][5] = 2; 
           inInstr[1][5][0] = "iflt -> X";        outInstr[1][5][0] = "iconst_0";
                                                  outInstr[1][5][1] = "if_icmplt -> X";
        
     numEmbedInstr[1] = 3;                 numNullifyInstr[1] = 3;
        embedInstr[1][0] = "iload X";             nullifyInstr[1][0] = "goto -> Z";
        embedInstr[1][1] = "iconst_0";            nullifyInstr[1][1] = "iload X";
        embedInstr[1][2] = "if_icmplt -> W";      nullifyInstr[1][2] = "iflt -> W";
        

           numSets[2] = 1; // ... group2

        numInInstr[2][0] = 3;                  numOutInstr[2][0] = 3;
           inInstr[2][0][0] = "iload X";          outInstr[2][0][0] = "getstatic Y Z";
           inInstr[2][0][1] = "getstatic Y Z";    outInstr[2][0][1] = "iload X";
           inInstr[2][0][2] = "iadd";             outInstr[2][0][2] = "iadd";

     numEmbedInstr[2] = 4;                 numNullifyInstr[2] = 4;
        embedInstr[2][0] = "iconst_m1";       nullifyInstr[2][0] = "iload X";
        embedInstr[2][1] = "iload X";         nullifyInstr[2][1] = "iconst_1";
        embedInstr[2][2] = "iadd";            nullifyInstr[2][2] = "iadd";
        embedInstr[2][3] = "istore X";        nullifyInstr[2][3] = "istore X";

        
           numSets[3] = 4; // ... group3

        numInInstr[3][0] = 3;                  numOutInstr[3][0] = 3;
           inInstr[3][0][0] = "iload X";          outInstr[3][0][0] = "iload Y";
           inInstr[3][0][1] = "iload Y";          outInstr[3][0][1] = "iload X";
           inInstr[3][0][2] = "if_icmpgt -> Z";   outInstr[3][0][2] = "if_icmplt -> Z";

        numInInstr[3][1] = 3;                  numOutInstr[3][1] = 3;
           inInstr[3][1][0] = "iload_X";          outInstr[3][1][0] = "iload Y";
           inInstr[3][1][1] = "iload Y";          outInstr[3][1][1] = "iload_X";
           inInstr[3][1][2] = "if_icmpgt -> Z";   outInstr[3][1][2] = "if_icmplt -> Z";

        numInInstr[3][2] = 3;                  numOutInstr[3][2] = 3;
           inInstr[3][2][0] = "iload X";          outInstr[3][2][0] = "iload_Y";
           inInstr[3][2][1] = "iload_Y";          outInstr[3][2][1] = "iload X";
           inInstr[3][2][2] = "if_icmpgt -> Z";   outInstr[3][2][2] = "if_icmplt -> Z";

        numInInstr[3][3] = 3;                  numOutInstr[3][3] = 3;
           inInstr[3][3][0] = "iload_X";          outInstr[3][3][0] = "iload_Y";
           inInstr[3][3][1] = "iload_Y";          outInstr[3][3][1] = "iload_X";
           inInstr[3][3][2] = "if_icmpgt -> Z";   outInstr[3][3][2] = "if_icmplt -> Z";

     numEmbedInstr[3] = 3;                 numNullifyInstr[3] = 4;
        embedInstr[3][0] = "iload X";             nullifyInstr[3][0] = "goto -> Z";
        embedInstr[3][1] = "iload Y";             nullifyInstr[3][1] = "iload Y";
        embedInstr[3][2] = "if_icmplt -> Z";      nullifyInstr[3][2] = "iload X";
                                                  nullifyInstr[3][3] = "if_icmpgt -> W";

           numSets[4] = 2; // ... group4
        
        numInInstr[4][0] = 3;                  numOutInstr[4][0] = 3; 
           inInstr[4][0][0] = "iload X";          outInstr[4][0][0] = "iload X";
           inInstr[4][0][1] = "iconst_1";         outInstr[4][0][1] = "iconst_m1";
           inInstr[4][0][2] = "iadd";             outInstr[4][0][2] = "isub";

        numInInstr[4][1] = 3;                  numOutInstr[4][1] = 3;
           inInstr[4][1][0] = "iload_X";          outInstr[4][1][0] = "iload_X";
           inInstr[4][1][1] = "iconst_1";         outInstr[4][1][1] = "iconst_m1";
           inInstr[4][1][2] = "iadd";             outInstr[4][1][2] = "isub";

     numEmbedInstr[4] = 4;                 numNullifyInstr[4] = 4;
        embedInstr[4][0] = "iload X";         nullifyInstr[4][0] = "iload X";
        embedInstr[4][1] = "iconst_m1";       nullifyInstr[4][1] = "iconst_1";
        embedInstr[4][2] = "isub";            nullifyInstr[4][2] = "isub";
        embedInstr[4][3] = "istore X";        nullifyInstr[4][3] = "istore X";

           numSets[5] = 1; // ... group5

        numInInstr[5][0] = 1;                  numOutInstr[5][0] = 4;
           inInstr[5][0][0] = "iinc X Y";         outInstr[5][0][0] = "iload X";
                                                  outInstr[5][0][1] = "bipush Y";
                                                  outInstr[5][0][2] = "iadd";
                                                  outInstr[5][0][3] = "istore X";

     numEmbedInstr[5] = 4;                 numNullifyInstr[5] = 4;
        embedInstr[5][0] = "iload X";         nullifyInstr[5][0] = "iload X";
        embedInstr[5][1] = "bipush -1";       nullifyInstr[5][1] = "iconst_1";
        embedInstr[5][2] = "iadd";            nullifyInstr[5][2] = "iadd";
        embedInstr[5][3] = "istore X";        nullifyInstr[5][3] = "istore X"; 

         numSets[6] = 4; // ... group6

        numInInstr[6][0] = 2;                  numOutInstr[6][0] = 4;
           inInstr[6][0][0] = "iconst_0";         outInstr[6][0][0] = "iconst_0";
           inInstr[6][0][1] = "istore X";         outInstr[6][0][1] = "bipush 10";
                                                  outInstr[6][0][2] = "idiv";
                                                  outInstr[6][0][3] = "istore X";
                                                   
        numInInstr[6][1] = 2;                  numOutInstr[6][1] = 4;
           inInstr[6][1][0] = "iconst_0";         outInstr[6][1][0] = "iconst_0";
           inInstr[6][1][1] = "istore_X";         outInstr[6][1][1] = "bipush 10";
                                                  outInstr[6][1][2] = "idiv";
                                                  outInstr[6][1][3] = "istore_X";

        numInInstr[6][2] = 2;                  numOutInstr[6][2] = 4;
           inInstr[6][2][0] = "iconst_1";         outInstr[6][2][0] = "iconst_1";
           inInstr[6][2][1] = "istore X";         outInstr[6][2][1] = "bipush 1";
                                                  outInstr[6][2][2] = "idiv";
                                                  outInstr[6][2][3] = "istore X";
                                                   
        numInInstr[6][3] = 2;                  numOutInstr[6][3] = 4;
           inInstr[6][3][0] = "iconst_1";         outInstr[6][3][0] = "iconst_1";
           inInstr[6][3][1] = "istore_X";         outInstr[6][3][1] = "bipush 1";
                                                  outInstr[6][3][2] = "idiv";
                                                  outInstr[6][3][3] = "istore_X";
                                                  
     numEmbedInstr[6] = 4;                 numNullifyInstr[6] = 0;
        embedInstr[6][0] = "iload X";        
        embedInstr[6][1] = "iconst_1";
        embedInstr[6][2] = "idiv";
        embedInstr[6][3] = "istore X";
                                           
         numSets[7] = 8; // ... group7

        numInInstr[7][0] = 3;                  numOutInstr[7][0] = 4;
           inInstr[7][0][0] = "iload X";          outInstr[7][0][0] = "iload X";
           inInstr[7][0][1] = "iload Y";          outInstr[7][0][1] = "iload Y";
           inInstr[7][0][2] = "if_icmpeq -> Z";   outInstr[7][0][2] = "isub";
                                                  outInstr[7][0][3] = "ifeq -> Z";

        numInInstr[7][1] = 3;                  numOutInstr[7][1] = 4;
           inInstr[7][1][0] = "iload_X";          outInstr[7][1][0] = "iload_X";
           inInstr[7][1][1] = "iload Y";          outInstr[7][1][1] = "iload Y";
           inInstr[7][1][2] = "if_icmpeq -> Z";   outInstr[7][1][2] = "isub";
                                                  outInstr[7][1][3] = "ifeq -> Z";

        numInInstr[7][2] = 3;                  numOutInstr[7][2] = 4;
           inInstr[7][2][0] = "iload X";          outInstr[7][2][0] = "iload X";
           inInstr[7][2][1] = "iload_Y";          outInstr[7][2][1] = "iload_Y";
           inInstr[7][2][2] = "if_icmpeq -> Z";   outInstr[7][2][2] = "isub";
                                                  outInstr[7][2][3] = "ifeq -> Z";

        numInInstr[7][3] = 3;                  numOutInstr[7][3] = 4;
           inInstr[7][3][0] = "iload_X";          outInstr[7][3][0] = "iload_X";
           inInstr[7][3][1] = "iload_Y";          outInstr[7][3][1] = "iload_Y";
           inInstr[7][3][2] = "if_icmpeq -> Z";   outInstr[7][3][2] = "isub";
                                                  outInstr[7][3][3] = "ifeq -> Z";

        numInInstr[7][4] = 3;                  numOutInstr[7][4] = 4;
           inInstr[7][4][0] = "iload X";          outInstr[7][4][0] = "iload X";
           inInstr[7][4][1] = "iload Y";          outInstr[7][4][1] = "iload Y";
           inInstr[7][4][2] = "if_icmpne -> Z";   outInstr[7][4][2] = "isub";
                                                  outInstr[7][4][3] = "ifne -> Z";

        numInInstr[7][5] = 3;                  numOutInstr[7][5] = 4;
           inInstr[7][5][0] = "iload_X";          outInstr[7][5][0] = "iload_X";
           inInstr[7][5][1] = "iload Y";          outInstr[7][5][1] = "iload Y";
           inInstr[7][5][2] = "if_icmpne -> Z";   outInstr[7][5][2] = "isub";
                                                  outInstr[7][5][3] = "ifne -> Z";

        numInInstr[7][6] = 3;                  numOutInstr[7][6] = 4;
           inInstr[7][6][0] = "iload X";          outInstr[7][6][0] = "iload X";
           inInstr[7][6][1] = "iload_Y";          outInstr[7][6][1] = "iload_Y";
           inInstr[7][6][2] = "if_icmpne -> Z";   outInstr[7][6][2] = "isub";
                                                  outInstr[7][6][3] = "ifne -> Z";

        numInInstr[7][7] = 3;                  numOutInstr[7][7] = 4;
           inInstr[7][7][0] = "iload_X";          outInstr[7][7][0] = "iload_X";
           inInstr[7][7][1] = "iload_Y";          outInstr[7][7][1] = "iload_Y";
           inInstr[7][7][2] = "if_icmpne -> Z";   outInstr[7][7][2] = "isub";
                                                  outInstr[7][7][3] = "ifne -> Z";

     numEmbedInstr[7] = 4;                 numNullifyInstr[7] = 5;
           embedInstr[7][0] = "iload Y";           nullifyInstr[7][0] = "iload X";
           embedInstr[7][1] = "iload X";           nullifyInstr[7][1] = "iload Y";
           embedInstr[7][2] = "isub";              nullifyInstr[7][2] = "swap";
           embedInstr[7][3] = "istore Y";          nullifyInstr[7][3] = "iadd";
                                                   nullifyInstr[7][4] = "istore Y";
    }


    /*  Returns 'true' if its a branch nullify instruction group;
     *  else returns 'false' 
     */
    public boolean isBranchEmbed(int vectorIndex)
    {
        if((vectorIndex==1)||(vectorIndex==3))
            return true;

        return false;
    }


    /*  This method implements ... 
     */
    private String[][] getParams(String iLSet[],int numInstr)
    {
        String params[][] = new String[20][2];
        /* Store parameters if any */
        for(int i=0; i<numInstr; i++){
            int cmdIndex1 = iLSet[i].indexOf(" ");
            if(cmdIndex1==-1){
                cmdIndex1 = iLSet[i].indexOf("_");
                if(cmdIndex1==-1)
                    cmdIndex1 = iLSet[i].length()-1;
            }
            String param = iLSet[i].substring(cmdIndex1+1,iLSet[i].length());
  
            if(!param.equals("")){
                int cmdIndex2 = param.indexOf(" ");
                if(cmdIndex2 == -1){
                    cmdIndex2 = param.length();
                    params[i][0] = param.substring(0,cmdIndex2);
                }
                else{
                    params[i][0] = param.substring(0,cmdIndex2);
                    params[i][1] = param.substring(cmdIndex2+1);
                }
            }
        }
        return(params);
    }


    /*  This method implements ...
     */
    private String[] putParams(String iSet[], int numInstr, String[][] newParams)
    {
        String newInstrSet[] = new String[20];

        /* Store parameters if any */
        for (int i=0; i<numInstr; i++){
            int cmdIndex1 = iSet[i].indexOf(" ");
            if(cmdIndex1==-1){
                cmdIndex1 = iSet[i].indexOf("_");
                if (cmdIndex1 == -1)
                    cmdIndex1 = iSet[i].length()-1;
            }
 
            String param = iSet[i].substring(cmdIndex1+1, iSet[i].length());
            String newInstr = iSet[i].substring(0, cmdIndex1+1);
 
            if(!param.equals("")){ 
                int cmdIndex2 = param.indexOf(" ");
 
                if(cmdIndex2==-1){ /* 1 operand */
                    cmdIndex2 = param.length();
                    newInstr = newInstr + newParams[i][0];
               }
               else  /* for 2 operands */
                    newInstr = newInstr + newParams[i][0] + " " + newParams[i][1];
            }
            newInstrSet[i] = newInstr;
        }
        return(newInstrSet);
    }


    /*  Main interface to the codeBook 
     */
    public int getInstructionFromCodeBook(String[] instrSet, int instrSetLength,
                                          int groupNum, int setNum, String resultSet[])
    {
        /* get the actual values from the input instr set */
        String actuals[][] = getParams(instrSet, instrSetLength);

        /* get the groupNum of the instruction group corresponding to the given vectorGrp */
        String dummies[][] = getParams(inInstr[groupNum][setNum], numInInstr[groupNum][setNum]);

        /* fill the result set */
        for (int i=0;i<numOutInstr[groupNum][setNum];i++)
            resultSet [i] = outInstr[groupNum][setNum][i];

        int numResInstr = numOutInstr[groupNum][setNum];
        String[][] newDummies=getParams(resultSet, numResInstr);

        for(int i=0; i<numResInstr; i++)
            for(int j=0; j<2; j++){
                int done = 0;
                if (newDummies[i][j] != null)
                    for (int k=0; k<numInInstr[groupNum][setNum]; k++){
                        for (int m=0; m<2; m++)
                            if (newDummies[i][j].equals(dummies[k][m])){
                                newDummies[i][j] = actuals[k][m];
                            done = 1;
                        }
                        if (done == 1)
                            break;
                    }
            }

        String tempSet[] = putParams(resultSet, numResInstr, newDummies);
        for(int j=0; j< numResInstr; j++)
            resultSet[j] = tempSet[j];

        return(numResInstr); 
    }
}

