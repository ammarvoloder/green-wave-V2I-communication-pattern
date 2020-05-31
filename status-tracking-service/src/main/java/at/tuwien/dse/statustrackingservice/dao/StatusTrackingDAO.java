package at.tuwien.dse.statustrackingservice.dao;

import at.tuwien.dse.statustrackingservice.connection.Connection;
import at.tuwien.dse.statustrackingservice.dto.Movement;
import at.tuwien.dse.statustrackingservice.dto.TrafficLightStatus;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Repository
public class StatusTrackingDAO {

    public static final Logger LOG = LoggerFactory.getLogger(StatusTrackingDAO.class);

    private static final String MOVEMENT_COLLECTION = "movements";

    private static final String STATUS_COLLECCTION = "statuses";


    /**
     * movements - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Movement> movements;
    /**
     * trafficLights - MongoDB Collection used for storing traffic lights
     */
    private MongoCollection<TrafficLightStatus> trafficLightStatus;

    @PostConstruct
    private void initialize() {
        try {
            movements = Connection.getDatabase().getCollection(MOVEMENT_COLLECTION, Movement.class);
            trafficLightStatus = Connection.getDatabase().getCollection(STATUS_COLLECCTION, TrafficLightStatus.class);
        } catch (IOException e) {
            LOG.error("Error while connecting to MongoDB.");
        }
    }

    public void addMovement(Movement movement) {
        try {
            movements.insertOne(movement);
        } catch (MongoWriteException e) {
            LOG.error("Error while writing in Mongo");
        }

    }


    public void addTrafficLightStatus(TrafficLightStatus status) {
        try {
            trafficLightStatus.insertOne(status);
        } catch (MongoWriteException e) {
            LOG.error("Error while writing in Mongo");
        }

    }
}
