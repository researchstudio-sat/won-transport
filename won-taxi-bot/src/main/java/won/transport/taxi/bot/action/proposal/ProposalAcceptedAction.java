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

package won.transport.taxi.bot.action.proposal;

import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.ProposalAcceptedEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.entity.ParseableResult;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;

import java.net.URI;

public class ProposalAcceptedAction extends BaseEventBotAction {
    public ProposalAcceptedAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof ProposalAcceptedEvent) {
            ProposalAcceptedEvent proposalAcceptedEvent = (ProposalAcceptedEvent) event;
            Connection con = proposalAcceptedEvent.getCon();
            URI agreementUri = proposalAcceptedEvent.getAgreementUri();

            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            DepartureAddress departureAddress = InformationExtractor.getDepartureAddress(proposalAcceptedEvent.getPayload());
            DestinationAddress destinationAddress = InformationExtractor.getDestinationAddress(proposalAcceptedEvent.getPayload());
            String departureName = InformationExtractor.getDepartureName(proposalAcceptedEvent.getPayload());
            String destinationName = InformationExtractor.getDestinationName(proposalAcceptedEvent.getPayload());

            ParseableResult createOrderResult = new ParseableResult(taxiBotContextWrapper.getMobileBooking().createOrder(departureAddress, destinationAddress));
            String orderId = "";

            Model messageModel;

            if(createOrderResult.isError()) {
                messageModel = WonRdfUtils.MessageUtils.textMessage(createOrderResult.toString());
                WonRdfUtils.MessageUtils.addProposesToCancel(messageModel, agreementUri);
            }else {
                orderId = createOrderResult.getOrderId().getValue();
                String rideText = "Your Order has been placed!";

                if((departureName != null || departureAddress != null) && (destinationName != null || destinationAddress != null)) {
                    rideText = "Ride from '" + ((departureName != null) ? departureName : destinationAddress) + "' to '" + ((destinationName != null)? destinationName : destinationAddress) + "':";
                }

                messageModel = WonRdfUtils.MessageUtils.textMessage(rideText +
                        "\n\nYour Order is: " + createOrderResult +
                        "\n\n....Get into the Taxi when it arrives!");
                taxiBotContextWrapper.addOfferIdForAgreementURI(agreementUri, orderId);
            }

            ctx.getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
        }
    }
}
