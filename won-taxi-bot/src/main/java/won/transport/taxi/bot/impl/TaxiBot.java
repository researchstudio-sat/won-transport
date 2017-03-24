package won.transport.taxi.bot.impl;

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.MultipleActions;
import won.bot.framework.eventbot.action.impl.needlifecycle.DeactivateNeedAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.lifecycle.InitializeEvent;
import won.bot.framework.eventbot.event.impl.mail.OpenConnectionEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherNeedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.HintFromMatcherEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherNeedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.OpenFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.transport.taxi.bot.action.*;
import won.transport.taxi.bot.client.MobileBooking;
import won.transport.taxi.bot.event.FactoryHintEvent;
import won.transport.taxi.bot.event.FactoryOfferCreatedEvent;

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

        bus.subscribe(FactoryOfferCreatedEvent.class,
            new ActionOnEventListener(
                ctx,
                "FactoryOfferCreatedEvent",
                new ConnectFactoryOfferAction(ctx)
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
