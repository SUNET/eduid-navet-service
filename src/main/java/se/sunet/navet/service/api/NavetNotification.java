package se.sunet.navet.service.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;
import se.sunet.navet.service.api.exceptions.RestException;
import se.sunet.navet.service.navetclient.PersonPostService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lundberg on 2015-09-11.
 *
 * This is the full response received from Navet. For most usage the PersonPost should be enough.
 */

@Path("/personpost/navetnotification")
@Produces(MediaType.APPLICATION_JSON)
public class NavetNotification {

    private final Logger slf4jLogger = LoggerFactory.getLogger(NavetNotification.class);
    private final Gson gson = new Gson();
    private final PersonPostService service;

    public NavetNotification() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
        String wsBaseEndpoint = System.getProperty("se.sunet.navet.service.wsBaseEndpoint");
        String organisationNumber = System.getProperty("se.sunet.navet.service.organisationNumber");
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
                // Sekretessmarkering
                if (post.getSekretessmarkering() != null) {
                    PopulationItem.SecrecyMark = post.getSekretessmarkering().getValue().toString();
                }
                // SkyddadFolkbokforing
                if (post.getSkyddadFolkbokforing() != null) {
                    PopulationItem.ProtectedPopulationRegistration = post.getSkyddadFolkbokforing().getValue().toString();
                }

