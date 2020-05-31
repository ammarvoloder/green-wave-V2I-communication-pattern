package at.tuwien.dse.actorcontrolservice.service;

import at.tuwien.dse.actorcontrolservice.dao.ActorControlDAO;
import at.tuwien.dse.actorcontrolservice.dto.Movement;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLight;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class ActorControlService
{

    private final ActorControlDAO actorControlDAO;
    private static final String MOVEMENT_QUEUE = "movement_queue";


    private static final Logger LOG = LoggerFactory.getLogger(ActorControlService.class);


    private ObjectMapper objectMapper;
    private Client client;

    private List<TrafficLight> trafficLights = new ArrayList<>();



    @Autowired
    public ActorControlService(ActorControlDAO actorControlDAO) {
        this.actorControlDAO = actorControlDAO;
        client = ClientBuilder.newClient();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    }

    @PostConstruct
    public void setUp()
    {
        findTrafficLights();
        consumeQueue();
    }

    private void findTrafficLights()
    {
        String uri = constructorURIofResource("actor-registry-service", 40001, "getAllTrafficLights", "");
        Response response = client.target(uri).request().get();
        trafficLights = parseFromRequestResultToList(response.readEntity(String.class), TrafficLight.class);
    }

    private void consumeQueue() {
        RabbitChannel rabbitChannel = new RabbitChannel();
        DeliverCallback movementCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            Movement movement = objectMapper.readValue(msg, Movement.class);
            LOG.info("Movement read: " + movement);
            if(trafficLights.isEmpty())
            {
                findTrafficLights();
            }

            isVehicleInRadius();


        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume("movement_queue", true, movementCallback, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void isVehicleInRadius() {
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


    private  String constructorURIofResource(String host, int port, String methodName, String pathParam) {
        StringBuilder stringBuilder = new StringBuilder("http://" + host +  ":" + port + "/" + methodName);
        if (!pathParam.isEmpty()) {
            stringBuilder.append("/").append(pathParam);
        }
        return stringBuilder.toString();
    }

}
