/*
 * Copyright 2017  Research Studios Austria Forschungsges.m.b.H.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package won.transport.taxi.bot.action.agreement;

import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.behaviour.AnalyzeBehaviour;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionMetEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandResultEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandSuccessEvent;
import won.bot.framework.eventbot.filter.impl.CommandResultFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.entity.ParseableResult;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;
import won.utils.goals.GoalInstantiationResult;

/**
 * Proposes an agreement based on the Data/Payload given within the PreconditionMetEvent
 */
public class PreconditionMetAction extends BaseEventBotAction{
    private AnalyzeBehaviour analyzeBehaviour;

    public PreconditionMetAction(EventListenerContext eventListenerContext, AnalyzeBehaviour analyzeBehaviour) {
        super(eventListenerContext);
        this.analyzeBehaviour = analyzeBehaviour;
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof PreconditionMetEvent) {
            Connection connection = ((BaseNeedAndConnectionSpecificEvent) event).getCon();

            TaxiBotContextWrapper botContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            GoalInstantiationResult preconditionEventPayload = ((PreconditionEvent) event).getPayload();

            DepartureAddress departureAddress = InformationExtractor.getDepartureAddress(preconditionEventPayload);
            DestinationAddress destinationAddress = InformationExtractor.getDestinationAddress(preconditionEventPayload);

            final ParseableResult checkOrderResponse = new ParseableResult(botContextWrapper.getMobileBooking().checkOrder(departureAddress, destinationAddress));
            final String preconditionUri = ((PreconditionEvent) event).getPreconditionUri();

            if(!checkOrderResponse.isError()) {
                final ConnectionMessageCommandEvent connectionMessageCommandEvent = new ConnectionMessageCommandEvent(connection, preconditionEventPayload.getInstanceModel());

                ctx.getEventBus().subscribe(ConnectionMessageCommandResultEvent.class, new ActionOnFirstEventListener(ctx, new CommandResultFilter(connectionMessageCommandEvent), new BaseEventBotAction(ctx) {
                    @Override
                    protected void doRun(Event event, EventListener executingListener) throws Exception {
                        ConnectionMessageCommandResultEvent connectionMessageCommandResultEvent = (ConnectionMessageCommandResultEvent) event;
                        if(connectionMessageCommandResultEvent.isSuccess()){
                            Model agreementMessage = WonRdfUtils.MessageUtils.textMessage("Ride from " + departureAddress + " to " + destinationAddress + ": "
                                    + checkOrderResponse + "....Do you want to confirm the taxi order? Then accept the proposal");
                            WonRdfUtils.MessageUtils.addProposes(agreementMessage, ((ConnectionMessageCommandSuccessEvent) connectionMessageCommandResultEvent).getWonMessage().getMessageURI());
                            ctx.getEventBus().publish(new ConnectionMessageCommandEvent(connection, agreementMessage));
                        }else{
                            logger.error("FAILURERESPONSEEVENT FOR PROPOSAL PAYLOAD");
                        }
                    }
                }));

                ctx.getEventBus().publish(connectionMessageCommandEvent);
            }else {
                analyzeBehaviour.removePreconditionMetPending(preconditionUri);
                analyzeBehaviour.addPreconditionMetError(preconditionUri);

                Model errorMessage = WonRdfUtils.MessageUtils.textMessage(checkOrderResponse.toString() + "\n type 'recheck' to check again");
                ctx.getEventBus().publish(new ConnectionMessageCommandEvent(connection, errorMessage));
            }
        }
    }
}
