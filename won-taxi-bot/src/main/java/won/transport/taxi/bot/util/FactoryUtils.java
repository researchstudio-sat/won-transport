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
