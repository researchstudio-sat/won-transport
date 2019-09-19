package won.transport.taxibot.entity;

import won.transport.taxibot.client.entity.Parameter.Error;
import won.transport.taxibot.client.entity.Result;
import won.transport.taxibot.client.entity.Parameter.*;

/**
 * Created by fsuda on 09.03.2018.
 */
public class ParseableResult {
    private ServiceId serviceId;
    private ServiceName serviceName;
    private ServicePhone servicePhone;
    private ServiceList serviceList;
    private OrderId orderId;
    private OrderState orderState;
    private OrderStateMessage orderStateMessage;
    private ArrivalMinutes arrivalMinutes;
    private DisplayText displayText;
    private Price price;
    private Vehicle vehicle;
    private VehicleList vehicleList;
    private DispoInfo dispoInfo;
    private EventLogList eventLogList;

    private won.transport.taxibot.client.entity.Parameter.Error error;

    public ParseableResult(Result result) {
        this.error = result.getError();

        if(this.error == null && result.getParameter() != null) {
            for (Parameter param : result.getParameter()) {
                if(param instanceof ServiceId) {
                    this.serviceId = (ServiceId) param;
                }else if(param instanceof OrderId) {
                    this.orderId = (OrderId) param;
                }else if(param instanceof OrderState) {
                    this.orderState = (OrderState) param;
                }else if(param instanceof ServiceName) {
                    this.serviceName = (ServiceName) param;
                }else if(param instanceof ServicePhone) {
                    this.servicePhone = (ServicePhone) param;
                }else if(param instanceof ArrivalMinutes) {
                    this.arrivalMinutes = (ArrivalMinutes) param;
                }else if(param instanceof DisplayText) {
                    this.displayText = (DisplayText) param;
                }else if(param instanceof Price) {
                    this.price = (Price) param;
                }else if(param instanceof Vehicle) {
                    this.vehicle = (Vehicle) param;
                }else if(param instanceof VehicleList) {
                    this.vehicleList = (VehicleList) param;
                }else if(param instanceof ServiceList) {
                    this.serviceList = (ServiceList) param;
                }else if(param instanceof OrderStateMessage) {
                    this.orderStateMessage = (OrderStateMessage) param;
                }else if(param instanceof DispoInfo){
                    this.dispoInfo = (DispoInfo) param;
                }else if(param instanceof EventLogList) {
                    this.eventLogList = (EventLogList) param;
                }else{
                    throw new IllegalArgumentException("Parameter of class is not parseable: "+param.getClass());
                }
            }
        }
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public ServiceName getServiceName() {
        return serviceName;
    }

    public ServicePhone getServicePhone() {
        return servicePhone;
    }

    public ArrivalMinutes getArrivalMinutes() {
        return arrivalMinutes;
    }

    public DisplayText getDisplayText() {
        return displayText;
    }

    public Price getPrice() {
        return price;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public VehicleList getVehicleList() {
        return vehicleList;
    }

    public ServiceList getServiceList() {
        return serviceList;
    }

    public OrderStateMessage getOrderStateMessage() {
        return orderStateMessage;
    }

    public DispoInfo getDispoInfo() {
        return dispoInfo;
    }

    public EventLogList getEventLogList() {
        return eventLogList;
    }

    public Error getError() {
        return error;
    }

    public boolean isError(){
        return getError() != null;
    }

    public String toString() {
        if(this.isError()) {
            return error.toString();
        }

        StringBuilder sb = new StringBuilder();

        if(serviceId != null) sb.append(serviceId.toString()).append(" ,");
        if(serviceName != null) sb.append(serviceName.toString()).append(" ,");
        if(servicePhone != null) sb.append(servicePhone.toString()).append(" ,");
        if(orderId != null) sb.append(orderId.toString()).append(" ,");
        if(orderState != null) sb.append(orderState.toString()).append(" ,");
        if(orderStateMessage != null) sb.append(orderStateMessage.toString()).append(" ,");
        if(arrivalMinutes != null) sb.append(arrivalMinutes.toString()).append(" ,");
        if(displayText != null) sb.append(displayText.toString()).append(" ,");
        if(price != null) sb.append(price.toString()).append(" ,");
        if(vehicle != null) sb.append(vehicle.toString()).append(" ,");
        if(vehicleList != null) sb.append(vehicleList.toString()).append(" ,");
        if(serviceList != null) sb.append(serviceList.toString()).append(" ,");
        if(dispoInfo != null) sb.append(dispoInfo.toString()).append(" ,");
        if(eventLogList != null) sb.append(eventLogList.toString()).append(" ,");

        return sb.toString();
    }
}
