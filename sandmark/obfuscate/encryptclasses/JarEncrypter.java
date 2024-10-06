package sandmark.obfuscate.encryptclasses;

public class JarEncrypter extends sandmark.obfuscate.AppObfuscator {
    private static String KEY_ALG = "DESede";
    private static String CIPHER_ALG = "DESede/ECB/NoPadding";
    private static int KEY_SIZE = 112;
    private static String ENCRYPTED_LOADER_CURRENT_PATH = 
        "/sandmark/obfuscate/encryptclasses/EncryptedClassLoader.class";
    private static String ENCRYPTED_LOADER_CLASS_NAME = 
        "sandmark.obfuscate.encryptclasses.EncryptedClassLoader";
    private static String USAGE = "usage: java JarEncrypter plain.jar cipher.jar";
    private static String KEY_FIELD_NAME = "sKeyStr";
    private static String MAIN_CLASS_FIELD_NAME = "sMainClassName";
    private static char hexStr[] = new char[] {
        '0','1','2','3','4','5','6','7','8','9',
        'a','b','c','d','e','f'
    };

    public void apply(sandmark.program.Application app) throws Exception {
       if(app.getClass(ENCRYPTED_LOADER_CLASS_NAME) != null)
          throw new IllegalArgumentException("Can't double encrypt");

	new sandmark.util.Publicizer().apply(app);
        
        java.security.Key key = generateKey(getConfigProperties().getProperty("Encryption Key"));
        javax.crypto.Cipher cipher = 
            javax.crypto.Cipher.getInstance(CIPHER_ALG);
        System.out.println("cipher class: " + cipher.getClass());

        String mainClassName = app.getMain() == null ? null : app.getMain().getName();
        sandmark.program.Class classes[] = app.getClasses();
        for(int i = 0 ; i < classes.length ; i++) {
            sandmark.program.Class clazz = classes[i];

            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE,key);
            byte fileBytes[] = clazz.getBytes();
            byte fileLengthBytes[] = new byte[] {
                (byte)((fileBytes.length >> 24) & 0xff),
                (byte)((fileBytes.length >> 16) & 0xff),
                (byte)((fileBytes.length >> 8) & 0xff),
                (byte)(fileBytes.length & 0xff),
            };

            byte encryptedLengthBytes[] = cipher.update(fileLengthBytes);
            byte encryptedFileBytes[] = cipher.update(fileBytes);
            byte encryptedLastBytes[] = cipher.doFinal(new byte[] { 0,0,0,0,0,0,0},0,(8 - ((4 + fileBytes.length) % 8)) % 8);
            byte allBytes[] = new byte[encryptedLengthBytes.length + encryptedFileBytes.length +
                                       encryptedLastBytes.length];
            System.arraycopy(encryptedLengthBytes,0,allBytes,0,encryptedLengthBytes.length);
            System.arraycopy(encryptedFileBytes,0,allBytes,encryptedLengthBytes.length,
                             encryptedFileBytes.length);
            System.arraycopy(encryptedLastBytes,0,allBytes,
                             encryptedLengthBytes.length + encryptedFileBytes.length,
                             encryptedLastBytes.length);
            System.out.println("lengths: " + fileBytes.length + " " + encryptedLengthBytes.length + " " +
                               encryptedFileBytes.length + " " + encryptedLastBytes.length +
                               " " + allBytes.length);
            new sandmark.program.File(app,clazz.getJarName() + ".enc",allBytes);
            clazz.delete();
        }

