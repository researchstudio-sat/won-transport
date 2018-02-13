package won.transport.taxi.bot.service;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.NotFoundException;
import won.protocol.model.Connection;
import won.transport.taxi.bot.client.entity.Parameter.DepartureAddress;
import won.transport.taxi.bot.client.entity.Parameter.DestinationAddress;
import won.utils.goals.GoalInstantiationResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;

/**
 * Created by fsuda on 30.11.2017.
 */
public class InformationExtractor {

    private static final String fromLocationRetrievalQuery;
    private static final String toLocationRetrievalQuery;
    private static final String LAT = "lat";
    private static final String LON = "lon";


    static {
        toLocationRetrievalQuery = loadStringFromFile("/temp/toLocationRetrieval.sq"); //TODO: SWITCH THIS TO CORRECT AND REMOVE THESE RESOURCES AFTER
        fromLocationRetrievalQuery = loadStringFromFile("/temp/fromLocationRetrieval.sq"); //TODO: SWITCH THIS TO CORRECT AND REMOVE THESE RESOURCES AFTER
    }

    //TODO; Create data based on the real info from the payload

    public static DepartureAddress getDepartureAddress(Object payload){
        throw new UnsupportedOperationException("Method needs to be implemented (AgreementEvent Payload is not defined yet)");
    }

    public static DepartureAddress getDepartureAddress(GoalInstantiationResult payload) {
        if(payload != null) {
            QuerySolution solution = executeQuery(fromLocationRetrievalQuery, payload.getInstanceModel());

            if (solution != null) {
                double lat = solution.getLiteral(LAT).getDouble();
                double lon = solution.getLiteral(LON).getDouble();
                return new DepartureAddress(lon, lat);
            }
        }
        return null;
    }

    public static DestinationAddress getDestinationAddress(Object payload){
        throw new UnsupportedOperationException("Method needs to be implemented (AgreementEvent Payload is not defined yet)");
    }

    public static DestinationAddress getDestinationAddress(GoalInstantiationResult payload) {
        if(payload != null) {
            QuerySolution solution = executeQuery(toLocationRetrievalQuery, payload.getInstanceModel());

            if (solution != null) {
                double lat = solution.getLiteral(LAT).getDouble();
                double lon = solution.getLiteral(LON).getDouble();
                return new DestinationAddress(lon, lat);
            }
        }
        return null;
    }

    public static URI getAgreementURI(Object payload){
        //TODO: REIMPL THIS CURRENTLY WE EXPECT A CONNECTION
        return ((Connection)payload).getConnectionURI();
    }

    private static QuerySolution executeQuery(String queryString, Model payload) {
        Query query = QueryFactory.create(queryString);
        try(QueryExecution qexec = QueryExecutionFactory.create(query, payload)){
            ResultSet resultSet = qexec.execSelect();
            if (resultSet.hasNext()){
                QuerySolution solution = resultSet.nextSolution();
                return solution;
            }
        }
        return null;
    }

    public static String loadStringFromFile(String filePath) {
        InputStream is  = InformationExtractor.class.getResourceAsStream(filePath);
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(is, writer, Charsets.UTF_8);
        } catch (IOException e) {
            throw new NotFoundException("failed to load resource: " + filePath);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        return writer.toString();
    }
}
