package sandmark.program;



/**
 * Represents arbitrary data stored as a "file" within a jar archive,
 * such as an image or a sound clip.
 *
 * <P> Modification methods in this class automatically call the
 * {@link sandmark.program.Object#mark() mark} method
 * to register their changes.
 */

public class File extends sandmark.program.JarElement {



   private byte[] data;                 /* file contents */



   /**
    * Constructs a File object from an array of bytes.
    *
    * @param parent the containing application
    * @param name the filename in the jar file
    * @param data the file contents
    */
   public File(sandmark.program.Application parent, String name, byte[] data) {
      setName(name);
      setData(data);
      parent.add(this);
   }



   /**
    * Constructs a File object from an input stream of data.
    *
    * @param parent the containing application
    * @param name the filename in the jar file
    * @param istr the file contents
    */
   public File(sandmark.program.Application parent, String name,
         java.io.InputStream istr) throws java.io.IOException {
      setName(name);
      setData(istr);
      parent.add(this);
   }



   /**
    * Gets the name used in the jar file.
    */
   public String getJarName() {
      return getName();
   }

    public String getCanonicalName(){
        return getName();
    }

   /**
    * Gets a copy of the file data as byte array.
    */
   public byte[] getBytes() {
      return (byte[]) data.clone();
   }



   /**
    * Gets a copy of the file data as an InputStream.
    */
   public java.io.InputStream getStream() {
      return new java.io.ByteArrayInputStream(data);
   }



   /**
    * Saves the data to an output stream.
    */
   void save(java.io.OutputStream ostream) throws java.io.IOException {
      ostream = new java.io.BufferedOutputStream(ostream);
      ostream.write(data, 0, data.length);
      ostream.flush();
   }



   /**
    * Sets the data by copying from a byte array.
    */
   public void setData(byte[] data) {
      this.data = (byte[]) data.clone();
      mark();
   }



   /**
    * Sets the data by reading from an InputStream.
    */
   public void setData(java.io.InputStream istream) throws java.io.IOException {
      this.data = sandmark.util.Misc.loadBytes(istream);
      mark();
   }



}

