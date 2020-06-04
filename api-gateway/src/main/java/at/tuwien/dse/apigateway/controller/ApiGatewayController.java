package at.tuwien.dse.apigateway.controller;

import at.tuwien.dse.apigateway.dto.TrafficLight;
import at.tuwien.dse.apigateway.dto.TrafficLightStatus;
import at.tuwien.dse.apigateway.dto.Vehicle;
import at.tuwien.dse.apigateway.service.ApiGatewayService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(exposedHeaders = "Access-Control-Allow-Origin")
public class ApiGatewayController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayController.class);

    @Autowired
    private ApiGatewayService apiGatewayService;

    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    ApiGatewayController(SimpMessagingTemplate template){
        this.simpMessagingTemplate = template;
    }

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

    @PostMapping(path = "/sendToSocket")
    public void sendVehicleToSocket(@RequestParam Long id,
                                    @RequestParam Boolean green,
                                    @RequestParam LocalDateTime time) {
        TrafficLightStatus trafficLightStatus = new TrafficLightStatus(green, id, time);
        this.simpMessagingTemplate.convertAndSend("/trafficLights",  trafficLightStatus);
    }

    /**
     * Rest GET Method - Get all vehicles to present on the UI
     * @return Response entity with a list of all vehicles to show to the client and the status received from entity store service
     */
    @GetMapping(path = "/getAllVehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        LOG.info("Received GET all vehicles");
        return apiGatewayService.getAllVehicles();
    }

    @PostMapping(path = "/addTrafficLight")
    public ResponseEntity addTrafficLight(@RequestParam Double longitude,
                                     @RequestParam Double latitude,
                                          @RequestParam Long id) {
        LOG.info("Received POST insert traffic light");
        return apiGatewayService.addTrafficLight(longitude, latitude,id);
    }

    @GetMapping(path = "/getAllTrafficLights")
    public ResponseEntity<List<TrafficLight>> getAllTrafficLights(){
        LOG.info("Recieved GET all traffic lights");
        return apiGatewayService.getAllTrafficLights();
    }

}
