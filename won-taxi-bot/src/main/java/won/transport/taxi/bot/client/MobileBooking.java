package won.transport.taxi.bot.client;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import won.transport.taxi.bot.client.entity.Function;
import won.transport.taxi.bot.client.entity.Parameter.DepartureAdress;
import won.transport.taxi.bot.client.entity.Parameter.OrderType;
import won.transport.taxi.bot.client.entity.Parameter.Parameter;

import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MobileBooking implements InitializingBean{
    private static final Logger logger = LoggerFactory.getLogger(MobileBooking.class);

    private String serverUrl;
    private String username;
    private String password;

    private RestTemplate restTemplate;
    private HttpHeaders defaultHeaders;

    public boolean ping() {
        Function ping = new Function("PING");
        HttpEntity entity = new HttpEntity(ping, defaultHeaders);
        try {
            ResponseEntity<String> response = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, String.class);

            return HttpStatus.OK_200.getStatusCode() == response.getStatusCodeValue();
        }catch(ResourceAccessException e){
            logger.error(e.getMessage());
            return false;
        }catch(Exception e){
            //FOR SERVER ERROR MAYBE
            logger.error(e.getMessage());
            return false;
        }
    }

    public boolean checkOrder(double x, double y, String state, String postCode, String city, String streetName, String streetNumber, String text) {
        List<Parameter> parameterList = new ArrayList<Parameter>();

        parameterList.add(new OrderType());
        parameterList.add(new DepartureAdress(x, y, state, postCode, city, streetName, streetNumber, text));


        Function checkOrder = new Function("CHECKORDER", parameterList);
        HttpEntity entity = new HttpEntity(checkOrder, defaultHeaders);

        try{
            ResponseEntity<String> response = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, String.class);

            return HttpStatus.OK_200.getStatusCode() == response.getStatusCodeValue();
        }catch(ResourceAccessException e){
            logger.error(e.getMessage());
            return false;
        }catch(Exception e){
            //FOR SERVER ERROR MAYBE
            logger.error(e.getMessage());
            return false;
        }
    }
    /**
     * PURE HELPER METHOD TO VALIDATE ALL CERTIFICATES
     * @return
     */
    private static CloseableHttpClient getCloseableHttpClient() {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.custom()
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy()
                    {
                        public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
                            {
                                return true;
                            }
                    }).build()).build();
        } catch (KeyManagementException e) {
            logger.error("KeyManagementException in creating http client instance", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException in creating http client instance", e);
        } catch (KeyStoreException e) {
            logger.error("KeyStoreException in creating http client instance", e);
        }
        return httpClient;
    }

    //******** SETTER ***************
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactoryBasicAuth(getCloseableHttpClient(), new HttpHost(serverUrl));
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(2000);

        this.restTemplate = new RestTemplate(requestFactory);
        this.restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(username, password));

        this.defaultHeaders = new HttpHeaders();
        this.defaultHeaders.setContentType(MediaType.APPLICATION_XML);
        this.defaultHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    }
}
