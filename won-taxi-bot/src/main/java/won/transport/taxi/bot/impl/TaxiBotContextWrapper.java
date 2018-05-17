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

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.bot.context.FactoryBotContextWrapper;
import won.bot.framework.eventbot.behaviour.BotBehaviour;
import won.transport.taxi.bot.client.MobileBooking;

import java.net.URI;

public class TaxiBotContextWrapper extends FactoryBotContextWrapper {
    private MobileBooking mobileBooking;
    private String agreementUriOfferIdMapName = getBotName() + ":agreementUriOfferIdMap";

    public TaxiBotContextWrapper(BotContext botContext, MobileBooking mobileBooking, String botName) {
        super(botContext, botName);
        this.mobileBooking = mobileBooking;
    }

    public MobileBooking getMobileBooking() {
        return mobileBooking;
    }

    public String getOfferIdForAgreementURI(URI agreementURI) {
        return (String) this.getBotContext().loadFromObjectMap(agreementUriOfferIdMapName, agreementURI.toString());
    }

    public void addOfferIdForAgreementURI(URI agreementURI, String offerId) {
        this.getBotContext().saveToObjectMap(agreementUriOfferIdMapName, agreementURI.toString(), offerId);
    }

    public void removeOfferIdForAgreementURI(URI agreementURI) {
        this.getBotContext().removeFromObjectMap(agreementUriOfferIdMapName, agreementURI.toString());
    }
}
