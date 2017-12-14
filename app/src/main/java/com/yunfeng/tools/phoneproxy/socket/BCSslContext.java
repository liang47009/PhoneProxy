package com.yunfeng.tools.phoneproxy.socket;

import com.yunfeng.tools.phoneproxy.tool.NoSSLv3SocketFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkApplicationProtocolNegotiator;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static java.util.Arrays.asList;

/**
 * new
 * Created by xll on 2017/12/7.
 */
public class BCSslContext extends SslContext {
    static final String PROTOCOL = "TLSv1";
    private static final String[] DEFAULT_PROTOCOLS;
    private static final List<String> DEFAULT_CIPHERS;
    private static final Set<String> SUPPORTED_CIPHERS;
    private final ClientAuth clientAuth;
    private final JdkApplicationProtocolNegotiator apn;
    private final SSLContext sslContext;
    private final List<String> unmodifiableCipherSuites;
    private final String[] cipherSuites;


    final static class MyNegotiator implements JdkApplicationProtocolNegotiator {
        public static final MyNegotiator INSTANCE = new MyNegotiator();
        private static final SslEngineWrapperFactory DEFAULT_SSL_ENGINE_WRAPPER_FACTORY = new SslEngineWrapperFactory() {
            @Override
            public SSLEngine wrapSslEngine(SSLEngine engine,
                                           JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
                return engine;
            }
        };

        private MyNegotiator() {
        }

        @Override
        public SslEngineWrapperFactory wrapperFactory() {
            return DEFAULT_SSL_ENGINE_WRAPPER_FACTORY;
        }

        @Override
        public ProtocolSelectorFactory protocolSelectorFactory() {
            throw new UnsupportedOperationException("Application protocol negotiation unsupported");
        }

        @Override
        public ProtocolSelectionListenerFactory protocolListenerFactory() {
            throw new UnsupportedOperationException("Application protocol negotiation unsupported");
        }

        @Override
        public List<String> protocols() {
            return Collections.emptyList();
        }
    }


    static final String[] DEFAULT_CIPHER_SUITES = {
            // GCM (Galois/Counter Mode) requires JDK 8.
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            // AES256 requires JCE unlimited strength jurisdiction policy files.
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            // GCM (Galois/Counter Mode) requires JDK 8.
            "TLS_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA",
            // AES256 requires JCE unlimited strength jurisdiction policy files.
            "TLS_RSA_WITH_AES_256_CBC_SHA"
    };

    static {
        SSLContext context;
        int i;
        try {
            context = SSLContext.getInstance(PROTOCOL);
            context.init(null, null, null);
        } catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }

        SSLEngine engine = context.createSSLEngine();

        // Choose the sensible default list of protocols.
        final String[] supportedProtocols = engine.getSupportedProtocols();
        Set<String> supportedProtocolsSet = new HashSet<String>(supportedProtocols.length);
        for (i = 0; i < supportedProtocols.length; ++i) {
            supportedProtocolsSet.add(supportedProtocols[i]);
        }
        List<String> protocols = new ArrayList<String>();
        addIfSupported(
                supportedProtocolsSet, protocols,
                "TLSv1.2", "TLSv1.1", "TLSv1", "SSL", "SSLv2", "SSLv3");

        if (!protocols.isEmpty()) {
            DEFAULT_PROTOCOLS = protocols.toArray(new String[protocols.size()]);
        } else {
            DEFAULT_PROTOCOLS = engine.getEnabledProtocols();
        }

