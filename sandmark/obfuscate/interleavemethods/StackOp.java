package sandmark.obfuscate.interleavemethods;

class StackOp {
    
    public int consumes, produces;
        
    public StackOp(int consumes, int produces){
        this.consumes = consumes;
        this.produces = produces;
    }
}
