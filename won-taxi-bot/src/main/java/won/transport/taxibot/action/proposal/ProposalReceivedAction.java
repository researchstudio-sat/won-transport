package won.transport.taxibot.action.proposal;

import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.behaviour.AnalyzeBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.BaseAtomAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCancellationRequestedEvent;
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
import won.transport.taxibot.entity.ParseableResult;
import won.transport.taxibot.impl.TaxiBotContextWrapper;
import won.transport.taxibot.service.InformationExtractor;
import won.transport.taxibot.client.entity.Parameter.DepartureAddress;
import won.transport.taxibot.client.entity.Parameter.DestinationAddress;

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
    private AnalyzeBehaviour analyzeBehaviour;

    public ProposalReceivedAction(EventListenerContext eventListenerContext, AnalyzeBehaviour analyzeBehaviour) {
        super(eventListenerContext);
        this.analyzeBehaviour = analyzeBehaviour;
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof ProposalReceivedEvent) {
            EventBus bus = ctx.getEventBus();
            LinkedDataSource linkedDataSource = ctx.getLinkedDataSource();
            String rejectMsg;

            Connection con = ((BaseAtomAndConnectionSpecificEvent) event).getCon();
            ProposalEvent proposalEvent = (ProposalEvent) event;
            URI proposalUri = proposalEvent.getProposalUri();

            if(!proposalEvent.hasProposesEvents()) {
                if(proposalEvent.getProposesToCancelEvents().size() > 1) {
                    //We do not handle anything that contains more than one cancellation in the taxibot
                    rejectMsg = "Too many proposeToCancel within a message - This Operation is not supported by the Bot";
                }else {
                    for (URI canceledAgreementUri : proposalEvent.getProposesToCancelEvents()) {
                        bus.publish(new AgreementCancellationRequestedEvent(con, canceledAgreementUri, proposalUri));
                    }
                    return;
                }
            } else if(!proposalEvent.hasProposesToCancelEvents()) {
                TaxiBotContextWrapper botContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

                if(analyzeBehaviour.hasMetPrecondition(proposalUri)){
                    AgreementProtocolState agreementProtocolState = AgreementProtocolState.of(con.getConnectionURI(), linkedDataSource);
                    Model proposalModel = agreementProtocolState.getPendingProposal(proposalUri);

                    DepartureAddress departureAddress = InformationExtractor.getDepartureAddress(proposalModel);
                    DestinationAddress destinationAddress = InformationExtractor.getDestinationAddress(proposalModel);
                    String destinationName = InformationExtractor.getDestinationName(proposalModel);
                    String departureName = InformationExtractor.getDepartureName(proposalModel);

                    final ParseableResult createOrderResponse = new ParseableResult(botContextWrapper.getMobileBooking().createOrder(departureAddress, destinationAddress));

                    if(createOrderResponse.isError()) {
                        rejectMsg = createOrderResponse.toString();
                    }else {
                        final String orderId = createOrderResponse.getOrderId().getValue();

                        String rideText = "Your Order has been placed!";

                        if((departureName != null || departureAddress != null) && (destinationName != null || destinationAddress != null)) {
                            rideText = "Ride from '" + ((departureName != null) ? departureName : destinationAddress) + "' to '" + ((destinationName != null)? destinationName : destinationAddress) + "':";
                        } else if ((departureName != null || departureAddress != null)) {
                            rideText = "Ride from '" + ((departureName != null) ? departureName : destinationAddress) + "':";
                        }

                        Model messageModel = WonRdfUtils.MessageUtils.textMessage(rideText + "\n\nHas the Order: '"+createOrderResponse + "....Get into the Taxi when it arrives!");

                        WonRdfUtils.MessageUtils.addAccepts(messageModel, proposalUri);
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
                } else {
                    rejectMsg = "No PreconditionMet";
                }
            } else {
                rejectMsg = "Error - MixedMessage, the proposal contains proposes and proposeToCancel events - This Operation is not supported by the Bot";
            }

            Model messageModel = WonRdfUtils.MessageUtils.textMessage("Rejecting Proposal due to: " + rejectMsg);
            WonRdfUtils.MessageUtils.addRejects(messageModel, proposalUri);
            bus.publish(new ConnectionMessageCommandEvent(con, messageModel));
        }
    }
}
