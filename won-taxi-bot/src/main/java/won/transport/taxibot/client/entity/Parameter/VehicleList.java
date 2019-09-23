package won.transport.taxibot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by fsuda on 04.12.2017.
 */
@XmlRootElement(name="VEHICLELIST")
public class VehicleList extends Parameter implements Serializable {
    private List<Vehicle> vehicleList;

    public VehicleList() {
    }

    public VehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @XmlElements({
        @XmlElement(name="VEHICLE", type=Vehicle.class)
    })
    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("VehicleList: {");

        for(Vehicle v : vehicleList) {
            sb.append(v.toString()).append(",");
        }
        sb.append("}");
        return sb.toString();
    }
}
