package se.sunet.navet.service.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import generated.FolkbokforingspostTYPE;
import generated.FolkbokforingsposterTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;
import se.sunet.navet.service.api.exceptions.RestException;
import se.sunet.navet.service.navetclient.PersonPostService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Created by lundberg on 2015-09-11.
 *
 * This is the full response received from Navet. For most usage the PersonPost should be enough.
 */

@Path("/personpost/navetnotification")
@Produces(MediaType.APPLICATION_JSON)
public class NavetNotification {

    private final Logger slf4jLogger = LoggerFactory.getLogger(PersonPost.class);
    private final Gson gson = new Gson();
    private final PersonPostService service;

    public NavetNotification() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
        String wsBaseEndpoint = System.getProperty("se.sunet.mm.service.wsBaseEndpoint");
        String organisationNumber = System.getProperty("se.sunet.mm.service.organisationNumber");
        String orderIdentity = System.getProperty("se.sunet.navet.service.orderIdentity");
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
        
        private PopulationItems PopulationItems = new PopulationItems();
        
        public Response(ResponseXMLTYPE data) {
            FolkbokforingsposterTYPE posts = data.getFolkbokforingsposter();
            for (FolkbokforingspostTYPE post: posts.getFolkbokforingspost()) {
                PopulationItems.PopulationItem PopulationItem = new PopulationItems.PopulationItem();
                PopulationItem.CaseInformation.setLastChanged(post.getArendeuppgift().getAndringstidpunkt().toString());
                // Add more data here
                post.getPersonpost();
                PopulationItems.items.add(PopulationItem);
            }
        }

        public static class PopulationItems {
            List<PopulationItem> items;

            public static class PopulationItem {

                private CaseInformation CaseInformation = new CaseInformation();

                public static class CaseInformation {
                    private String lastChanged;

                    public String getLastChanged() {
                        return lastChanged;
                    }

                    public void setLastChanged(String lastChanged) {
                        this.lastChanged = lastChanged;
                    }
                }

            }
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String isReachable(String json) {
        try {
            Request request = gson.fromJson(json, Request.class);
            slf4jLogger.info("API personpost request received");
            request.validate();
            ResponseXMLTYPE data = service.getData(request.getIdentityNumber());
            slf4jLogger.info("navetclient response received");
            //Response response = new Response(data);
            slf4jLogger.info("API personpost response created");
            return null;
            //return gson.toJson(response);
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
