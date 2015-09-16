package se.sunet.navet.service.navetclient;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skatteverket.folkbokforing.na.personpostxml.v2.NaWebServiceException;
import se.skatteverket.folkbokforing.na.personpostxml.v2.PersonpostXMLService;
import se.skatteverket.folkbokforing.na.personpostxml.v2.PersonpostXMLInterface;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.BestallningTYPE;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.PersonpostRequestTYPE;
import se.skatteverket.xmls.se.skatteverket.folkbokforing.na.epersondata.v1.ResponseXMLTYPE;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.xml.ws.BindingProvider;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by lundberg on 2015-09-09.
 */
public class PersonPostService  {

    private final Logger slf4jLogger = LoggerFactory.getLogger(PersonPostService.class);
    private PersonpostXMLService svc = new PersonpostXMLService();
    private KeyStore keyStore;
    private KeyManager[] keyManagers;
    private String organisationNumber;
    private String orderId;
    private String wsBaseEndpoint;
    private static String serviceName = "V2/personpostXML";

    public PersonPostService(String wsBaseEndpoint, String orgNum, String orderId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
        this.wsBaseEndpoint = wsBaseEndpoint;
        this.organisationNumber = orgNum;
        this.orderId = orderId;
        this.keyStore = KeyStore.getInstance(System.getProperty("javax.net.ssl.keyStoreType"));
        this.keyStore.load(new FileInputStream(System.getProperty("javax.net.ssl.keyStore")), System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
        this.keyManagers = getKeyManagers();
        slf4jLogger.info("PersonPostService initialized");
    }

    private PersonpostXMLInterface getPort() {
        PersonpostXMLInterface port = svc.getPersonpostXML();
        String serviceEndpoint = String.format("%s/%s", wsBaseEndpoint, serviceName);
        // This binding needs to be first or the conduit modifications are lost
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceEndpoint);
        // Setup tls and keyStore stuff
        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setKeyManagers(keyManagers);
        HTTPConduit conduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
        conduit.setTlsClientParameters(tlsClientParameters);
        return port;
    }

    private PersonpostRequestTYPE getRequest(String nationalIdentityNumber)  {
        PersonpostRequestTYPE ppr = new PersonpostRequestTYPE();
        BestallningTYPE order = new BestallningTYPE();
        Long orgNum = new Long(this.organisationNumber);
        order.setOrgNr(orgNum);
        order.setBestallningsId(this.orderId);
        ppr.setBestallning(order);
        Long nin = new Long(nationalIdentityNumber);
        ppr.setPersonId(nin);
        return ppr;
    }

    private KeyManager[] getKeyManagers() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        String algorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
        factory.init(keyStore, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
        return factory.getKeyManagers();
    }

    public ResponseXMLTYPE getData(String nationalIdentityNumber) throws NaWebServiceException {
        PersonpostXMLInterface port = getPort();
        PersonpostRequestTYPE request = getRequest(nationalIdentityNumber);
        return port.getData(request);
    }

}
