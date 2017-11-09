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

package won.transport.taxi.bot.impl;

import won.bot.framework.bot.base.FactoryBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.MultipleActions;
import won.bot.framework.eventbot.action.impl.wonmessage.execCommand.ExecuteDeactivateNeedCommandAction;
import won.bot.framework.eventbot.action.impl.wonmessage.execCommand.ExecuteConnectCommandAction;
import won.bot.framework.eventbot.action.impl.wonmessage.execCommand.ExecuteConnectionMessageCommandAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.factory.FactoryHintEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherNeedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherNeedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.OpenFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.transport.taxi.bot.action.*;
import won.transport.taxi.bot.event.FactoryOfferCancelEvent;
import won.transport.taxi.bot.event.FactoryOfferConfirmedEvent;
import won.transport.taxi.bot.event.FactoryOfferValidEvent;

/**
 * Created by fsuda on 27.02.2017.
 */
public class TaxiBot extends FactoryBot {
    private EventBus bus;

    protected void initializeFactoryEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        bus = getEventBus();

        bus.subscribe(FactoryHintEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryHintEvent",
                new CreateFactoryOfferAction(ctx)
            )
        );

        bus.subscribe(ConnectCommandEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferCreatedEvent",
                new ExecuteConnectCommandAction(ctx)
            )
        );

        bus.subscribe(MessageFromOtherNeedEvent.class,
            new ActionOnEventListener(
                ctx,
                "MessageReceived",
                new CheckMessageAction(ctx)
            )
        );

        bus.subscribe(OpenFromOtherNeedEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferOpened",
                new OpenedFactoryOfferAction(ctx)
            )
        );

        bus.subscribe(CloseFromOtherNeedEvent.class,
            new ActionOnEventListener(
                ctx,
                    "FactoryOfferClosed",
                    new MultipleActions(
                        ctx,
                        new ExecuteDeactivateNeedCommandAction(ctx),
                        new CancelTaxiOrderAction(ctx)
                    )
            )
        );

        bus.subscribe(FactoryOfferCancelEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferCanceled",
                new CancelTaxiOrderAction(ctx)
            )
        );

        bus.subscribe(FactoryOfferConfirmedEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferConfirmed",
                new ExecuteTaxiOrderAction(ctx)
            )
        );

        bus.subscribe(FactoryOfferValidEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferValid",
                new ConfirmTaxiOrderAction(ctx)
            )
        );

        bus.subscribe(ConnectionMessageCommandEvent.class,
            new ActionOnEventListener(
                    ctx,
                    "ConnectionMessageSend",
                    new ExecuteConnectionMessageCommandAction(ctx)
            )
        );
    }
}
