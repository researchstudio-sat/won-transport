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

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.needlifecycle.AbstractCreateNeedAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.model.BasicNeedType;
import won.protocol.util.NeedModelBuilder;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.transport.taxi.bot.client.MobileBooking;
import won.transport.taxi.bot.event.FactoryHintEvent;

import java.net.URI;

/**
 * Creates a specific FactoryOffer (that will not be matched with anybody)
 */
public class CreateFactoryOfferAction extends AbstractCreateNeedAction {
    private MobileBooking mobileBooking;

    public CreateFactoryOfferAction(EventListenerContext eventListenerContext, MobileBooking mobileBooking, String uriListName, URI... facets) {
        super(eventListenerContext, uriListName, false, true, facets);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        if(!(event instanceof FactoryHintEvent)) {
            logger.error("CreateFactoryOfferAction can only handle FactoryHintEvent");
            return;
        }
        FactoryHintEvent factoryHintEvent = (FactoryHintEvent) event;

        EventBus bus = getEventListenerContext().getEventBus();
        EventListenerContext ctx = getEventListenerContext();
        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();

        Model factoryOfferModel = createFactoryOfferFromTemplate(ctx, factoryHintEvent.getFactoryNeedURI(), factoryHintEvent.getRequesterURI());
        URI factoryOfferURI = WonRdfUtils.NeedUtils.getNeedURI(factoryOfferModel);

        logger.debug("creating factoryoffer on won node {} with content {} ", wonNodeUri, StringUtils.abbreviate(RdfUtils.toString(factoryOfferModel), 150));

        WonMessage createNeedMessage = createWonMessage(ctx.getWonNodeInformationService(), factoryOfferURI, wonNodeUri, factoryOfferModel, this.usedForTesting, this.doNotMatch);
        EventBotActionUtils.rememberInList(ctx, factoryOfferURI, uriListName);

        EventListener successCallback = successEvent -> {
            logger.debug("factoryoffer creation successful, new need URI is {}", factoryOfferURI);
            //publish connect between the specific offer and the requester need
            bus.publish(new ConnectCommandEvent(factoryOfferURI, factoryHintEvent.getRequesterURI()));
        };

        EventListener failureCallback = failureEvent -> {
            String textMessage = WonRdfUtils.MessageUtils.getTextMessage(((FailureResponseEvent) failureEvent).getFailureMessage());

            logger.debug("factoryoffer creation failed for need URI {}, original message URI {}: {}", new Object[]{factoryOfferURI, ((FailureResponseEvent) failureEvent).getOriginalMessageURI(), textMessage});
            EventBotActionUtils.removeFromList(getEventListenerContext(), factoryOfferURI, uriListName);
        };

        EventBotActionUtils.makeAndSubscribeResponseListener(createNeedMessage, successCallback, failureCallback, getEventListenerContext());

        logger.debug("registered listeners for response to message URI {}", createNeedMessage.getMessageURI());
        getEventListenerContext().getWonMessageSender().sendWonMessage(createNeedMessage);
        logger.debug("factoryoffer creation message sent with message URI {}", createNeedMessage.getMessageURI());

    }

    private Model createFactoryOfferFromTemplate(EventListenerContext ctx, URI factoryNeedURI, URI requesterURI){
        //TODO: retrieve real template from factory
        Dataset factoryNeedDataSet = ctx.getLinkedDataSource().getDataForResource(factoryNeedURI);
        Dataset requesterNeedDataSet = ctx.getLinkedDataSource().getDataForResource(requesterURI);
        String connectTitle =  WonRdfUtils.NeedUtils.getNeedTitle(requesterNeedDataSet) + "<->" + WonRdfUtils.NeedUtils.getNeedTitle(factoryNeedDataSet);

        Model factoryOfferModel = new NeedModelBuilder()
                .setTitle(connectTitle)
                .setBasicNeedType(BasicNeedType.SUPPLY)
                .setDescription("This is a automatically created need by the TaxiBot")
                .setUri(ctx.getWonNodeInformationService().generateNeedURI(ctx.getNodeURISource().getNodeURI()))
                .setFacetTypes(facets)
                .build();

        return factoryOfferModel;
    }
}
