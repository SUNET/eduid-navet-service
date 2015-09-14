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
 */


@Path("/personpost/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonPost {

    private final Logger slf4jLogger = LoggerFactory.getLogger(PersonPost.class);
    private final Gson gson = new Gson();
    private final PersonPostService service;

    public PersonPost() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
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

    /*
 public static class Response {


     private Boolean SenderAccepted;
     private AccountStatus AccountStatus = new AccountStatus();

     public Response(Boolean senderAccepted, String recipient, String accountStatusType, String serviceAddress) {
         this.setSenderAccepted(senderAccepted);
         this.AccountStatus.setRecipient(recipient);
         this.AccountStatus.setType(accountStatusType);
         this.AccountStatus.ServiceSupplier.setServiceAddress(serviceAddress);
     }

     public Response(ReachabilityStatus status) {
         this.setSenderAccepted(status.isSenderAccepted());
         this.AccountStatus.setRecipient(status.getAccountStatus().getRecipientId());
         this.AccountStatus.setType(status.getAccountStatus().getType().value());
         if (status.getAccountStatus().getServiceSupplier() != null) {
             this.AccountStatus.ServiceSupplier.setServiceAddress(
                     status.getAccountStatus().getServiceSupplier().getServiceAdress());
         }
     }

     public static class AccountStatus {
         private String RecipientId;
         private String Type;
         private ServiceSupplier ServiceSupplier = new ServiceSupplier();

         public static class ServiceSupplier {
             private String ServiceAddress = "";

             public void setServiceAddress(String serviceAddress) {
                 ServiceAddress = serviceAddress;
             }

             public String getServiceAddress() {
                 return ServiceAddress;
             }
         }

         public void setType(String type) {
             Type = type;
         }

         public String getType() {
             return Type;
         }

         public String getRecipient() {
             return RecipientId;
         }

         public void setRecipient(String recipient) {
             this.RecipientId = recipient;
         }

         public Response.AccountStatus.ServiceSupplier getServiceSupplier() {
             return ServiceSupplier;
         }
     }

     public void setSenderAccepted(Boolean senderAccepted) {
         SenderAccepted = senderAccepted;
     }

     public Boolean getSenderAccepted() {
         return SenderAccepted;
     }

     public Response.AccountStatus getAccountStatus() {
         return AccountStatus;
     }
 }
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String isReachable(String json) {
        try {
            Request request = gson.fromJson(json, Request.class);
            slf4jLogger.info("API personpost request received");
            request.validate();
            ResponseXMLTYPE status = service.getData(request.getIdentityNumber());
            slf4jLogger.info("navetclient response received");
            //Response response = new Response(status);
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