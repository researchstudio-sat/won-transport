package won.transport.taxi.bot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by fsuda on 04.12.2017.
 */
@XmlRootElement(name="VEHICLELIST")
public class EventLogList extends Parameter implements Serializable {
    private List<EventLog> eventLogList;

    public EventLogList() {
    }

    public EventLogList(List<EventLog> eventLogList) {
        this.eventLogList = eventLogList;
    }

    @XmlElements({
        @XmlElement(name="EVENTLOG", type=EventLog.class)
    })
    public void setEventLogList(List<EventLog> eventLogList) {
        this.eventLogList = eventLogList;
    }

    public List<EventLog> getEventLogList() {
        return eventLogList;
    }
}
