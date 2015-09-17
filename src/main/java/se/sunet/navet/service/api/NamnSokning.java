package se.sunet.navet.service.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import generated.FolkbokforingspostTYPE;
import generated.FolkbokforingsposterTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;
import se.sunet.navet.service.api.exceptions.RestException;
import se.sunet.navet.service.navetclient.NamnSokningService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lundberg on 2015-09-17.
 */


@Path("/namnsokning")
@Produces(MediaType.APPLICATION_JSON)
public class NamnSokning {

    private final Logger slf4jLogger = LoggerFactory.getLogger(PersonPost.class);
    private final Gson gson = new Gson();
    private final NamnSokningService service;

    public NamnSokning() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
        String wsBaseEndpoint = System.getProperty("se.sunet.navet.service.wsBaseEndpoint");
        String organisationNumber = System.getProperty("se.sunet.navet.service.organisationNumber");
        String orderIdentity = System.getProperty("se.sunet.navet.service.orderIdentity");
        slf4jLogger.info(wsBaseEndpoint + ' ' + organisationNumber + ' ' + orderIdentity);
        this.service = new NamnSokningService(wsBaseEndpoint, organisationNumber, orderIdentity);
    }

    public static class Request {
        private String given_name;
        private String surname;
        private String city;

        public Request(String given_name, String surname, String city) {
            this.given_name = given_name;
            this.surname = surname;
            this.city = city;
        }

        public String getGiven_name() {
            if (given_name == null) {
                return "";
            }
            return given_name;
        }

        public String getSurname() {
            if (surname == null) {
                return "";
            }
            return surname;
        }

        public String getCity() {
            if (city == null) {
                return "";
            }
            return city;
        }

        public void validate() throws WebApplicationException {
            if (this.given_name == null && this.surname == null && this.city == null) {
                throw new RestException(javax.ws.rs.core.Response.Status.BAD_REQUEST, "You need to supply at least one of the following values: \"given_name\": \"<string>\", \"surname\": \"<string>\" or \"city\": \"<string>\"");
            }
        }
    }

    public static class Response {
        List<NavetNotification.Response.PopulationItem.PersonItem> PersonItems = new ArrayList<>();

        public Response(ResponseXMLTYPE data) {
            FolkbokforingsposterTYPE posts = data.getFolkbokforingsposter();
            for (FolkbokforingspostTYPE post: posts.getFolkbokforingspost()) {
                NavetNotification.Response.PopulationItem.PersonItem personItem = new NavetNotification.Response.PopulationItem.PersonItem();
                personItem.setAll(post.getPersonpost());
                personItem.setRelations(null);  // namnsokning does not return relations
                PersonItems.add(personItem);
            }
        }

        public List<NavetNotification.Response.PopulationItem.PersonItem> getPersonItems() {
            return PersonItems;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String doNameSearch(String json) {
        try {
            Request request = gson.fromJson(json, Request.class);
            slf4jLogger.info("API personsokning request received");
            request.validate();
            ResponseXMLTYPE status = service.getData(request.getGiven_name(), request.getSurname(), request.getCity());
            slf4jLogger.info("navetclient response received");
            Response response = new Response(status);
            slf4jLogger.info("API namsokning response created");
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
            slf4jLogger.error("Could not return NamnSokning.Response", e);
            throw new RestException(e);
        }
    }
}