package won.transport.taxi.bot.event;

import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.event.impl.needlifecycle.NeedCreatedEvent;
import won.protocol.model.FacetType;

import java.net.URI;

/**
 * Created by fsuda on 23.03.2017.
 */
public class FactoryOfferCreatedEvent extends NeedCreatedEvent {
    private URI requesterURI;
    private String welcomeMessage;

    public FactoryOfferCreatedEvent(URI factoryOfferURI, URI requesterURI, URI wonNodeUri, Model needModel, FacetType facetType) {
        super(factoryOfferURI, wonNodeUri, needModel, facetType);
        this.requesterURI = requesterURI;
    }

    public URI getRequesterURI() {
        return requesterURI;
    }
}
