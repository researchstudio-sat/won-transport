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
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementAcceptedEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.client.entity.Parameter.Error;
import won.transport.taxi.bot.client.entity.Result;
import won.transport.taxi.bot.entity.ParseableResult;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;

import java.net.URI;

/**
 * Created by fsuda on 08.05.2017.
 */
public class AgreementAcceptedAction extends BaseEventBotAction {
    public AgreementAcceptedAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof AgreementAcceptedEvent) {
            Connection con = ((AgreementAcceptedEvent) event).getCon();
            URI agreementUri = ((AgreementAcceptedEvent) event).getAgreementUri();

            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            DepartureAddress departureAddress = InformationExtractor.getDepartureAddress(((AgreementAcceptedEvent) event).getPayload());
            DestinationAddress destinationAddress = InformationExtractor.getDestinationAddress(((AgreementAcceptedEvent) event).getPayload());

            ParseableResult createOrderResult = new ParseableResult(taxiBotContextWrapper.getMobileBooking().createOrder(departureAddress, destinationAddress));
            String orderId = "";

            Model messageModel;

            if(createOrderResult.isError()) {
                messageModel = WonRdfUtils.MessageUtils.textMessage(createOrderResult.toString());
                WonRdfUtils.MessageUtils.addProposesToCancel(messageModel, agreementUri);
            }else {
                orderId = createOrderResult.getOrderId().getValue();
                messageModel = WonRdfUtils.MessageUtils.textMessage("Ride from " + departureAddress + " to " + destinationAddress + ": "
                        + "Your Order is: " + createOrderResult
                        +"....Get into the Taxi when it arrives!");
                taxiBotContextWrapper.addOfferIdForAgreementURI(agreementUri, orderId);
            }

            ctx.getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
        }
    }
}
