package won.transport.taxibot.service;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.NotFoundException;
import won.transport.taxibot.client.entity.Parameter.DepartureAddress;
import won.transport.taxibot.client.entity.Parameter.DestinationAddress;
import won.utils.goals.GoalInstantiationResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Created by fsuda on 30.11.2017.
 */
public class InformationExtractor {

    private static final String fromLocationRetrievalQuery;
    private static final String toLocationRetrievalQuery;
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String NAME = "name";


    static {
        toLocationRetrievalQuery = loadStringFromFile("/correct/toLocationRetrieval.rq");
        fromLocationRetrievalQuery = loadStringFromFile("/correct/fromLocationRetrieval.rq");
    }

    public static DepartureAddress getDepartureAddress(Model payload){
        if(payload != null && !payload.isEmpty()) {
            QuerySolution solution = executeQuery(fromLocationRetrievalQuery, payload);

            if (solution != null) {
                double lat = solution.getLiteral(LAT).getDouble();
                double lon = solution.getLiteral(LON).getDouble();
                return new DepartureAddress(lon, lat);
            }
        }
        return null;
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

    public static DestinationAddress getDestinationAddress(Model payload){
        if(payload != null && !payload.isEmpty()) {
            QuerySolution solution = executeQuery(toLocationRetrievalQuery, payload);

            if (solution != null) {
                double lat = solution.getLiteral(LAT).getDouble();
                double lon = solution.getLiteral(LON).getDouble();
                return new DestinationAddress(lon, lat);
            }
        }
        return null;
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

    public static String getDestinationName(GoalInstantiationResult payload) {
        if(payload != null) {
            QuerySolution solution = executeQuery(toLocationRetrievalQuery, payload.getInstanceModel());

            if (solution != null) {
                return solution.getLiteral(NAME).getString();
            }
        }
        return null;
    }

    public static String getDepartureName(GoalInstantiationResult payload) {
        if(payload != null) {
            QuerySolution solution = executeQuery(fromLocationRetrievalQuery, payload.getInstanceModel());

            if (solution != null) {
                return solution.getLiteral(NAME).getString();
            }
        }
        return null;
    }

    public static String getDestinationName(Model payload){
        if(payload != null && !payload.isEmpty()) {
            QuerySolution solution = executeQuery(toLocationRetrievalQuery, payload);

            if (solution != null) {
                return solution.getLiteral(NAME).getString();
            }
        }
        return null;
    }

    public static String getDepartureName(Model payload){
        if(payload != null && !payload.isEmpty()) {
            QuerySolution solution = executeQuery(fromLocationRetrievalQuery, payload);

            if (solution != null) {
                return solution.getLiteral(NAME).getString();
            }
        }
        return null;
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
