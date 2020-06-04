package at.tuwien.dse.actorcontrolservice.service;

import at.tuwien.dse.actorcontrolservice.dao.ActorControlDAO;
import at.tuwien.dse.actorcontrolservice.dto.Movement;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLight;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLightStatus;
import at.tuwien.dse.actorcontrolservice.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActorControlService {

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    private static final Logger LOG = LoggerFactory.getLogger(ActorControlService.class);
    private static final String MOVEMENT_QUEUE = "movement_queue";
    private final ActorControlDAO actorControlDAO;
    private ObjectMapper objectMapper;
    private Client client;
    private Map<Long, TrafficLight> trafficLights = new HashMap<>();
    private Map<Long, TrafficLightStatus> statusMap = new HashMap<>();

    @Autowired
    public ActorControlService(ActorControlDAO actorControlDAO) {
        this.actorControlDAO = actorControlDAO;
        client = ClientBuilder.newClient();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    }

    @PostConstruct
    public void setUp() {
        findTrafficLights();
        consumeQueue();
    }

    private void findTrafficLights() {
        String uri = constructorURIofResource("actor-registry-service", 40001, "getAllTrafficLights", "");
        Response response = client.target(uri).request().get();
        List<TrafficLight> list = parseFromRequestResultToList(response.readEntity(String.class), TrafficLight.class);
        list.forEach(t -> trafficLights.put(t.getId(), t));
    }

    private void consumeQueue() {
        RabbitChannel rabbitChannel = new RabbitChannel();
        DeliverCallback movementCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);

            if (trafficLights.isEmpty()) {
                findTrafficLights();
            }

            TrafficLightStatus status;
            Movement movement;

            if (message.getProperties().getMessageId().equals("traffic")) {
                status = objectMapper.readValue(msg, TrafficLightStatus.class);
                statusMap.put(status.getTrafficLightId(), status);
                LOG.info("Traffic Light status read: " + status);

            } else {
                movement = objectMapper.readValue(msg, Movement.class);
                isVehicleInRadius(movement, rabbitChannel);
                LOG.info("Movement read: " + movement);
            }


        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume("movement_queue", true, movementCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void isVehicleInRadius(Movement movement, RabbitChannel rabbitChannel) throws IOException {

        if (statusMap.isEmpty()) return;

        String uri = constructorURIofResource("actor-registry-service", 40001, "checkRadius", "");
        Response response = client.target(uri)
                .queryParam("longitude", movement.getLongitude())
                .queryParam("latitude", movement.getLatitude())
                .request().get();

        Long trafficLight = response.readEntity(Long.class);
        if (trafficLight == 0L) return;
        determineSpeed(movement, trafficLight, rabbitChannel);

    }

    public int calculateDistanceInKilometer(double userLat, double userLng,
                                            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }

    private void determineSpeed(Movement movement, Long trafficLightId, RabbitChannel rabbitChannel) throws IOException {
        TrafficLight trafficLight = trafficLights.get(trafficLightId);
        TrafficLightStatus status = statusMap.get(trafficLightId);

        int distance = calculateDistanceInKilometer(movement.getLatitude(), movement.getLongitude(), trafficLight.getLatitude(), trafficLight.getLongitude());
        long secondsLeft = status.getDateTime().until(LocalDateTime.now(), ChronoUnit.HOURS);
        distance *= 1000;
        double speed = ((double) distance) / secondsLeft;
        speed *= 3.6;

        if (speed > 130 || (speed < 40 && distance > 100)) {
            return;
        }
        movement.setSpeed(speed);
        String msg = objectMapper.writeValueAsString(movement);
        rabbitChannel.getChannel().basicPublish("", "speed_queue", null, msg.getBytes());
    }

    private <T> List<T> parseFromRequestResultToList(String requestResult, Class clazz) {
        LOG.info("Sending request: " + requestResult);
        List<T> resultList = new ArrayList<>();
        try {
            resultList = objectMapper.readValue(requestResult, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            LOG.error("Error while parsing object from String to List.");
        }
        return resultList;
    }


    private String constructorURIofResource(String host, int port, String methodName, String pathParam) {
        StringBuilder stringBuilder = new StringBuilder("http://" + host + ":" + port + "/" + methodName);
        if (!pathParam.isEmpty()) {
            stringBuilder.append("/").append(pathParam);
        }
        return stringBuilder.toString();
    }

}
