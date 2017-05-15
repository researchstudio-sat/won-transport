package won.transport.taxi.bot.client;

import org.junit.Assert;
import org.junit.Test;
import won.transport.taxi.bot.client.entity.Parameter.DepartureAdress;
import won.transport.taxi.bot.client.entity.Parameter.DestinationAdress;
import won.transport.taxi.bot.client.entity.Result;

public class MobileBookingTest {
    private MobileBooking mobileBooking;

    public void init() throws Exception {
        mobileBooking = new MobileBooking();
        mobileBooking.setServerUrl(System.getProperty("api.serverUrl"));
        mobileBooking.setPassword(System.getProperty("api.password"));
        mobileBooking.setUsername(System.getProperty("api.username"));
        mobileBooking.afterPropertiesSet();
    }

    public void initUnauthorized() throws Exception {
        mobileBooking = new MobileBooking();
        mobileBooking.setServerUrl(System.getProperty("api.serverUrl"));
        mobileBooking.setPassword(System.getProperty("api.password"));
        mobileBooking.setUsername(System.getProperty("api.username")+"bla");
        mobileBooking.afterPropertiesSet();
    }

    public void initWrongURL() throws Exception {
        mobileBooking = new MobileBooking();
        mobileBooking.setServerUrl(System.getProperty("api.serverUrlWrong"));
        mobileBooking.setPassword(System.getProperty("api.password"));
        mobileBooking.setUsername(System.getProperty("api.username"));
        mobileBooking.afterPropertiesSet();
    }

    @Test
    public void testPing_OK() throws Exception{
        init();
        Assert.assertTrue(mobileBooking.ping().getError() == null);
    }

    @Test
    public void testPing_Unauthorized() throws Exception{
        initUnauthorized();
        Assert.assertFalse(mobileBooking.ping().getError() == null);
    }

    @Test
    public void testPing_WrongUrl() throws Exception{
        initWrongURL();
        Assert.assertFalse(mobileBooking.ping().getError() == null);
    }

    @Test
    public void testCheckOrder_OK() throws Exception{
        init();

        DepartureAdress departureAdress = new DepartureAdress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
        Assert.assertTrue(mobileBooking.checkOrder(departureAdress).getError() == null);
    }

    @Test
    public void testCreateOrder_OK() throws Exception {
        init();
        DepartureAdress departureAdress = new DepartureAdress(
                0.0,
                0.0,
                "A",
                "1060",
                "Wien",
                "Webgasse",
                "8",
                "");
        DestinationAdress destinationAdress = new DestinationAdress(
                0.0,
                0.0,
                "A",
                "1060",
                "Wien",
                "Webgasse",
                "3",
                "");

        Result result = mobileBooking.createOrder(departureAdress);
        Assert.assertTrue(result.getError() == null);
    }

    @Test
    public void testCancelOrder_OK() throws Exception{
        init();
        Assert.assertTrue(mobileBooking.cancelOrder("12").getError() == null);
    }

    @Test
    public void testGetOrderState_OK() throws Exception{
        init();
        Assert.assertTrue(mobileBooking.getOrderState("12").getError() == null);
    }

    @Test
    public void testCheckOrder_Unauthorized() throws Exception{
        initUnauthorized();
        DepartureAdress departureAdress = new DepartureAdress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
        Assert.assertTrue(mobileBooking.checkOrder(departureAdress).getError() == null);
    }

    @Test
    public void testCheckOrder_WrongUrl() throws Exception{
        initWrongURL();
        DepartureAdress departureAdress = new DepartureAdress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
        Assert.assertTrue(mobileBooking.checkOrder(departureAdress).getError() == null);
    }
}