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

package won.transport.taxibot.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.bot.context.FactoryBotContextWrapper;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractCreateAtomAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.factory.FactoryHintEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.exception.WonMessageBuilderException;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageBuilder;
import won.protocol.model.AtomGraphType;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.AtomModelWrapper;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.vocabulary.WON;
import won.protocol.vocabulary.WONMATCH;
import won.transport.taxibot.service.InformationExtractor;

import java.io.ByteArrayInputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;

/**
 * Creates a specific FactoryOffer (that will not be matched with anybody)
 */
public class CreateFactoryOfferAction extends AbstractCreateAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final URI STUB_NEED_URI = URI.create("http://example.com/content");
    private static final URI STUB_SHAPES_URI = URI.create("http://example.com/shapes");

    private static final String goalString;

    static {
        goalString = InformationExtractor.loadStringFromFile("/correct/goals.trig");
    }

    public CreateFactoryOfferAction(EventListenerContext eventListenerContext, URI... sockets) {
        super(eventListenerContext, (eventListenerContext.getBotContextWrapper()).getAtomCreateListName(), false, true, sockets);
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

        Model factoryOfferModel = createFactoryOfferFromTemplate(ctx, factoryHintEvent.getFactoryAtomURI(), factoryHintEvent.getRequesterURI());
        URI factoryOfferURI = WonRdfUtils.AtomUtils.getAtomURI(factoryOfferModel);
        Model shapesModel = createShapesModelFromTemplate(ctx, factoryHintEvent.getFactoryAtomURI());
        //TODO: ADD BOT ALREADY PROCESSED THE NEEDURI FOR THIS FACTORY OFFER
        logger.debug("creating factoryoffer on won node {} with content {} ", wonNodeUri, StringUtils.abbreviate(RdfUtils.toString(factoryOfferModel), 150));

        WonMessage createNeedMessage = createWonMessage(ctx.getWonNodeInformationService(), factoryOfferURI, wonNodeUri, factoryOfferModel, shapesModel);
        EventBotActionUtils.rememberInList(ctx, factoryOfferURI, uriListName);

        EventListener successCallback = successEvent -> {
            logger.debug("factoryoffer creation successful, new atom URI is {}", factoryOfferURI);
            //publish connect between the specific offer and the requester atom
            ((FactoryBotContextWrapper) ctx.getBotContextWrapper()).addFactoryAtomURIOfferRelation(factoryOfferURI, factoryHintEvent.getFactoryAtomURI());
            bus.publish(new ConnectCommandEvent(factoryOfferURI, factoryHintEvent.getRequesterURI(), "Hi this is the Automated Taxi-Bot-Demo! I might be able to order a taxi for you.\n\nLet's see, but first you have to accept this chat request!"));
        };

        EventListener failureCallback = failureEvent -> {
            String textMessage = WonRdfUtils.MessageUtils.getTextMessage(((FailureResponseEvent) failureEvent).getFailureMessage());

            logger.debug("factoryoffer creation failed for atom URI {}, original message URI {}: {}", new Object[]{factoryOfferURI, ((FailureResponseEvent) failureEvent).getOriginalMessageURI(), textMessage});
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
        DefaultAtomModelWrapper factoryNeedModelWrapper = new DefaultAtomModelWrapper(factoryNeedDataSet);

        Dataset requesterNeedDataSet = ctx.getLinkedDataSource().getDataForResource(requesterURI);
        DefaultAtomModelWrapper requesterNeedModelWrapper = new DefaultAtomModelWrapper(requesterNeedDataSet);

        String connectTitle =  "Test TaxiBot Offer";//factoryNeedModelWrapper.getSomeTitleFromIsOrAll() + "-" + requesterNeedModelWrapper.getSomeTitleFromIsOrAll();

        final URI atomURI = ctx.getWonNodeInformationService().generateAtomURI(ctx.getNodeURISource().getNodeURI());
        DefaultAtomModelWrapper atomModelWrapper = new DefaultAtomModelWrapper(atomURI.toString());

        atomModelWrapper.setTitle(connectTitle);
        //atomModelWrapper.setDescription("This is a automatically created atom by the TaxiBot");
        atomModelWrapper.addFlag(WONMATCH.NoHintForCounterpart);
        atomModelWrapper.addFlag(WONMATCH.NoHintForMe);
        atomModelWrapper.setShapesGraphReference(STUB_SHAPES_URI);

        int i = 1;
        for(URI socket : sockets){
            atomModelWrapper.addSocket(atomURI + "#socket" + i, socket.toString());
            i++;
        }

        return atomModelWrapper.copyAtomModel(AtomGraphType.ATOM);
    }

    private Model createShapesModelFromTemplate(EventListenerContext ctx, URI factoryNeedURI) {
        Dataset dataset = DatasetFactory.createGeneral();
        RDFDataMgr.read(dataset, new ByteArrayInputStream(goalString.getBytes()), RDFFormat.TRIG.getLang());

        return dataset.getUnionModel();
    }



    private WonMessage createWonMessage(
        WonNodeInformationService wonNodeInformationService, URI atomURI, URI wonNodeURI, Model atomModel, Model shapesModel) throws WonMessageBuilderException {

        AtomModelWrapper atomModelWrapper = new AtomModelWrapper(atomModel, null);

        atomModelWrapper.addFlag(WONMATCH.NoHintForMe);
        atomModelWrapper.addFlag(WONMATCH.NoHintForCounterpart);

        RdfUtils.replaceBaseURI(atomModel, atomURI.toString(), true);

        Dataset contentDataset = DatasetFactory.createGeneral();

        contentDataset.addNamedModel(STUB_NEED_URI.toString(), atomModel);
        contentDataset.addNamedModel(STUB_SHAPES_URI.toString(), shapesModel);

        return WonMessageBuilder.setMessagePropertiesForCreate(
                wonNodeInformationService.generateEventURI(wonNodeURI),
                atomURI,
                wonNodeURI)
                .addContent(contentDataset)
                .build();
    }
}
