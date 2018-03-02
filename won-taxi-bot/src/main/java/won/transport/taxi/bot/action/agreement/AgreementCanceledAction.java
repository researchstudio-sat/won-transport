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

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCanceledEvent;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.model.Connection;
import won.transport.taxi.bot.client.entity.Result;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.transport.taxi.bot.service.InformationExtractor;

import java.net.URI;

public class AgreementCanceledAction extends BaseEventBotAction {

    public AgreementCanceledAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && (event instanceof AgreementCanceledEvent || event instanceof CloseFromOtherNeedEvent)) {
            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            Connection con = ((BaseNeedAndConnectionSpecificEvent) event).getCon();

            //RETRIEVE ORDER ID FROM CON URI FROM FACTORYBOTCONTEXTWRAPPER
            URI agreementURI = event instanceof AgreementEvent ? ((AgreementEvent)event).getAgreementUri(): null;
            if(agreementURI != null) {
                String offerId = taxiBotContextWrapper.getOfferIdForAgreementURI(agreementURI);

                if (offerId != null) {
                    logger.debug("Trying to cancel with the offerId: " + offerId + " fo agreementURI: " + agreementURI);
                    Result cancelOrderResult = taxiBotContextWrapper.getMobileBooking().cancelOrder(offerId);
                    //TODO: IMPL RESPONSE AND ERROR CASES
                    taxiBotContextWrapper.removeOfferIdForAgreementURI(agreementURI);
                } else {
                    logger.debug("No Offer present for agreementURI:" + agreementURI + ", no need to cancel anything");
                }
            }else{
                logger.debug("No agreement present, no need to cancel anything");
            }
        }
    }
}
