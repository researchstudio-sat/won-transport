package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

/**
 * Created by fsuda on 30.11.2017.
 */
@XmlRootElement(name="TEST")
public class Test extends Parameter implements Serializable {
    @XmlValue
    public int value=1;
}
