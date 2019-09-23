package won.transport.taxibot.client;

import won.transport.taxibot.client.entity.Parameter.*;
import won.transport.taxibot.client.entity.Result;

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
