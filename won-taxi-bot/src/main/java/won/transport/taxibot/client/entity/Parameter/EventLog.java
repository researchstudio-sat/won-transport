package won.transport.taxibot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by fsuda on 13.12.2017.
 */
@XmlRootElement(name="EVENTLOG")
public class EventLog extends Parameter implements Serializable {
    //private Date time;
    private String text;
    private int id;
    private String vehicleId;
    private String driverId;
    private String driverName;
    private String userId;

    public EventLog() {
    }

    /*@XmlAttribute(name="TIME")
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }*/

    @XmlAttribute(name="TEXT")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @XmlAttribute(name="ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlAttribute(name="VEHICLE_ID")
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @XmlAttribute(name="DRIVER_ID")
    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    @XmlAttribute(name="DRIVER_NAME")
    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    @XmlAttribute(name="USER_ID")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "EventLog: '" +
                "text:" + text +
                " id:" + id +
                " vehicleId:" + vehicleId +
                " driverId:" + driverId +
                " driverName:" + driverName +
                " userId:" + userId + "'";
    }
}
