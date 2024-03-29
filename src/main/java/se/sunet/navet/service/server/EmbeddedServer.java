package se.sunet.navet.service.server;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

public class EmbeddedServer {

    private final Server server = new Server();

    public EmbeddedServer() {}

    public Server getServer() {
        return this.server;
    }

    private Properties getProperties(String configFile) throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream(configFile);
        prop.load(inputStream);
        return prop;
    }

    private void configureBasicAuth(String realmProperties, String realm, ServletContextHandler servletContext) {
        LoginService loginService = new HashLoginService(realm, realmProperties);
        this.server.addBean(loginService);
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate( true );
        constraint.setRoles(new String[]{"user"});
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/*");
        mapping.setConstraint(constraint);
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);
        // The the servlet handler needs to be chained with the security handler
        security.setHandler(servletContext);
        this.server.setHandler(security);
    }

    private ServletContextHandler getServletContext(String packagesLocation, String rootPath) {
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter(ServerProperties.PROVIDER_PACKAGES, packagesLocation);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(sh, rootPath);
        return context;
    }

    private SslContextFactory.Server getSslContextFactory(String keyStorePath, String keyStorePassword, String keyManagerPassword) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyManagerPassword(keyManagerPassword);
        return sslContextFactory;
    }

    private void configureSslServer(String host, Integer port, SslContextFactory.Server sslContextFactory) {
        HttpConfiguration https_config = new HttpConfiguration();
        https_config.setSecureScheme("https");
        https_config.setSecurePort(port);
        https_config.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(https_config);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory,"http/1.1");
        ServerConnector https = new ServerConnector(this.server, sslConnectionFactory, httpConnectionFactory);
        https.setHost(host);
        https.setPort(port);
        this.server.addConnector(https);
    }

    private void setupNavetClientStores(String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword) {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.keyStoreType");

        System.setProperty("javax.net.ssl.keyStore", keyStorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    public void setup(String configFile) throws Exception {
        // Get configuration
        Properties prop = getProperties(configFile);

        String host = prop.getProperty("host", "localhost");
        Integer port = Integer.parseInt(prop.getProperty("port", "8080"));
        String packagesLocation = prop.getProperty("apiPackagesPath", "se.sunet.navet.service.api");
        String rootPath = prop.getProperty("apiRootPath", "/*");
        Boolean https = Boolean.parseBoolean(prop.getProperty("https", "false"));
        Boolean basicAuth = Boolean.parseBoolean(prop.getProperty("basicAuth", "false"));

        // Set up configuration for navetclient services
        String navetKeyStorePath = prop.getProperty("navetKeyStorePath");
        String navetKeyStorePassword = prop.getProperty("navetKeyStorePassword");
        String navetTrustStorePath = prop.getProperty("navetTrustStorePath");
        String navetTrustStorePassword = prop.getProperty("navetTrustStorePassword");
        setupNavetClientStores(navetKeyStorePath, navetKeyStorePassword, navetTrustStorePath, navetTrustStorePassword);
        String navetWsBaseEndpoint = prop.getProperty("wsBaseEndpoint");
        System.setProperty("se.sunet.navet.service.wsBaseEndpoint", navetWsBaseEndpoint);

        // Requester configuration
        String organisationNumber = prop.getProperty("organisationNumber");
        String orderIdentity = prop.getProperty("orderIdentity");
        System.setProperty("se.sunet.navet.service.organisationNumber", organisationNumber);
        System.setProperty("se.sunet.navet.service.orderIdentity", orderIdentity);

        // Get servlet context handler
        ServletContextHandler servletContext = getServletContext(packagesLocation, rootPath);

        // Set context handlers
        if (!basicAuth)  {
            this.server.setHandler(servletContext);
        } else {
            String realmProperties = prop.getProperty("hashLoginServiceProperties");
            String realm = prop.getProperty("hashLoginServiceRealm");
            configureBasicAuth(realmProperties, realm, servletContext);
        }

        // Set connectors
        if (!https) {
            HttpConfiguration http_config = new HttpConfiguration();
            ServerConnector http = new ServerConnector(this.server, new HttpConnectionFactory(http_config));
            http.setHost(host);
            http.setPort(port);
            this.server.addConnector(http);
        } else {
            String jettyKeyStorePath = prop.getProperty("jettyKeyStorePath");
            String jettyKeyStorePassword = prop.getProperty("jettyKeyStorePassword");
            String jettyKeyManagerPassword = prop.getProperty("jettyKeyManagerPassword");
            SslContextFactory.Server sslContextFactory = getSslContextFactory(jettyKeyStorePath, jettyKeyStorePassword, jettyKeyManagerPassword);
            configureSslServer(host, port, sslContextFactory);
        }
    }

    public void start() throws Exception {
        this.server.start();
    }

    public void join() throws Exception {
        this.server.join();
    }

    public void stop() throws Exception {
        this.server.stop();
    }
}
