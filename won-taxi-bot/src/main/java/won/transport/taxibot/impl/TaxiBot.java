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

package won.transport.taxibot.impl;

import won.bot.framework.bot.base.FactoryBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.MultipleActions;
import won.bot.framework.eventbot.action.impl.wonmessage.execCommand.ExecuteDeactivateAtomCommandAction;
import won.bot.framework.eventbot.behaviour.AnalyzeBehaviour;

import won.bot.framework.eventbot.behaviour.BotBehaviour;
import won.bot.framework.eventbot.behaviour.EagerlyPopulateCacheBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCancellationAcceptedEvent;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCancellationRequestedEvent;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.ProposalAcceptedEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionMetEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionUnmetEvent;
import won.bot.framework.eventbot.event.impl.analyzation.proposal.ProposalReceivedEvent;
import won.bot.framework.eventbot.event.impl.factory.FactoryHintEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.transport.taxibot.action.CreateFactoryOfferAction;
import won.transport.taxibot.action.proposal.ProposalAcceptedAction;
import won.transport.taxibot.action.agreement.AgreementCanceledAction;
import won.transport.taxibot.action.agreement.PreconditionMetAction;
import won.transport.taxibot.action.precondition.PreconditionUnmetAction;
import won.transport.taxibot.action.proposal.ProposalReceivedAction;
import won.transport.taxibot.action.ControlMessageAction;

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

        //eagerly cache RDF data
        BotBehaviour eagerlyCacheBehaviour = new EagerlyPopulateCacheBehaviour(ctx);
        eagerlyCacheBehaviour.activate();

        //Analyzation Events
        bus.subscribe(PreconditionMetEvent.class,
            new ActionOnEventListener(
                ctx,
                "PreconditionMetEvent",
                new PreconditionMetAction(ctx, analyzeBehaviour)
            )
        );

        bus.subscribe(PreconditionUnmetEvent.class,
            new ActionOnEventListener(
                ctx,
                "PreconditionUnmetEvent",
                new PreconditionUnmetAction(ctx)
            )
        );

        bus.subscribe(ProposalAcceptedEvent.class,
            new ActionOnEventListener(
                ctx,
                "ProposalAcceptedEvent",
                new ProposalAcceptedAction(ctx)
            )
        );

        bus.subscribe(ProposalReceivedEvent.class,
             new ActionOnEventListener(
                 ctx,
                 "ProposalReceivedEvent",
                 new ProposalReceivedAction(ctx, analyzeBehaviour)
             )
        );

        bus.subscribe(AgreementCancellationAcceptedEvent.class,
            new ActionOnEventListener(
                ctx,
                "AgreementCancellationAcceptedEvent",
                new AgreementCanceledAction(ctx, analyzeBehaviour)
            )
        );

        bus.subscribe(AgreementCancellationRequestedEvent.class,
            new ActionOnEventListener(
                ctx,
                "AgreementCancellationAcceptedEvent",
                new AgreementCanceledAction(ctx, analyzeBehaviour)
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

        bus.subscribe(CloseFromOtherAtomEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferClosed",
                new MultipleActions(
                    ctx,
                    new ExecuteDeactivateAtomCommandAction(ctx),
                    new AgreementCanceledAction(ctx, analyzeBehaviour)
                )
            )
        );

        //This event is for control purposes
        bus.subscribe(MessageFromOtherAtomEvent.class,
            new ActionOnEventListener(
                ctx,
                new ControlMessageAction(ctx, analyzeBehaviour)
            )
        );
    }
}
