package at.tuwien.dse.apigateway.controller;

import at.tuwien.dse.apigateway.service.ApiGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiGatewayController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayController.class);

    @Autowired
    private ApiGatewayService apiGatewayService;

    /**
     * Rest POST Method - Insert new vehicle
     * @param producer of vehicle that is going to be stored in the db
     * @param vehicleID of vehicle that is going to be stored in the db
     * @param model of vehicle that is going to be stored in the db
     * @return Response entity with the status received after vehicle insertion in entity store service
     */
    @PostMapping(path = "/addVehicle")
    public ResponseEntity addVehicle(@RequestParam String producer,
                                        @RequestParam String vehicleID,
                                        @RequestParam String model,
                                        @RequestHeader("id") String headerId) {
        LOG.info("Received POST insert vehicle with id: " + vehicleID);
        return apiGatewayService.addVehicle(producer, vehicleID, model, headerId);
    }

}
