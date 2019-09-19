package won.transport.taxibot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by fsuda on 04.12.2017.
 */
@XmlRootElement(name="SERVICELIST")
public class ServiceList extends Parameter implements Serializable {
    private List<Service> serviceList;

    public ServiceList() {
    }

    public ServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    @XmlElements({
        @XmlElement(name="SERVICE", type=Service.class)
    })
    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public List<Service> getServiceList() {
        return serviceList;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ServiceList: {");

        for(Service s : serviceList) {
            sb.append(s.toString()).append(",");
        }
        sb.append("}");
        return sb.toString();
    }
}
