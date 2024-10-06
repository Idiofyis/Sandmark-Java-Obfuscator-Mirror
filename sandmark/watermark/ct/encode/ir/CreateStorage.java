package sandmark.watermark.ct.encode.ir;

public class CreateStorage extends Method {
   sandmark.watermark.ct.encode.storage.StorageClass storageClass;
   sandmark.util.ConfigProperties props;

   public CreateStorage(
      sandmark.util.newgraph.MutableGraph graph,
      sandmark.watermark.ct.encode.storage.StorageClass storageClass,
      sandmark.util.ConfigProperties props){
       this.graph = graph;
       this.storageClass = storageClass;
       this.props = props;
   }

   public String name() {
      return "CreateStorage_" + storageClass.variableName(props);
   }

   public String type() {
      return storageClass.typeName(props);
   }

   public String varName() {
      return storageClass.variableName(props);
   }

   public boolean isGlobal() {
      return storageClass.getStoreLocation() == sandmark.watermark.ct.encode.storage.StorageClass.GLOBAL;
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
       return new CreateStorage(graph, storageClass, props);
   }

   public String toString(String indent) {
      return indent +  "CreateStorage(" + name() + ")";
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      String[] attributes = {"public","static"};
      String nodeType = props.getProperty("Node Class");
      String var = storageClass.variableName(props);

      sandmark.util.javagen.List args = new sandmark.util.javagen.List();

      sandmark.util.javagen.List body = new sandmark.util.javagen.List();

      sandmark.util.javagen.Statement create = storageClass.toJavaCreate(graph, props);

      body.cons(create);

      if (isGlobal()) {
         sandmark.util.javagen.AssignStatic assign =
             new sandmark.util.javagen.AssignStatic(
                nodeType,
                var,
                type(),
                new sandmark.util.javagen.LocalRef(var, type())
	    );
	 body.cons(assign);
      }

      sandmark.util.javagen.Return ret =
             new sandmark.util.javagen.Return(
                new sandmark.util.javagen.LocalRef(var, type())
	    );
      body.cons(ret);

      sandmark.util.javagen.Method method =
        new sandmark.util.javagen.Method (name(), type(), attributes, args, body);

      return method;
   }
}


