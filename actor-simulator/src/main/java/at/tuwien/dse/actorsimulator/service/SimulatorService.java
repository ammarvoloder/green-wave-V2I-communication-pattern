package at.tuwien.dse.actorsimulator.service;

import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

@Service
public class SimulatorService {

    private final static String MOVEMENT_QUEUE = "movement_queue";
    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;
    private Client client;

    @Autowired
    public SimulatorService() {
        client = ClientBuilder.newClient();
    }

    private static void getVehicles(Client client) {
        String uri = createTargetForRequest("localhost", 10113, "getAllVehicles", "");
        Response response = client.target(uri)
                .request()
                .build("GET")
                .invoke();
        System.out.println(response);
    }

    private static String createTargetForRequest(String host, int port, String methodName, String pathParam) {
        StringBuilder stringBuilder = new StringBuilder("http://" + host + ":" + port + "/" + methodName);
        if (!pathParam.isEmpty()) {
            stringBuilder.append("/").append(pathParam);
        }
        return stringBuilder.toString();
    }

    public void saveVehicle(String vehicleId, String model, String producer) {
        String uri = createTargetForRequest("localhost", 10113, "addVehicle", "");
        this.client.target(uri)
                .queryParam("vehicleID", vehicleId)
                .queryParam("producer", producer)
                .queryParam("model", model)
                .request().header("id", producer)
                .build("POST")
                .invoke();
    }

    public void saveTrafficLight(Double longitude, Double latitude, Long id) {
        String uri = createTargetForRequest("localhost", 10113, "addTrafficLight", "");
        this.client.target(uri)
                .queryParam("longitude", longitude)
                .queryParam("latitude", latitude)
                .queryParam("id", id)
                .request()
                .build("POST")
                .invoke();
    }
}
