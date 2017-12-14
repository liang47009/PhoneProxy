package com.yunfeng.tools.phoneproxy.tool;

/**
 * Created by xll on 2017/12/7.
 */


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.net.ServerSocketFactory;

/**
 * Created by kingj on 2014/8/13.
 */

/**
 * Created by kingj on 2014/8/13.
 */
class CertifcateUtils {
    public static byte[] readCertifacates() throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream in = new FileInputStream("c:/https.crt");
        java.security.cert.Certificate cate = factory.generateCertificate(in);
        return cate.getEncoded();
    }

    public static byte[] readPrivateKey() throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");
        InputStream in = new FileInputStream("c:/https.keystore");
        store.load(in, "wangyi".toCharArray());
        PrivateKey pk = (PrivateKey) store.getKey("wangyi", "wangyi".toCharArray());
        return pk.getEncoded();
    }

    public static PrivateKey readPrivateKeys() throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");
        InputStream in = new FileInputStream("c:/https.keystore");
        store.load(in, "wangyi".toCharArray());
        PrivateKey pk = (PrivateKey) store.getKey("wangyi", "wangyi".toCharArray());
        return pk;
    }

    public static PublicKey readPublicKeys() throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream in = new FileInputStream("c:/https.crt");
        java.security.cert.Certificate cate = factory.generateCertificate(in);
        return cate.getPublicKey();
    }

    public static java.security.cert.Certificate createCertiface(byte b[]) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(b);
        java.security.cert.Certificate cate = factory.generateCertificate(in);
        return cate;
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
}

class SocketUtils {
    public static void close(Socket s) {
        try {
            s.shutdownInput();
            s.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] readBytes(DataInputStream in, int length) throws IOException {
        int r = 0;
        byte[] data = new byte[length];
        while (r < length) {
            r += in.read(data, r, length - r);
        }
        return data;
    }

    public static void writeBytes(DataOutputStream out, byte[] bytes, int length) throws IOException {
        out.writeInt(length);
        out.write(bytes, 0, length);
        out.flush();
    }
}

/**
 * server
 * Created by kingj on 2014/8/13.
 */
public class HttpsMockServer extends HttpsMockBase {
    static DataInputStream in;
    static DataOutputStream out;
    static String hash;
    static Key key;
    static ExecutorService executorService = Executors.newFixedThreadPool(20);

