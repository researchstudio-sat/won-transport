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

package won.transport.taxi.bot.action;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.ProposalCanceledEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.model.Connection;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;

import java.net.URI;

public class CancelTaxiOrderAction extends BaseEventBotAction {

    public CancelTaxiOrderAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && (event instanceof ProposalCanceledEvent || event instanceof CloseFromOtherNeedEvent)) {
            TaxiBotContextWrapper taxiBotContextWrapper = (TaxiBotContextWrapper) ctx.getBotContextWrapper();

            Connection con = ((BaseNeedAndConnectionSpecificEvent) event).getCon();

            //RETRIEVE ORDER ID FROM CON URI FROM FACTORYBOTCONTEXTWRAPPER
            URI offerURI = con.getNeedURI();
            String offerId = taxiBotContextWrapper.getOfferIdForOfferURI(con.getNeedURI());

            if(offerId != null){
                logger.debug("Trying to cancel with the offerId: "+offerId+" fo offerURI: "+offerURI);
                taxiBotContextWrapper.getMobileBooking().cancelOrder(offerId);
            }else{
                logger.debug("No Offer present for offerUri:"+offerURI+", no need to cancel anything");
            }
        }
    }
}
