package at.tuwien.dse.actorcontrolservice.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.DeliverCallback;

import at.tuwien.dse.actorcontrolservice.dao.ActorControlDAO;
import at.tuwien.dse.actorcontrolservice.dto.Movement;
import at.tuwien.dse.actorcontrolservice.rabbit.RabbitChannel;

@Service
public class ActorControlService
{

    private final ActorControlDAO actorControlDAO;
    private static final String MOVEMENT_QUEUE = "movement_queue";


    private static final Logger LOG = LoggerFactory.getLogger(ActorControlService.class);


    private ObjectMapper objectMapper;


    @Autowired
    public ActorControlService(ActorControlDAO actorControlDAO) {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        consumeQueue();
        this.actorControlDAO = actorControlDAO;
    }

    private void consumeQueue() {
        RabbitChannel rabbitChannel = new RabbitChannel();
        DeliverCallback movementCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            Movement movement = objectMapper.readValue(msg, Movement.class);
            LOG.info("Movement read: " + movement);

        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume("movement_queue", true, movementCallback, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
