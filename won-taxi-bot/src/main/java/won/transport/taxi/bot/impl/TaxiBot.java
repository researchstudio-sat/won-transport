package won.transport.taxi.bot.impl;

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.lifecycle.InitializeEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.HintFromMatcherEvent;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.transport.taxi.bot.action.Hint2TaxiAction;
import won.transport.taxi.bot.action.InitBotAction;
import won.transport.taxi.bot.event.InitBotEvent;

/**
 * Created by fsuda on 27.02.2017.
 */
public class TaxiBot extends EventBot {
    private static final String NAME_NEEDS = "taxiNeeds";

    private EventBus bus;

    @Override
    protected void initializeEventListeners() {
        logger.info("INITIALIZING EVENT LISTENERS FOR TAXIBOT");
        EventListenerContext ctx = getEventListenerContext();

        bus = getEventBus();

        bus.subscribe(HintFromMatcherEvent.class,
                new ActionOnEventListener(ctx,
                    "HintReceived",
                    new Hint2TaxiAction(ctx, NAME_NEEDS)
                ));

        bus.subscribe(InitializeEvent.class,
                new ActionOnEventListener(
                    ctx,
                    "InitTaxiBot",
                    new InitBotAction(ctx, NAME_NEEDS)
                ));
    }


}
