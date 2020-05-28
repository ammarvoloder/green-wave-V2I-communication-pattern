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
import registryservice.dto.TrafficLight;
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
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";


    /**
     * vehicles - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Document> vehicles;
    /**
     * trafficLights - MongoDB Collection used for storing traffic lights
     */
    private MongoCollection<Document> trafficLights;

    @PostConstruct
    private void initialize(){
        try {
            vehicles = Connection.getDatabase().getCollection("VEHICLE_COLLECTION");
        } catch (IOException e) {
            LOG.error("Error while connecting to MongoDB.");
        }
    }

    public void addVehicle(Vehicle vehicle){
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

    public void addTrafficLight(TrafficLight trafficLight){
        try {
            LOG.info("Inserting new traffic light: " + trafficLight.getId());
            Document document = new Document("_id", trafficLight.getId())
                    .append(LATITUDE, trafficLight.getLatitude())
                    .append(LONGITUDE, trafficLight.getLongitude());
            trafficLights.insertOne(document);
        } catch (MongoWriteException e){
            LOG.error("Error while writing in Mongo");
        }

    }

    /**
     * Methods which gets all traffic lights
     * @return List of all traffic lights
     */
    public List<TrafficLight> getAllTrafficLights() {
        LOG.info("Getting all traffic lights");
        FindIterable<Document> document = trafficLights.find();
        ArrayList<TrafficLight> list = new ArrayList<>();
        for (Document d : document) {
            list.add(new TrafficLight(d.getLong("_id"), d.getDouble(LONGITUDE), d.getDouble(LATITUDE)));
        }
        return list;
    }

}
