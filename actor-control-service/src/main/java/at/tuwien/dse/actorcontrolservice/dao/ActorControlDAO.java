package at.tuwien.dse.actorcontrolservice.dao;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;

import at.tuwien.dse.actorcontrolservice.connection.Connection;
import at.tuwien.dse.actorcontrolservice.dto.Movement;

@Repository
public class ActorControlDAO
{

    private static final String MOVEMENT_COLLECTION = "movements";

    /**
     * movements - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Movement> movements;

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
