package won.transport.taxi.bot.client;

import org.apache.http.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import won.transport.taxi.bot.client.entity.Function;
import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.client.entity.Parameter.Error;
import won.transport.taxi.bot.client.entity.Result;

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

    public Result ping() {
        Function ping = new Function("PING");
        HttpEntity entity = new HttpEntity(ping, defaultHeaders);

        return postEntity(entity);
    }

    public Result checkOrder(DepartureAdress departureAdress) {
        return checkOrder(departureAdress, null);
    }


    public Result checkOrder(DepartureAdress departureAdress, DestinationAdress destinationAdress) {
        List<Parameter> parameterList = new ArrayList<Parameter>();

        parameterList.add(new OrderType());
        parameterList.add(departureAdress);

        if(destinationAdress!= null){
            parameterList.add(destinationAdress);
        }

        Function checkOrder = new Function("CHECKORDER", parameterList);
        HttpEntity entity = new HttpEntity(checkOrder, defaultHeaders);

        return postEntity(entity);
    }

    public Result getPrice(DepartureAdress departureAdress, DestinationAdress destinationAdress) {
        List<Parameter> parameterList = new ArrayList<Parameter>();

        parameterList.add(new OrderType());
        parameterList.add(departureAdress);
        parameterList.add(destinationAdress);

        Function checkOrder = new Function("GETPRICE", parameterList);
        HttpEntity entity = new HttpEntity(checkOrder, defaultHeaders);

        return postEntity(entity);
    }

    public Result createOrder(DepartureAdress departureAdress) {
        return createOrder(departureAdress, null);
    }

    public Result createOrder(DepartureAdress departureAdress, DestinationAdress destinationAdress) {
        List<Parameter> parameterList = new ArrayList<Parameter>();

        parameterList.add(new OrderType());
        parameterList.add(departureAdress);

        if(destinationAdress != null){
            parameterList.add(destinationAdress);
        }

        Function checkOrder = new Function("CREATEORDER", parameterList);
        HttpEntity entity = new HttpEntity(checkOrder, defaultHeaders);

        return postEntity(entity);
    }

    public Result cancelOrder(String orderId) {
        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(new OrderId(orderId));

        Function cancelOrder = new Function("CANCELORDER", parameterList);
        HttpEntity entity = new HttpEntity(cancelOrder, defaultHeaders);

        return postEntity(entity);
    }

    public Result getOrderState(String orderId) {
        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(new OrderId(orderId));

        Function cancelOrder = new Function("GETORDERSTATE", parameterList);
        HttpEntity entity = new HttpEntity(cancelOrder, defaultHeaders);

        return postEntity(entity);
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

    private Result postEntity(HttpEntity entity) {
        try{
            ResponseEntity<Result> response = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, Result.class);

            return response.getBody();
        }catch(ResourceAccessException e){
            logger.error(e.getMessage());
            Result result = new Result();
            result.setError(new Error(e.getMessage()));
            return result;
        }catch(Exception e){
            //FOR SERVER ERROR MAYBE
            logger.error(e.getMessage());
            Result result = new Result();
            result.setError(new Error(e.getMessage()));
            return result;
        }
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
