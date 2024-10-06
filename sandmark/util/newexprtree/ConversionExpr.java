package sandmark.util.newexprtree;

/** Represents the conversion of one type to another, 
 *  either BasicType-to-BasicType or ReferenceType-to-ReferenceType.
 *  ConversionExpr.getType() will return the convert-to type (the target type).
 *  Emits D2F, D2I, D2L, F2D, F2I, F2L, L2D, L2F, L2I, I2D, I2F,
 *        I2L, I2S, I2B, I2C, and CHECKCAST.
 */
public class ConversionExpr extends ValueExpr{
   private static org.apache.bcel.generic.Instruction[] OPS = {
      null,
      org.apache.bcel.generic.InstructionConstants.D2F,
      org.apache.bcel.generic.InstructionConstants.D2I,
      org.apache.bcel.generic.InstructionConstants.D2L,

      org.apache.bcel.generic.InstructionConstants.F2D,
      null,
      org.apache.bcel.generic.InstructionConstants.F2I,
      org.apache.bcel.generic.InstructionConstants.F2L,

      org.apache.bcel.generic.InstructionConstants.L2D,
      org.apache.bcel.generic.InstructionConstants.L2F,
      org.apache.bcel.generic.InstructionConstants.L2I,
      null,

      org.apache.bcel.generic.InstructionConstants.I2D,
      org.apache.bcel.generic.InstructionConstants.I2F,
      null,
      org.apache.bcel.generic.InstructionConstants.I2L,
      org.apache.bcel.generic.InstructionConstants.I2S,
      org.apache.bcel.generic.InstructionConstants.I2B,
      org.apache.bcel.generic.InstructionConstants.I2C
   };

   private ValueExpr from;
   private org.apache.bcel.generic.Type totype, fromtype;

   /** Makes a new ConversionExpr between fromtype and totype (both assumed to be BasicTypes)
    *  @param _from the ValueExpr that is being converted.
    *  @param _fromtype the type of _from (should be equal to _from.getType()).
    *  @param _totype the type to convert to
    */
   public ConversionExpr(ValueExpr _from, org.apache.bcel.generic.BasicType _fromtype, 
                         org.apache.bcel.generic.BasicType _totype){
      super(_totype);
      from = _from;
      totype = _totype;
      fromtype = _fromtype;
   }

   /** Makes a new ConversionExpr between fromtype and totype (both assumed to be ReferenceTypes)
    *  @param _from the ValueExpr that is being converted.
    *  @param _fromtype the type of _from (should be equal to _from.getType()). 
    *         (this value is mostly unnecessary and can easily be filled in with Type.OBJECT)
    *  @param _totype the type to convert to
    */
   public ConversionExpr(ValueExpr _from, org.apache.bcel.generic.ReferenceType _fromtype, 
                         org.apache.bcel.generic.ReferenceType _totype){
      super(_totype);
      from = _from;
      totype = _totype;
      fromtype = _fromtype;
   }
   
   /** Returns the type to convert TO.
    */
   public org.apache.bcel.generic.Type getToType(){
      return totype;
   }

   /** Returns the type to convert FROM.
    */
   public org.apache.bcel.generic.Type getFromType(){
      return fromtype;
   }

   /** The ValueExpr being converted.
    */
   public ValueExpr getConvertValue(){
      return from;
   }

   /** Sets the value being converted
    */
   public void setConvertValue(ValueExpr conv){
      from = conv;
   }

   public String toString(){
      return "ConversionExpr["+fromtype+"-->"+totype+","+from+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(from.emitBytecode(factory));

      if (fromtype instanceof org.apache.bcel.generic.BasicType){
         int fromoffset=0, tooffset=0;
         switch(fromtype.getType()){
         case T_DOUBLE: fromoffset=0; break;
         case T_FLOAT:  fromoffset=4; break;
         case T_LONG:   fromoffset=8; break;
         case T_INT:    fromoffset=12; break;
         }
         
         switch(totype.getType()){
         case T_DOUBLE: tooffset=0; break;
         case T_FLOAT:  tooffset=1; break;
         case T_INT:    tooffset=2; break;
         case T_LONG:   tooffset=3; break;
         case T_SHORT:  tooffset=4; break;
         case T_BYTE:   tooffset=5; break;
         case T_CHAR:   tooffset=6; break;
         }
         result.add(OPS[fromoffset+tooffset]);
      }else{
         // CHECKCAST
         result.add(factory.createCheckCast
                    ((org.apache.bcel.generic.ReferenceType)totype));
      }
      return result;
   }   
}
