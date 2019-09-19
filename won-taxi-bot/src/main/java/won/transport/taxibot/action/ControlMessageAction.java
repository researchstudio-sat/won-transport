package won.transport.taxibot.action;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.impl.factory.model.Precondition;
import won.bot.framework.eventbot.behaviour.AnalyzeBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.transport.taxibot.client.entity.Parameter.*;
import won.transport.taxibot.client.entity.Parameter.Error;
import won.transport.taxibot.entity.ParseableResult;
import won.transport.taxibot.impl.TaxiBotContextWrapper;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

public class ControlMessageAction extends BaseEventBotAction {
    public static final String HELP_MESSAGE =
            "Possible Commands:\n\n" +
            "'preconditions' list all preconditions for the connection\n" +
            "'status' calculate the status of made agreements including the orderstate\n" +
            "'list' show all the available services";

    private AnalyzeBehaviour analyzeBehaviour;

    public ControlMessageAction(EventListenerContext eventListenerContext, AnalyzeBehaviour analyzeBehaviour) {
        super(eventListenerContext);
        this.analyzeBehaviour = analyzeBehaviour;
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper &&  event instanceof MessageFromOtherAtomEvent){
            EventBus eventBus = ctx.getEventBus();
            Connection con = ((MessageFromOtherAtomEvent) event).getCon();
            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();
            MessageFromOtherAtomEvent messageFromOtherAtomEvent = (MessageFromOtherAtomEvent) event;

            String textMessage = WonRdfUtils.MessageUtils.getTextMessage(messageFromOtherAtomEvent.getWonMessage());

            if ("status".equals(textMessage)) {
                publishAnalyzingMessage(con);

                Dataset fullConversationDataset = WonLinkedDataUtils.getConversationAndAtomsDataset(con.getConnectionURI(), ctx.getLinkedDataSource());
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
                eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage(HELP_MESSAGE)));
            } else if ("list".equals(textMessage)) {
                ParseableResult serviceListResult = new ParseableResult(taxiBotContextWrapper.getMobileBooking().getServiceList(new State("AT")));
                if (serviceListResult != null) {
                    Error error = serviceListResult.getError();

                    if(error != null) {
                        eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("Retrieving ServiceList failed due to Error: " + error)));
                    } else {
                        ServiceList serviceListObject = serviceListResult.getServiceList();
                        if(serviceListObject == null || serviceListObject.getServiceList().size() == 0) {
                            eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("No Services available")));
                        } else {
                            for(Service service : serviceListObject.getServiceList()) {
                                String serviceString =
                                    "ID: " + service.getId() + "\n" +
                                    "Name: " + service.getName() + "\n" +
                                    "Phone: " + service.getPhone() + "\n";
                                List<Attribute> attributes = service.getAttributeList();

                                if(attributes != null && attributes.size() > 0){
                                    serviceString += "Attributes:\n";
                                    for(Attribute attribute : attributes) {
                                        String attributeString = "\tID: " + attribute.getId() + ": " + attribute.getName()+"\n";
                                        serviceString += attributeString;
                                    }
                                }

                                eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage(serviceString)));
                            }
                        }
                    }
                } else {
                    eventBus.publish(new ConnectionMessageCommandEvent(con, WonRdfUtils.MessageUtils.textMessage("Retrieving ServiceList failed, try again later")));
                }
            }
        }
    }

    private void publishAnalyzingMessage(Connection connection) {
        Model messageModel = WonRdfUtils.MessageUtils.processingMessage("Calculating your Orderstatus...");
        getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(connection, messageModel));
    }
}
