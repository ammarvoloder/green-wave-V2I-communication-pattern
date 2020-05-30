package at.tuwien.dse.statustrackingservice.dao;

import at.tuwien.dse.statustrackingservice.connection.Connection;
import at.tuwien.dse.statustrackingservice.dto.Movement;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Repository
public class StatusTrackingDAO {

    private static final String MOVEMENT_COLLECTION = "movements";

    /**
     * movements - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Movement> movements;
    /**
     * trafficLights - MongoDB Collection used for storing traffic lights
     */
    private MongoCollection<Document> trafficLights;

    @PostConstruct
    private void initialize(){
        try {
            movements = Connection.getDatabase().getCollection(MOVEMENT_COLLECTION,Movement.class);
        } catch (IOException e) {
          //  LOG.error("Error while connecting to MongoDB.");
        }
    }

    public void addMovement(Movement movement){
        try {
            //LOG.info("Inserting new vehicle: " + vehicle.getVin());
            movements.insertOne(movement);
        } catch (MongoWriteException e){
            //LOG.error("Error while writing in Mongo");
        }

    }
}
