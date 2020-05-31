package registryservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import registryservice.dto.TrafficLight;
import registryservice.dto.Vehicle;
import registryservice.service.ActorRegistryService;

import java.util.List;


@RestController
public class ActorRegistryController {

    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryController.class);

    @Autowired
    private ActorRegistryService actorRegistryService;


    /**
     * Rest POST Method - Insert new vehicle
     * @param vin of vehicle that is going to be stored in the db
     * @param model of vehicle that is going to be stored in the db
     * @param producer of vehicle that is going to be stored in the db
     * @return Response entity with response status 200
     */
    @PostMapping(path = "/addVehicle")
    public ResponseEntity addVehicle(@RequestParam String vin,
                                        @RequestParam String model,
                                        @RequestParam String producer) {
        LOG.info("Received POST insert vehicle: " + vin);
        actorRegistryService.addVehicle(vin, model, producer);
        return ResponseEntity.status(HttpStatus.OK).body("");

    }

    @PostMapping(path = "/addTrafficLight")
    public ResponseEntity addTrafficLight(@RequestParam long id,
                                     @RequestParam double longitude,
                                     @RequestParam double latitude) {
        LOG.info("Received POST add traffic light: " + id);
        actorRegistryService.addTrafficLight(id, longitude, latitude);
        return ResponseEntity.status(HttpStatus.OK).body("");

    }



    /**
     * Rest GET Method - Get all vehicles from the db
     * @return Response entity with a list of all vehicles and response status 200
     */
    @GetMapping(path = "/getAllVehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(actorRegistryService.getAllVehicles());
    }

    @GetMapping(path = "/getAllTrafficLights")
    public ResponseEntity<List<TrafficLight>> getAllTrafficLights() {
        return ResponseEntity.ok(actorRegistryService.getAllTrafficLights());
    }

}
