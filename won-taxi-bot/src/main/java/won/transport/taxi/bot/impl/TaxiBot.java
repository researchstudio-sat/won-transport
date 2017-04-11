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

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.MultipleActions;
import won.bot.framework.eventbot.action.impl.needlifecycle.DeactivateNeedAction;
import won.bot.framework.eventbot.action.impl.wonmessage.execCommand.ExecuteConnectCommandAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.lifecycle.InitializeEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherNeedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.HintFromMatcherEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherNeedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.OpenFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.transport.taxi.bot.action.*;
import won.transport.taxi.bot.client.MobileBooking;
import won.transport.taxi.bot.event.FactoryHintEvent;

/**
 * Created by fsuda on 27.02.2017.
 */
public class TaxiBot extends EventBot{
    private static final String NAME_NEEDS = "taxiNeeds";
    private static final String NAME_FACTORYNEEDS = "taxiBotFactoryNeeds";

    private EventBus bus;
    private MobileBooking mobileBooking;

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        bus = getEventBus();

        bus.subscribe(HintFromMatcherEvent.class,
            new ActionOnEventListener(ctx,
                "HintReceived",
                new FactoryHintCheckAction(ctx, NAME_FACTORYNEEDS)
            ));

        bus.subscribe(InitializeEvent.class,
            new ActionOnEventListener(
                ctx,
                "InitTaxiBot",
                new InitFactoryAction(ctx, NAME_FACTORYNEEDS, NAME_NEEDS)
            ));

        bus.subscribe(FactoryHintEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryHintEvent",
                new CreateFactoryOfferAction(ctx, mobileBooking, NAME_NEEDS)
            ));

        bus.subscribe(ConnectCommandEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferCreatedEvent",
                new ExecuteConnectCommandAction(ctx)
            ));

        bus.subscribe(MessageFromOtherNeedEvent.class,
            new ActionOnEventListener(
                ctx,
                "MessageReceived",
                null//TODO: ADD ACTION
            ));

        bus.subscribe(OpenFromOtherNeedEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferOpened",
                new OpenedFactoryOfferAction(ctx, NAME_FACTORYNEEDS)
            ));

        bus.subscribe(CloseFromOtherNeedEvent.class,
            new ActionOnEventListener(
                ctx,
                    "FactoryOfferClosed",
                    new MultipleActions(ctx,
                        new DeactivateNeedAction(ctx)/*,
                        //TODO CALL ACTION TO CANCEL ORDER*/)
            ));
    }

    // ******* SETTER**********
    public void setMobileBooking(MobileBooking mobileBooking) {
        this.mobileBooking = mobileBooking;
    }
}
