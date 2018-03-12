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
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementAcceptedEvent;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCanceledEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionMetEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionUnmetEvent;
import won.bot.framework.eventbot.event.impl.analyzation.proposal.ProposalReceivedEvent;
import won.bot.framework.eventbot.event.impl.factory.FactoryHintEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherNeedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.transport.taxi.bot.action.*;
import won.transport.taxi.bot.action.agreement.AgreementAcceptedAction;
import won.transport.taxi.bot.action.agreement.AgreementCanceledAction;
import won.transport.taxi.bot.action.agreement.ProposeAgreementAction;
import won.transport.taxi.bot.action.precondition.PreconditionUnmetAction;
import won.transport.taxi.bot.action.proposal.ProposalReceivedAction;

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
        bus.subscribe(PreconditionMetEvent.class,
            new ActionOnEventListener(
                ctx,
                "PreconditionMetEvent",
                new ProposeAgreementAction(ctx)
            )
        );

        bus.subscribe(PreconditionUnmetEvent.class,
            new ActionOnEventListener(
                ctx,
                "PreconditionUnmetEvent",
                new PreconditionUnmetAction(ctx)
            )
        );

        bus.subscribe(AgreementAcceptedEvent.class,
            new ActionOnEventListener(
                ctx,
                "AgreementAcceptedEvent",
                new AgreementAcceptedAction(ctx)
            )
        );

        bus.subscribe(ProposalReceivedEvent.class,
             new ActionOnEventListener(
                 ctx,
                 "ProposalReceivedEvent",
                 new ProposalReceivedAction(ctx)
             )
        );

        //TODO: not sure if necessary, depending on what ProposalCanceledEvent is (either cancel a proposal before accepting, or cancel a proposal that was already accepted....)
        bus.subscribe(AgreementCanceledEvent.class,
            new ActionOnEventListener(
                ctx,
                "AgreementCanceledEvent",
                new AgreementCanceledAction(ctx)
            )
        );

        /*bus.subscribe(AgreementErrorEvent.class,
            new ActionOnEventListener(
                ctx,
                "AgreementErrorEvent",
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
                    new AgreementCanceledAction(ctx)
                )
            )
        );

        //This event is for control purposes
        bus.subscribe(MessageFromOtherNeedEvent.class,
            new ActionOnEventListener(
                ctx,
                new ControlMessageAction(ctx)
            )
        );
    }
}
