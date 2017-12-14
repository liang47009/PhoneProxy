package com.yunfeng.tools.phoneproxy.tool;

import com.yunfeng.tools.phoneproxy.util.Log;

import org.bouncycastle.util.encoders.Base64Encoder;
import org.bouncycastle.x509.X509V1CertificateGenerator;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

public class RSAHelper {

    public static PublicKey getPublicKey(String key) throws Exception {
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        int i = (new Base64Encoder()).decode(key, fos);
        byte[] keyBytes = fos.toByteArray();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String key) throws Exception {
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        int i = (new Base64Encoder()).decode(key, fos);
        byte[] keyBytes = fos.toByteArray();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    public static String getKeyString(Key key) throws Exception {
        byte[] keyBytes = key.getEncoded();
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        int i = (new Base64Encoder()).encode(keyBytes, 0, keyBytes.length, fos);
        keyBytes = fos.toByteArray();
        return new String(keyBytes);
    }

    public static X509Certificate generateCert(PublicKey publicKey, PrivateKey privateKey) {
        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);
        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        X500Principal dnname = new X500Principal("CN=CA Certificate");
        certGen.setSerialNumber(BigInteger.ONE);
        certGen.setIssuerDN(dnname);
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(endDate);
        certGen.setSubjectDN(dnname);
        certGen.setPublicKey(publicKey);
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        try {
            X509Certificate cert = certGen.generate(privateKey, "BC");
            return cert;
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

//    AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
//    AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
//
//    //define lwPrivKey
//    RSAKeyParameters lwPubKey = new RSAKeyParameters(
//            false,
//            new BigInteger("b4a7e46170574f16a97082b22be58b6a2a629798419be12872a4bdba626cfae9900f76abfb12139dce5de56564fab2b6543165a040c606887420e33d91ed7ed7", 16),
//            new BigInteger("11", 16));
//
//
//    RSAPrivateCrtKeyParameters lwPrivKey = new RSAPrivateCrtKeyParameters(
//            new BigInteger("b4a7e46170574f16a97082b22be58b6a2a629798419be12872a4bdba626cfae9900f76abfb12139dce5de56564fab2b6543165a040c606887420e33d91ed7ed7", 16),
//            new BigInteger("11", 16),
//            new BigInteger("9f66f6b05410cd503b2709e88115d55daced94d1a34d4e32bf824d0dde6028ae79c5f07b580f5dce240d7111f7ddb130a7945cd7d957d1920994da389f490c89", 16),
//            new BigInteger("c0a0758cdf14256f78d4708c86becdead1b50ad4ad6c5c703e2168fbf37884cb", 16),
//            new BigInteger("f01734d7960ea60070f1b06f2bb81bfac48ff192ae18451d5e56c734a5aab8a5", 16),
//            new BigInteger("b54bb9edff22051d9ee60f9351a48591b6500a319429c069a3e335a1d6171391", 16),
//            new BigInteger("d3d83daf2a0cecd3367ae6f8ae1aeb82e9ac2f816c6fc483533d8297dd7884cd", 16),
//            new BigInteger("b8f52fc6f38593dabb661d3f50f8897f8106eee68b1bce78a95b132b4e5b5d19", 16));
////
//    public void creation() {
//        try {
//            ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(lwPrivKey);//
//            byte[] publickeyb = sigAlgId.getEncoded();//SubjectPublicKeyInfo subPubKeyInfo = ....;
//            String data = new String(publickeyb);
//            SubjectPublicKeyInfo subPubKeyInfo = new SubjectPublicKeyInfo((ASN1Sequence) ASN1Object.getEncoded(data));
//            Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
//            Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);
//
//            X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(
//                    new X500Name("CN=Test"),
//                    BigInteger.ONE,
//                    startDate, endDate,
//                    new X500Name("CN=Test"),
//                    subPubKeyInfo);
//            X509CertificateHolder certHolder = v1CertGen.build(sigGen);
//        } catch (Exception E) {
//            E.printStackTrace();
//        }
//    }


    public static void main(String[] args) {
        try {

            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            // 密钥位数
            keyPairGen.initialize(1024);
            // 密钥对
            KeyPair keyPair = keyPairGen.generateKeyPair();

            // 公钥
            PublicKey publicKey = keyPair.getPublic();

            // 私钥
            PrivateKey privateKey = keyPair.getPrivate();

            String publicKeyString = getKeyString(publicKey);
            Log.d("public:/n" + publicKeyString);

            String privateKeyString = getKeyString(privateKey);
            Log.d("private:/n" + privateKeyString);

            X509Certificate cert = generateCert(publicKey, privateKey);
            Log.d(cert);
            // 加解密类
            Cipher cipher = Cipher.getInstance("RSA");// Cipher.getInstance("RSA/ECB/PKCS1Padding");

            // 明文
            byte[] plainText = "我们都很好！邮件：@sina.com".getBytes();

            // 加密
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] enBytes = cipher.doFinal(plainText);

            // 通过密钥字符串得到密钥
            publicKey = getPublicKey(publicKeyString);
            privateKey = getPrivateKey(privateKeyString);

            // 解密
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] deBytes = cipher.doFinal(enBytes);

            publicKeyString = getKeyString(publicKey);
            Log.d("public:/n" + publicKeyString);

            privateKeyString = getKeyString(privateKey);
            Log.d("private:/n" + privateKeyString);

            String s = new String(deBytes);
            Log.d(s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void genCaKey() {
    }
}