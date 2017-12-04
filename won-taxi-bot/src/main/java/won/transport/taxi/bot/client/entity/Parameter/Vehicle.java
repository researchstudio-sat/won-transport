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

    public Vehicle() {
    }

    public Vehicle(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

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
}
