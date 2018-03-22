package won.transport.taxi.bot.action.proposal;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCanceledEvent;
import won.bot.framework.eventbot.event.impl.analyzation.proposal.ProposalEvent;
import won.bot.framework.eventbot.event.impl.analyzation.proposal.ProposalReceivedEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandResultEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandSuccessEvent;
import won.bot.framework.eventbot.filter.impl.CommandResultFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.LinkedDataSource;
import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.entity.ParseableResult;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;

import java.net.URI;

/**
 * Specific Handling for Received Proposals for TaxiBot
 * Reject any Proposal that contains proposes and proposesToCancel within the same event
 * Reject any Proposal with more than one proposesToCancel within the same event
 * If a Precondition is Met then:
 * Create an TaxiOrder and Accept the Proposal if the order creation was succesful
 * If the Order Creation was not successful reject the proposal
 */
public class ProposalReceivedAction extends BaseEventBotAction {
    public ProposalReceivedAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof ProposalReceivedEvent) {
            EventBus bus = ctx.getEventBus();
            LinkedDataSource linkedDataSource = ctx.getLinkedDataSource();
            String rejectMsg;

            Connection con = ((BaseNeedAndConnectionSpecificEvent) event).getCon();
            ProposalEvent proposalEvent = (ProposalEvent) event;
            URI proposalURI = proposalEvent.getProposalUri();

            if(!proposalEvent.hasProposesEvents()) {
                if(proposalEvent.getProposesToCancelEvents().size() > 1) {
                    //We do not handle anything that contains more than one cancellation in the taxibot
                    rejectMsg = "Too many proposeToCancel within a message - This Operation is not supported by the Bot";
                }else {
                    for (URI canceledAgreementUri : proposalEvent.getProposesToCancelEvents()) {
                        bus.publish(new AgreementCanceledEvent(con, canceledAgreementUri));
                    }
                    return;
                }
            } else if(!proposalEvent.hasProposesToCancelEvents()) {
                TaxiBotContextWrapper botContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

                if(botContextWrapper.hasMetPrecondition(proposalURI)){
                    AgreementProtocolState agreementProtocolState = AgreementProtocolState.of(con.getConnectionURI(), linkedDataSource);
                    Model proposalModel = agreementProtocolState.getPendingProposal(proposalURI);

                    DepartureAddress departureAddress = InformationExtractor.getDepartureAddress(proposalModel);
                    DestinationAddress destinationAddress = InformationExtractor.getDestinationAddress(proposalModel);

                    final ParseableResult createOrderResponse = new ParseableResult(botContextWrapper.getMobileBooking().createOrder(departureAddress, destinationAddress));

                    if(createOrderResponse.isError()) {
                        rejectMsg = createOrderResponse.toString();
                    }else {
                        final String orderId = createOrderResponse.getOrderId().getValue();
                        Model messageModel = WonRdfUtils.MessageUtils.textMessage("Ride from " + departureAddress + " to " + destinationAddress + ": "
                                + "Has the Order: '"+createOrderResponse + "....Get into the Taxi when it arrives!");

                        WonRdfUtils.MessageUtils.addAccepts(messageModel, proposalURI);
                        ConnectionMessageCommandEvent connectionMessageCommandEvent = new ConnectionMessageCommandEvent(con, messageModel);

                        bus.subscribe(ConnectionMessageCommandResultEvent.class, new ActionOnFirstEventListener(ctx, new CommandResultFilter(connectionMessageCommandEvent), new BaseEventBotAction(ctx) {
                            @Override
                            protected void doRun(Event event, EventListener executingListener) throws Exception {
                                ConnectionMessageCommandResultEvent connectionMessageCommandResultEvent = (ConnectionMessageCommandResultEvent) event;
                                if (connectionMessageCommandResultEvent.isSuccess()) {
                                    botContextWrapper.addOfferIdForAgreementURI(((ConnectionMessageCommandSuccessEvent) connectionMessageCommandResultEvent).getWonMessage().getMessageURI(), orderId);
                                } else {
                                    botContextWrapper.getMobileBooking().cancelOrder(orderId);
                                }
                            }
                        }));

                        bus.publish(connectionMessageCommandEvent);
                        return;
                    }
                }else{
                    rejectMsg = "No PreconditionMet";
                }
            } else {
                rejectMsg = "Error - MixedMessage, the proposal contains proposes and proposeToCancel events - This Operation is not supported by the Bot";
            }

            Model messageModel = WonRdfUtils.MessageUtils.textMessage("Rejecting Proposal due to: " + rejectMsg);
            WonRdfUtils.MessageUtils.addRejects(messageModel, proposalURI);
            bus.publish(new ConnectionMessageCommandEvent(con, messageModel));
        }
    }
}
