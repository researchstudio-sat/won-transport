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
import won.bot.framework.eventbot.behaviour.AnalyzeBehaviour;

import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.analyzation.*;
import won.bot.framework.eventbot.event.impl.factory.FactoryHintEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.transport.taxi.bot.action.*;

/**
 * Created by fsuda on 27.02.2017.
 */
public class TaxiBot extends FactoryBot {
    private EventBus bus;

    protected void initializeFactoryEventListeners() {
        bus = getEventBus();
        EventListenerContext ctx = getEventListenerContext();

        AnalyzeBehaviour analyzeBehaviour = new AnalyzeBehaviour(ctx);
        analyzeBehaviour.activate();

        //Analyzation Events
        bus.subscribe(GoalSatisfiedEvent.class,
            new ActionOnEventListener(
                ctx,
                "GoalSatisfiedEvent",
                new TaxiOfferProposalAction(ctx)
            )
        );


        //GoalUnsatisfiedEvent is the superclass of GoalShapeMissingEvent and GoalShapeAmbivalentEvent
        /*bus.subscribe(GoalUnsatisfiedEvent.class,
            new ActionOnEventListener(
                ctx,
                "GoalUnsatisfiedEvent",
                null
            )
        );*/

        bus.subscribe(ProposalAcceptedEvent.class,
            new ActionOnEventListener(
                ctx,
                "ProposalAcceptedEvent",
                new ExecuteTaxiOrderAction(ctx)
            )
        );

        //TODO: not sure if necessary, depending on what ProposalCanceledEvent is (either cancel a proposal before accepting, or cancel a proposal that was already accepted....)
        bus.subscribe(ProposalCanceledEvent.class,
            new ActionOnEventListener(
                ctx,
                "ProposalCanceledEvent",
                new CancelTaxiOrderAction(ctx)
            )
        );

        /*bus.subscribe(ProposalErrorEvent.class,
            new ActionOnEventListener(
                ctx,
                "ProposalErrorEvent",
                null
            )
        );

        bus.subscribe(PassThroughEvent.class,
            new ActionOnEventListener(
                ctx,
                "PassThroughEvent",
                null
            )
        );*/

        //Other Events
        bus.subscribe(FactoryHintEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryHintEvent",
                new CreateFactoryOfferAction(ctx)
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
    }
}
