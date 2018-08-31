package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by fsuda on 13.12.2017.
 */
@XmlRootElement(name = "SERVICE")
public class Service extends Parameter implements Serializable{
    private String id;
    private String name;
    private String phone;
    private List<PostCode> postCodeList;
    private List<Attribute> attributeList;

    public Service() {
    }

    @XmlAttribute(name="ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name="NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name="PHONE")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @XmlElementWrapper(name = "POSTCODELIST")
    @XmlElements({
        @XmlElement(name="POSTCODE", type=PostCode.class)
    })
    public List<PostCode> getPostCodeList() {
        return postCodeList;
    }

    public void setPostCodeList(List<PostCode> postCodeList) {
        this.postCodeList = postCodeList;
    }

    @XmlElementWrapper(name = "ATTRIBUTELIST")
    @XmlElements({
        @XmlElement(name="ATTRIBUTE", type=Attribute.class)
    })
    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", postCodeList=" + postCodeList +
                ", attributeList=" + attributeList +
                '}';
    }
}
