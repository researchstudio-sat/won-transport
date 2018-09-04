package won.transport.taxi.bot.client;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

/**
 * Created by fsuda on 04.09.2018.
 */
public interface MobileBooking {
    public Result ping();

    public Result checkOrder(DepartureAddress departureAddress);

    public Result checkOrder(DepartureAddress departureAddress, DestinationAddress destinationAddress);

    public Result getPrice(DepartureAddress departureAddress, DestinationAddress destinationAddress);

    public Result getFleetRadar(Area area);

    public Result getFleetRadar();

    public Result createOrder(DepartureAddress departureAddress);

    public Result createOrder(DepartureAddress departureAddress, DestinationAddress destinationAddress);

    public Result getRadar(DepartureAddress departureAddress);

    public Result getVehicleList(DepartureAddress departureAddress);

    public Result getServiceList(State state);

    public Result getServiceList(State state, PostCode postCode);

    public Result cancelOrder(String orderId);

    public Result getOrderState(String orderId);

    public Result getOrderState(String orderId, boolean extended);
}
