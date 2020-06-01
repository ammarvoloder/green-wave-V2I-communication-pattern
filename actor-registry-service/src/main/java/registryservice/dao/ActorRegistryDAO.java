package registryservice.dao;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import registryservice.connection.Connection;
import registryservice.dto.TrafficLight;
import registryservice.dto.Vehicle;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

@Repository
public class ActorRegistryDAO {


    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryDAO.class);

    private static final String VEHICLE_COLLECTION = "vehicles";
    private static final String TRAFFIC_LIGHTS_COLLECTION = "traffic_lights";
    private static final String ID = "id";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String COORDINATES = "coordinates";
    private static final String LOCATION = "location";


    /**
     * vehicles - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Vehicle> vehicles;
    /**
     * trafficLights - MongoDB Collection used for storing traffic lights
     */
    private MongoCollection<Document> trafficLights;

    @PostConstruct
    private void initialize() {
        try {
            vehicles = Connection.getDatabase().getCollection(VEHICLE_COLLECTION, Vehicle.class);
            vehicles.createIndex(Indexes.text("vin"), new IndexOptions().unique(true));
            trafficLights = Connection.getDatabase().getCollection(TRAFFIC_LIGHTS_COLLECTION);
            trafficLights.createIndex(Indexes.text("id"), new IndexOptions().unique(true));
            trafficLights.createIndex(Indexes.geo2d("location"));
        } catch (IOException e) {
            LOG.error("Error while connecting to MongoDB.");
        }
    }

    public void addVehicle(Vehicle vehicle) {
        try {
            LOG.info("Inserting new vehicle: " + vehicle.getVin());
            vehicles.insertOne(vehicle);
        } catch (MongoWriteException e) {
            LOG.error("Error while writing in Mongo");
        }

    }

    /**
     * Methods which gets all vehicles
     *
     * @return List of all vehicles
     */
    public List<Vehicle> getAllVehicles() {
        LOG.info("Getting all vehicles");
        return vehicles.find().into(new ArrayList<>());
    }

    public void addTrafficLight(TrafficLight trafficLight) {
        try {
            LOG.info("Inserting new traffic light");
            List<Double> coordinates = new ArrayList<>();
            coordinates.add(trafficLight.getLatitude());
            coordinates.add(trafficLight.getLongitude());
            Document location = new Document("type", "Point").append(COORDINATES, coordinates);
            String _id = new ObjectId().toHexString();
            Document document = new Document("_id", _id)
                    .append(ID, trafficLight.getId())
                    .append(LOCATION, location);
            trafficLights.insertOne(document);
        } catch (MongoWriteException e) {
            LOG.error("Error while writing in Mongo");
        }
    }

    /**
     * It returns id of the traffic light in which radius vehicle is found
     *
     * @param latitude
     * @param longitude
     * @return
     */
    public Long findIfInRadius(double latitude, double longitude) {
        Position position = new Position(latitude, longitude);
        Point point = new Point(position);
        MongoCursor<Document> trafficLight = trafficLights.find(and(eq(ID, "s3"), near(LOCATION, point, 1300.0, 0.0))).iterator();

        if (!trafficLight.hasNext()) {
            trafficLight = trafficLights.find(and(eq(ID, "s2"), near(LOCATION, point, 700.0, 0.0))).iterator();

            if (!trafficLight.hasNext()) {
                trafficLight = trafficLights.find(and(eq(ID, "s1"), near(LOCATION, point, 1000.0, 0.0))).iterator();
            }
        }

        return trafficLight.hasNext() ? (Long) trafficLight.next().get(ID) : 0L;

    }

    /**
     * Methods which gets all traffic lights
     *
     * @return List of all traffic lights
     */
    public List<TrafficLight> getAllTrafficLights() {
        LOG.info("Getting all traffic lights");
        //return trafficLights.find().into(new ArrayList<>());
        FindIterable<Document> iterable = trafficLights.find();
        //TODO map document to traffic light dto
        return null;
    }

}
