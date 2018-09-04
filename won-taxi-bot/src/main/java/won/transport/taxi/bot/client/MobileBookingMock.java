package won.transport.taxi.bot.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.client.entity.Parameter.Error;
import won.transport.taxi.bot.client.entity.Result;

import java.util.ArrayList;
import java.util.List;


public class MobileBookingMock implements MobileBooking{
    private static final Logger logger = LoggerFactory.getLogger(MobileBookingMock.class);

    private int msgId;

    @Override
    public Result ping() {
        return new Result();
    }

    @Override
    public Result checkOrder(DepartureAddress departureAddress) {
        Price p = new Price();
        p.setAmount(2.99);
        p.setCurrency("EUR");
        p.setType(3);
        p.setDisplayText("Grundpreis");

        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(new ArrivalMinutes(10));
        parameterList.add(new DisplayText("This is not mocked checkorder-result"));
        parameterList.add(p);

        Result result = new Result();
        result.setParameter(parameterList);

        return result;
    }

    @Override
    public Result checkOrder(DepartureAddress departureAddress, DestinationAddress destinationAddress) {
        Price p = new Price();
        p.setAmount(19.99);
        p.setCurrency("EUR");
        p.setType(1);
        p.setDisplayText("Festpreis");

        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(new ArrivalMinutes(10));
        parameterList.add(new DisplayText("This is not mocked checkorder-result"));
        parameterList.add(p);

        Result result = new Result();
        result.setParameter(parameterList);

        return result;
    }

    @Override
    public Result getPrice(DepartureAddress departureAddress, DestinationAddress destinationAddress) {
        Price p = new Price();
        p.setAmount(19.99);
        p.setCurrency("EUR");
        p.setType(1);
        p.setDisplayText("Festpreis");

        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(p);

        Result result = new Result();
        result.setParameter(parameterList);

        return result;
    }

    @Override
    public Result getFleetRadar(Area area) {
        List<Parameter> parameterList = new ArrayList<>();
        VehicleList vehicleList = new VehicleList();

        Vehicle one = new Vehicle();
        one.setId("1");
        one.setDriverId(5);
        one.setDriverName("Bob Dylan");
        one.setX(35.0);
        one.setY(35.0);

        Vehicle two = new Vehicle();
        two.setId("2");
        two.setDriverId(6);
        two.setDriverName("Neil Young");
        two.setX(32.0);
        two.setY(32.0);

        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(one);
        vehicles.add(two);
        vehicleList.setVehicleList(vehicles);
        parameterList.add(vehicleList);

        Result result = new Result();
        result.setParameter(parameterList);
        return result;
    }

    @Override
    public Result getFleetRadar() {
        Result result = new Result();
        Error error = new Error();
        error.setType(1);
        error.setId(1);
        error.setText("NOT IMPLEMENTED IN MOCK");
        result.setError(error);
        return result;
    }


    @Override
    public Result createOrder(DepartureAddress departureAddress) {
        return createOrder(departureAddress, null);
    }

    @Override
    public Result createOrder(DepartureAddress departureAddress, DestinationAddress destinationAddress) {
        Result result = new Result();
        List<Parameter> parameterList = new ArrayList<>();
        OrderId orderId = new OrderId(""+(++msgId));
        parameterList.add(orderId);
        result.setParameter(parameterList);

        return result;
    }

    @Override
    public Result getRadar(DepartureAddress departureAddress){
        Result result = new Result();
        Error error = new Error();
        error.setType(1);
        error.setId(1);
        error.setText("NOT IMPLEMENTED IN MOCK");
        result.setError(error);

        return result;
    }

    @Override
    public Result getVehicleList(DepartureAddress departureAddress){
        Result result = new Result();
        Error error = new Error();
        error.setType(1);
        error.setId(1);
        error.setText("NOT IMPLEMENTED IN MOCK");
        result.setError(error);

        return result;
    }

    @Override
    public Result getServiceList(State state) {
        return getServiceList(state, null);
    }

    @Override
    public Result getServiceList(State state, PostCode postCode) {
        Result result = new Result();
        Error error = new Error();
        error.setType(1);
        error.setId(1);
        error.setText("NOT IMPLEMENTED IN MOCK");
        result.setError(error);
        return result;
    }

    @Override
    public Result cancelOrder(String orderId) {
        return new Result();
    }

    @Override
    public Result getOrderState(String orderId) {
        return getOrderState(orderId, false);
    }

    @Override
    public Result getOrderState(String orderId, boolean extended) {
        Vehicle one = new Vehicle();
        one.setId("1");
        one.setDriverId(5);
        one.setDriverName("Bob Dylan");
        one.setX(35.0);
        one.setY(35.0);

        Price p = new Price();
        p.setAmount(19.99);
        p.setCurrency("EUR");

        ArrivalMinutes arrivalMinutes = new ArrivalMinutes(5);

        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(one);
        parameterList.add(p);
        parameterList.add(arrivalMinutes);

        Result result = new Result();
        result.setParameter(parameterList);

        return result;
    }
}
