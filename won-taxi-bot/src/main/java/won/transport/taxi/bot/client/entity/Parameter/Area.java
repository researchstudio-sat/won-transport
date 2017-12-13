package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by fsuda on 06.12.2017.
 */
@XmlRootElement(name="AREA")
public class Area extends Parameter implements Serializable {
    private double min_x;
    private double max_x;
    private double min_y;
    private double max_y;

    public Area() {
    }

    public Area(double min_x, double max_x, double min_y, double max_y) {
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
    }

    @XmlAttribute(name="MIN_X")
    public double getMin_x() {
        return min_x;
    }

    public void setMin_x(double min_x) {
        this.min_x = min_x;
    }

    @XmlAttribute(name="MAX_X")
    public double getMax_x() {
        return max_x;
    }

    public void setMax_x(double max_x) {
        this.max_x = max_x;
    }

    @XmlAttribute(name="MIN_Y")
    public double getMin_y() {
        return min_y;
    }

    public void setMin_y(double min_y) {
        this.min_y = min_y;
    }

    @XmlAttribute(name="MAX_Y")
    public double getMax_y() {
        return max_y;
    }

    public void setMax_y(double max_y) {
        this.max_y = max_y;
    }
}
