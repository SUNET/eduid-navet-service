package se.sunet.navet.service.navetclient;

import org.testng.annotations.Test;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by lundberg on 2015-09-16.
 */
public class PersonPostServiceTest extends SetupCommon {
    @Test
    public void testSendSecureMessage() throws Exception {
        PersonPostService personPostService = new PersonPostService(WS_BASE_ENDPOINT, ORG_NR, ORDER_ID);

        ResponseXMLTYPE result = personPostService.getData(TEST_PERSON_NIN);

        assertNotNull(result.getFolkbokforingsposter());
        assertNotNull(result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost());
        assertEquals(result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost().getPersonId().getPersonNr().getValue().toString(),
                TEST_PERSON_NIN);
    }
}
