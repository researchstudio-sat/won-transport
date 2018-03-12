package won.transport.taxi.bot.action.proposal;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCanceledEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionUnmetEvent;
import won.bot.framework.eventbot.event.impl.analyzation.proposal.ProposalEvent;
import won.bot.framework.eventbot.event.impl.analyzation.proposal.ProposalReceivedEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandResultEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandSuccessEvent;
import won.bot.framework.eventbot.filter.impl.CommandResultFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.protocol.highlevel.HighlevelProtocols;
import won.protocol.model.Connection;
import won.protocol.util.NeedModelWrapper;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.entity.ParseableResult;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;
import won.utils.goals.GoalInstantiationProducer;
import won.utils.goals.GoalInstantiationResult;

import java.net.URI;
import java.util.Collection;

/**
 * Created by fsuda on 08.03.2018.
 */
public class ProposalReceivedAction extends BaseEventBotAction {
    public ProposalReceivedAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof ProposalReceivedEvent) {
            Connection con = ((BaseNeedAndConnectionSpecificEvent) event).getCon();
            ProposalEvent proposalEvent = (ProposalEvent) event;

            if(!proposalEvent.hasProposesEvents()) {
                if(proposalEvent.getProposesToCancelEvents().size() > 1) {
                    //We do not handle anything that contains more than one cancellation in the taxibot
                    Model messageModel = WonRdfUtils.MessageUtils.textMessage("Rejecting Proposal due to: Too many proposeToCancel within a message - This Operation is not supported by the Bot");
                    WonRdfUtils.MessageUtils.addRejects(messageModel, proposalEvent.getProposalUri());
                    getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
                }
                for (URI canceledAgreementUri : proposalEvent.getProposesToCancelEvents()) {
                    ctx.getEventBus().publish(new AgreementCanceledEvent(con, canceledAgreementUri));
                }

            } else if(!proposalEvent.hasProposesToCancelEvents()) {
                Dataset needDataset = ctx.getLinkedDataSource().getDataForResource(con.getNeedURI());
                Dataset fullConversationDataset = WonLinkedDataUtils.getConversationAndNeedsDataset(con.getConnectionURI(), ctx.getLinkedDataSource());
                Model proposalModel = HighlevelProtocols.getProposal(fullConversationDataset, proposalEvent.getProposalUri().toString());

                NeedModelWrapper needWrapper = new NeedModelWrapper(needDataset);
                Collection<Resource> goalsInNeed = needWrapper.getGoals();

                if(goalsInNeed.isEmpty()) {
                    Model messageModel = WonRdfUtils.MessageUtils.textMessage("Rejecting Proposal due to: No Goals defined in Need - precondition calculation is not possiblem");
                    WonRdfUtils.MessageUtils.addRejects(messageModel, proposalEvent.getProposalUri());
                    getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
                }

                for(Resource goal : goalsInNeed) {
                    GoalInstantiationResult result = GoalInstantiationProducer.findInstantiationForGoalInDataset(needDataset, goal, proposalModel);
                    if(result.isConform()){
                        TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

                        DepartureAddress departureAddress = InformationExtractor.getDepartureAddress(proposalModel);
                        DestinationAddress destinationAddress = InformationExtractor.getDestinationAddress(proposalModel);

                        final ParseableResult createOrderResponse = new ParseableResult(taxiBotContextWrapper.getMobileBooking().createOrder(departureAddress, destinationAddress));

                        if(createOrderResponse.isError()) {
                            Model messageModel = WonRdfUtils.MessageUtils.textMessage("Rejecting Proposal due to: " + createOrderResponse);
                            WonRdfUtils.MessageUtils.addRejects(messageModel, proposalEvent.getProposalUri());
                            getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
                        }else {
                            final String orderId = createOrderResponse.getOrderId().getValue();
                            Model messageModel = WonRdfUtils.MessageUtils.textMessage("Ride from " + departureAddress + " to " + destinationAddress + ": "
                                    + "Has the Order: '"+createOrderResponse + "....Get into the Taxi when it arrives!");

                            WonRdfUtils.MessageUtils.addAccepts(messageModel, proposalEvent.getProposalUri());
                            ConnectionMessageCommandEvent connectionMessageCommandEvent = new ConnectionMessageCommandEvent(con, messageModel);

                            ctx.getEventBus().subscribe(ConnectionMessageCommandResultEvent.class, new ActionOnFirstEventListener(ctx, new CommandResultFilter(connectionMessageCommandEvent), new BaseEventBotAction(ctx) {
                                @Override
                                protected void doRun(Event event, EventListener executingListener) throws Exception {
                                    ConnectionMessageCommandResultEvent connectionMessageCommandResultEvent = (ConnectionMessageCommandResultEvent) event;
                                    if(connectionMessageCommandResultEvent.isSuccess()) {
                                        taxiBotContextWrapper.addOfferIdForAgreementURI(((ConnectionMessageCommandSuccessEvent) connectionMessageCommandResultEvent).getWonMessage().getMessageURI(), orderId);
                                    }else{
                                        taxiBotContextWrapper.getMobileBooking().cancelOrder(orderId); //TODO: HANDLE WHAT HAPPENS IF THERE IS NOTHING TO BE DONE
                                    }
                                }
                            }));

                            getEventListenerContext().getEventBus().publish(connectionMessageCommandEvent);
                        }
                    }else{
                        //TODO: SEE IF THERE IS A PRECONDITION THAT WILL ALLOW THE HANDLING, IF NOT THEN REJECT THE PROPOSAL
                        ctx.getEventBus().publish(new PreconditionUnmetEvent(con, result));
                    }
                }
            } else {
                //We do not handle anything that contains more than one cancellation in the taxibot
                Model messageModel = WonRdfUtils.MessageUtils.textMessage("Rejecting Proposal due to: Error - MixedMessage, the proposal contains proposes and proposeToCancel events - This Operation is not supported by the Bot");
                WonRdfUtils.MessageUtils.addRejects(messageModel, proposalEvent.getProposalUri());
                getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
            }
        }
    }
}
