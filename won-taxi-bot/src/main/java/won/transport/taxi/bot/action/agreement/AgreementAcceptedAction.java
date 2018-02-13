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
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;

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

            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            DepartureAddress departureAddress = InformationExtractor.getDepartureAddress(((AgreementAcceptedEvent) event).getPayload());
            DestinationAddress destinationAddress = InformationExtractor.getDestinationAddress(((AgreementAcceptedEvent) event).getPayload());

            Result createOrderResponse = taxiBotContextWrapper.getMobileBooking().createOrder(departureAddress, destinationAddress);
            //TODO: SAFE ORDER NUMBER WITH CONNECTION URI FOR LATER USE (e.g. checkups and cancelations or stuff) -> taxiBotContextWrapper.addOfferIdForAgreementURI(...
            //TODO: FIGURE OUT HOW TO HANDLE MULTIPLE ORDERS (BLOCK IF ORDER ALREADY EXISTS)

            String respondWith = "";
            String orderId = "";
            boolean errorPresent = false;

            if(createOrderResponse.getError() != null) {
                //TODO: ERROR CASES
                Error error = createOrderResponse.getError();
                respondWith = "ErrorID:"+ error.getId() + " Text: "+ error.getText();
                errorPresent = true;
            }else {
                respondWith = "Ride from " + departureAddress + " to " + destinationAddress + ": ";
                for (Parameter param : createOrderResponse.getParameter()) {
                    if (param instanceof OrderId) {
                        orderId = ((OrderId) param).getValue();
                        respondWith = respondWith + "Your OrderId is: " + orderId + ";";
                    } else if (param instanceof DisplayText) {
                        respondWith = respondWith + ((DisplayText) param).getValue();
                    } else if (param instanceof Price) {
                        respondWith = respondWith + " for a price of:" + ((Price) param).getAmount() + " " + ((Price) param).getCurrency();
                    }
                }
                respondWith += "....Get into the Taxi when it arrives!";

                taxiBotContextWrapper.addOfferIdForAgreementURI(InformationExtractor.getAgreementURI(con), orderId); //TODO: IMPL WITH PAYLOAD AND RETRIEVE REAL OFFER ID
                errorPresent = false;
            }

            Model messageModel = WonRdfUtils.MessageUtils.textMessage(respondWith);
            getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
        }
    }
}
