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

package won.transport.taxi.bot.action;

import org.apache.jena.query.Dataset;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.OpenFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.transport.taxi.bot.util.FactoryUtils;

import java.net.URI;

public class OpenedFactoryOfferAction extends BaseEventBotAction {
    private String factoryListName;

    public OpenedFactoryOfferAction(EventListenerContext eventListenerContext, String factoryListName) {
        super(eventListenerContext);
        this.factoryListName = factoryListName;
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        if(!(event instanceof OpenFromOtherNeedEvent)) {
            logger.error("OpenedFactoryOfferAction can only handle OpenFromOtherNeedEvent");
            return;
        }

        URI ownURI = ((OpenFromOtherNeedEvent) event).getNeedURI();
        EventListenerContext ctx = getEventListenerContext();


        if(FactoryUtils.isUriInList(ctx, factoryListName, ownURI)) {
            logger.warn("Opened Connection on factoryneed, no proceeding actions will be called");
            return;
        }

        URI requesterURI = ((OpenFromOtherNeedEvent) event).getRemoteNeedURI();
        URI connectionURI =  ((OpenFromOtherNeedEvent) event).getConnectionURI();

        Dataset requesterNeedDataSet = ctx.getLinkedDataSource().getDataForResource(requesterURI);

        //TODO: TRY TO EXTRACT INFORMATION THAT IS ALREADY PRESENT IN THE DATA (e.g. LatLng, StreetAdress, PLZ etc)

    }
}