package won.transport.taxi.bot.action;

import org.springframework.messaging.support.GenericMessage;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.mail.model.UriType;
import won.bot.framework.eventbot.action.impl.mail.model.WonURI;
import won.bot.framework.eventbot.action.impl.mail.receive.MailContentExtractor;
import won.bot.framework.eventbot.action.impl.mail.send.WonMimeMessage;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.HintFromMatcherEvent;
import won.protocol.message.WonMessage;
import won.protocol.model.Match;

import javax.mail.internet.MimeMessage;
import java.net.URI;

/**
 * Created by fsuda on 28.02.2017.
 */
public class Hint2TaxiAction extends BaseEventBotAction {
    private String uriListName;

    public Hint2TaxiAction(EventListenerContext eventListenerContext, String uriListName) {
        super(eventListenerContext);
        this.uriListName = uriListName;
    }

    @Override
    protected void doRun(Event event) throws Exception {
        if(event instanceof HintFromMatcherEvent) {
            Match match = ((HintFromMatcherEvent) event).getMatch();
            WonMessage message = ((HintFromMatcherEvent) event).getWonMessage();

            URI ownUri = match.getFromNeed();
            URI remoteUri = match.getToNeed();

            logger.debug("Found a hint for ownURI: " + ownUri + " from the remoteUri: "+remoteUri);

            //TODO: CREATE A NEW SPECIFIC RESPONSE TAXI NEED AND SEND A REQUEST TO THE REMOTEURI
        }
    }
}
