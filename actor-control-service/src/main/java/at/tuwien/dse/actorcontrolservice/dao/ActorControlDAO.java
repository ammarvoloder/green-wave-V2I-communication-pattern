package at.tuwien.dse.actorcontrolservice.dao;

import at.tuwien.dse.actorcontrolservice.connection.Connection;
import at.tuwien.dse.actorcontrolservice.dto.Movement;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Repository
public class ActorControlDAO {

    private static final String MOVEMENT_COLLECTION = "movements";
    private static final Logger LOG = LoggerFactory.getLogger(ActorControlDAO.class);

    /**
     * movements - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Movement> movements;

    @PostConstruct
    private void initialize() {
        try {
            movements = Connection.getDatabase().getCollection(MOVEMENT_COLLECTION, Movement.class);
        } catch (IOException e) {
            LOG.error("Error while connecting to MongoDB.");
        }
    }

    public void addMovement(Movement movement) {
        try {
            LOG.info("Inserting new movement/speed: " + movement);
            movements.insertOne(movement);
        } catch (MongoWriteException e) {
            LOG.error("Error while writing in Mongo");
        }

    }
}
