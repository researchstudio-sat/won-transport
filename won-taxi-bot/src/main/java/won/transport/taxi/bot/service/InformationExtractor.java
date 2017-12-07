package won.transport.taxi.bot.service;

import won.protocol.model.Connection;
import won.transport.taxi.bot.client.entity.Parameter.DepartureAddress;
import won.transport.taxi.bot.client.entity.Parameter.DestinationAddress;

import java.net.URI;

/**
 * Created by fsuda on 30.11.2017.
 */
public class InformationExtractor {
    //TODO; Create data based on the real info from the payload

    public static DepartureAddress getDepartureAdress(Object payload){
        return new DepartureAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
    }

    public static DestinationAddress getDestinationAdress(Object payload){
        return new DestinationAddress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "346",
                "");
    }

    public static URI getAgreementURI(Object payload){
        //TODO: REIMPL THIS CURRENTLY WE EXPECT A CONNECTION
        return ((Connection)payload).getConnectionURI();
    }
}
