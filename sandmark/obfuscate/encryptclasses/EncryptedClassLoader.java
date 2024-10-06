package sandmark.obfuscate.encryptclasses;

public class EncryptedClassLoader extends java.lang.ClassLoader {
    private static String KEY_ALG = "DESede";
    private static String CIPHER_ALG = "DESede/ECB/NoPadding";
    private static String sKeyStr;
    private static String sMainClassName;
    private java.security.Key mKey;

    public EncryptedClassLoader() throws java.io.IOException,
        java.security.NoSuchAlgorithmException,
        java.security.InvalidKeyException,
        javax.crypto.NoSuchPaddingException,
        java.security.spec.InvalidKeySpecException {
        super();

        if(sKeyStr == null || sKeyStr.equals("") || !sKeyStr.startsWith("0x"))
            throw new RuntimeException();

        byte keyBytes[] = new byte[(sKeyStr.length() - 2) / 2];
        for(int i = 0 ; i < keyBytes.length ; i++) {
            byte b1,b2;
            b1 = Byte.parseByte(sKeyStr.substring(2*(i + 1),2*(i + 1) + 1),16);
            b2 = Byte.parseByte(sKeyStr.substring(2*(i + 1) + 1,2*(i + 1) + 2),16);
            keyBytes[i] = (byte)((b1 << 4) | b2);
        }

        javax.crypto.SecretKeyFactory skf = 
            javax.crypto.SecretKeyFactory.getInstance(KEY_ALG);
        javax.crypto.spec.DESedeKeySpec keySpec =
            new javax.crypto.spec.DESedeKeySpec(keyBytes);
        mKey = skf.generateSecret(keySpec);

    }
    public Class findClass(String name) throws ClassNotFoundException {
        try {
            if(name == null)
                throw new ClassNotFoundException(name);

            byte[] b = loadClassData(name);
            
            if(b == null)
                throw new ClassNotFoundException(name);

            return defineClass(name,b,0,b.length);
        } catch(Exception e) {
            throw new ClassNotFoundException(name);
        }
    }
    private byte[] loadClassData(String name) throws Exception {
        String resourceName = name.replace('.','/') + ".class.enc";
        java.io.InputStream in = getResourceAsStream(resourceName);
        javax.crypto.Cipher cipher = 
            javax.crypto.Cipher.getInstance(CIPHER_ALG);
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, mKey);
        javax.crypto.CipherInputStream cin = 
            new javax.crypto.CipherInputStream(in,cipher);

        if(in == null)
            return null;

        byte bytes[] = new byte[8192];
        
        int size = 0;
        for(int rv = 1,capacity = 8192 ; rv > 0 ; ) {
            if(size == capacity) {
                capacity <<= 1;
                byte newBytes[] = new byte[capacity];
                System.arraycopy(bytes,0,newBytes,0,size);
                bytes = newBytes;
            }
            if((rv = cin.read(bytes,size,capacity - size)) > 0)
                size += rv;
        }

        int realSize = 
            ((((int)bytes[0]) & 0xff) << 24) |
            ((((int)bytes[1]) & 0xff) << 16) | 
            ((((int)bytes[2]) & 0xff) << 8) | 
            (((int)bytes[3]) & 0xff);

        byte finalBytes[] = new byte[realSize];
        System.arraycopy(bytes,4,finalBytes,0,realSize);
        return finalBytes;
    }

    public static void main(String argv[]) throws Throwable {
        EncryptedClassLoader ecl = new EncryptedClassLoader();

        if(sMainClassName == null || sMainClassName.equals("")) {
            System.out.println("This encrypted jar is not runnable");
            System.exit(1);
        }

        Class mainClass = ecl.findClass(sMainClassName);
        java.lang.reflect.Method mainMethod = 
            mainClass.getDeclaredMethod("main",new Class[] {(new String[0]).getClass()});
        mainMethod.invoke(null,new Object[] { argv });
    }
}

