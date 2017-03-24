package won.transport.taxi.bot.client;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(mobileBooking.ping());
    }

    @Test
    public void testPing_Unauthorized() throws Exception{
        initUnauthorized();
        Assert.assertFalse(mobileBooking.ping());
    }

    @Test
    public void testPing_WrongUrl() throws Exception{
        initWrongURL();
        Assert.assertFalse(mobileBooking.ping());
    }

    @Test
    public void testCheckOrder_OK() throws Exception{
        init();
        Assert.assertTrue(mobileBooking.checkOrder(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                ""));
    }

    @Test
    public void testCheckOrder_Unauthorized() throws Exception{
        initUnauthorized();
        Assert.assertFalse(mobileBooking.checkOrder(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                ""));
    }

    @Test
    public void testCheckOrder_WrongUrl() throws Exception{
        initWrongURL();
        Assert.assertFalse(mobileBooking.checkOrder(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                ""));
    }
}