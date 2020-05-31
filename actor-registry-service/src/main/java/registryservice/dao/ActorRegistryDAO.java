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
    private static final String TRAFFIC_LIGHTS_COLLECTION = "traffic_lights";
    private static final String PRODUCER = "producer";
    private static final String MODEL = "model";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";


    /**
     * vehicles - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Vehicle> vehicles;
    /**
     * trafficLights - MongoDB Collection used for storing traffic lights
     */
    private MongoCollection<TrafficLight> trafficLights;

    @PostConstruct
    private void initialize(){
        try {
            vehicles = Connection.getDatabase().getCollection(VEHICLE_COLLECTION,Vehicle.class);
            trafficLights = Connection.getDatabase().getCollection(TRAFFIC_LIGHTS_COLLECTION, TrafficLight.class);
        } catch (IOException e) {
            LOG.error("Error while connecting to MongoDB.");
        }
    }

    public void addVehicle(Vehicle vehicle){
        try {
            LOG.info("Inserting new vehicle: " + vehicle.getVin());
            vehicles.insertOne(vehicle);
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
        return vehicles.find().into(new ArrayList<>());
    }

    public void addTrafficLight(TrafficLight trafficLight){
        try {
            LOG.info("Inserting new traffic light");
            trafficLights.insertOne(trafficLight);
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
        return trafficLights.find().into(new ArrayList<>());
    }

}
