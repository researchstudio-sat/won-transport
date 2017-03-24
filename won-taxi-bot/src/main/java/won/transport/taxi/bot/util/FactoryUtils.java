package won.transport.taxi.bot.util;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;

import java.net.URI;

/**
 * Created by fsuda on 24.03.2017.
 */
public class FactoryUtils {
    public static boolean isUriInList(EventListenerContext ctx, String listName, URI uri){ //TODO: move to EventBotActionUtils.java
        for(URI factoryNeedURI : ctx.getBotContext().getNamedNeedUriList(listName)){
            if(factoryNeedURI.equals(uri)){
                return true;
            }
        }

        return false;
    }
}
