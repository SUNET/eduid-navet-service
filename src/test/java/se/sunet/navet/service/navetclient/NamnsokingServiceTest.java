package se.sunet.navet.service.navetclient;

import org.testng.annotations.Test;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by lundberg on 2015-09-17.
 */
public class NamnsokingServiceTest extends SetupCommon {
    @Test
    public void testSendSecureMessage() throws Exception {
        NamnsokningService namnsokningService = new NamnsokningService(WS_BASE_ENDPOINT, ORG_NR, ORDER_ID);

        ResponseXMLTYPE result = namnsokningService.getData(TEST_PERSON_GIVEN_NAME, "", "");

        assertNotNull(result.getFolkbokforingsposter());
        assertNotNull(result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost());
        assertEquals(result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost().getPersonId().getPersonNr().getValue().toString(),
                TEST_PERSON_NIN);
    }
}
