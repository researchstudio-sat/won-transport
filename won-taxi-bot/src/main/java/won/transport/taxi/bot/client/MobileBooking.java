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
    private int msgId;

    public Result ping() {
        return executeFunction("PING");
    }

    public Result checkOrder(DepartureAddress departureAddress) {
        return checkOrder(departureAddress, null);
    }

    public Result checkOrder(DepartureAddress departureAddress, DestinationAddress destinationAddress) {
        List<Parameter> parameterList = new ArrayList<>();

        parameterList.add(new OrderType());
        parameterList.add(departureAddress);

        if(destinationAddress != null){
            parameterList.add(destinationAddress);
        }

        return executeFunction("CHECKORDER", parameterList);
    }

    public Result getPrice(DepartureAddress departureAddress, DestinationAddress destinationAddress) {
        List<Parameter> parameterList = new ArrayList<>();

        parameterList.add(new OrderType());
        parameterList.add(departureAddress);
        parameterList.add(destinationAddress);

        return executeFunction("GETPRICE", parameterList);
    }

    public Result getFleetRadar(Area area) {
        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(area);

        return executeFunction("GETFLEETRADAR", parameterList);
    }

    public Result getFleetRadar() {
        return getFleetRadar(null);
    }


    public Result createOrder(DepartureAddress departureAddress) {
        return createOrder(departureAddress, null);
    }

    public Result createOrder(DepartureAddress departureAddress, DestinationAddress destinationAddress) {
        List<Parameter> parameterList = new ArrayList<Parameter>();

        parameterList.add(new OrderType());
        parameterList.add(departureAddress);

        if(destinationAddress != null){
            parameterList.add(destinationAddress);
        }

        return executeFunction("CREATEORDER", parameterList);
    }

    public Result getRadar(DepartureAddress departureAddress){
        List<Parameter> parameterList = new ArrayList<>();

        parameterList.add(new OrderType());
        parameterList.add(departureAddress);

        return executeFunction("GETRADAR", parameterList);
    }

    public Result getVehicleList(DepartureAddress departureAddress){
        List<Parameter> parameterList = new ArrayList<>();

        parameterList.add(new OrderType());
        parameterList.add(departureAddress);

        return executeFunction("GETVEHICLELIST", parameterList);
    }

    public Result getServiceList(State state) {
        return getServiceList(state, null);
    }

    public Result getServiceList(State state, PostCode postCode) {
        List<Parameter> parameterList = new ArrayList<>();

        parameterList.add(state);

        if(postCode != null){
            parameterList.add(postCode);
        }

        return executeFunction("GETSERVICELIST", parameterList);
    }

    /*

    public Result sendTextMessage(VehicleId vehicleId, ServiceId serviceId, Text text) {
        return sendTextMessage(vehicleId, serviceId, text);
    }

    public Result sendTextMessage(OrderId orderId, ServiceId serviceId, Text text) {
        return sendTextMessage(orderId, serviceId, text);
    }

    private Result sendTextMessage(Parameter param, ServiceId serviceId, Text text){
        List<Parameter> parameterList = new ArrayList<Parameter>();

        parameterList.add(param); //either orderId or vehicleId
        parameterList.add(serviceId);
        parameterList.add(text);

        return executeFunction("SENDTEXTMESSAGE", parameterList);
    }*/

    public Result cancelOrder(String orderId) {
        List<Parameter> parameterList = Collections.singletonList(new OrderId(orderId));

        return executeFunction("CANCELORDER", parameterList);
    }

    public Result getOrderState(String orderId) {
        return getOrderState(orderId, false);
    }

    public Result getOrderState(String orderId, boolean extended) {
        List<Parameter> parameterList = Collections.singletonList(new OrderId(orderId));

        return executeFunction("GETORDERSTATE" + (extended ? "_EXTENDED" : ""), parameterList);
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

    private Result executeFunction(String name) {
        return executeFunction(name, null);
    }

    private Result executeFunction(String name, List<Parameter> parameterList) {
        return executeFunction(name, parameterList, true);
    }

    private Result executeFunction(String name, List<Parameter> parameterList, boolean tryAgain) {
        Function function = new Function(name, msgId++, parameterList);
        HttpEntity entity = new HttpEntity(function, defaultHeaders);

        try{
            ResponseEntity<Result> response = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, Result.class);

            return response.getBody();
        }/*catch(ResourceAccessException e){
            logger.error(e.getMessage());
            Result result = new Result();
            result.setError(new Error(e.getMessage()));
            return result;
        }*/catch(Exception e){
            if(tryAgain){
                logger.debug("WS-Error on first try, just try again once more");
                return executeFunction(name, parameterList, false);
            }else {
                //FOR SERVER ERROR MAYBE
                logger.error(e.getMessage());

                Result result = new Result();
                result.setError(new Error(e.getMessage()));
                return result;
            }
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
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);

        this.restTemplate = new RestTemplate(requestFactory);
        this.restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(username, password));

        this.defaultHeaders = new HttpHeaders();
        this.defaultHeaders.setContentType(MediaType.APPLICATION_XML);
        this.defaultHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    }
}