        // Choose the sensible default list of cipher suites.
        final String[] supportedCiphers = engine.getSupportedCipherSuites();
        SUPPORTED_CIPHERS = new HashSet<String>(supportedCiphers.length);
        for (i = 0; i < supportedCiphers.length; ++i) {
            String supportedCipher = supportedCiphers[i];
            SUPPORTED_CIPHERS.add(supportedCipher);
            // IBM's J9 JVM utilizes a custom naming scheme for ciphers and only returns ciphers with the "SSL_"
            // prefix instead of the "TLS_" prefix (as defined in the JSSE cipher suite names [1]). According to IBM's
            // documentation [2] the "SSL_" prefix is "interchangeable" with the "TLS_" prefix.
            // See the IBM forum discussion [3] and issue on IBM's JVM [4] for more details.
            //[1] http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#ciphersuites
            //[2] https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.security.component.80.doc/
            // security-component/jsse2Docs/ciphersuites.html
            //[3] https://www.ibm.com/developerworks/community/forums/html/topic?id=9b5a56a9-fa46-4031-b33b-df91e28d77c2
            //[4] https://www.ibm.com/developerworks/rfe/execute?use_case=viewRfe&CR_ID=71770
            if (supportedCipher.startsWith("SSL_")) {
                SUPPORTED_CIPHERS.add("TLS_" + supportedCipher.substring("SSL_".length()));
            }
        }
        List<String> ciphers = new ArrayList<String>();
        addIfSupported(SUPPORTED_CIPHERS, ciphers, DEFAULT_CIPHER_SUITES);
        useFallbackCiphersIfDefaultIsEmpty(ciphers, engine.getEnabledCipherSuites());
        DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);
    }

    static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, String... fallbackCiphers) {
        useFallbackCiphersIfDefaultIsEmpty(defaultCiphers, asList(fallbackCiphers));
    }

    static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, Iterable<String> fallbackCiphers) {
        if (defaultCiphers.isEmpty()) {
            for (String cipher : fallbackCiphers) {
                if (cipher.startsWith("SSL_") || cipher.contains("_RC4_")) {
                    continue;
                }
                defaultCiphers.add(cipher);
            }
        }
    }

    /**
     * Add elements from {@code names} into {@code enabled} if they are in {@code supported}.
     */
    static void addIfSupported(Set<String> supported, List<String> enabled, String... names) {
        for (String n : names) {
            if (supported.contains(n)) {
                enabled.add(n);
            }
        }
    }

    public BCSslContext(Provider sslContextProvider, X509Certificate[] trustCertCollection,
                 TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain,
                 PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory,
                 long sessionCacheSize, long sessionTimeout) throws SSLException {
        super(false);
        SSLContext sslContext = newSSLContext(sslContextProvider, trustCertCollection, trustManagerFactory,
                keyCertChain, key, keyPassword, keyManagerFactory, sessionCacheSize, sessionTimeout);
        this.sslContext = sslContext;
        CipherSuiteFilter cipherFilter = IdentityCipherSuiteFilter.INSTANCE;
        this.clientAuth = ClientAuth.NONE;
        cipherSuites = checkNotNull(cipherFilter, "cipherFilter").filterCipherSuites(
                null, DEFAULT_CIPHERS, SUPPORTED_CIPHERS);
        unmodifiableCipherSuites = Collections.unmodifiableList(Arrays.asList(cipherSuites));
        apn = MyNegotiator.INSTANCE;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public List<String> cipherSuites() {
        return unmodifiableCipherSuites;
    }

    @Override
    public long sessionCacheSize() {
        return sessionContext().getSessionCacheSize();
    }

    @Override
    public long sessionTimeout() {
        return sessionContext().getSessionTimeout();
    }

    @Override
    public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return apn;
    }

    @Override
    public SSLEngine newEngine(ByteBufAllocator alloc) {
        return configureAndWrapEngine(context().createSSLEngine(), alloc);
    }

    @Override
    public SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
        return configureAndWrapEngine(context().createSSLEngine(peerHost, peerPort), alloc);
    }

    @Override
    public SSLSessionContext sessionContext() {
        if (isServer()) {
            return context().getServerSessionContext();
        } else {
            return context().getClientSessionContext();
        }
    }

    /**
     * Returns the JDK {@link SSLContext} object held by this context.
     */
    public final SSLContext context() {
        return sslContext;
    }

    @SuppressWarnings("deprecation")
    private SSLEngine configureAndWrapEngine(SSLEngine engine, ByteBufAllocator alloc) {
        engine.setEnabledCipherSuites(cipherSuites);
        engine.setEnabledProtocols(engine.getEnabledProtocols());
        engine.setUseClientMode(isClient());
        if (isServer()) {
            switch (clientAuth) {
                case OPTIONAL:
                    engine.setWantClientAuth(true);
                    break;
                case REQUIRE:
                    engine.setNeedClientAuth(true);
                    break;
                case NONE:
                    break; // exhaustive cases
                default:
                    throw new Error("Unknown auth " + clientAuth);
            }
        }
        MyNegotiator.SslEngineWrapperFactory factory = apn.wrapperFactory();
//        if (factory instanceof MyNegotiator.AllocatorAwareSslEngineWrapperFactory) {
//            return ((MyNegotiator.AllocatorAwareSslEngineWrapperFactory) factory)
//                    .wrapSslEngine(engine, alloc, apn, isServer());
//        }
        return factory.wrapSslEngine(engine, apn, isServer());
    }

    static ApplicationProtocolConfig toApplicationProtocolConfig(Iterable<String> nextProtocols) {
        ApplicationProtocolConfig apn;
        if (nextProtocols == null) {
            apn = ApplicationProtocolConfig.DISABLED;
        } else {
            apn = new ApplicationProtocolConfig(
                    ApplicationProtocolConfig.Protocol.NPN_AND_ALPN, ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL,
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT, nextProtocols);
        }
        return apn;
    }

    static PrivateKey toPrivateKeyInternal(File keyFile, String keyPassword) throws SSLException {
        try {
            return toPrivateKey(keyFile, keyPassword);
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    static PrivateKey toPrivateKey(File keyFile, String keyPassword) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeySpecException,
            InvalidAlgorithmParameterException,
            KeyException, IOException {
        if (keyFile == null) {
            return null;
        }
        return getPrivateKeyFromByteBuffer(PemReader.readPrivateKey(keyFile), keyPassword);
    }

    private static PrivateKey getPrivateKeyFromByteBuffer(ByteBuf encodedKeyBuf, String keyPassword)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            InvalidAlgorithmParameterException, KeyException, IOException {

        byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
        encodedKeyBuf.readBytes(encodedKey).release();

        PKCS8EncodedKeySpec encodedKeySpec = generateKeySpec(
                keyPassword == null ? null : keyPassword.toCharArray(), encodedKey);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException ignore) {
            try {
                return KeyFactory.getInstance("DSA").generatePrivate(encodedKeySpec);
            } catch (InvalidKeySpecException ignore2) {
                try {
                    return KeyFactory.getInstance("EC").generatePrivate(encodedKeySpec);
                } catch (InvalidKeySpecException e) {
                    throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
                }
            }
        }
    }

    static KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChain, PrivateKey key, String keyPassword,
                                                    KeyManagerFactory kmf)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        return buildKeyManagerFactory(certChain, algorithm, key, keyPassword, kmf);
    }

    static KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChainFile,
                                                    String keyAlgorithm, PrivateKey key,
                                                    String keyPassword, KeyManagerFactory kmf)
            throws KeyStoreException, NoSuchAlgorithmException, IOException,
            CertificateException, UnrecoverableKeyException {
        char[] keyPasswordChars = keyPassword == null ? EmptyArrays.EMPTY_CHARS : keyPassword.toCharArray();
        KeyStore ks = buildKeyStore(certChainFile, key, keyPasswordChars);
        // Set up key manager factory to use our key store
        if (kmf == null) {
            kmf = KeyManagerFactory.getInstance(keyAlgorithm);
        }
        kmf.init(ks, keyPasswordChars);

        return kmf;
    }

    static KeyStore buildKeyStore(X509Certificate[] certChain, PrivateKey key, char[] keyPasswordChars)
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        KeyStore ks = KeyStore.getInstance("BKS");
        ks.load(null, null);
        ks.setKeyEntry("key", key, keyPasswordChars, certChain);
        return ks;
    }

    /**
     * Build a {@link TrustManagerFactory} from a certificate chain file.
     *
     * @param certChainFile       The certificate file to build from.
     * @param trustManagerFactory The existing {@link TrustManagerFactory} that will be used if not {@code null}.
     * @return A {@link TrustManagerFactory} which contains the certificates in {@code certChainFile}
     */
    @Deprecated
    protected static TrustManagerFactory buildTrustManagerFactory(
            File certChainFile, TrustManagerFactory trustManagerFactory)
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        X509Certificate[] x509Certs = toX509Certificates(certChainFile);

        return buildTrustManagerFactory(x509Certs, trustManagerFactory);
    }

    static X509Certificate[] toX509CertificatesInternal(File file) throws SSLException {
        try {
            return toX509Certificates(file);
        } catch (CertificateException e) {
            throw new SSLException(e);
        }
    }

    static X509Certificate[] toX509Certificates(File file) throws CertificateException {
        if (file == null) {
            return null;
        }
        return getCertificatesFromBuffers(PemReader.readCertificates(file));
    }

    static X509Certificate[] toX509Certificates(InputStream in) throws CertificateException {
        if (in == null) {
            return null;
        }
        return getCertificatesFromBuffers(PemReader.readCertificates(in));
    }

    private static X509Certificate[] getCertificatesFromBuffers(ByteBuf[] certs) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate[] x509Certs = new X509Certificate[certs.length];

        int i = 0;
        try {
            for (; i < certs.length; i++) {
                InputStream is = new ByteBufInputStream(certs[i], true);
                try {
                    x509Certs[i] = (X509Certificate) cf.generateCertificate(is);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // This is not expected to happen, but re-throw in case it does.
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            for (; i < certs.length; i++) {
                certs[i].release();
            }
        }
        return x509Certs;
    }

    static TrustManagerFactory buildTrustManagerFactory(
            X509Certificate[] certCollection, TrustManagerFactory trustManagerFactory)
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        KeyStore ks = KeyStore.getInstance("BKS");
        ks.load(null, null);

        int i = 1;
        for (X509Certificate cert : certCollection) {
            String alias = Integer.toString(i);
            ks.setCertificateEntry(alias, cert);
            i++;
        }

        // Set up trust manager factory to use our key store.
        if (trustManagerFactory == null) {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        }
        trustManagerFactory.init(ks);

        return trustManagerFactory;
    }

    public static SSLContext newSSLContext(Provider sslContextProvider, X509Certificate[] trustCertCollection,
                                           TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain,
                                           PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory,
                                           long sessionCacheSize, long sessionTimeout)
            throws SSLException {
        if (key == null && keyManagerFactory == null) {
            throw new NullPointerException("key, keyManagerFactory");
        }

        try {
            if (trustCertCollection != null) {
                trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory);
            }
            if (key != null) {
                keyManagerFactory = buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory);
            }

            // Initialize the SSLContext to work with our key managers.
            SSLContext ctx = SSLContext.getInstance(PROTOCOL);
