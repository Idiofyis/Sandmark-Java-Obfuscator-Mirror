package sandmark.obfuscate.interleavemethods;


/** The resulting method of an Interleave.interleave call.
    Interleaving methods A and B results in an InterleavedMethod
    C, which takes an extra byte as its last parameter. E.g.,
    calls to A should be changed from:
    A(...) -> C(...,C.getByteA())
    and the same for B.
*/

public class InterleavedMethod {
    
    byte byteA, byteB;
    sandmark.program.Method method;
    
    public InterleavedMethod(sandmark.program.Method method,
                             byte a, byte b){
        byteA = a;
        byteB = b;
        this.method = method;
    }                             
   
    public byte getByteA(){
        return byteA;
    }
    
    public byte getByteB(){
        return byteB;
    }

    public sandmark.program.Method getMethod(){
        return method;
    }
}
