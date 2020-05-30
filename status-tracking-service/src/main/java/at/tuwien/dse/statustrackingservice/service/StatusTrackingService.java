package at.tuwien.dse.statustrackingservice.service;

import at.tuwien.dse.statustrackingservice.dao.StatusTrackingDAO;
import at.tuwien.dse.statustrackingservice.dto.Movement;
import at.tuwien.dse.statustrackingservice.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class StatusTrackingService {

    private final StatusTrackingDAO statusTrackingDAO;

    private ObjectMapper objectMapper;


    @Autowired
    public StatusTrackingService(StatusTrackingDAO statusTrackingDAO) {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        consumeQueue();
        this.statusTrackingDAO = statusTrackingDAO;
    }

    private void consumeQueue() {
        RabbitChannel rabbitChannel = new RabbitChannel();
        DeliverCallback movementCallback = new DeliverCallback() {
            @Override
            public void handle(String consumerTag, Delivery message) throws IOException {
                String msg = new String(message.getBody(), StandardCharsets.UTF_8);
                Movement movement = objectMapper.readValue(msg, Movement.class);
                statusTrackingDAO.addMovement(movement);
            }
        };
        try {
            rabbitChannel.getChannel().basicConsume("q_entity_notification", true, movementCallback, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
