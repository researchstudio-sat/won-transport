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
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionMetEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.client.entity.Result;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;

/**
 * Proposes an agreement based on the Data/Payload given within the PreconditionMetEvent
 */
public class ProposeAgreementAction extends BaseEventBotAction{

    public ProposeAgreementAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof PreconditionMetEvent) {
            Connection con = ((BaseNeedAndConnectionSpecificEvent) event).getCon();

            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            DepartureAddress departureAddress = InformationExtractor.getDepartureAdress(((PreconditionMetEvent) event).getPayload());
            DestinationAddress destinationAddress = InformationExtractor.getDestinationAdress(((PreconditionMetEvent) event).getPayload());

            Result checkOrderResponse = taxiBotContextWrapper.getMobileBooking().checkOrder(departureAddress, destinationAddress);

            String respondWith = "Ride from " + departureAddress + " to " + destinationAddress + ": ";

            for(Parameter param : checkOrderResponse.getParameter()){
                if(param instanceof DisplayText){
                    respondWith = respondWith + ((DisplayText) param).getValue();
                }else if(param instanceof Price){
                    respondWith = respondWith + " for a price of:"+((Price) param).getAmount()+" "+((Price) param).getCurrency();
                }
            }

            Model messageModel = WonRdfUtils.MessageUtils.textMessage(respondWith + "....Do you want to confirm the taxi order? type 'AgreementAcceptedEvent'");
            //TODO: Create Real Proposal and send it over the EventBus (probably via ConnectionMessageCommandEvent)
            //TODO: ERROR CASES
            getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
        }
    }
}
