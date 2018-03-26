package won.transport.taxi.bot.action;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.transport.taxi.bot.entity.ParseableResult;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;

import java.net.URI;
import java.util.Iterator;

public class ControlMessageAction extends BaseEventBotAction {
    public ControlMessageAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper &&  event instanceof MessageFromOtherNeedEvent){
            Connection con = ((MessageFromOtherNeedEvent) event).getCon();
            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();
            MessageFromOtherNeedEvent messageFromOtherNeedEvent = (MessageFromOtherNeedEvent) event;

            String textMessage = WonRdfUtils.MessageUtils.getTextMessage(messageFromOtherNeedEvent.getWonMessage());

            if ("status".equals(textMessage)) {
                publishAnalyzingMessage(con);

                Dataset fullConversationDataset = WonLinkedDataUtils.getConversationAndNeedsDataset(con.getConnectionURI(), ctx.getLinkedDataSource());
                Dataset presentAgreements = AgreementProtocolState.of(fullConversationDataset).getAgreements();

                if(presentAgreements.isEmpty()){
                    ctx.getEventBus().publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("No Agreements Present for your connection")));
                }else{
                    Iterator<String> presentAgreementUris = presentAgreements.listNames();

                    while(presentAgreementUris.hasNext()){
                        String agreementUri = presentAgreementUris.next();
                        String orderId = taxiBotContextWrapper.getOfferIdForAgreementURI(URI.create(agreementUri));

                        ParseableResult orderState = new ParseableResult(taxiBotContextWrapper.getMobileBooking().getOrderState(orderId));
                        ctx.getEventBus().publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("Agreement: <"+agreementUri+"> has the orderResponse: " + orderState)));
                    }
                }
            }
        }
    }

    private void publishAnalyzingMessage(Connection connection) {
        Model messageModel = WonRdfUtils.MessageUtils.processingMessage("Calculating your Orderstatus...");
        getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(connection, messageModel));
    }
}
