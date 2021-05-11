package se.sunet.navet.service.navetclient;

import org.testng.annotations.Test;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;

import javax.xml.bind.JAXBElement;

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
    @Test
    public void testReferenceNationalIdentityNumber() throws Exception {
        PersonPostService personPostService = new PersonPostService(WS_BASE_ENDPOINT, ORG_NR, ORDER_ID);

        ResponseXMLTYPE current_result = personPostService.getData(TEST_PERSON_REF_NIN);
        JAXBElement<Long> ref_nin = current_result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost().getHanvisningsPersonNr();
        ResponseXMLTYPE ref_result = personPostService.getData(ref_nin.getValue().toString());

        assertNotNull(current_result.getFolkbokforingsposter());
        assertNotNull(ref_result.getFolkbokforingsposter());
        // assert the Personpost points to each other via HanvisningsPersonNr
        assertEquals(current_result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost().getPersonId().getPersonNr().getValue(), ref_result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost().getHanvisningsPersonNr().getValue());
        assertEquals(ref_result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost().getPersonId().getPersonNr().getValue(), current_result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost().getHanvisningsPersonNr().getValue());
    }
}
