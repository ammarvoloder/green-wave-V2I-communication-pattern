package registryservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import registryservice.service.ActorRegistryService;


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
    public ResponseEntity insertVehicle(@RequestParam String vin,
                                        @RequestParam String model,
                                        @RequestParam String producer) {
        LOG.info("Received POST insert vehicle: " + vin);
        actorRegistryService.insertVehicle(vin, model, producer);
        return ResponseEntity.status(HttpStatus.OK).body("");

    }

}
