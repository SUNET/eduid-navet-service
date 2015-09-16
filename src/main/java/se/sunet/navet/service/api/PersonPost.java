package se.sunet.navet.service.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import generated.FolkbokforingspostTYPE;
import generated.FolkbokforingsposterTYPE;
import generated.PersonpostTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;
import se.sunet.navet.service.api.exceptions.RestException;
import se.sunet.navet.service.navetclient.PersonPostService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Created by lundberg on 2015-09-11.
 */


@Path("/personpost/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonPost {

    private final Logger slf4jLogger = LoggerFactory.getLogger(PersonPost.class);
    private final Gson gson = new Gson();
    private final PersonPostService service;

    public PersonPost() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
        String wsBaseEndpoint = System.getProperty("se.sunet.navet.service.wsBaseEndpoint");
        String organisationNumber = System.getProperty("se.sunet.navet.service.organisationNumber");
        String orderIdentity = System.getProperty("se.sunet.navet.service.orderIdentity");
        slf4jLogger.info(wsBaseEndpoint + ' ' + organisationNumber + ' ' + orderIdentity);
        this.service = new PersonPostService(wsBaseEndpoint, organisationNumber, orderIdentity);
    }

    public static class Request {
        private String identity_number;

        public Request(String identity_number) {
            this.identity_number = identity_number;
        }

        public String getIdentityNumber() {
            return identity_number;
        }

        public void validate() throws WebApplicationException {
            if (this.identity_number == null) {
                throw new RestException(javax.ws.rs.core.Response.Status.BAD_REQUEST, "Missing \"identity_number\": \"<string>\"");
            }
        }
    }

    public static class Response {
        private NavetNotification.Response.PopulationItem.PersonItem PersonItem;

        public Response(ResponseXMLTYPE data) {
            try {
                PersonpostTYPE personPost = data.getFolkbokforingsposter().getFolkbokforingspost().get(0).getPersonpost();
                this.PersonItem = new NavetNotification.Response.PopulationItem.PersonItem();
                this.PersonItem.setAll(personPost);
            } catch (IndexOutOfBoundsException e) {
                throw new RestException(javax.ws.rs.core.Response.Status.OK, "No match for identity number");
            }
        }

        public NavetNotification.Response.PopulationItem.PersonItem getPersonItem() {
            return PersonItem;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String getPersonPost(String json) {
        try {
            Request request = gson.fromJson(json, Request.class);
            slf4jLogger.info("API personpost request received");
            request.validate();
            ResponseXMLTYPE status = service.getData(request.getIdentityNumber());
            slf4jLogger.info("navetclient response received");
            Response response = new Response(status);
            slf4jLogger.info("API personpost response created");
            return gson.toJson(response);
        } catch (RestException e) {
            throw e;
        } catch (NullPointerException e) {
            String message = "Received empty POST data";
            slf4jLogger.error(message, e);
            throw new RestException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, message);
        } catch (JsonSyntaxException e) {
            slf4jLogger.error(e.getMessage());
            throw new RestException(javax.ws.rs.core.Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            slf4jLogger.error("Could not return PersonPost.Response", e);
            throw new RestException(e);
        }
    }
}