package won.transport.taxi.bot.action;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.impl.factory.model.Precondition;
import won.bot.framework.eventbot.behaviour.AnalyzeBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
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
import java.util.List;

public class ControlMessageAction extends BaseEventBotAction {
    private AnalyzeBehaviour analyzeBehaviour;

    public ControlMessageAction(EventListenerContext eventListenerContext, AnalyzeBehaviour analyzeBehaviour) {
        super(eventListenerContext);
        this.analyzeBehaviour = analyzeBehaviour;
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper &&  event instanceof MessageFromOtherNeedEvent){
            EventBus eventBus = ctx.getEventBus();
            Connection con = ((MessageFromOtherNeedEvent) event).getCon();
            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();
            MessageFromOtherNeedEvent messageFromOtherNeedEvent = (MessageFromOtherNeedEvent) event;

            String textMessage = WonRdfUtils.MessageUtils.getTextMessage(messageFromOtherNeedEvent.getWonMessage());

            if ("status".equals(textMessage)) {
                publishAnalyzingMessage(con);

                Dataset fullConversationDataset = WonLinkedDataUtils.getConversationAndNeedsDataset(con.getConnectionURI(), ctx.getLinkedDataSource());
                Dataset presentAgreements = AgreementProtocolState.of(fullConversationDataset).getAgreements();

                if(presentAgreements.isEmpty()){
                    eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("No Agreements Present for your connection")));
                }else{
                    Iterator<String> presentAgreementUris = presentAgreements.listNames();

                    while(presentAgreementUris.hasNext()){
                        String agreementUri = presentAgreementUris.next();
                        String orderId = taxiBotContextWrapper.getOfferIdForAgreementURI(URI.create(agreementUri));

                        ParseableResult orderState = new ParseableResult(taxiBotContextWrapper.getMobileBooking().getOrderState(orderId));
                        eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("Agreement: <" + agreementUri + ">\n\nhas the orderResponse:\n" + orderState)));
                    }
                }
            } else if ("preconditions".equals(textMessage)) {
                List<Precondition> preconditionList = analyzeBehaviour.getPreconditionListForConnectionUri(con.getConnectionURI().toString());

                StringBuilder printablePreconditions = new StringBuilder();

                printablePreconditions.append("Found ")
                        .append(preconditionList.size())
                        .append(" Precondition(s) for ConnectionUri ")
                        .append(con.getConnectionURI().toString())
                        .append(":\n\n");

                for(Precondition precondition : preconditionList) {
                    printablePreconditions
                        .append("\t")
                        .append(precondition.getUri())
                        .append(" - met: ")
                        .append(precondition.isMet());
                    if(analyzeBehaviour.isPreconditionMetPending(precondition.getUri())) {
                        printablePreconditions.append(" preconditionMetPending");
                    }
                    if(analyzeBehaviour.isPreconditionMetError(precondition.getUri())) {
                        printablePreconditions.append(" preconditionMetError");
                    }

                    printablePreconditions.append("\n");
                }


                eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage(printablePreconditions.toString())));
            } else if("recheck".equals(textMessage)){
                List<Precondition> preconditionList = analyzeBehaviour.getPreconditionListForConnectionUri(con.getConnectionURI().toString());

                boolean resetMade = false;
                for(Precondition precondition : preconditionList) {
                    if(analyzeBehaviour.isPreconditionMetError(precondition.getUri())) {
                        analyzeBehaviour.removePreconditionMetError(precondition.getUri());
                        analyzeBehaviour.removePreconditionConversationState(precondition.getUri());
                        resetMade = true;
                    };
                }
                if(resetMade) {
                    eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("Reset all the preconditions that resulted in an error, starting recheck...")));
                } else {
                    eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("No error happened for any precondition, no recheck necessary")));
                }

            } else if ("help".equals(textMessage)) {
                eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("Possible Commands:\n\n'preconditions' list all preconditions for the connection\n'status' calculate the status of made agreements including the orderstate")));
            }
        }
    }

    private void publishAnalyzingMessage(Connection connection) {
        Model messageModel = WonRdfUtils.MessageUtils.processingMessage("Calculating your Orderstatus...");
        getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(connection, messageModel));
    }
}
