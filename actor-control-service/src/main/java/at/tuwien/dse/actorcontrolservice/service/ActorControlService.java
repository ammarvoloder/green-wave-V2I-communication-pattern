package at.tuwien.dse.actorcontrolservice.service;

import at.tuwien.dse.actorcontrolservice.dao.ActorControlDAO;
import at.tuwien.dse.actorcontrolservice.dto.Movement;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLight;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLightStatus;
import at.tuwien.dse.actorcontrolservice.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActorControlService {

    public final static double AVERAGE_RADIUS_OF_EARTH_METERS = 6371000;
    public static final int TRAFFIC_LIGHT_CHANGE = 10;
    private static final Logger LOG = LoggerFactory.getLogger(ActorControlService.class);
    private static final String ACTOR_QUEUE = "actor_queue";

    @Autowired
    private ActorControlDAO actorControlDAO;
    private ObjectMapper objectMapper;
    private Client client;
    private Map<Long, TrafficLight> trafficLights = new HashMap<>();
    private ConcurrentHashMap<Long, TrafficLightStatus> statusMap = new ConcurrentHashMap<>();
    private Map<String, Long> vehicleProvidedSpeed = new HashMap<>();

    public ActorControlService() {
        client = ClientBuilder.newClient();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    }

    @PostConstruct
    public void setUp() {
        //findTrafficLights();
        consumeQueue();
    }

    private void findTrafficLights() {
        String uri = constructorURIofResource("actor-registry-service", 40001, "getAllTrafficLights", "");
        Response response = client.target(uri).request().get();
        List<TrafficLight> list = parseJsonToList(response.readEntity(String.class));
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

            if (("traffic").equals(message.getProperties().getMessageId())) {
                status = objectMapper.readValue(msg, TrafficLightStatus.class);
                statusMap.put(status.getTrafficLightId(), status);
                LOG.info("Traffic Light status read: " + status);

            } else {
                movement = objectMapper.readValue(msg, Movement.class);
                LOG.info("Movement read: " + movement);
                isVehicleInRadius(movement, rabbitChannel);
            }


        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume(ACTOR_QUEUE, true, movementCallback, consumerTag -> {
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
        if (trafficLight == 0L) {
            LOG.info("Movement not in radius");
            return;
        }
        if (trafficLight.equals(vehicleProvidedSpeed.get(movement.getVin()))) {
            if (movement.isCrash() && movement.getSpeed() == 0) {
                LOG.info("Movement with crash and speed 0 recognized!");
            } else if (movement.isCrash() && movement.getSpeed() == 50) {
                LOG.info("Movement with crash and speed 50.0 recognized!");
                timeForManualActivation(rabbitChannel, movement, trafficLight);
            } else {
                LOG.info("Traffic light " + trafficLight + " already provided speed to " + movement.getVin());
            }
            return;
        }
        determineSpeed(movement, trafficLight, rabbitChannel);
    }

    private void timeForManualActivation(RabbitChannel rabbitChannel, Movement movement, Long trafficLightId) throws IOException {
        TrafficLight trafficLight = trafficLights.get(trafficLightId);
        TrafficLightStatus status = statusMap.get(trafficLightId);

        int distance = calculateDistanceInMeter(movement.getLatitude(), movement.getLongitude(), trafficLight.getLatitude(), trafficLight.getLongitude());
        long secondsPassed = status.getDateTime().until(LocalDateTime.now(), ChronoUnit.SECONDS);
        long secondsLeft = TRAFFIC_LIGHT_CHANGE - secondsPassed;
        long timeUnitTL = (long) (distance / (movement.getSpeed() / 3.6));
        LOG.info("Distance: {}  Speed: {}   Manual time: {}", distance, movement.getSpeed(), timeUnitTL);
        LOG.info("Starting manual determination..");
        // if green and no need for manual traffic light activation
        if (status.isGreen() && secondsLeft > timeUnitTL) {
            LOG.info("Vehicle will made it after NCE event! Current status: Green!");
            return;
        }
        // if red but still getting on green wave with 50 km/h
        if (!status.isGreen() && secondsLeft <= timeUnitTL && (secondsLeft + TRAFFIC_LIGHT_CHANGE) > timeUnitTL) {
            LOG.info("Vehicle will made it after NCE event! Current status: Red!");
            return;
        }
        LOG.info("Set traffic light status manually");
        status.setDateTime(LocalDateTime.now().plusSeconds(timeUnitTL));

        AMQP.BasicProperties messageId = new AMQP.BasicProperties().builder().messageId("traffic").build();
        String msg = objectMapper.writeValueAsString(status);
        rabbitChannel.getChannel().basicPublish("", "speed_queue", messageId, msg.getBytes());

    }

    public int calculateDistanceInMeter(double userLat, double userLng,
                                        double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_METERS * c));
    }

    private void determineSpeed(Movement movement, Long trafficLightId, RabbitChannel rabbitChannel) throws IOException {
        double speed;
        TrafficLight trafficLight = trafficLights.get(trafficLightId);
        TrafficLightStatus status = statusMap.get(trafficLightId);

        LOG.info("Movement in radius of " + trafficLight.toString());

        LOG.info(status.toString());
        int distance = calculateDistanceInMeter(movement.getLatitude(), movement.getLongitude(), trafficLight.getLatitude(), trafficLight.getLongitude());
        long secondsPassed = status.getDateTime().until(LocalDateTime.now(), ChronoUnit.SECONDS);
        long secondsLeft = TRAFFIC_LIGHT_CHANGE - secondsPassed;

        if (status.isGreen()) {
            LOG.info("Current traffic light status: green");
            // if green try to reach green light with max speed (130 km/h)
            if ((distance / (130.0 / 3.6)) < secondsLeft) {
                speed = 130;
            }
            // else wait for next green light
            else {
                speed = ((double) distance) / (secondsLeft + TRAFFIC_LIGHT_CHANGE);
            }
        } else {
            LOG.info("Current traffic light status: red");
            long minTime = (long) (distance / (130.0 / 3.6));
            // try to reach next green with max speed
            if (minTime > secondsLeft && minTime < (secondsLeft + 10)) {
                speed = 130;
            } else {
                speed = ((double) distance) / (secondsLeft);
            }
        }
        speed *= 3.6;

        LOG.info("Determined speed " + speed);

        // speed can't be greater than 130 and
        // less then 40 if distance to traffic light is still large (> 500m)
        if (speed > 130 || (speed < 40 && distance > 600)) {
            return;
        }

        // save which traffic light provided this vehicle with the speed
        // so that we only have to determine speed once per traffic light
        vehicleProvidedSpeed.put(movement.getVin(), trafficLightId);
        movement.setSpeed(speed);
        String msg = objectMapper.writeValueAsString(movement);
        rabbitChannel.getChannel().basicPublish("", "speed_queue", null, msg.getBytes());
        actorControlDAO.addMovement(movement);
    }

    private <T> List<T> parseJsonToList(String requestResult) {
        LOG.info("Sending request: " + requestResult);
        List<T> resultList = new ArrayList<>();
        try {
            resultList = objectMapper.readValue(requestResult, objectMapper.getTypeFactory().constructCollectionType(List.class, TrafficLight.class));
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
