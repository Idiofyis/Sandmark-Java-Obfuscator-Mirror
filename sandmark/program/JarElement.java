package sandmark.program;



/**
 * Parent class of any jar file component.
 */

public abstract class JarElement extends sandmark.program.Object
implements java.lang.Comparable {



   /**
    * Gets the name under which this component should be stored.
    */
   public abstract String getJarName();



   /**
    * Writes this component to the specified output stream.
    */
   abstract void save(java.io.OutputStream ostream)
         throws java.io.IOException;



   /**
    * Compares this JarElement with another.
    * Files come first, then classes, both ordered lexically by name.
    */
   public int compareTo(java.lang.Object o) {
      JarElement a = this;
      JarElement b = (JarElement) o;

      if (a.getClass() != b.getClass()) {
         return (a instanceof sandmark.program.File) ? -1 : 1;
      } else {
         return a.getName().compareTo(b.getName());
      }
   }



}

