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

package won.transport.taxi.bot.action.precondition;

import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.BaseAtomAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionUnmetEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.impl.TaxiBotContextWrapper;
import won.utils.goals.GoalInstantiationResult;

/**
 * Action that sends a message into the conversation about the missing/faulty preconditions(shapes) within the PreconditionUnmetEvent
 */
public class PreconditionUnmetAction extends BaseEventBotAction {

    public PreconditionUnmetAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        if(ctx.getBotContextWrapper() instanceof TaxiBotContextWrapper && event instanceof PreconditionUnmetEvent) {
            Connection con = ((BaseAtomAndConnectionSpecificEvent) event).getCon();

            GoalInstantiationResult preconditionEventPayload = ((PreconditionEvent) event).getPayload();

            String respondWith = "TaxiOrder not possible yet, missing necessary Values: " + preconditionEventPayload;

            Model messageModel = WonRdfUtils.MessageUtils.textMessage(respondWith);
            //TODO: Create Message that tells the other side which preconditions(shapes) are not yet met in a better way and not just by pushing a string into the conversation
            getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
        }
    }
}
