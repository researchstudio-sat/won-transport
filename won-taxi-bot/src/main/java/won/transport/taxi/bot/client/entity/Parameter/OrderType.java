package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

@XmlRootElement(name="ORDERTYPE")
public class OrderType extends Parameter implements Serializable {
    @XmlValue
    public String orderType="1";
}
