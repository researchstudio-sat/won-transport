package won.transport.taxi.bot.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.needlifecycle.AbstractCreateNeedAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.model.BasicNeedType;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.NeedModelBuilder;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.client.MobileBooking;
import won.transport.taxi.bot.event.FactoryHintEvent;
import won.transport.taxi.bot.event.FactoryOfferCreatedEvent;

import java.net.URI;

/**
 * Creates a specific FactoryOffer (that will not be matched with anybody)
 */
public class CreateFactoryOfferAction extends AbstractCreateNeedAction {
    private MobileBooking mobileBooking;

    public CreateFactoryOfferAction(EventListenerContext eventListenerContext, MobileBooking mobileBooking, String uriListName, URI... facets) {
        super(eventListenerContext, uriListName, false, true, facets);
    }

    @Override
    protected void doRun(Event event) throws Exception {
        if(event instanceof FactoryHintEvent) {
            FactoryHintEvent factoryHintEvent = (FactoryHintEvent) event;

            EventBus bus = getEventListenerContext().getEventBus();
            EventListenerContext ctx = getEventListenerContext();
            final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();

            Model factoryOfferModel = createFactoryOfferFromTemplate(ctx, factoryHintEvent.getFactoryNeedURI(), factoryHintEvent.getRequesterURI());
            URI factoryOfferURI = WonRdfUtils.NeedUtils.getNeedURI(factoryOfferModel);

            logger.debug("creating factoryoffer on won node {} with content {} ", wonNodeUri, StringUtils.abbreviate(RdfUtils.toString(factoryOfferModel), 150));

            WonMessage createNeedMessage = createWonMessage(ctx.getWonNodeInformationService(), factoryOfferURI, wonNodeUri, factoryOfferModel, this.usedForTesting, this.doNotMatch);
            EventBotActionUtils.rememberInList(ctx, factoryOfferURI, uriListName);

            EventListener successCallback = successEvent -> {
                logger.debug("factoryoffer creation successful, new need URI is {}", factoryOfferURI);
                bus.publish(new FactoryOfferCreatedEvent(factoryOfferURI, factoryHintEvent.getRequesterURI(), wonNodeUri, factoryOfferModel, null));
            };

            EventListener failureCallback = failureEvent -> {
                String textMessage = WonRdfUtils.MessageUtils.getTextMessage(((FailureResponseEvent) failureEvent).getFailureMessage());

                logger.debug("factoryoffer creation failed for need URI {}, original message URI {}: {}", new Object[]{factoryOfferURI, ((FailureResponseEvent) failureEvent).getOriginalMessageURI(), textMessage});
                EventBotActionUtils.removeFromList(getEventListenerContext(), factoryOfferURI, uriListName);
            };

            EventBotActionUtils.makeAndSubscribeResponseListener(createNeedMessage, successCallback, failureCallback, getEventListenerContext());

            logger.debug("registered listeners for response to message URI {}", createNeedMessage.getMessageURI());
            getEventListenerContext().getWonMessageSender().sendWonMessage(createNeedMessage);
            logger.debug("factoryoffer creation message sent with message URI {}", createNeedMessage.getMessageURI());
        }
    }

    private Model createFactoryOfferFromTemplate(EventListenerContext ctx, URI factoryNeedURI, URI requesterURI){
        //TODO: retrieve real template from factory
        Dataset factoryNeedDataSet = ctx.getLinkedDataSource().getDataForResource(factoryNeedURI);
        Dataset requesterNeedDataSet = ctx.getLinkedDataSource().getDataForResource(requesterURI);
        String connectTitle =  WonRdfUtils.NeedUtils.getNeedTitle(requesterNeedDataSet) + "<->" + WonRdfUtils.NeedUtils.getNeedTitle(factoryNeedDataSet);

        Model factoryOfferModel = new NeedModelBuilder()
                .setTitle(connectTitle)
                .setBasicNeedType(BasicNeedType.SUPPLY)
                .setDescription("This is a automatically created need by the TaxiBot")
                .setUri(ctx.getWonNodeInformationService().generateNeedURI(ctx.getNodeURISource().getNodeURI()))
                .setFacetTypes(facets)
                .build();

        return factoryOfferModel;
    }
}
