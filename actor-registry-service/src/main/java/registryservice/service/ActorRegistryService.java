package registryservice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import registryservice.dao.ActorRegistryDAO;
import registryservice.dto.Vehicle;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ActorRegistryService {

    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryService.class);


    @Autowired
    private ActorRegistryDAO actorRegistryDAO;
    private ObjectMapper objectMapper;

    @PostConstruct
    private void postConstruct(){
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * Store new vehicle in the db
     * @param vin of vehicle that is going to be stored in the db
     * @param model of vehicle that is going to be stored in the db
     * @param producer of vehicle that is going to be stored in the db
     */
    public void addVehicle(String vin, String model, String producer){
        LOG.info("Inserting new Vehicle with ID: " + vin);
        Vehicle vehicle = new Vehicle(vin, model, producer);
        actorRegistryDAO.addVehicle(vehicle);
    }

    /**
     * Get all vehicles from the db
     * @return List of all vehicles
     */
    public List<Vehicle> getAllVehicles() {
        LOG.info("Getting all vehicles");
        return actorRegistryDAO.getAllVehicles();
    }
}