    public static void startup(String host, final int port) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket ss = ServerSocketFactory.getDefault().createServerSocket(port);
                    ss.setReceiveBufferSize(102400);
                    ss.setReuseAddress(false);
                    while (true) {
                        try {
                            final Socket s = ss.accept();
                            doHttpsShakeHands(s);
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    doSocketTransport(s);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void doSocketTransport(Socket s) {
        try {
            System.out.println("--------------------------------------------------------");
            int length = in.readInt();
            byte[] clientMsg = readBytes(length);
            System.out.println("客户端指令内容为:" + byte2hex(clientMsg));

            writeBytes("服务器已经接受请求".getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static byte[] readBytes(int length) throws Exception {
        byte[] undecrpty = SocketUtils.readBytes(in, length);
        System.out.println("读取未解密消息:" + byte2hex(undecrpty));
        return DesCoder.decrypt(undecrpty, key);
    }

    public static void writeBytes(byte[] data) throws Exception {
        byte[] encrpted = DesCoder.encrypt(data, key);
        System.out.println("写入加密后消息:" + byte2hex(encrpted));
        SocketUtils.writeBytes(out, encrpted, encrpted.length);
    }

    private static void doHttpsShakeHands(Socket s) throws Exception {
        in = new DataInputStream(s.getInputStream());
        out = new DataOutputStream(s.getOutputStream());

        //第一步 获取客户端发送的支持的验证规则，包括hash算法，这里选用SHA1作为hash
        int length = in.readInt();
        in.skipBytes(4);
        byte[] clientSupportHash = SocketUtils.readBytes(in, length);
        String clientHash = new String(clientSupportHash);
        hash = clientHash;
        System.out.println("客户端发送了hash算法为:" + clientHash);

        //第二步，发送服务器证书到客户端
        byte[] certificateBytes = CertifcateUtils.readCertifacates();
        privateKey = CertifcateUtils.readPrivateKeys();
        System.out.println("发送证书给客户端,字节长度为:" + certificateBytes.length);
        System.out.println("证书内容为:" + byte2hex(certificateBytes));
        SocketUtils.writeBytes(out, certificateBytes, certificateBytes.length);

        System.out.println("获取客户端通过公钥加密后的随机数");
        int secureByteLength = in.readInt();
        byte[] secureBytes = SocketUtils.readBytes(in, secureByteLength);

        System.out.println("读取到的客户端的随机数为:" + byte2hex(secureBytes));
        byte secureSeed[] = decrypt(secureBytes);
        System.out.println("解密后的随机数密码为:" + byte2hex(secureSeed));

        //第三步 获取客户端加密字符串
        int skip = in.readInt();
        System.out.println("第三步 获取客户端加密消息,消息长度为 ：" + skip);
        byte[] data = SocketUtils.readBytes(in, skip);

        System.out.println("客户端发送的加密消息为 : " + byte2hex(data));
        System.out.println("用私钥对消息解密，并计算SHA1的hash值");
        byte message[] = decrypt(data, new SecureRandom(secureBytes));
        byte serverHash[] = cactHash(message);


        System.out.println("获取客户端计算的SHA1摘要");
        int hashSkip = in.readInt();
        byte[] clientHashBytes = SocketUtils.readBytes(in, hashSkip);
        System.out.println("客户端SHA1摘要为 : " + byte2hex(clientHashBytes));

        System.out.println("开始比较客户端hash和服务器端从消息中计算的hash值是否一致");
        boolean isHashEquals = byteEquals(serverHash, clientHashBytes);
        System.out.println("是否一致结果为 ： " + isHashEquals);


        System.out.println("第一次校验客户端发送过来的消息和摘译一致，服务器开始向客户端发送消息和摘要");
        System.out.println("生成密码用于加密服务器端消息,secureRandom : " + byte2hex(secureSeed));
        SecureRandom secureRandom = new SecureRandom(secureSeed);

        String randomMessage = random();
        System.out.println("服务器端生成的随机消息为 : " + randomMessage);

        System.out.println("用DES算法并使用客户端生成的随机密码对消息加密");
        byte[] desKey = DesCoder.initSecretKey(secureRandom);
        key = DesCoder.toKey(desKey);

        byte serverMessage[] = DesCoder.encrypt(randomMessage.getBytes(), key);
        SocketUtils.writeBytes(out, serverMessage, serverMessage.length);
        System.out.println("服务器端发送的机密后的消息为:" + byte2hex(serverMessage) + ",加密密码为:" + byte2hex(secureSeed));

        System.out.println("服务器端开始计算hash摘要值");
        byte serverMessageHash[] = cactHash(randomMessage.getBytes());
        System.out.println("服务器端计算的hash摘要值为 :" + byte2hex(serverMessageHash));
        SocketUtils.writeBytes(out, serverMessageHash, serverMessageHash.length);

        System.out.println("握手成功，之后所有通信都将使用DES加密算法进行加密");
    }

}

/**
 * aa
 * Created by kingj on 2014/8/13.
 */
class HttpsMockClient extends HttpsMockBase {
    static DataInputStream in;
    static DataOutputStream out;
    static Key key;

    public static void main(String args[]) throws Exception {
        int port = 80;
        Socket s = new Socket("localhost", port);
        s.setReceiveBufferSize(102400);
        s.setKeepAlive(true);
        in = new DataInputStream(s.getInputStream());
        out = new DataOutputStream(s.getOutputStream());
        shakeHands();

        System.out.println("------------------------------------------------------------------");
        String name = "duck";
        writeBytes(name.getBytes());

        int len = in.readInt();
        byte[] msg = readBytes(len);
        System.out.println("服务器反馈消息:" + byte2hex(msg));
        Thread.sleep(1000 * 100);


    }

    private static void shakeHands() throws Exception {
        //第一步 客户端发送自己支持的hash算法
        String supportHash = "SHA1";
        int length = supportHash.getBytes().length;
        out.writeInt(length);
        SocketUtils.writeBytes(out, supportHash.getBytes(), length);

        //第二步 客户端验证服务器端证书是否合法
        int skip = in.readInt();
        byte[] certificate = SocketUtils.readBytes(in, skip);
        java.security.cert.Certificate cc = CertifcateUtils.createCertiface(certificate);

        publicKey = cc.getPublicKey();
        cc.verify(publicKey);
        System.out.println("客户端校验服务器端证书是否合法：" + true);

        //第三步  客户端校验服务器端发送过来的证书成功,生成随机数并用公钥加密
        System.out.println("客户端校验服务器端发送过来的证书成功,生成随机数并用公钥加密");
        SecureRandom seed = new SecureRandom();
        int seedLength = 2;
        byte seedBytes[] = seed.generateSeed(seedLength);
        System.out.println("生成的随机数为 : " + byte2hex(seedBytes));
        System.out.println("将随机数用公钥加密后发送到服务器");
        byte[] encrptedSeed = encryptByPublicKey(seedBytes, null);
        SocketUtils.writeBytes(out, encrptedSeed, encrptedSeed.length);

        System.out.println("加密后的seed值为 :" + byte2hex(encrptedSeed));

        String message = random();
        System.out.println("客户端生成消息为:" + message);

        System.out.println("使用随机数并用公钥对消息加密");
        byte[] encrpt = encryptByPublicKey(message.getBytes(), seed);
        System.out.println("加密后消息位数为 : " + encrpt.length);
        SocketUtils.writeBytes(out, encrpt, encrpt.length);

        System.out.println("客户端使用SHA1计算消息摘要");
        byte hash[] = cactHash(message.getBytes());
        System.out.println("摘要信息为:" + byte2hex(hash));

        System.out.println("消息加密完成，摘要计算完成，发送服务器");
        SocketUtils.writeBytes(out, hash, hash.length);


        System.out.println("客户端向服务器发送消息完成，开始接受服务器端发送回来的消息和摘要");
        System.out.println("接受服务器端发送的消息");
        int serverMessageLength = in.readInt();
        byte[] serverMessage = SocketUtils.readBytes(in, serverMessageLength);
        System.out.println("服务器端的消息内容为 ：" + byte2hex(serverMessage));

        System.out.println("开始用之前生成的随机密码和DES算法解密消息,密码为:" + byte2hex(seedBytes));
        byte[] desKey = DesCoder.initSecretKey(new SecureRandom(seedBytes));
        key = DesCoder.toKey(desKey);

        byte[] decrpytedServerMsg = DesCoder.decrypt(serverMessage, key);
        System.out.println("解密后的消息为:" + byte2hex(decrpytedServerMsg));

        int serverHashLength = in.readInt();
        byte[] serverHash = SocketUtils.readBytes(in, serverHashLength);
        System.out.println("开始接受服务器端的摘要消息:" + byte2hex(serverHash));

        byte[] serverHashValues = cactHash(decrpytedServerMsg);
        System.out.println("计算服务器端发送过来的消息的摘要 : " + byte2hex(serverHashValues));

        System.out.println("判断服务器端发送过来的hash摘要是否和计算出的摘要一致");
        boolean isHashEquals = byteEquals(serverHashValues, serverHash);

        if (isHashEquals) {
            System.out.println("验证完成，握手成功");
        } else {
            System.out.println("验证失败，握手失败");
        }
    }


    public static byte[] readBytes(int length) throws Exception {
        byte[] undecrpty = SocketUtils.readBytes(in, length);
        System.out.println("读取未解密消息:" + byte2hex(undecrpty));
        return DesCoder.decrypt(undecrpty, key);
    }

    public static void writeBytes(byte[] data) throws Exception {
        byte[] encrpted = DesCoder.encrypt(data, key);
        System.out.println("写入加密后消息:" + byte2hex(encrpted));
        SocketUtils.writeBytes(out, encrpted, encrpted.length);
    }
}

class DesCoder {

    /**
     * 密钥算法
     */
    private static final String KEY_ALGORITHM = "DES";

    private static final String DEFAULT_CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";
//  private static final String DEFAULT_CIPHER_ALGORITHM = "DES/ECB/ISO10126Padding";


    /**
     * 初始化密钥
     *
     * @return byte[] 密钥
     * @throws Exception
     */
    public static byte[] initSecretKey(SecureRandom random) throws Exception {
        //返回生成指定算法的秘密密钥的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        //初始化此密钥生成器，使其具有确定的密钥大小
        kg.init(random);
        //生成一个密钥
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 转换密钥
     *
     * @param key 二进制密钥
     * @return Key  密钥
     * @throws Exception
     */
    public static Key toKey(byte[] key) throws Exception {
        //实例化DES密钥规则
        DESKeySpec dks = new DESKeySpec(key);
        //实例化密钥工厂
        SecretKeyFactory skf = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        //生成密钥
        SecretKey secretKey = skf.generateSecret(dks);
        return secretKey;
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return byte[]   加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, Key key) throws Exception {
        return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @param key  二进制密钥
     * @return byte[]   加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
    }


    /**
     * 加密
     *
     * @param data            待加密数据
     * @param key             二进制密钥
     * @param cipherAlgorithm 加密算法/工作模式/填充方式
     * @return byte[]   加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
        //还原密钥
        Key k = toKey(key);
        return encrypt(data, k, cipherAlgorithm);
    }

    /**
     * 加密
     *
     * @param data            待加密数据
     * @param key             密钥
     * @param cipherAlgorithm 加密算法/工作模式/填充方式
     * @return byte[]   加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
        //实例化
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        //使用密钥初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, key);
        //执行操作
        return cipher.doFinal(data);
    }


    /**
     * 解密
     *
     * @param data 待解密数据
     * @param key  二进制密钥
     * @return byte[]   解密数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
    }

    /**
     * 解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[]   解密数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, Key key) throws Exception {
        return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
    }

    /**
     * 解密
     *
     * @param data            待解密数据
     * @param key             二进制密钥
     * @param cipherAlgorithm 加密算法/工作模式/填充方式
     * @return byte[]   解密数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
        //还原密钥
        Key k = toKey(key);
        return decrypt(data, k, cipherAlgorithm);
    }

    /**
     * 解密
     *
     * @param data            待解密数据
     * @param key             密钥
     * @param cipherAlgorithm 加密算法/工作模式/填充方式
     * @return byte[]   解密数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
        //实例化
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        //使用密钥初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, key);
        //执行操作
        return cipher.doFinal(data);
    }

    private static String showByteArray(byte[] data) {
        if (null == data) {
            return null;
        }
        StringBuilder sb = new StringBuilder("{");
        for (byte b : data) {
            sb.append(b).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

}

class HttpsMockBase {
    static PrivateKey privateKey;
    static PublicKey publicKey;


    public static boolean byteEquals(byte a[], byte[] b) {
        boolean equals = true;
        if (a == null || b == null) {
            equals = false;
        }

        if (a != null && b != null) {
            if (a.length != b.length) {
                equals = false;
            } else {
                for (int i = 0; i < a.length; i++) {
                    if (a[i] != b[i]) {
                        equals = false;
                        break;
                    }
                }
            }

        }
        return equals;
    }

    public static byte[] decrypt(byte data[]) throws Exception {
        // 对数据解密
        Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte data[], SecureRandom seed) throws Exception {
        // 对数据解密
        Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey, seed);
        return cipher.doFinal(data);
    }

    public static byte[] decryptByPublicKey(byte data[], SecureRandom seed) throws Exception {
        if (publicKey == null) {
            publicKey = CertifcateUtils.readPublicKeys();
        }
        // 对数据解密
        Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        if (seed == null) {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, publicKey, seed);
        }

        return cipher.doFinal(data);
    }

    public static byte[] decryptByDes(byte data[], SecureRandom seed) throws Exception {
        if (publicKey == null) {
            publicKey = CertifcateUtils.readPublicKeys();
        }
        // 对数据解密
        Cipher cipher = Cipher.getInstance("DES");
        if (seed == null) {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, publicKey, seed);
        }

        return cipher.doFinal(data);
    }


    public static byte[] encryptByPublicKey(byte[] data, SecureRandom seed)
            throws Exception {
        if (publicKey == null) {
            publicKey = CertifcateUtils.readPublicKeys();
        }
        // 对数据加密
        Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        if (seed == null) {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, seed);
        }

        return cipher.doFinal(data);
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + "  " + stmp;
            }
        }
        return hs.toUpperCase();
    }

    public static byte[] cactHash(byte[] bytes) {
        byte[] _bytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(bytes);
            _bytes = md.digest();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return _bytes;
    }


    static String random() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        int seedLength = 10;
        for (int i = 0; i < seedLength; i++) {
            builder.append(digits[random.nextInt(seedLength)]);
        }

        return builder.toString();
    }

    static char[] digits = {
            '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j'
    };

}
