package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="DEPARTUREADDRESS")
public class DepartureAdress extends Adress implements Serializable {
    public DepartureAdress() {

    }

    public DepartureAdress(double x, double y, String state, String postCode, String city, String streetName, String streetNumber, String text) {
        super(x, y, state, postCode, city, streetName, streetNumber, text);
    }
}
