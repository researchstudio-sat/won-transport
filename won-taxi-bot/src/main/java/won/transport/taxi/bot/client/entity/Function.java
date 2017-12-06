package won.transport.taxi.bot.client.entity;

import won.transport.taxi.bot.client.entity.Parameter.*;

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

    public Function(String name, Integer msgId) {
        this(name);
        this.msgId = msgId;
    }

    public Function(String name, Integer msgId, List<Parameter> parameter) {
        this(name, msgId);
        this.parameter = parameter;
    }

    private String name;
    private Integer msgId;
    private List<Parameter> parameter;

    @XmlAttribute(name="NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name="MSGID")
    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }

    @XmlElementWrapper(name = "PARAMETER")
    @XmlElements({
            @XmlElement(name="ORDERTYPE", type=OrderType.class),
            @XmlElement(name="DEPARTUREADRESS", type=DepartureAdress.class),
            @XmlElement(name="DESTINATIONADRESS", type=DestinationAdress.class),
            @XmlElement(name="ORDERID", type=OrderId.class),
            @XmlElement(name="SERVICEID", type=ServiceId.class),
            @XmlElement(name="VEHICLEID", type=VehicleId.class),
            //@XmlElement(name="TEXT", type=Text.class), //TODO: CANT INCLUDE THIS (USED FOR SENDTEXTMESSAGE) BECAUSE OF NAME COLLISION WITH "TEXT" Attribute in adresses
            @XmlElement(name="TEST", type=Test.class),
            @XmlElement(name="STATE", type=State.class),
            @XmlElement(name="POSTCODE", type=PostCode.class),
            @XmlElement(name="ONLY_FREE", type=OnlyFree.class),
            @XmlElement(name="AREA", type=Area.class)
    })
    public List<Parameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }
}
