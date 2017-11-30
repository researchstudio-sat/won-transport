package won.transport.taxi.bot.service;

import won.transport.taxi.bot.client.entity.Parameter.DepartureAdress;
import won.transport.taxi.bot.client.entity.Parameter.DestinationAdress;

import java.net.URI;

/**
 * Created by fsuda on 30.11.2017.
 */
public class InformationExtractor {
    //TODO; Create data based on the real info from the payload

    public static DepartureAdress getDepartureAdress(Object payload){
        return new DepartureAdress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "345",
                "");
    }

    public static DestinationAdress getDestinationAdress(Object payload){
        return new DestinationAdress(
                11.5599861703,
                48.1448925705,
                "D",
                "80333",
                "München",
                "Karlsstraße",
                "346",
                "");
    }
}
