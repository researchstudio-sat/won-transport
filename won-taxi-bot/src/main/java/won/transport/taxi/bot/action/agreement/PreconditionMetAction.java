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
import won.transport.taxi.bot.client.entity.Result;
import won.transport.taxi.bot.entity.ParseableResult;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;
import won.utils.goals.GoalInstantiationResult;

import java.net.URI;

/**
 * Proposes an agreement based on the Data/Payload given within the PreconditionMetEvent
 */
public class PreconditionMetAction extends BaseEventBotAction{

    public PreconditionMetAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof PreconditionMetEvent) {
            Connection con = ((BaseNeedAndConnectionSpecificEvent) event).getCon();

            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            GoalInstantiationResult preconditionEventPayload = ((PreconditionEvent) event).getPayload();

            DepartureAddress departureAddress = InformationExtractor.getDepartureAddress(preconditionEventPayload);
            DestinationAddress destinationAddress = InformationExtractor.getDestinationAddress(preconditionEventPayload);

            final ParseableResult checkOrderResponse = new ParseableResult(taxiBotContextWrapper.getMobileBooking().checkOrder(departureAddress, destinationAddress));

            if(!checkOrderResponse.isError()) {
                final ConnectionMessageCommandEvent connectionMessageCommandEvent = new ConnectionMessageCommandEvent(con, preconditionEventPayload.getInstanceModel());

                ctx.getEventBus().subscribe(ConnectionMessageCommandResultEvent.class, new ActionOnFirstEventListener(ctx, new CommandResultFilter(connectionMessageCommandEvent), new BaseEventBotAction(ctx) {
                    @Override
                    protected void doRun(Event event, EventListener executingListener) throws Exception {
                        ConnectionMessageCommandResultEvent connectionMessageCommandResultEvent = (ConnectionMessageCommandResultEvent) event;
                        if(connectionMessageCommandResultEvent.isSuccess()){
                            Model agreementMessage = WonRdfUtils.MessageUtils.textMessage("Ride from " + departureAddress + " to " + destinationAddress + ": "
                                    + checkOrderResponse + "....Do you want to confirm the taxi order? Then accept the proposal");
                            WonRdfUtils.MessageUtils.addProposes(agreementMessage, ((ConnectionMessageCommandSuccessEvent) connectionMessageCommandResultEvent).getWonMessage().getMessageURI());
                            ctx.getEventBus().publish(new ConnectionMessageCommandEvent(con, agreementMessage));
                        }else{
                            logger.error("FAILURERESPONSEEVENT FOR PROPOSAL PAYLOAD");
                            //TODO: MAKE THE PRECONDITION FAIL BY ADDING A TRIPLE THAT SHOULD NOT BE PRESENT AND RETRACT IT AFTER A CERTAIN TIME TO CHECK AGAIN -> you do not need to send an error then once the analyzer also works on messages from your own side
                            //THE ABOVE SENTENCE IS NOT GOING TO BE POSSIBLE, SINCE THE DATA OF THE PROPOSAL WILL NEVER CHANGE AND THUS IT IS NOT POSSIBLE TO ADD A TRIPLE TO SUPPORT THIS
                        }
                    }
                }));

                ctx.getEventBus().publish(connectionMessageCommandEvent);
            }else {
                Model errorMessage = WonRdfUtils.MessageUtils.textMessage(checkOrderResponse.toString());
                ctx.getEventBus().publish(new ConnectionMessageCommandEvent(con, errorMessage));
                //TODO: MAKE THE PRECONDITION FAIL BY ADDING A TRIPLE THAT SHOULD NOT BE PRESENT AND RETRACT IT AFTER A CERTAIN TIME TO CHECK AGAIN -> you do not need to send an error then once the analyzer also works on messages from your own side
                //TODO: ERROR CASES
            }
        }
    }
}