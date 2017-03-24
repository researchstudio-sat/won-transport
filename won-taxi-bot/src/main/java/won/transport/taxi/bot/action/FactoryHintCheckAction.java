package won.transport.taxi.bot.action;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.HintFromMatcherEvent;
import won.protocol.model.Match;
import won.transport.taxi.bot.client.MobileBooking;
import won.transport.taxi.bot.event.FactoryHintEvent;

import java.net.URI;

/**
 * Checks if the received hint is for a factoryURI
 */
public class FactoryHintCheckAction extends BaseEventBotAction {
    private String factoryListName;

    public FactoryHintCheckAction(EventListenerContext eventListenerContext, String factoryListName) {
        super(eventListenerContext);
        this.factoryListName = factoryListName;
    }

    @Override
    protected void doRun(Event event) throws Exception {
        if(event instanceof HintFromMatcherEvent) {
            Match match = ((HintFromMatcherEvent) event).getMatch();

            URI ownUri = match.getFromNeed();
            URI requesterUri = match.getToNeed();

            boolean correspondingFactoryNeedExists = false;

            for(URI factoryNeedURI : getEventListenerContext().getBotContext().getNamedNeedUriList(factoryListName)){
                if(factoryNeedURI.equals(ownUri)){
                    correspondingFactoryNeedExists = true;
                    break;
                }
            }

            if(correspondingFactoryNeedExists) {
                logger.debug("FactoryHint for factoryURI: " + ownUri + " from the requesterUri: "+requesterUri);
                EventBus bus = getEventListenerContext().getEventBus();
                bus.publish(new FactoryHintEvent(requesterUri, ownUri));
            }else{
                logger.warn("NON FactoryHint for URI: " + ownUri + " from the requesterUri: "+requesterUri+" ignore the hint");
            }
        }
    }
}
