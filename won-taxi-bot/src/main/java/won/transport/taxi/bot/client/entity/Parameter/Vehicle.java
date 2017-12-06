package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

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

    @XmlAttribute(name="STATE")
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setArrivalMinutes(int arrivalMinutes) {
        this.arrivalMinutes = arrivalMinutes;
    }
}
