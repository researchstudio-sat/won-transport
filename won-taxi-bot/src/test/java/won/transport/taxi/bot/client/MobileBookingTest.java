package won.transport.taxi.bot.client;

import org.junit.Assert;
import org.junit.Test;
import won.transport.taxi.bot.client.entity.Parameter.*;
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
        Assert.assertNull(mobileBooking.ping().getError());
    }

    @Test
    public void testPing_Unauthorized() throws Exception{
        initUnauthorized();
        Assert.assertNotNull(mobileBooking.ping().getError());
    }

    @Test
    public void testPing_WrongUrl() throws Exception{
        initWrongURL();
        Assert.assertNotNull(mobileBooking.ping().getError());
    }

    @Test
    public void testCheckOrder_OK() throws Exception{
        init();

        DepartureAddress departureAddress = new DepartureAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
        Assert.assertNull(mobileBooking.checkOrder(departureAddress).getError());
    }

    @Test
    public void testGetServiceList_OK() throws Exception {
        init();

        Result result = mobileBooking.getServiceList(new State("D"));

        Assert.assertNull(result.getError());
    }

    @Test
    public void testGetServiceListWithPostCode_OK() throws Exception {
        init();

        Result result = mobileBooking.getServiceList(new State("D"), new PostCode("80333"));

        Assert.assertNull(result.getError());
    }

    @Test
    public void testGetPrice_OK() throws Exception {
        init();
        DepartureAddress departureAddress = new DepartureAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
        DestinationAddress destinationAddress = new DestinationAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "346",
                "");

        Result result = mobileBooking.getPrice(departureAddress, destinationAddress);
        Assert.assertNull(result.getError());
        Assert.assertNotNull(result.getParameter());
        for(Parameter param : result.getParameter()){
            if(param instanceof Price){
                Assert.assertTrue(((Price) param).getAmount() == 10.0);
            }
        }
    }

    @Test
    public void testGetPriceJustCoords_OK() throws Exception {
        init();
        DepartureAddress departureAddress = new DepartureAddress(16.370691, 48.216974);
        DestinationAddress destinationAddress = new DestinationAddress(
                //16.343933,
                //48.199128,
                16.333333,
                48.189451);

        Result result = mobileBooking.getPrice(departureAddress, destinationAddress);
        Assert.assertNull(result.getError());
        Assert.assertNotNull(result.getParameter());
        for(Parameter param : result.getParameter()){
            if(param instanceof Price){
                Assert.assertTrue(((Price) param).getAmount() == 10.37);
            }
        }
    }

    @Test
    public void testGetPriceJustAddress_OK() throws Exception {
        init();
        DepartureAddress departureAddress = new DepartureAddress("AT","1090","Wien","Thurngasse","8", "");
        DestinationAddress destinationAddress = new DestinationAddress("AT","1060","Wien","Hirschengasse","10", "");

        Result result = mobileBooking.getPrice(departureAddress, destinationAddress);
        Assert.assertNull(result.getError());
        Assert.assertNotNull(result.getParameter());
        for(Parameter param : result.getParameter()){
            if(param instanceof Price){
                Assert.assertTrue(((Price) param).getAmount() == 7.07);
            }
        }
    }

    @Test
    public void testCreateOrderCheckOrderAndCancelOrder_OK() throws Exception{
        init();
        DepartureAddress departureAddress = new DepartureAddress(16.370691, 48.216974);
        DestinationAddress destinationAddress = new DestinationAddress(
                //16.343933,
                //48.199128,
                16.333333,
                48.189451);

        Result createResult = mobileBooking.createOrder(departureAddress, destinationAddress);
        Assert.assertNull(createResult.getError());
        Assert.assertNotNull(createResult.getParameter());

        for(Parameter param : createResult.getParameter()){
            if(param instanceof OrderId){
                OrderId orderId = (OrderId) param;

                Result orderStateResult = mobileBooking.getOrderState(orderId.getValue());
                Assert.assertNull(orderStateResult.getError());
                Result orderStateExtendedResult = mobileBooking.getOrderState(orderId.getValue(), true);
                Assert.assertNull(orderStateExtendedResult.getError());
                Result cancelationResult = mobileBooking.cancelOrder(orderId.getValue());
                Assert.assertNull(cancelationResult.getError());
            }
        }
    }

    @Test
    public void testGetRadar_OK() throws Exception{
        init();
        DepartureAddress departureAddress = new DepartureAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");

        Result getRadarResult = mobileBooking.getRadar(departureAddress);

        Assert.assertNull(getRadarResult.getError());
    }

    @Test
    public void testGetVehicleList_OK() throws Exception{
        init();
        DepartureAddress departureAddress = new DepartureAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");

        Result getVehicleListResult = mobileBooking.getVehicleList(departureAddress);

        Assert.assertNull(getVehicleListResult.getError());
    }

    /*@Test
    public void testSendTextMessageWithVehicleId_OK() throws Exception {
        init();

        VehicleId vehicleId = new VehicleId();
        ServiceId serviceId = new ServiceId();
        Text text = new Text("testmessage");

        Result result = mobileBooking.sendTextMessage(vehicleId, serviceId, text);
        Assert.assertNull(result.getError());
    }

    @Test
    public void testSendTextMessageWithOrderId_OK() throws Exception {
        init();

        OrderId orderId = new OrderId();
        ServiceId serviceId = new ServiceId();
        Text text = new Text("testmessage");

        Result result = mobileBooking.sendTextMessage(orderId, serviceId, text);
        Assert.assertNull(result.getError());
    }*/

    @Test
    public void testGetFleetRadar_OK() throws Exception {
        init();

        Result getFleetRadarResult = mobileBooking.getFleetRadar();

        Assert.assertNull(getFleetRadarResult.getError());
    }

    @Test
    public void testCheckOrder_Unauthorized() throws Exception{
        initUnauthorized();
        DepartureAddress departureAddress = new DepartureAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
        Assert.assertNotNull(mobileBooking.checkOrder(departureAddress).getError());
    }

    @Test
    public void testCheckOrder_WrongUrl() throws Exception{
        initWrongURL();
        DepartureAddress departureAddress = new DepartureAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
        Assert.assertNotNull(mobileBooking.checkOrder(departureAddress).getError());
    }
}