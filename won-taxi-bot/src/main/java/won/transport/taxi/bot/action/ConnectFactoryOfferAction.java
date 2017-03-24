package won.transport.taxi.bot.action;

import org.apache.jena.query.Dataset;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.protocol.exception.WonMessageBuilderException;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageBuilder;
import won.protocol.model.FacetType;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.event.FactoryOfferCreatedEvent;

import java.net.URI;

/**
 * Sends a connect Message from the given factoryOfferURI to the requesterURI
 */
public class ConnectFactoryOfferAction extends BaseEventBotAction{
    public ConnectFactoryOfferAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event) throws Exception {
        if(!(event instanceof FactoryOfferCreatedEvent)) {
            logger.error("ConnectFactoryOfferAction can only handle FactoryOfferCreatedEvents");
        }

        final URI factoryOfferURI = ((FactoryOfferCreatedEvent) event).getNeedURI();
        final URI requesterURI = ((FactoryOfferCreatedEvent) event).getRequesterURI();

        try{
            getEventListenerContext().getWonMessageSender().sendWonMessage(createConnectMessage(factoryOfferURI, requesterURI));
        }catch(Exception e) {
            logger.warn("could not connect "+factoryOfferURI+" and" + requesterURI, e);
        }
    }

    private WonMessage createConnectMessage(URI fromURI, URI toURI) throws WonMessageBuilderException {
        WonNodeInformationService wonNodeInformationService = getEventListenerContext().getWonNodeInformationService();

        Dataset localNeedRDF = getEventListenerContext().getLinkedDataSource().getDataForResource(fromURI);
        Dataset remoteNeedRDF = getEventListenerContext().getLinkedDataSource().getDataForResource(toURI);

        URI localWonNode = WonRdfUtils.NeedUtils.getWonNodeURIFromNeed(localNeedRDF, fromURI);
        URI remoteWonNode = WonRdfUtils.NeedUtils.getWonNodeURIFromNeed(remoteNeedRDF, toURI);

        return WonMessageBuilder.setMessagePropertiesForConnect(
                    wonNodeInformationService.generateEventURI(localWonNode),
                    FacetType.OwnerFacet.getURI(),
                    fromURI,
                    localWonNode,
                    FacetType.OwnerFacet.getURI(),
                    toURI,
                    remoteWonNode, "We offer you a ride") //TODO: EXTRACT WELCOME MESSAGE FROM A PROPERTY OR FACTORYTEMPLATE
                .build();
    }
}
