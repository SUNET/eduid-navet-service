package se.sunet.navet.service.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.PersonpostRequestTYPE;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;
import se.sunet.navet.service.api.exceptions.RestException;
import se.sunet.navet.service.navetclient.PersonPostService;

import javax.management.relation.RelationType;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
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

        List<PopulationItem> PopulationItems = new ArrayList<>();
        
        public Response(ResponseXMLTYPE data) {
            FolkbokforingsposterTYPE posts = data.getFolkbokforingsposter();
            for (FolkbokforingspostTYPE post: posts.getFolkbokforingspost()) {
                PopulationItem PopulationItem = new PopulationItem();
                // Arendeuppgift
                PopulationItem.CaseInformation.setAll(post.getArendeuppgift());
                // PersonPost
                PersonpostTYPE personPost = post.getPersonpost();
                PopulationItem.PersonItem.setAll(personPost);

                // Add more data here

                personPost.getAdresser();
                personPost.getAvregistrering();
                personPost.getCivilstand();
                personPost.getFodelse();
                personPost.getFolkbokforing();
                personPost.getHanvisningsPersonNr();
                personPost.getInvandring();
                personPost.getNamn();
                personPost.getPersonId().getPersonNr();
                personPost.getRelationer();
                personPost.getMedborgarskap();
                this.PopulationItems.add(PopulationItem);
            }
        }

        public static class PopulationItem {

            private CaseInformation CaseInformation = new CaseInformation();
            private PersonItem PersonItem = new PersonItem();

            public static class CaseInformation {
                private String lastChanged;

                public void setAll(FolkbokforingspostTYPE.Arendeuppgift arendeuppgift) {
                    this.setLastChanged(arendeuppgift.getAndringstidpunkt().toString());
                }

                public String getLastChanged() {
                    return lastChanged;
                }

                public void setLastChanged(String lastChanged) {
                    this.lastChanged = lastChanged;
                }
            }

            public static class PersonItem {
                private PersonId PersonId = new PersonId();
                private Name Name = new Name();
                private PostalAddresses PostalAddresses = new PostalAddresses();
                List<Relation> Relations = new ArrayList<>();

                public void setAll(PersonpostTYPE personPost) {
                    // NationalIdentityNumber
                    this.PersonId.setAll(personPost.getPersonId());

                    // Name
                    NamnTYPE namn = personPost.getNamn();
                    if (namn != null) {
                        this.Name.setAll(namn);
                    }

                    // PostalAddresses
                    AdressTYPE adresser = personPost.getAdresser();
                    if (adresser != null) {
                        this.PostalAddresses.setAll(adresser);
                    }
                    // Relations
                    RelationerTYPE relations = personPost.getRelationer();
                    if (relations != null) {
                        for (RelationerTYPE.Relation relation: relations.getRelation()) {
                            NamnTYPE name = relation.getNamn();
                            JAXBElement<Integer> relationStartDate = relation.getRelationFromdatum();
                            JAXBElement<Integer> relationEndDate = relation.getRelationTomdatum();
                            RelationPersonIdTYPE relationId = relation.getRelationId();
                            RelationstypTYPE relationshipType = relation.getRelationstyp();
                            String status = relation.getStatus();
                        }
                    }
                }

                public static class PersonId {
                    private String NationalIdentityNumber;
                    private String CoOrdinationNumber;

                    public void setAll(PersonIdTYPE personId) {
                        // NationalIdentityNumber
                        JAXBElement<Long> personNr = personId.getPersonNr();
                        if (personNr != null) {
                            this.setNationalIdentityNumber(personNr.getValue());
                        }
                        // CoOrdinationNumber
                        Long tilldelatPersonNrSamordningsNr = personId.getTilldelatPersonNrSamordningsNr();
                        if (tilldelatPersonNrSamordningsNr != null) {
                            this.setCoOrdinationNumber(tilldelatPersonNrSamordningsNr);
                        }
                    }

                    public String getCoOrdinationNumber() {
                        return CoOrdinationNumber;
                    }

                    public void setCoOrdinationNumber(Long coOrdinationNumber) {
                        CoOrdinationNumber = coOrdinationNumber.toString();
                    }

                    public String getNationalIdentityNumber() {
                        return NationalIdentityNumber;
                    }

                    public void setNationalIdentityNumber(Long nationalIdentityNumber) {
                        NationalIdentityNumber = nationalIdentityNumber.toString();
                    }
                }

                public static class Name {
                    private String GivenNameMarking;
                    private String GivenName;
                    private String MiddleName;
                    private String SurName;

                    public void setAll(NamnTYPE name) {
                        // GivenNameMarking
                        JAXBElement<Integer> givenNameMarkingElement = name.getTilltalsnamnsmarkering();
                        if (givenNameMarkingElement != null) {
                            this.setGivenNameMarking(givenNameMarkingElement.getValue());
                        }
                        // GivenName
                        JAXBElement<NamnTYPE.Fornamn> givenNameElement = name.getFornamn();
                        if (givenNameElement != null) {
                            this.setGivenName(givenNameElement.getValue().getValue());
                        }
                        // MiddleName
                        JAXBElement<NamnTYPE.Mellannamn> middleNameElement = name.getMellannamn();
                        if (middleNameElement != null) {
                            this.setMiddleName(middleNameElement.getValue().getValue());
                        }
                        // SurName
                        JAXBElement<NamnTYPE.Efternamn> surNameElement = name.getEfternamn();
                        if (surNameElement != null) {
                            this.setSurName(surNameElement.getValue().getValue());
                        }

                    }

                    public String getGivenNameMarking() {
                        return GivenNameMarking;
                    }

                    public void setGivenNameMarking(Integer givenNameMarking) {
                        GivenNameMarking = givenNameMarking.toString();
                    }

                    public String getGivenName() {
                        return GivenName;
                    }

                    public void setGivenName(String givenName) {
                        GivenName = givenName;
                    }

                    public String getMiddleName() {
                        return MiddleName;
                    }

                    public void setMiddleName(String middleName) {
                        MiddleName = middleName;
                    }

                    public String getSurName() {
                        return SurName;
                    }

                    public void setSurName(String surName) {
                        SurName = surName;
                    }
                }

                public static class PostalAddresses {
                    OfficialAddress OfficialAddress = new OfficialAddress();

                    public void setAll(AdressTYPE addresses) {
                        SvenskAdressTYPE officialAddress = addresses.getFolkbokforingsadress();
                        if (officialAddress != null) {
                            this.OfficialAddress.setAll(officialAddress);
                        }

                    }

                    public static class OfficialAddress {
                        private String CareOf;
                        private String PostalCode;
                        private String City;
                        private String Address1;
                        private String Address2;

                        public void setAll(SvenskAdressTYPE officialAddress) {
                            // CareOf
                            JAXBElement<String> careOf = officialAddress.getCareOf();
                            if (careOf != null) {
                                this.setCareOf(careOf.getValue());
                            }
                            // PostalCode
                            JAXBElement<Integer> postalCode = officialAddress.getPostNr();
                            if (postalCode != null) {
                                this.setPostalCode(postalCode.getValue());
                            }
                            // City
                            JAXBElement<String> city = officialAddress.getPostort();
                            if (city != null) {
                                this.setCity(city.getValue());
                            }
                            // Address1
                            JAXBElement<String> address1 = officialAddress.getUtdelningsadress1();
                            if (address1 != null) {
                                this.setAddress1(address1.getValue());
                            }
                            // Address2
                            JAXBElement<String> address2 = officialAddress.getUtdelningsadress2();
                            if (address2 != null) {
                                this.setAddress2(address2.getValue());
                            }
                        }

                        public String getAddress1() {
                            return Address1;
                        }

                        public void setAddress1(String address1) {
                            Address1 = address1;
                        }

                        public String getAddress2() {
                            return Address2;
                        }

                        public void setAddress2(String address2) {
                            Address2 = address2;
                        }

                        public String getPostalCode() {
                            return PostalCode;
                        }

                        public void setPostalCode(Integer postalCode) {
                            PostalCode = postalCode.toString();
                        }

                        public String getCity() {
                            return City;
                        }

                        public void setCity(String city) {
                            City = city;
                        }

                        public String getCareOf() {
                            return CareOf;
                        }

                        public void setCareOf(String careOf) {
                            CareOf = careOf;
                        }
                    }

                }

                public static class Relation {
                    private Name Name = new Name();
                    private String RelationStartDate;
                    private String RelationEndDate;
                    private RelationId RelationId = new RelationId();

                    public void setAll(RelationerTYPE.Relation relation) {
                        // Relation name
                        NamnTYPE relationName = relation.getNamn();
                        if (relationName != null) {
                            this.Name.setAll(relationName);
                        }
                        // RelationStartDate
                        JAXBElement<Integer> relationStartDate = relation.getRelationFromdatum();
                        if (relationStartDate != null) {
                            this.setRelationStartDate(relationStartDate.getValue());
                        }
                        // RelationEndDate
                        JAXBElement<Integer> relationEndDate = relation.getRelationTomdatum();
                        if (relationEndDate != null) {
                            this.setRelationEndDate(relationEndDate.getValue());
                        }

                    }

                    public static class RelationId {
                        private String NationalIdentityNumber;
                        private String BirthTimeNumber;

                        public void setAll(RelationPersonIdTYPE relationPersonId) {
                            // NationalIdentityNumber
                            JAXBElement<Long> personNr = relationPersonId.getPersonNr();
                            if (personNr != null) {
                                this.setNationalIdentityNumber(personNr.getValue());
                            }
                            // CoOrdinationNumber
                            JAXBElement<Long> fodelsetidNr = relationPersonId.getFodelsetidNr();
                            if (fodelsetidNr != null) {
                                this.setBirthTimeNumber(fodelsetidNr.getValue());
                            }
                        }

                        public String getBirthTimeNumber() {
                            return BirthTimeNumber;
                        }

                        public void setBirthTimeNumber(Long birthTimeNumber) {
                            BirthTimeNumber = birthTimeNumber.toString();
                        }

                        public String getNationalIdentityNumber() {
                            return NationalIdentityNumber;
                        }

                        public void setNationalIdentityNumber(Long nationalIdentityNumber) {
                            NationalIdentityNumber = nationalIdentityNumber.toString();
                        }
                    }

                    public String getRelationStartDate() {
                        return RelationStartDate;
                    }

                    public void setRelationStartDate(Integer relationStartDate) {
                        RelationStartDate = relationStartDate.toString();
                    }

                    public String getRelationEndDate() {
                        return RelationEndDate;
                    }

                    public void setRelationEndDate(Integer relationEndDate) {
                        RelationEndDate = relationEndDate.toString();
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

