package won.transport.taxibot.client.entity.Parameter;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by fsuda on 04.12.2017.
 */
@XmlRootElement(name="VEHICLE")
public class Vehicle extends Parameter implements Serializable {
    private String id;
    private double x;
    private double y;
    private int arrivalMinutes;
    private int state; //0=Free 1=Occupied
    private int driverId;
    private String driverName;
    private int sectorId;
    private String sectorName;
    private String sectorPosition; //TODO: should probably not be a string is a num but not sure what that even entails as a single num is not a position
    private List<Attribute> attributeList;

    public Vehicle() {}

    @XmlAttribute(name="ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name="X")
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @XmlAttribute(name="Y")
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @XmlAttribute(name="ARRIVALMINUTES")
    public int getArrivalMinutes() {
        return arrivalMinutes;
    }

    public void setArrivalMinutes(int arrivalMinutes) {
        this.arrivalMinutes = arrivalMinutes;
    }

    @XmlAttribute(name="STATE")
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @XmlAttribute(name="DRIVER_ID")
    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    @XmlAttribute(name="DRIVER_NAME")
    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    @XmlAttribute(name="SECTOR_ID")
    public int getSectorId() {
        return sectorId;
    }

    public void setSectorId(int sectorId) {
        this.sectorId = sectorId;
    }

    @XmlAttribute(name="SECTOR_NAME")
    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    @XmlAttribute(name="SECTOR_POSITION")
    public String getSectorPosition() {
        return sectorPosition;
    }

    public void setSectorPosition(String sectorPosition) {
        this.sectorPosition = sectorPosition;
    }

    @XmlElementWrapper(name = "ATTRIBUTELIST")
    @XmlElements({
        @XmlElement(name="ATTRIBUTE", type=Attribute.class)
    })
    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public String toString() {
        //TODO: Implement Vehicle Info
        return "Vehicle:'NO VEHICLE IFNO IMPLEMENTED YET'";
    }
}
