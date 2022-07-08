package se.sunet.navet.service.api;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Server;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;

/**
 * Created by lundberg on 2015-09-16.
 */
public class NavetNotificationTest extends SetupCommon {
    private final Gson gson = new Gson();

    @Test
    public void testNINMatch() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/personpost/navetnotification";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);

        HashMap<String, String> data = new HashMap<>();
        data.put("identity_number", TEST_PERSON_NIN);

        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);

        NavetNotification.Response jsonResponse = gson.fromJson(response.readEntity(String.class), NavetNotification.Response.class);

        assertNotNull(jsonResponse.getPopulationItems().get(0).getPersonItem());
        assertEquals(jsonResponse.getPopulationItems().get(0).getPersonItem().getPersonId().getNationalIdentityNumber(), TEST_PERSON_NIN);
        assertNotNull(jsonResponse.getPopulationItems().get(0).getPersonItem().getPostalAddresses());
        assertNotNull(jsonResponse.getPopulationItems().get(0).getPersonItem().getName());
        assertNotNull(jsonResponse.getPopulationItems().get(0).getPersonItem().getRelations());
        assertNotNull(jsonResponse.getPopulationItems().get(0).getCaseInformation());
        assertNull(jsonResponse.getPopulationItems().get(0).getPersonItem().getDeregistrationInformation().getCauseCode());
        assertNull(jsonResponse.getPopulationItems().get(0).getPersonItem().getDeregistrationInformation().getDate());
    }

    @Test
    public void testNINNoMatch() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/personpost/navetnotification";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);

        HashMap<String, String> data = new HashMap<>();
        data.put("identity_number", TEST_PERSON_FAILING_NIN);

        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);

        NavetNotification.Response jsonResponse = gson.fromJson(response.readEntity(String.class), NavetNotification.Response.class);

        assertTrue(jsonResponse.getPopulationItems().isEmpty());
    }

    @Test
    public void testNINDeregistered() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/personpost/navetnotification";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);

        HashMap<String, String> data = new HashMap<>();
        data.put("identity_number", TEST_PERSON_DEREGISTERED_NIN);

        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);

        NavetNotification.Response jsonResponse = gson.fromJson(response.readEntity(String.class), NavetNotification.Response.class);

        assertNotNull(jsonResponse.getPopulationItems().get(0).getPersonItem());
        assertEquals(jsonResponse.getPopulationItems().get(0).getPersonItem().getPersonId().getNationalIdentityNumber(), TEST_PERSON_DEREGISTERED_NIN);
        NavetNotification.Response.PopulationItem.PersonItem.DeregistrationInformation deregistrationInformation = jsonResponse.getPopulationItems().get(0).getPersonItem().getDeregistrationInformation();
        assertEquals(deregistrationInformation.getCauseCode(), "UV");
        assertEquals(deregistrationInformation.getDate(), "19980810");
    }
}
