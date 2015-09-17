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
import static org.testng.Assert.assertNull;

/**
 * Created by lundberg on 2015-09-16.
 */
public class PersonPostTest extends SetupCommon {
    private final Gson gson = new Gson();

    @Test
    public void testNINMatch() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/personpost/person";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);

        HashMap<String, String> data = new HashMap<>();
        data.put("identity_number", TEST_PERSON_NIN);

        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);

        PersonPost.Response jsonResponse = gson.fromJson(response.readEntity(String.class), PersonPost.Response.class);

        assertNotNull(jsonResponse.getPersonItem());
        assertEquals(jsonResponse.getPersonItem().getPersonId().getNationalIdentityNumber(), TEST_PERSON_NIN);
        assertNotNull(jsonResponse.getPersonItem().getPostalAddresses());
        assertNotNull(jsonResponse.getPersonItem().getName());
        assertNotNull(jsonResponse.getPersonItem().getRelations());
    }

    @Test
    public void testNINNoMatch() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/personpost/person";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);

        HashMap<String, String> data = new HashMap<>();
        data.put("identity_number", TEST_PERSON_FAILING_NIN);

        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);

        PersonPost.Response jsonResponse = gson.fromJson(response.readEntity(String.class), PersonPost.Response.class);

        assertNull(jsonResponse.getPersonItem());
    }
}
