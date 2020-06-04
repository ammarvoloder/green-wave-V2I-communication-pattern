package at.tuwien.dse.apigateway.service;

import at.tuwien.dse.apigateway.dto.TrafficLight;
import at.tuwien.dse.apigateway.dto.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiGatewayService {

    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayService.class);

    private Client client;
    private ObjectMapper objectMapper;

    public ApiGatewayService() {
        client = ClientBuilder.newClient();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // TESTING PURPOSES
    public ApiGatewayService(Client client) {
        this.client = client;
    }
    /**
     * Forward call to insert vehicle to the entity store service and then dao
     *
     * @param producer  The producer of the new vehicle
     * @param vehicleID The id of the new vehicle
     * @param model     The model of the new vehicle
     * @param headerId  Header value of a key id
     * @return Response entity with the status received after vehicle insertion in entity store service
     */
    public ResponseEntity addVehicle(String producer, String vehicleID, String model, String headerId) {
        LOG.info("Send REST request to insert new vehicle with id: " + vehicleID);
        String uri = constructorURIofResource("actor-registry-service", 40001, "addVehicle", "");
        Response response = client.target(uri).queryParam("producer", producer).queryParam("vin", vehicleID).queryParam("model", model)
                .request()
                .build("POST")
                .invoke();

        return ResponseEntity.status(response.getStatus()).body("");
    }

    public ResponseEntity addTrafficLight(Double longitude, Double latitude, Long id){
        LOG.info("Send REST request to insert new traffic light.");
        String uri = constructorURIofResource("localhost", 40001, "addTrafficLight", "");
        Response response = client.target(uri).queryParam("longitude", longitude)
                .queryParam("latitude", latitude)
                .queryParam("id",id)
                .request()
                .build("POST")
                .invoke();

        return ResponseEntity.status(response.getStatus()).body("");
    }

    public ResponseEntity<List<TrafficLight>> getAllTrafficLights(){
        LOG.info("Send REST request to get all traffic lights");
        String uri = constructorURIofResource("localhost", 40001, "getAllTrafficLights", "");
        Response response = client.target(uri).request().get();
        return ResponseEntity.status(response.getStatus()).body(parseFromRequestResultToList(response.readEntity(String.class), TrafficLight.class));
    }

    /**
     * Get all vehicles from entity store service to present on the UI
     * @return Response entity with a list of all vehicles to show to the client and the status received from entity store service
     */
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        LOG.info("Send REST request to get all vehicles");
        String uri = constructorURIofResource("actor-registry-service", 40001, "getAllVehicles", "");
        Response response = client.target(uri).request().get();
        return ResponseEntity.status(response.getStatus()).body(parseFromRequestResultToList(response.readEntity(String.class), Vehicle.class));
    }

    // parse request response in one method and return the list
    private <T> List<T> parseFromRequestResultToList(String requestResult, Class clazz) {
        LOG.info("Sending request: " + requestResult);
        List<T> resultList = new ArrayList<>();
        try {
            resultList = objectMapper.readValue(requestResult, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Error while parsing object from String to List.");
        }
        return resultList;
    }

    // construct and return URI of the request for all REST request in one method
    private static String constructorURIofResource(String host, int port, String methodName, String pathParam) {
        StringBuilder stringBuilder = new StringBuilder("http://" + host +  ":" + port + "/" + methodName);
        if (!pathParam.isEmpty()) {
            stringBuilder.append("/").append(pathParam);
        }
        return stringBuilder.toString();
    }



}
