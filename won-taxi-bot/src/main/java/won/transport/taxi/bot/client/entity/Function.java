package won.transport.taxi.bot.client.entity;

import won.transport.taxi.bot.client.entity.Parameter.DepartureAdress;
import won.transport.taxi.bot.client.entity.Parameter.OrderType;
import won.transport.taxi.bot.client.entity.Parameter.Parameter;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "FUNCTION")
public class Function implements Serializable{
    public Function() {
    }

    public Function(String name) {
        this.name = name;
    }

    public Function(String name, List<Parameter> parameter) {
        this(name);
        this.parameter = parameter;
    }

    private String name;
    private List<Parameter> parameter;

    @XmlAttribute(name="NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "PARAMETER")
    @XmlElements({
            @XmlElement(name="ORDERTYPE", type=OrderType.class),
            @XmlElement(name="DEPARTUREADRESS", type=DepartureAdress.class),
    })
    public List<Parameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }
}
