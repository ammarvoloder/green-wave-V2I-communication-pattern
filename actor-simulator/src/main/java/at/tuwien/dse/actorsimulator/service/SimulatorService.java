package at.tuwien.dse.actorsimulator.service;

import at.tuwien.dse.actorsimulator.dto.Movement;
import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Service
public class SimulatorService {


    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;
    private Client client;

    @Autowired
    public SimulatorService() {
        client = ClientBuilder.newClient();
    }

    public void putMovementInQueue(Movement movement) throws IOException {
        String msg = objectMapper.writeValueAsString(movement);
        rabbitChannel.getChannel().basicPublish("","movement_queue",null,msg.getBytes());
    }

    public void saveVehicle(String vehicleId, String model, String producer) {
        String uri = constructorURIofResource("localhost",  10113, "addVehicle",  "");
        this.client.target(uri)
                .queryParam("vehicleID", vehicleId)
                .queryParam("producer", producer)
                .queryParam("model", model)
                .request().header("id", producer)
                .build("POST")
                .invoke();
    }

    public void saveTrafficLight(double longitude, double latitude){
        String uri = constructorURIofResource("localhost", 10113, "addTrafficLight","");
        this.client.target(uri)
                .queryParam("longitude", longitude)
                .queryParam("latitude", latitude)
                .request()
                .build("POST")
                .invoke();
    }

    private static void getVehicles(Client client){
        String uri = constructorURIofResource("localhost",  10113, "getAllVehicles",  "");
        Response response = client.target(uri)
                .request()
                .build("GET")
                .invoke();
        System.out.println(response);
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
