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

import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.event.FactoryOfferConfirmedEvent;
import won.transport.taxi.bot.event.FactoryOfferValidEvent;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;

/**
 * Created by fsuda on 08.05.2017.
 */
public class ExecuteTaxiOrderAction extends BaseEventBotAction {
    public ExecuteTaxiOrderAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof FactoryOfferConfirmedEvent) {
            //TODO: CONFIRM TAXI ORDER WITH MOBILEBOOKING INTERFACE SEND CONFIRM MESSAGE
            Connection con = ((FactoryOfferConfirmedEvent) event).getCon();
            Model messageModel = WonRdfUtils.MessageUtils.textMessage("TAXI ORDER CONFIRMED IT WILL ARRIVE AT SOME TIME"); //TODO: Change Message

            getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
        }
    }
}
