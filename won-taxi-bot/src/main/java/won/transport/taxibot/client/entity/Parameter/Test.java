package won.transport.taxibot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

/**
 * Created by fsuda on 30.11.2017.
 */
@XmlRootElement(name="TEST")
public class Test extends Parameter implements Serializable {
    private int value=1;

    @XmlValue
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
