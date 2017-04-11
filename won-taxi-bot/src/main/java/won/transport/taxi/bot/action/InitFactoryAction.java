package won.transport.taxi.bot.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.needlifecycle.AbstractCreateNeedAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.lifecycle.InitializeEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.model.BasicNeedType;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.NeedModelBuilder;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates all provided FactoryNeeds
 */
public class InitFactoryAction extends AbstractCreateNeedAction {
    private String factoryListName;

    public InitFactoryAction(EventListenerContext eventListenerContext, String factoryListName, String uriListName, URI... facets) {
        super(eventListenerContext, uriListName, false, false, facets);
        this.factoryListName = factoryListName;
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        if(!(event instanceof InitializeEvent)) {
            logger.error("InitFactoryAction can only handle InitializeEvent");
            return;
        }
        logger.debug("initializing the taxibot");

        List<Model> factoryNeeds = getInitFactoryNeeds();
        logger.debug("checking existance of "+factoryNeeds.size()+" FactoryNeeds");

        List<URI> initializedFactoryNeedUris = getEventListenerContext().getBotContext().getNamedNeedUriList(factoryListName);

        for(Model factoryNeed : factoryNeeds) {
            boolean isInitialized = false;
            URI factoryNeedURI = WonRdfUtils.NeedUtils.getNeedURI(factoryNeed);

            for(URI initializedFactoryNeedUri : initializedFactoryNeedUris){
                if(initializedFactoryNeedUri.equals(factoryNeedURI)){
                    isInitialized = true;
                    break;
                }
            }

            if(!isInitialized){
                logger.debug("initializing factoryneed with uri: "+factoryNeedURI);
                initializeFactoryNeed(factoryNeed);
            }else{
                logger.debug("factoryneed with uri: "+factoryNeedURI+" already exists");
            }
        }
    }

    private List<Model> getInitFactoryNeeds() {
        //TODO: RETRIEVE FACTORYNEEDS FROM A REAL PLACE/DO NOT HARDCODE THESE NEEDS ANYMORE
        List<Model> factoryNeeds = new ArrayList<>();

        Model factoryNeed1 = new NeedModelBuilder()
                .setTitle("Taxi In Wien")
                .setBasicNeedType(BasicNeedType.SUPPLY)
                .setDescription("Biete Taxifahrten in Wien")
                .setUri(URI.create("https://satsrv06.researchstudio.at/won/resource/need/bbi881b2asjxk62bdrc"))
                .setTags(new String[]{"Taxi", "Wien"})
                .setFacetTypes(facets)
                .build();

        factoryNeeds.add(factoryNeed1);

        Model factoryNeed2 = new NeedModelBuilder()
                .setTitle("Taxi In Salzburg")
                .setBasicNeedType(BasicNeedType.SUPPLY)
                .setDescription("Biete Taxifahrten in Salzburg")
                .setUri(URI.create("https://satsrv06.researchstudio.at/won/resource/need/bbi881b2asjxk62bsal"))
                .setTags(new String[]{"Taxi", "Salzburg"})
                .setFacetTypes(facets)
                .build();

        factoryNeeds.add(factoryNeed2);

        return factoryNeeds;
    }

    private void initializeFactoryNeed(Model factoryNeed) {
        EventListenerContext ctx = getEventListenerContext();
        WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();

        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();

        logger.debug("creating need on won node {} with content {} ", wonNodeUri, StringUtils.abbreviate(RdfUtils.toString(factoryNeed), 150));
        URI factoryNeedURI = WonRdfUtils.NeedUtils.getNeedURI(factoryNeed);

        WonMessage createNeedMessage = createWonMessage(wonNodeInformationService, factoryNeedURI, wonNodeUri, factoryNeed, this.usedForTesting, this.doNotMatch);
        EventBotActionUtils.rememberInList(ctx, factoryNeedURI, uriListName);
        EventBotActionUtils.rememberInList(ctx, factoryNeedURI, factoryListName);

        EventListener successCallback = new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                logger.debug("need creation successful, new need URI is {}", factoryNeedURI);
            }
        };

        EventListener failureCallback = new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                String textMessage = WonRdfUtils.MessageUtils.getTextMessage(((FailureResponseEvent) event).getFailureMessage());

                logger.debug("factoryneed creation failed for need URI {}, original message URI {}: {}", new Object[]{factoryNeedURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage});

                if(!textMessage.startsWith("UriAlreadyInUseException")) {
                    logger.debug("need creation failed for need URI {}, original message URI {}: {}", new Object[]{factoryNeedURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage});
                    EventBotActionUtils.removeFromList(getEventListenerContext(), factoryNeedURI, uriListName);
                    EventBotActionUtils.removeFromList(getEventListenerContext(), factoryNeedURI, factoryListName);
                }else{
                    logger.debug("factoryneed creation not necessary for uri: {} as it already exists", new Object[]{factoryNeedURI});
                }
            }
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(createNeedMessage, successCallback, failureCallback, getEventListenerContext());

        logger.debug("registered listeners for response to message URI {}", createNeedMessage.getMessageURI());
        getEventListenerContext().getWonMessageSender().sendWonMessage(createNeedMessage);
        logger.debug("factoryneed creation message sent with message URI {}", createNeedMessage.getMessageURI());
    }
}
