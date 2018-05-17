package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by fsuda on 13.12.2017.
 */
@XmlRootElement(name = "DISPOINFO")
public class DispoInfo extends Parameter implements Serializable {
    private int sectorId;
    private String sectorName;
    private String cancelInfo;

    public DispoInfo() {
    }

    @XmlAttribute(name="SECTOR_ID")
    public int getSectorId() {
        return sectorId;
    }

    public void setSectorId(int sectorId) {
        this.sectorId = sectorId;
    }

    @XmlAttribute(name="SECTOR_NAME")
    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    @XmlAttribute(name="CANCEL_INFO")
    public String getCancelInfo() {
        return cancelInfo;
    }

    public void setCancelInfo(String cancelInfo) {
        this.cancelInfo = cancelInfo;
    }

    public String toString() {
        return "DispoInfo: '"+"SectorId"+sectorId+" SectorName:"+sectorName+" cancelInfo:"+cancelInfo+"'";
    }
}
