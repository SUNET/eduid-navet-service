package se.sunet.navet.service.api;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import se.sunet.navet.service.server.EmbeddedServer;

/**
 * Created by lundberg on 2015-09-16.
 */
public class SetupCommon {

    public EmbeddedServer embeddedServer = new EmbeddedServer();
    public String configFile = "./src/test/resources/navet-service.properties";
    public static final String TEST_PERSON_NIN = "196608253081";
    public static final String TEST_PERSON_FAILING_NIN = "197601020303";
    public static final String TEST_PERSON_REF_NIN = "196709132887";
    public static final String TEST_PERSON_GIVEN_NAME = "Teofil";
    public static final String TEST_PERSON_FAILING_GIVEN_NAME = "Mupp";
    public static final String TEST_PERSON_DEREGISTERED_NIN = "195003072260";

    @BeforeClass
    public void setUp() throws Exception {
        setupServer();
    }

    @AfterClass
    public void tearDown() throws Exception {
        tearDownServer();
    }

    public void setupServer() throws Exception {
        this.embeddedServer.setup(configFile);
        this.embeddedServer.start();
    }

    public void tearDownServer() throws Exception {
        this.embeddedServer.stop();
    }
}
