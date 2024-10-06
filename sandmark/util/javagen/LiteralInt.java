package sandmark.util.javagen;

public class LiteralInt
    extends sandmark.util.javagen.Expression {
    int value;

    public LiteralInt(int value){
	this.value = value;
	this.type = "int";
    }

    public String toString(String indent)  {
	return java.lang.String.valueOf(value);
    }

    public void toByteCode(org.apache.bcel.generic.ClassGen cg,
			   org.apache.bcel.generic.MethodGen mg) {
	org.apache.bcel.generic.ConstantPoolGen cp =
	    cg.getConstantPool();
	org.apache.bcel.generic.InstructionList il =
	    mg.getInstructionList();

	org.apache.bcel.generic.PUSH push =
	    new org.apache.bcel.generic.PUSH(cp,value);
	il.append(push);
    }
    public void toCode(
		      sandmark.program.Class cg,
	          sandmark.program.Method mg) {

		org.apache.bcel.generic.ConstantPoolGen cp =
		    cg.getConstantPool();
		org.apache.bcel.generic.InstructionList il =
		    mg.getInstructionList();

		org.apache.bcel.generic.PUSH push =
		    new org.apache.bcel.generic.PUSH(cp,value);
		il.append(push);
    }
}