                // Add more data?
                /*
                personPost.getCivilstand();
                personPost.getFodelse();
                personPost.getFolkbokforing();
                personPost.getInvandring();
                personPost.getMedborgarskap();
                */
                this.PopulationItems.add(PopulationItem);
            }
        }

        public static class PopulationItem {

            private CaseInformation CaseInformation = new CaseInformation();
            private String SecrecyMark = "";
            private String ProtectedPopulationRegistration = "";
            private PersonItem PersonItem = new PersonItem();

            public static class CaseInformation {
                private String lastChanged;

                public void setAll(FolkbokforingspostTYPE.Arendeuppgift arendeuppgift) {
                    if (arendeuppgift != null) {
                        this.setLastChanged(arendeuppgift.getAndringstidpunkt().toString());
                    }
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
                private DeregistrationInformation DeregistrationInformation = new DeregistrationInformation();
                private String ReferenceNationalIdentityNumber = "";
                private Name Name = new Name();
                private PostalAddresses PostalAddresses = new PostalAddresses();
                List<Relation> Relations = new ArrayList<>();

                public void setAll(PersonpostTYPE personPost) {
                    // NationalIdentityNumber (CoOrdinationNumber if it is received)
                    this.PersonId.setAll(personPost.getPersonId());

                    // ReferenceNationalIdentityNumber
                    JAXBElement<Long> refNin = personPost.getHanvisningsPersonNr();
                    if (refNin != null) {
                        this.setReferenceNationalIdentityNumber(refNin.getValue());
                    }

                    // Deregistration information
                    this.DeregistrationInformation.setAll(personPost.getAvregistrering());

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
                        for (RelationerTYPE.Relation relationData: relations.getRelation()) {
                            Relation relation = new Relation();
                            relation.setAll(relationData);
                            Relations.add(relation);
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
                        JAXBElement<Long> tilldelatPersonNrSamordningsNr = personId.getTilldelatPersonNrSamordningsNr();
                        if (tilldelatPersonNrSamordningsNr != null) {
                            this.setCoOrdinationNumber(tilldelatPersonNrSamordningsNr.getValue());
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

                public static class DeregistrationInformation {
                    private String date;
                    // AV = Avliden
                    // UV = Utvandrad
                    // GN = Gammalt personnummer
                    // AN = Annan anledning
                    // TA = Tekniskt avregistrerad
                    // OB = Försvunnen
                    // FI = Falsk identitet
                    private String causeCode;

                    public void setAll(AvregistreringTYPE deregistration) {
                        if (deregistration != null) {
                            date = deregistration.getAvregistreringsdatum().getValue().toString();
                            causeCode = deregistration.getAvregistreringsorsakKod().getValue().toString();
                        }
                    }

                    public String getDate() { return date; }

                    public String getCauseCode() { return causeCode; }
                }

                public static class Name {
                    private String GivenNameMarking;
                    private String GivenName;
                    private String MiddleName;
                    private String Surname;
                    private String NotificationName;

                    public void setAll(NamnTYPE name) {
                        // GivenNameMarking
                        JAXBElement<Integer> givenNameMarkingElement = name.getTilltalsnamnsmarkering();
                        if (givenNameMarkingElement != null) {
                            this.setGivenNameMarking(givenNameMarkingElement.getValue());
                        }
                        // GivenName
                        JAXBElement<String> givenNameElement = name.getFornamn();
                        if (givenNameElement != null) {
                            this.setGivenName(givenNameElement.getValue());
                        }
                        // MiddleName
                        JAXBElement<String> middleNameElement = name.getMellannamn();
                        if (middleNameElement != null) {
                            this.setMiddleName(middleNameElement.getValue());
                        }
                        // Surname
                        JAXBElement<String> surNameElement = name.getEfternamn();
                        if (surNameElement != null) {
                            this.setSurname(surNameElement.getValue());
                        }
                        // NotificationName
                        // created by SKV to be maximum 36 characters long
                        JAXBElement<String> notificationNameElement = name.getAviseringsnamn();
                        if (notificationNameElement != null) {
                            this.setNotificationName(notificationNameElement.getValue());
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

                    public String getSurname() {
                        return Surname;
                    }

                    public void setSurname(String surname) {
                        Surname = surname;
                    }

                    public String getNotificationName() {
                        return NotificationName;
                    }

                    public void setNotificationName(String notificationName) {
                        NotificationName = notificationName;
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
                    private Name Name;
                    private String RelationStartDate;
                    private String RelationEndDate;
                    private RelationId RelationId = new RelationId();
                    private String RelationType;
                    private String Status;

                    public void setAll(RelationerTYPE.Relation relation) {
                        // Relation name
                        NamnTYPE relationName = relation.getNamn();
                        if (relationName != null) {
                            this.Name = new PersonItem.Name();
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
                        // RelationId
                        RelationPersonIdTYPE relationId = relation.getRelationId();
                        if (relationId != null) {
                            RelationId.setAll(relationId);
                        }
                        // RelationType
                        RelationstypTYPE relationType = relation.getRelationstyp();
                        if (relationType != null) {
                            this.setRelationType(relationType.value());
                        }
                        // Status
                        this.setStatus(relation.getStatus());
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
                            // BirthTimeNumber
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

                    public String getStatus() {
                        return Status;
                    }

                    public void setStatus(String status) {
                        Status = status;
                    }

                    public String getRelationType() {
                        return RelationType;
                    }

                    public void setRelationType(String relationType) {
                        RelationType = relationType;
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

                public PopulationItem.PersonItem.PersonId getPersonId() {
                    return PersonId;
                }

                public void setPersonId(PopulationItem.PersonItem.PersonId personId) { PersonId = personId; }

                public String getReferenceNationalIdentityNumber() { return ReferenceNationalIdentityNumber; }

                public void setReferenceNationalIdentityNumber(Long refNin) {
                    ReferenceNationalIdentityNumber = refNin.toString();
                }

                public PopulationItem.PersonItem.DeregistrationInformation getDeregistrationInformation() {
                    return DeregistrationInformation;
                }

                public PopulationItem.PersonItem.Name getName() {
                    return Name;
                }

                public void setName(PopulationItem.PersonItem.Name name) {
                    Name = name;
                }

                public PopulationItem.PersonItem.PostalAddresses getPostalAddresses() {
                    return PostalAddresses;
                }

                public void setPostalAddresses(PopulationItem.PersonItem.PostalAddresses postalAddresses) {
                    PostalAddresses = postalAddresses;
                }

                public List<Relation> getRelations() {
                    return Relations;
                }

                public void setRelations(List<Relation> relations) {
                    Relations = relations;
                }
            }

            public PopulationItem.CaseInformation getCaseInformation() {
                return CaseInformation;
            }

            public void setCaseInformation(PopulationItem.CaseInformation caseInformation) {
                CaseInformation = caseInformation;
            }
            public String getSecrecyMark () {
                return SecrecyMark;
            }

            public String ProtectedPopulationRegistration () {
                return ProtectedPopulationRegistration;
            }

            public PopulationItem.PersonItem getPersonItem() {
                return PersonItem;
            }

            public void setPersonItem(PopulationItem.PersonItem personItem) {
                PersonItem = personItem;
            }
        }

        public List<PopulationItem> getPopulationItems() {
            return PopulationItems;
        }

        public void setPopulationItems(List<PopulationItem> populationItems) {
            PopulationItems = populationItems;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String getNavetNotification(String json) {
        try {
            Request request = gson.fromJson(json, Request.class);
            slf4jLogger.info("API navetnotification request received");
            request.validate();
            ResponseXMLTYPE data = service.getData(request.getIdentityNumber());
            slf4jLogger.info("navetclient response received");
            Response response = new Response(data);
            slf4jLogger.info("API navetnotification response created");
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
            slf4jLogger.error("Could not return NavetNotification.Response", e);
            throw new RestException(e);
        }
    }
}