        java.io.InputStream loaderStream = 
            getClass().getResourceAsStream(ENCRYPTED_LOADER_CURRENT_PATH);
        org.apache.bcel.classfile.JavaClass jc = fixupLoader(loaderStream,mainClassName,key);
        sandmark.program.LocalClass lc = new sandmark.program.LocalClass(app,jc);
        app.setMain(lc);
    }

    public static void main(String argv[]) throws Throwable {
        if(argv.length != 2) {
            System.out.println(USAGE);
            System.exit(1);
        }
        sandmark.program.Application app = 
            new sandmark.program.Application(argv[0]);
        JarEncrypter je = new JarEncrypter();
        je.apply(app);
        app.save(argv[1]);
    }
    private org.apache.bcel.classfile.JavaClass fixupLoader
        (java.io.InputStream is,String mainClassName,java.security.Key key) 
        throws java.io.IOException {
        org.apache.bcel.classfile.JavaClass jc =
            new org.apache.bcel.classfile.ClassParser
            (is,ENCRYPTED_LOADER_CURRENT_PATH).parse();
        org.apache.bcel.generic.ClassGen cg =
            new org.apache.bcel.generic.ClassGen(jc);
        org.apache.bcel.generic.InstructionFactory factory =
            new org.apache.bcel.generic.InstructionFactory(cg);
        org.apache.bcel.classfile.Method meth = cg.containsMethod("<clinit>","()V");
        org.apache.bcel.generic.MethodGen newClInit = null, oldClInit = null;

        if(meth == null) {
            newClInit = 
                new org.apache.bcel.generic.MethodGen
                (org.apache.bcel.Constants.ACC_PUBLIC,org.apache.bcel.generic.Type.VOID,
                 new org.apache.bcel.generic.Type[0],new String[0],"<clinit>",
                 cg.getClassName(),new org.apache.bcel.generic.InstructionList(),
                 cg.getConstantPool());
        } else {
            org.apache.bcel.classfile.Method testMeth = null;
            int i = -1;
            do {
                i++;
                testMeth = cg.containsMethod("sm$ci" + i,"()V");
            } while(testMeth != null);

            newClInit = 
                new org.apache.bcel.generic.MethodGen
                (meth,cg.getClassName(),cg.getConstantPool());
            oldClInit = (org.apache.bcel.generic.MethodGen)newClInit.clone();
            oldClInit.setName("sm$ci" + i);
            newClInit.setInstructionList
                (new org.apache.bcel.generic.InstructionList());
            newClInit.getInstructionList().append
                (factory.createInvoke
                 (cg.getClassName(),oldClInit.getName(),org.apache.bcel.generic.Type.VOID,
                  new org.apache.bcel.generic.Type[0],
                  org.apache.bcel.Constants.INVOKESTATIC));
        }

        newClInit.removeLineNumbers();
        newClInit.removeLocalVariables();

        newClInit.getInstructionList().append
            ((new org.apache.bcel.generic.PUSH
              (cg.getConstantPool(),getKeyStr(key))).getInstruction());
        newClInit.getInstructionList().append
            (factory.createPutStatic(cg.getClassName(),KEY_FIELD_NAME,
                                     org.apache.bcel.generic.Type.STRING));

        if(mainClassName != null) {
            newClInit.getInstructionList().append
                ((new org.apache.bcel.generic.PUSH
                  (cg.getConstantPool(),mainClassName)).getInstruction());
            newClInit.getInstructionList().append
                (factory.createPutStatic(cg.getClassName(),MAIN_CLASS_FIELD_NAME,
                                         org.apache.bcel.generic.Type.STRING));
        }

        newClInit.getInstructionList().append
            (new org.apache.bcel.generic.RETURN());

        newClInit.setMaxStack();
        
        if(meth == null) {
            cg.addMethod(newClInit.getMethod());
        } else {
            cg.replaceMethod(meth,newClInit.getMethod());
            cg.addMethod(oldClInit.getMethod());
        }

        //cg.setClassName(ENCRYPTED_LOADER_CLASS_NAME);
        return cg.getJavaClass();
    }
    private java.security.Key generateKey(String keyStr) throws 
        java.security.NoSuchAlgorithmException,
        javax.crypto.NoSuchPaddingException,
        java.security.InvalidKeyException,
        javax.crypto.IllegalBlockSizeException,
        java.security.spec.InvalidKeySpecException {

        java.security.Key key;

        if(keyStr == null || keyStr.equals("") || !keyStr.startsWith("0x")) {

            javax.crypto.KeyGenerator keyGen = 
                javax.crypto.KeyGenerator.getInstance(KEY_ALG);
            keyGen.init(KEY_SIZE);
            key = keyGen.generateKey();

        } else {

            byte keyBytes[] = new byte[(keyStr.length() - 2) / 2];
            for(int i = 0 ; i < keyBytes.length ; i++) {
                byte b1,b2;
                b1 = Byte.parseByte(keyStr.substring(2*(i + 1),2*(i + 1) + 1),16);
                b2 = Byte.parseByte(keyStr.substring(2*(i + 1) + 1,2*(i + 1) + 2),16);
                keyBytes[i] = (byte)((b1 << 4) | b2);
            }
            System.out.println("key length: " + keyBytes.length);
            for(int i = 0 ; i < keyBytes.length ; i++)
                System.out.print(keyBytes[i] + " ");
            System.out.println();
            
            javax.crypto.SecretKeyFactory skf = 
                javax.crypto.SecretKeyFactory.getInstance(KEY_ALG);
            javax.crypto.spec.DESedeKeySpec keySpec =
                new javax.crypto.spec.DESedeKeySpec(keyBytes);
            key = skf.generateSecret(keySpec);

        }
        
        return key;
    }
    private String getKeyStr(java.security.Key key) {
        try {
            String secKey = "0x";
            byte bytes[] = key.getEncoded();
            
            for(int i = 0 ; i < bytes.length ; i++) {
                secKey += hexStr[(byte)((bytes[i] >> 4) & 0xf)];
                secKey += hexStr[(byte)(bytes[i] & 0xf)];
            }
            
            return secKey;
        } catch(Exception e) {
            return null;
        }
    }
    public String getShortName() { return "Class Encrypter"; }
    public String getLongName() { 
        return "Encrypt Classes, Decrypt At Runtime";
    }
    public String getAlgHTML() {
        return           
          "<HTML><BODY>" +
          "Class Encrypter encrypts class files and causes them to be decrypted " +
          "at runtime.\n" +
          "<TABLE>" +
          "<TR><TD>" +
          "Author: <A HREF = \"mailto:ash@huntwork.net\">Andrew Huntwork</A>" +
          "</TD></TR>" +
          "</TABLE>" +
          "</BODY></HTML>";
    }
    public String getAlgURL() { return "sandmark/obfuscate/encryptclasses/doc/help.html"; }
    private sandmark.util.ConfigProperties mProps;
    public sandmark.util.ConfigProperties getConfigProperties() {
        if(mProps == null) {
            String args[][] = {
                {"Encryption Key","","What key to use","","S"}
            };
            mProps = 
                new sandmark.util.ConfigProperties
                (args,null);
        }
        return mProps;
    }
    public String getAuthor() { return "Andrew Huntwork"; }
    public String getAuthorEmail() { return "ash@huntwork.net"; }
    public String getDescription() {
        return "Class Encrypter encrypts class files and causes them to be decrypted " +
            "at runtime.";
    }
    public sandmark.config.ModificationProperty[] getMutations() { return null; }
}

