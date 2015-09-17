package se.sunet.navet.service.navetclient;

import org.testng.annotations.BeforeTest;

public class SetupCommon  {
    private static final String KEY_STORE_PATH = "./src/test/resources/notarealhost.kommun-a.se.p12";
    private static final String KEY_STORE_PASSWORD = "3129445131123535";
    private static final String TRUST_KEYSTORE_PATH = "./src/test/resources/truststore.jks";
    private static final String TRUST_KEYSTORE_PASSWORD = "abc123";
    public static final String WS_BASE_ENDPOINT = "https://ppx4.skatteverket.se/na/na_epersondata_demo";
    public static final String TEST_PERSON_NIN = "197609272393";
    public static final String TEST_PERSON_GIVEN_NAME = "Teofil";
    public static final String ORG_NR = "162021004748";
    public static final String ORDER_ID = "00000079-FO01-0001";

    @BeforeTest
    public void setUp() throws Exception {
        setupKeystore();
    }

    private void setupKeystore() {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.keyStoreType");

        System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASSWORD);
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

        System.setProperty("javax.net.ssl.trustStore", TRUST_KEYSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUST_KEYSTORE_PASSWORD);
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        //System.setProperty("javax.net.debug", "ssl, handshake, failure");
    }
}
