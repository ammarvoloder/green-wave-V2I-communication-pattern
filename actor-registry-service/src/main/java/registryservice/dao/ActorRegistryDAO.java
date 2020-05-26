package registryservice.dao;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import registryservice.connection.Connection;
import registryservice.dto.Vehicle;
import registryservice.service.ActorRegistryService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ActorRegistryDAO {


    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryDAO.class);

    private static final String VEHICLE_COLLECTION = "vehicles";
    private static final String PRODUCER = "producer";
    private static final String MODEL = "model";


    /**
     * vehicles - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Document> vehicles;

    @PostConstruct
    private void initialize(){
        try {
            vehicles = Connection.getDatabase().getCollection("VEHICLE_COLLECTION");
        } catch (IOException e) {
            LOG.error("Error while connecting to MongoDB.");
        }
    }

    public void insertVehicle(Vehicle vehicle){
        try {
            LOG.info("Inserting new vehicle: " + vehicle.getVin());
            Document document = new Document("_id", vehicle.getVin())
                    .append(PRODUCER, vehicle.getProducer())
                    .append(MODEL, vehicle.getModel());
            vehicles.insertOne(document);
        } catch (MongoWriteException e){
            LOG.error("Error while writing in Mongo");
        }

    }

    /**
     * Methods which gets all vehicles
     * @return List of all vehicles
     */
    public List<Vehicle> getAllVehicles() {
        LOG.info("Getting all vehicles");
        FindIterable<Document> document = vehicles.find();
        ArrayList<Vehicle> list = new ArrayList<>();
        for (Document d : document) {
            list.add(new Vehicle(d.getString("_id"), d.getString(MODEL), d.getString(PRODUCER)));
        }
        return list;
    }

}
