package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="DEPARTUREADDRESS")
public class DepartureAdress extends Parameter implements Serializable {
    private double x;
    private double y;
    private String state;
    private String postCode;
    private String city;
    private String streetName;
    private String streetNumber;
    private String text;

    public DepartureAdress() {
    }

    public DepartureAdress(double x, double y, String state, String postCode, String city, String streetName, String streetNumber, String text) {
        this.x = x;
        this.y = y;
        this.state = state;
        this.postCode = postCode;
        this.city = city;
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.text = text;
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

    @XmlAttribute(name="STATE")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @XmlAttribute(name="POSTCODE")
    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @XmlAttribute(name="CITY")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @XmlAttribute(name="STREETNAME")
    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    @XmlAttribute(name="STREETNUMBER")
    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    @XmlAttribute(name="TEXT")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
