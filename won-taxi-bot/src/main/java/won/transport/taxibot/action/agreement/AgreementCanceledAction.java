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

package won.transport.taxibot.action.agreement;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.behaviour.AnalyzeBehaviour;
import won.bot.framework.eventbot.event.BaseAtomAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCancellationAcceptedEvent;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCancellationRequestedEvent;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.transport.taxibot.entity.ParseableResult;
import won.transport.taxibot.impl.TaxiBotContextWrapper;

import java.lang.invoke.MethodHandles;
import java.net.URI;

public class AgreementCanceledAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private AnalyzeBehaviour analyzeBehaviour;

    public AgreementCanceledAction(EventListenerContext eventListenerContext, AnalyzeBehaviour analyzeBehaviour) {
        super(eventListenerContext);
        this.analyzeBehaviour = analyzeBehaviour;
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && (event instanceof AgreementCancellationRequestedEvent || event instanceof AgreementCancellationAcceptedEvent|| event instanceof CloseFromOtherAtomEvent)) { //TODO: CLOSE FROM OTHER ATOM IS NOT REALLY COOL TO DO HERE
            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            Connection connection = ((BaseAtomAndConnectionSpecificEvent) event).getCon();

            //RETRIEVE ORDER ID FROM CON URI FROM FACTORYBOTCONTEXTWRAPPER
            URI agreementURI = event instanceof AgreementEvent ? ((AgreementEvent) event).getAgreementUri() : null;

            URI messageUri = null;
            boolean isRequestedCancellation = false;

            if(event instanceof AgreementCancellationRequestedEvent){
                messageUri = ((AgreementCancellationRequestedEvent) event).getMessageUri();
                isRequestedCancellation = true;
            }

            if(agreementURI != null) {
                String offerId = taxiBotContextWrapper.getOfferIdForAgreementURI(agreementURI);


                if (offerId != null) {
                    logger.debug("Trying to cancel with the offerId: " + offerId + " for agreementURI: " + agreementURI + ((messageUri != null)? (" Cancellation is requested by the Client "+"(messageUri which contains the proposeToCancel) "+messageUri+")") : "Cancellation was already accepted by the Client"));
                    Model messageModel;

                    ParseableResult cancelOrderResult = new ParseableResult(taxiBotContextWrapper.getMobileBooking().cancelOrder(offerId));
                    if(!cancelOrderResult.isError()){
                        if(isRequestedCancellation) {
                            messageModel = WonRdfUtils.MessageUtils.textMessage("Order Cancellation accepted and successfully executed:\n\n"+cancelOrderResult);
                            WonRdfUtils.MessageUtils.addAccepts(messageModel, messageUri);
                        }else{
                            messageModel = WonRdfUtils.MessageUtils.textMessage("Order Cancellation Cancellation successfully executed:\n\n"+cancelOrderResult);
                        }
                        analyzeBehaviour.removeProposalReferences(agreementURI);
                        taxiBotContextWrapper.removeOfferIdForAgreementURI(agreementURI);
                    }else{
                        messageModel = WonRdfUtils.MessageUtils.textMessage(cancelOrderResult.toString());
                        if(isRequestedCancellation) {
                            WonRdfUtils.MessageUtils.addRejects(messageModel, messageUri);
                        }
                    }
                    getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(connection, messageModel));
                } else {
                    logger.debug("No Offer present for agreementURI:" + agreementURI + ", no need to cancel anything");
                }
            }else{
                logger.debug("No agreement present, no need to cancel anything");
            }
        }
    }
}
