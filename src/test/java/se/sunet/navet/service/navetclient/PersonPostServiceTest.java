package se.sunet.navet.service.navetclient;

import generated.PersonIdTYPE;
import generated.PersonpostTYPE;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
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
        PersonpostTYPE personpost = current_result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost();

        // Get reference NIN from Hanvisning element inside PersonId
        String refNin = getHanvisningsPersonNr(personpost);
        assertNotNull(refNin, "Expected a Hanvisning with HanvisningsPersonNr");

        ResponseXMLTYPE ref_result = personPostService.getData(refNin);

        assertNotNull(current_result.getFolkbokforingsposter());
        assertNotNull(ref_result.getFolkbokforingsposter());
        assertEquals(current_result.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost().getPersonId().getPersonNr().getValue().toString(),
                TEST_PERSON_REF_NIN);
    }

    private String getHanvisningsPersonNr(PersonpostTYPE personpost) {
        // Check HanvisningsPersonNr at PersonpostTYPE level first
        JAXBElement<Long> refNin = personpost.getHanvisningsPersonNr();
        if (refNin != null) {
            return refNin.getValue().toString();
        }
        // Fall back to Hanvisning element inside PersonId (newer responses)
        PersonIdTYPE personId = personpost.getPersonId();
        for (Object obj : personId.getAny()) {
            if (obj instanceof Element) {
                Element el = (Element) obj;
                if ("Hanvisning".equals(el.getLocalName())) {
                    return el.getTextContent().trim();
                }
            }
        }
        return null;
    }
}