//            SSLContext ctx = SSLContext.getInstance("SSL", sslContextProvider);
            ctx.init(null, null, null);
            SSLSessionContext sessCtx = ctx.getServerSessionContext();
            if (sessionCacheSize > 0) {
                sessCtx.setSessionCacheSize((int) Math.min(sessionCacheSize, Integer.MAX_VALUE));
            }
            if (sessionTimeout > 0) {
                sessCtx.setSessionTimeout((int) Math.min(sessionTimeout, Integer.MAX_VALUE));
            }
            return ctx;
        } catch (Exception e) {
            if (e instanceof SSLException) {
                throw (SSLException) e;
            }
            throw new SSLException("failed to initialize the server-side SSL context", e);
        }
    }
}

/**
 * Reads a PEM file and converts it into a list of DERs so that they are imported into a {@link KeyStore} easily.
 */
final class PemReader {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PemReader.class);

    private static final Pattern CERT_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" + // Header
                    "([a-z0-9+/=\\r\\n]+)" +                    // Base64 text
                    "-+END\\s+.*CERTIFICATE[^-]*-+",            // Footer
            Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" + // Header
                    "([a-z0-9+/=\\r\\n]+)" +                       // Base64 text
                    "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+",            // Footer
            Pattern.CASE_INSENSITIVE);

    static ByteBuf[] readCertificates(File file) throws CertificateException {
        try {
            InputStream in = new FileInputStream(file);

            try {
                return readCertificates(in);
            } finally {
                safeClose(in);
            }
        } catch (FileNotFoundException e) {
            throw new CertificateException("could not find certificate file: " + file);
        }
    }

    static ByteBuf[] readCertificates(InputStream in) throws CertificateException {
        String content;
        try {
            content = readContent(in);
        } catch (IOException e) {
            throw new CertificateException("failed to read certificate input stream", e);
        }

        List<ByteBuf> certs = new ArrayList<ByteBuf>();
        Matcher m = CERT_PATTERN.matcher(content);
        int start = 0;
        for (; ; ) {
            if (!m.find(start)) {
                break;
            }

            ByteBuf base64 = Unpooled.copiedBuffer(m.group(1), CharsetUtil.US_ASCII);
            ByteBuf der = Base64.decode(base64);
            base64.release();
            certs.add(der);

            start = m.end();
        }

        if (certs.isEmpty()) {
            throw new CertificateException("found no certificates in input stream");
        }

        return certs.toArray(new ByteBuf[certs.size()]);
    }

    static ByteBuf readPrivateKey(File file) throws KeyException {
        try {
            InputStream in = new FileInputStream(file);

            try {
                return readPrivateKey(in);
            } finally {
                safeClose(in);
            }
        } catch (FileNotFoundException e) {
            throw new KeyException("could not find key file: " + file);
        }
    }

    static ByteBuf readPrivateKey(InputStream in) throws KeyException {
        String content;
        try {
            content = readContent(in);
        } catch (IOException e) {
            throw new KeyException("failed to read key input stream", e);
        }

        Matcher m = KEY_PATTERN.matcher(content);
        if (!m.find()) {
            throw new KeyException("could not find a PKCS #8 private key in input stream" +
                    " (see http://netty.io/wiki/sslcontextbuilder-and-private-key.html for more information)");
        }

        ByteBuf base64 = Unpooled.copiedBuffer(m.group(1), CharsetUtil.US_ASCII);
        ByteBuf der = Base64.decode(base64);
        base64.release();
        return der;
    }

    private static String readContent(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[8192];
            for (; ; ) {
                int ret = in.read(buf);
                if (ret < 0) {
                    break;
                }
                out.write(buf, 0, ret);
            }
            return out.toString(CharsetUtil.US_ASCII.name());
        } finally {
            safeClose(out);
        }
    }

    private static void safeClose(InputStream in) {
        try {
            in.close();
        } catch (IOException e) {
            logger.warn("Failed to close a stream.", e);
        }
    }

    private static void safeClose(OutputStream out) {
        try {
            out.close();
        } catch (IOException e) {
            logger.warn("Failed to close a stream.", e);
        }
    }

    private PemReader() {
    }
}
