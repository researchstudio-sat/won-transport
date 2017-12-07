package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="DEPARTUREADDRESS")
public class DepartureAddress extends Address implements Serializable {
    public DepartureAddress() {

    }

    public DepartureAddress(double x, double y, String state, String postCode, String city, String streetName, String streetNumber, String text) {
        super(x, y, state, postCode, city, streetName, streetNumber, text);
    }

    public DepartureAddress(double x, double y) {
        super(x, y);
    }

    public DepartureAddress(String state, String postCode, String city, String streetName, String streetNumber, String text) {
        super(state, postCode, city, streetName, streetNumber, text);
    }
}
