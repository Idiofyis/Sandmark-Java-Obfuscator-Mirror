package sandmark.util.newexprtree;

/** Represents a dummy value of a reference type. This includes
 *  arrays, but arrays are more properly represented by ArrayDummyExpr.
 */
public class ObjectDummyExpr extends DummyExpr{
   /** Constructs a dummy Expr for the given ObjectType.
    */
   public ObjectDummyExpr(org.apache.bcel.generic.ReferenceType _type){
      super(_type);
   }

   /** Constructs a dummy Expr for an unknown Object type (i.e. Type.OBJECT).
    */
   public ObjectDummyExpr(){
      super(org.apache.bcel.generic.Type.OBJECT);
   }

   public String toString(){
      return "ObjectDummyExpr["+getType()+"]";
   }
}
