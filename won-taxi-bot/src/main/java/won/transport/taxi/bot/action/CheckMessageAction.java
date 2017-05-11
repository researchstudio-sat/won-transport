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

import won.bot.framework.bot.context.FactoryBotContextWrapper;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.event.FactoryOfferCancelEvent;
import won.transport.taxi.bot.event.FactoryOfferConfirmedEvent;
import won.transport.taxi.bot.event.FactoryOfferValidEvent;

/**
 * Created by fsuda on 04.05.2017.
 */
public class CheckMessageAction extends BaseEventBotAction {
    public CheckMessageAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();
        if(event instanceof MessageFromOtherNeedEvent && ctx.getBotContextWrapper() instanceof FactoryBotContextWrapper) {
            Connection con = ((MessageFromOtherNeedEvent) event).getCon();
            WonMessage msg = ((MessageFromOtherNeedEvent) event).getWonMessage();

            String textMessage = WonRdfUtils.MessageUtils.getTextMessage(msg);

            //TODO: IMPL CORRECT CHECK TO CONFIRM MESSAGE OR QUESTION MESSAGE AS A RESPONSE
            if("confirm".equals(textMessage)) {
                ctx.getEventBus().publish(new FactoryOfferConfirmedEvent(con, msg));
            }else if("cancel".equals(textMessage)) {
                ctx.getEventBus().publish(new FactoryOfferCancelEvent(con, msg));
            }else {
                //TODO: PUBLISH DIFFERENT EVENT IN CASE OF NO MATCH OF MESSAGE
                ctx.getEventBus().publish(new FactoryOfferValidEvent(con, msg));
            }
        }
    }
}
